package com.example.data

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.model.Ayah
import com.example.model.QuranReciter
import com.example.model.QuranReciters
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AyahTiming(
    val ayahNumber: Int,
    val startMs: Int,
    val endMs: Int
)

data class QuranAudioState(
    val currentSurahNumber: Int = 1,
    val currentSurahName: String = "Fâtiha",
    val currentReciter: QuranReciter = QuranReciters.defaultReciter,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val currentAyahNumber: Int = 1,
    val totalAyahsInSurah: Int = 7,
    val isHatimMode: Boolean = false,
    val currentJuzNumber: Int = 1,
    val errorMessage: String? = null
) {
    val progress: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

class QuranAudioPlayer(private val context: Context) {
    companion object {
        const val AYAH_ISTIADHA = -1
        const val AYAH_BASMALAH = -2
    }

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressTrackerJob: Job? = null

    // Ayet takip ve kesintisiz oynatma kuyruğu
    private var activeAyahsList: List<Ayah> = emptyList()
    private var currentAyahIndex: Int = -1
    private var isPlayingBasmalah: Boolean = false
    private var currentSpeed: Float = 1.0f

    // Geriye dönük uyumluluk ve tam sûre modu için
    private var currentTimingSegments: List<AyahTiming> = emptyList()
    private var currentAyahsList: List<Ayah> = emptyList()

    val audioCacheDir: File by lazy {
        File(context.cacheDir, "quran_audio").apply { if (!exists()) mkdirs() }
    }

    fun getCachedFile(reciterId: String, surahNumber: Int): File {
        return File(audioCacheDir, "reciter_${reciterId}_surah_${surahNumber}.mp3")
    }

    fun getCachedAyahFile(reciterFolder: String, surahNumber: Int, ayahNumber: Int): File {
        return File(audioCacheDir, "reciter_${reciterFolder}_s${surahNumber}_a${ayahNumber}.mp3")
    }

    fun isSurahCached(surahNumber: Int, reciterId: String = _state.value.currentReciter.id): Boolean {
        val file = getCachedFile(reciterId, surahNumber)
        return file.exists() && file.length() > 5000
    }

    private fun cacheAudioInBackground(targetUrl: String, destinationFile: File) {
        if (!targetUrl.startsWith("http")) return
        if (destinationFile.exists() && destinationFile.length() > 1000) return

        scope.launch(Dispatchers.IO) {
            try {
                val tempFile = File(destinationFile.parentFile, "${destinationFile.name}.tmp")
                val conn = (java.net.URL(targetUrl).openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 12000
                    readTimeout = 25000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Ezanla-Android")
                }
                conn.connect()
                if (conn.responseCode in 200..299) {
                    conn.inputStream.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (tempFile.exists() && tempFile.length() > 1000) {
                        tempFile.renameTo(destinationFile)
                    } else {
                        tempFile.delete()
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                // Background cache fail is silent
            }
        }
    }

    fun preloadPopularSurahs(
        reciter: QuranReciter = _state.value.currentReciter,
        onComplete: (() -> Unit)? = null
    ) {
        val popularSurahs = listOf(1, 36, 67, 78, 112, 113, 114)
        scope.launch(Dispatchers.IO) {
            for (surahNum in popularSurahs) {
                val surahObj = IslamicDatabase.surahs.find { it.number == surahNum }
                val count = surahObj?.ayahCount ?: 7
                for (aNum in 1..count.coerceAtMost(10)) {
                    val targetFile = getCachedAyahFile(reciter.everyAyahFolder, surahNum, aNum)
                    if (!targetFile.exists() || targetFile.length() < 1000) {
                        val url = QuranReciters.getAyahAudioUrl(reciter, surahNum, aNum)
                        try {
                            val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
                            val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                                connectTimeout = 10000
                                readTimeout = 20000
                                instanceFollowRedirects = true
                                setRequestProperty("User-Agent", "Ezanla-Android")
                            }
                            conn.connect()
                            if (conn.responseCode in 200..299) {
                                conn.inputStream.use { input ->
                                    tempFile.outputStream().use { output -> input.copyTo(output) }
                                }
                                if (tempFile.exists() && tempFile.length() > 1000) {
                                    tempFile.renameTo(targetFile)
                                } else {
                                    tempFile.delete()
                                }
                            }
                            conn.disconnect()
                        } catch (_: Exception) {}
                    }
                }
            }
            withContext(Dispatchers.Main) {
                onComplete?.invoke()
            }
        }
    }

    fun getCachedSurahsCount(): Int {
        return audioCacheDir.listFiles { file -> file.isFile && file.extension == "mp3" && file.length() > 1000 }?.size ?: 0
    }

    fun getCachedSurahsSizeBytes(): Long {
        return audioCacheDir.listFiles { file -> file.isFile && file.extension == "mp3" }?.sumOf { it.length() } ?: 0L
    }

    fun clearCache() {
        scope.launch(Dispatchers.IO) {
            try {
                audioCacheDir.listFiles()?.forEach { it.delete() }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private val _state = MutableStateFlow(QuranAudioState())
    val state: StateFlow<QuranAudioState> = _state.asStateFlow()

    fun setPlaybackSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.5f, 2.0f)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        mp.playbackParams = mp.playbackParams.setSpeed(currentSpeed)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Speed set error", e)
        }
    }

    fun setReciter(reciter: QuranReciter) {
        val previousReciter = _state.value.currentReciter
        if (previousReciter.id == reciter.id) return

        _state.value = _state.value.copy(
            currentReciter = reciter,
            errorMessage = null
        )

        val wasPlaying = _state.value.isPlaying
        val wasBuffering = _state.value.isBuffering

        if (wasPlaying || wasBuffering) {
            if (isPlayingBasmalah) {
                playBasmalah(_state.value.currentSurahNumber)
            } else if (currentAyahIndex in activeAyahsList.indices) {
                playAyahAtIndex(currentAyahIndex)
            }
        }
    }

    private fun ensureAyahsList(surahNumber: Int, providedAyahs: List<Ayah>? = null): List<Ayah> {
        if (providedAyahs != null && providedAyahs.isNotEmpty()) {
            currentAyahsList = providedAyahs
            return providedAyahs
        }
        if (currentAyahsList.isNotEmpty() && _state.value.currentSurahNumber == surahNumber) {
            return currentAyahsList
        }
        val surah = IslamicDatabase.surahs.find { it.number == surahNumber }
        val loaded = if (surah != null) QuranAyahProvider.getAllAyahsForSurah(surah) else emptyList()
        if (loaded.isNotEmpty()) {
            currentAyahsList = loaded
        }
        return currentAyahsList
    }

    /**
     * Kuran Sûre Tilâveti:
     * Hoca âyet âyet okurken, tam eş zamanlı ve sıfır kayma (gecikme / önden gitme olmadan)
     * okunan âyet görünür ve takip edilir.
     */
    fun playSurah(
        surahNumber: Int,
        surahName: String,
        totalAyahs: Int = 7,
        customUrl: String? = null,
        ayahs: List<Ayah>? = null,
        startAyahNumber: Int = 1
    ) {
        val ayahsList = ensureAyahsList(surahNumber, ayahs)
        activeAyahsList = ayahsList
        isPlayingBasmalah = false

        _state.value = _state.value.copy(
            currentSurahNumber = surahNumber,
            currentSurahName = surahName,
            totalAyahsInSurah = if (ayahsList.isNotEmpty()) ayahsList.size else totalAyahs,
            isHatimMode = false,
            errorMessage = null
        )

        // Eğer 1. Âyetten başlatılıyorsa ve Fatiha(1) ya da Tevbe(9) değilse, önce Besmele okunur
        if (startAyahNumber == 1 && surahNumber != 1 && surahNumber != 9) {
            playBasmalah(surahNumber)
        } else {
            val targetIdx = activeAyahsList.indexOfFirst { it.ayahNumber == startAyahNumber }.coerceAtLeast(0)
            playAyahAtIndex(targetIdx)
        }
    }

    /**
     * Hatim / Cüz Tilâveti:
     * Cüzdeki âyetler sırayla hoca tarafından tilâvet edilirken okuduğu yer tam eş zamanlı takip edilir.
     */
    fun playHatim(
        juzNumber: Int,
        juzName: String,
        ayahs: List<Ayah>,
        startAyahNumber: Int = 1
    ) {
        activeAyahsList = ayahs
        isPlayingBasmalah = false

        val targetIdx = activeAyahsList.indexOfFirst { it.ayahNumber == startAyahNumber }.coerceAtLeast(0)
        val initialAyah = activeAyahsList.getOrNull(targetIdx)

        _state.value = _state.value.copy(
            isHatimMode = true,
            currentJuzNumber = juzNumber,
            currentSurahNumber = initialAyah?.surahNumber ?: juzNumber,
            currentSurahName = juzName,
            totalAyahsInSurah = ayahs.size,
            errorMessage = null
        )

        playAyahAtIndex(targetIdx)
    }

    /**
     * Hatim modunda doğrudan seçilen bir âyetten tilâveti başlatır.
     */
    fun playAyahInHatim(
        juzNumber: Int,
        targetAyah: Ayah,
        allAyahs: List<Ayah>
    ) {
        activeAyahsList = allAyahs
        isPlayingBasmalah = false

        val targetIdx = activeAyahsList.indexOfFirst {
            it.surahNumber == targetAyah.surahNumber && it.ayahNumber == targetAyah.ayahNumber
        }.coerceAtLeast(0)

        _state.value = _state.value.copy(
            isHatimMode = true,
            currentJuzNumber = juzNumber,
            currentSurahNumber = targetAyah.surahNumber,
            totalAyahsInSurah = allAyahs.size,
            errorMessage = null
        )

        playAyahAtIndex(targetIdx)
    }

    private fun playBasmalah(surahNumber: Int) {
        isPlayingBasmalah = true
        currentAyahIndex = -1
        stopProgressTracker()

        val reciter = _state.value.currentReciter
        val basmalahUrl = QuranReciters.getBasmalahAudioUrl(reciter, surahNumber)
        val cachedFile = getCachedAyahFile(reciter.everyAyahFolder, surahNumber, 0)
        val source = if (cachedFile.exists() && cachedFile.length() > 1000) cachedFile.absolutePath else basmalahUrl

        if (!cachedFile.exists() || cachedFile.length() < 1000) {
            cacheAudioInBackground(basmalahUrl, cachedFile)
        }

        // 1. Âyeti arka planda önbelleğe al (geçişin anında olması için)
        activeAyahsList.firstOrNull()?.let { firstAyah ->
            val firstCached = getCachedAyahFile(reciter.everyAyahFolder, firstAyah.surahNumber, firstAyah.ayahNumber)
            if (!firstCached.exists() || firstCached.length() < 1000) {
                cacheAudioInBackground(
                    QuranReciters.getAyahAudioUrl(reciter, firstAyah.surahNumber, firstAyah.ayahNumber),
                    firstCached
                )
            }
        }

        _state.value = _state.value.copy(
            currentAyahNumber = AYAH_BASMALAH,
            isPlaying = true,
            isBuffering = true,
            currentPositionMs = 0,
            durationMs = 0
        )

        playAudioSource(source, onCompleted = {
            isPlayingBasmalah = false
            playAyahAtIndex(0)
        })
    }

    private fun playAyahAtIndex(index: Int) {
        isPlayingBasmalah = false
        if (activeAyahsList.isEmpty()) return

        if (index !in activeAyahsList.indices) {
            // Tilâvet tamamlandı
            _state.value = _state.value.copy(
                isPlaying = false,
                isBuffering = false,
                currentPositionMs = 0
            )
            stopProgressTracker()
            return
        }

        currentAyahIndex = index
        val currentAyah = activeAyahsList[index]
        stopProgressTracker()

        val reciter = _state.value.currentReciter
        val ayahUrl = QuranReciters.getAyahAudioUrl(reciter, currentAyah.surahNumber, currentAyah.ayahNumber)
        val cachedFile = getCachedAyahFile(reciter.everyAyahFolder, currentAyah.surahNumber, currentAyah.ayahNumber)
        val source = if (cachedFile.exists() && cachedFile.length() > 1000) cachedFile.absolutePath else ayahUrl

        if (!cachedFile.exists() || cachedFile.length() < 1000) {
            cacheAudioInBackground(ayahUrl, cachedFile)
        }

        // Bir sonraki âyeti arka planda hemen önbelleğe al (aralıksız, akıcı tilâvet)
        activeAyahsList.getOrNull(index + 1)?.let { nextAyah ->
            val nextCached = getCachedAyahFile(reciter.everyAyahFolder, nextAyah.surahNumber, nextAyah.ayahNumber)
            if (!nextCached.exists() || nextCached.length() < 1000) {
                cacheAudioInBackground(
                    QuranReciters.getAyahAudioUrl(reciter, nextAyah.surahNumber, nextAyah.ayahNumber),
                    nextCached
                )
            }
        }

        _state.value = _state.value.copy(
            currentSurahNumber = currentAyah.surahNumber,
            currentAyahNumber = currentAyah.ayahNumber,
            isPlaying = true,
            isBuffering = true,
            currentPositionMs = 0,
            durationMs = 0,
            errorMessage = null
        )

        playAudioSource(source, onCompleted = {
            // Tam eş zamanlı: Bu âyet bittiği mikrosaniyede hemen sonraki âyete geç
            playAyahAtIndex(currentAyahIndex + 1)
        })
    }

    private fun playAudioSource(source: String, onCompleted: () -> Unit) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(source)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && currentSpeed != 1.0f) {
                    try {
                        playbackParams = playbackParams.setSpeed(currentSpeed)
                    } catch (_: Exception) {}
                }

                setOnPreparedListener { mp ->
                    _state.value = _state.value.copy(
                        isBuffering = false,
                        isPlaying = true,
                        durationMs = mp.duration
                    )
                    mp.start()
                    startProgressTracker()
                }

                setOnCompletionListener {
                    onCompleted()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("QuranAudioPlayer", "Audio error: $what, $extra")
                    _state.value = _state.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        errorMessage = "Âyet sesi yüklenemedi. İnternet bağlantınızı kontrol edin."
                    )
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "playAudioSource exception", e)
            _state.value = _state.value.copy(
                isBuffering = false,
                isPlaying = false,
                errorMessage = "Ses başlatılamadı: ${e.localizedMessage}"
            )
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            _state.value = _state.value.copy(isPlaying = false)
            stopProgressTracker()
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Pause error", e)
        }
    }

    fun resume() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                _state.value = _state.value.copy(isPlaying = true)
                startProgressTracker()
            } else if (currentAyahIndex in activeAyahsList.indices) {
                playAyahAtIndex(currentAyahIndex)
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Resume error", e)
        }
    }

    fun stop() {
        try {
            stopProgressTracker()
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
            _state.value = _state.value.copy(
                isPlaying = false,
                isBuffering = false,
                currentPositionMs = 0
            )
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Stop error", e)
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _state.value = _state.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Seek error", e)
        }
    }

    /**
     * Kullanıcı herhangi bir âyete tıkladığında veya atladığında:
     * Hoca tam o âyeti okumaya başlar, ekran ve ses sıfır kayma ile kilitlenir.
     */
    fun seekToAyah(ayahNumber: Int) {
        if (activeAyahsList.isNotEmpty()) {
            val targetIdx = activeAyahsList.indexOfFirst { it.ayahNumber == ayahNumber }
            if (targetIdx >= 0) {
                playAyahAtIndex(targetIdx)
                return
            }
        }
        _state.value = _state.value.copy(currentAyahNumber = ayahNumber)
    }

    fun skipForward10Sec() {
        // Sonraki âyete geç
        if (currentAyahIndex + 1 in activeAyahsList.indices) {
            playAyahAtIndex(currentAyahIndex + 1)
        } else {
            mediaPlayer?.let { mp ->
                val newPos = (mp.currentPosition + 5000).coerceAtMost(mp.duration)
                seekTo(newPos)
            }
        }
    }

    fun skipBackward10Sec() {
        val currentPos = mediaPlayer?.currentPosition ?: 0
        if (currentPos > 3000) {
            // Âyetin başına sar
            seekTo(0)
        } else if (currentAyahIndex - 1 in activeAyahsList.indices) {
            // Önceki âyete geç
            playAyahAtIndex(currentAyahIndex - 1)
        } else {
            seekTo(0)
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackerJob = scope.launch {
            while (_state.value.isPlaying) {
                mediaPlayer?.let { mp ->
                    try {
                        if (mp.isPlaying) {
                            val pos = mp.currentPosition
                            val dur = mp.duration
                            _state.value = _state.value.copy(
                                currentPositionMs = pos,
                                durationMs = dur
                            )
                        }
                    } catch (_: Exception) {}
                }
                delay(100)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }
}
