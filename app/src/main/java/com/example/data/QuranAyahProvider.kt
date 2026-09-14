package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.Ayah
import com.example.model.Surah
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Kullanıcı Talebi:
 * "Kuranı hocaların okuduğu ayetlere göre yap veriyi istediğin yerden alabilirsin yani hoca kuranda ne okuyorsa gözümle takip edebilmek istiyorum o şekilde yap."
 *
 * Her sûrenin tüm âyetlerini hem yerel önbellekten hem de eksiksiz resmi Uthmani Arapça metni ve Diyanet Meali ile
 * dinamik olarak sağlar. Böylece ses çaldığında hoca ne okuyorsa tam o âyetin orijinal Arapça metni ve meali
 * eşzamanlı olarak ekranda takip edilir.
 */
object QuranAyahProvider {

    private val memoryCache = ConcurrentHashMap<Int, List<Ayah>>()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Yerleşik Tam Sûreler (Çevrimdışı hazır)
    private val curatedSurahAyahs: Map<Int, List<Ayah>> = mapOf(
        // 1. Fatiha (7 Âyet)
        1 to listOf(
            Ayah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Rahmân ve Rahîm olan Allah'ın adıyla."),
            Ayah(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Hamd, âlemlerin Rabbi olan Allah'a mahsustur."),
            Ayah(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "O, Rahmândır ve Rahîmdir."),
            Ayah(1, 4, "مَالِكِ يَوْمِ الدِّينِ", "Din (hesap ve ceza) gününün mâlikidir."),
            Ayah(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Yalnız sana kulluk eder ve yalnız senden yardım dileriz."),
            Ayah(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Bizi doğru yola ilet;"),
            Ayah(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Kendilerine lütufta bulunduğun kimselerin yoluna; gazaba uğramışların ve sapmışların yoluna değil.")
        ),

        // 93. Duha (11 Âyet)
        93 to listOf(
            Ayah(93, 1, "وَالضُّحَىٰ", "Kuşluk vaktine andolsun,"),
            Ayah(93, 2, "وَاللَّيْلِ إِذَا سَجَىٰ", "Karanlığı çöktüğü zaman geceye andolsun ki,"),
            Ayah(93, 3, "مَا وَدَّعَكَ رَبُّكَ وَمَا قَلَىٰ", "Rabbin seni bırakmadı ve sana darılmadı."),
            Ayah(93, 4, "وَلَلْآخِرَةُ خَيْرٌ لَكَ مِنَ الْأُولَىٰ", "Elbette senin için ahiret, dünyadan daha hayırlıdır."),
            Ayah(93, 5, "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ", "Muhakkak ki Rabbin sana verecek, sen de hoşnut olacaksın."),
            Ayah(93, 6, "أَلَمْ يَجِدْكَ يَتِيمًا فَآوَىٰ", "O seni bir yetim iken bulup barındırmadı mı?"),
            Ayah(93, 7, "وَوَجَدَكَ ضَالًّا فَهَدَىٰ", "Seni yolunu kaybetmiş bulup doğru yola iletmedi mi?"),
            Ayah(93, 8, "وَوَجَدَكَ عَائِلًا فَأَغْنَىٰ", "Seni ihtiyaç içinde bulup zengin etmedi mi?"),
            Ayah(93, 9, "فَأَمَّا الْيَتِيمَ فَلَا تَقْهَرْ", "Öyleyse yetimi sakın ezme;"),
            Ayah(93, 10, "وَأَمَّا السَّائِلَ فَلَا تَنْهَرْ", "İsteyeni de sakın azarlama;"),
            Ayah(93, 11, "وَأَمَّا بِنِعْمَةِ رَبِّكَ فَحَدِّثْ", "Ve Rabbinin nimetini minnetle an.")
        ),

        // 94. Inshirah (8 Âyet)
        94 to listOf(
            Ayah(94, 1, "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ", "Biz senin göğsünü yarıp genişletmedik mi?"),
            Ayah(94, 2, "وَوَضَعْنَا عَنْكَ وِزْرَكَ", "Senden o ağır yükünü indirmedik mi?"),
            Ayah(94, 3, "الَّذِي أَنْقَضَ ظَهْرَكَ", "Ki o, senin belini bükmüştü."),
            Ayah(94, 4, "وَرَفَعْنَا لَكَ ذِكْرَكَ", "Senin şanını ve şerefini yüceltmedik mi?"),
            Ayah(94, 5, "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "Elbette her zorlukla beraber bir kolaylık vardır."),
            Ayah(94, 6, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Gerçekten, zorlukla beraber mutlaka bir kolaylık vardır."),
            Ayah(94, 7, "فَإِذَا فَرَغْتَ فَانْصَبْ", "Öyleyse bir işi bitirince hemen diğerine koyul,"),
            Ayah(94, 8, "وَإِلَىٰ رَبِّكَ فَارْغَبْ", "Ve yalnız Rabbine yönelip O'na rağbet et.")
        ),

        // 95. Tin (8 Âyet)
        95 to listOf(
            Ayah(95, 1, "وَالتِّينِ وَالزَّيْتُونِ", "İncire ve zeytine andolsun,"),
            Ayah(95, 2, "وَطُورِ سِينِينَ", "Sina Dağı'na andolsun,"),
            Ayah(95, 3, "وَهَٰذَا الْبَلَدِ الْأَمِينِ", "Ve şu güvenli şehre (Mekke'ye) andolsun ki;"),
            Ayah(95, 4, "لَقَدْ خَلَقْنَا الْإِنْسَانَ فِي أَحْسَنِ تَقْوِيمٍ", "Biz insanı en güzel biçimde yarattık."),
            Ayah(95, 5, "ثُمَّ رَدَدْنَاهُ أَسْفَلَ سَافِلِينَ", "Sonra onu aşağıların aşağısına indirdik;"),
            Ayah(95, 6, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ فَلَهُمْ أَجْرٌ غَيْرُ مَمْنُونٍ", "Ancak iman edip salih amel işleyenler müstesnadır; onlar için kesintisiz bir mükâfat vardır."),
            Ayah(95, 7, "فَمَا يُكَذِّبُكَ بَعْدُ بِالدِّينِ", "Öyleyse bundan sonra hangi şey sana hesap gününü yalanlatabilir?"),
            Ayah(95, 8, "أَلَيْسَ اللَّهُ بِأَحْكَمِ الْحَاكِمِينَ", "Allah, hüküm verenlerin en âdili değil midir?")
        ),

        // 97. Kadir (5 Âyet)
        97 to listOf(
            Ayah(97, 1, "إِنَّا أَنْزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", "Şüphesiz biz Kur'an'ı Kadir gecesinde indirdik."),
            Ayah(97, 2, "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", "Kadir gecesinin ne olduğunu sen nereden bileceksin?"),
            Ayah(97, 3, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِنْ أَلْفِ شَهْرٍ", "Kadir gecesi, bin aydan daha hayırlıdır."),
            Ayah(97, 4, "تَنَزَّلُ الْمَلَائِكَةُ وَالرُّوحُ فِيهَا بِإِذْنِ رَبِّهِمْ مِنْ كُلِّ أَمْرٍ", "Melekler ve Rûh (Cebrail), o gecede Rablerinin izniyle her türlü iş için iner de iner."),
            Ayah(97, 5, "سَلَامٌ هِيَ حَتَّىٰ مَطْلَعِ الْفَجْرِ", "O gece, tanyeri ağarıncaya kadar tam bir selamet ve esenliktir.")
        ),

        // 103. Asr (3 Âyet)
        103 to listOf(
            Ayah(103, 1, "وَالْعَصْرِ", "Asra (zamana) andolsun ki;"),
            Ayah(103, 2, "إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ", "Gerçekten insan kesin bir ziyan ve hüsran içindedir."),
            Ayah(103, 3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Ancak iman edip salih amel işleyenler, birbirlerine hakkı ve sabrı tavsiye edenler müstesnadır.")
        ),

        // 105. Fil (5 Âyet)
        105 to listOf(
            Ayah(105, 1, "أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ", "Rabbinin fil sahiplerine ne yaptığını görmedin mi?"),
            Ayah(105, 2, "أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ", "Onların tuzaklarını boşa çıkarmadı mı?"),
            Ayah(105, 3, "وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ", "Üzerlerine bölük bölük kuşlar gönderdi;"),
            Ayah(105, 4, "تَرْمِيهِمْ بِحِجَارَةٍ مِنْ سِجِّيلٍ", "Onlara pişmiş çamurdan taşlar atıyorlardı."),
            Ayah(105, 5, "فَجَعَلَهُمْ كَعَصْفٍ مَأْكُولٍ", "Nihayet onları yenilmiş ekin yaprağı gibi yaptı.")
        ),

        // 106. Kureys (4 Âyet)
        106 to listOf(
            Ayah(106, 1, "لِإِيلَافِ قُرَيْشٍ", "Kureyş'in uzlaşması ve güvenliği için,"),
            Ayah(106, 2, "إِيلَافِهِمْ رِحْلَةَ الشِّتَاءِ وَالصَّيْفِ", "Kış ve yaz seferlerinde güvenliğin sağlanması hatırına;"),
            Ayah(106, 3, "فَلْيَعْبُدُوا رَبَّ هَٰذَا الْبَيْتِ", "Artık onlar bu Beyt'in (Kâbe'nin) Rabbine kulluk etsinler;"),
            Ayah(106, 4, "الَّذِي أَطْعَمَهُمْ مِنْ جُوعٍ وَآمَنَهُمْ مِنْ خَوْفٍ", "O ki, onları açlıktan doyurdu ve her türlü korkudan emin kıldı.")
        ),

        // 107. Maun (7 Âyet)
        107 to listOf(
            Ayah(107, 1, "أَرَأَيْتَ الَّذِي يُكَذِّبُ بِالدِّينِ", "Gördün mü o hesap ve ceza gününü yalanlayanı?"),
            Ayah(107, 2, "فَذَٰلِكَ الَّذِي يَدُعُّ الْيَتِيمَ", "İşte yetimi itip kakan odur;"),
            Ayah(107, 3, "وَلَا يَحُضُّ عَلَىٰ طَعَامِ الْمِسْكِينِ", "Yoksulu doyurmaya önayak olmaz."),
            Ayah(107, 4, "فَوَيْلٌ لِلْمُصَلِّينَ", "Yazıklar olsun o namaz kılanlara ki;"),
            Ayah(107, 5, "الَّذِينَ هُمْ عَنْ صَلَاتِهِمْ سَاهُونَ", "Onlar namazlarından gafildirler;"),
            Ayah(107, 6, "الَّذِينَ هُمْ يُرَاءُونَ", "Onlar gösteriş yaparlar;"),
            Ayah(107, 7, "وَيَمْنَعُونَ الْمَاعُونَ", "Ve en ufak bir yardıma bile engel olurlar.")
        ),

        // 108. Kevser (3 Âyet)
        108 to listOf(
            Ayah(108, 1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Şüphesiz biz sana Kevser'i verdik."),
            Ayah(108, 2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "Şimdi sen Rabbin için namaz kıl ve kurban kes!"),
            Ayah(108, 3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Asıl soyu kesik olan, sana buğzedip düşmanlık besleyendir.")
        ),

        // 109. Kafirun (6 Âyet)
        109 to listOf(
            Ayah(109, 1, "قُلْ يَا أَيُّهَا الْكَافِرُونَ", "De ki: Ey inkârcılar!"),
            Ayah(109, 2, "لَا أَعْبُدُ مَا تَعْبُدُونَ", "Ben sizin taptıklarınıza tapmam."),
            Ayah(109, 3, "وَلَا أَنْتُمْ عَابِدُونَ مَا أَعْبُدُ", "Siz de benim taptığıma kulluk ediciler değilsiniz."),
            Ayah(109, 4, "وَلَا أَنَا عَابِدٌ مَا عَبَدْتُمْ", "Ben asla sizin taptıklarınıza tapacak değilim;"),
            Ayah(109, 5, "وَلَا أَنْتُمْ عَابِدُونَ مَا أَعْبُدُ", "Siz de benim taptığıma kulluk edecek değilsiniz."),
            Ayah(109, 6, "لَكُمْ دِينُكُمْ وَلِيَ دِينِ", "Sizin dininiz size, benim dinim banadır.")
        ),

        // 110. Nasr (3 Âyet)
        110 to listOf(
            Ayah(110, 1, "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", "Allah'ın yardımı ve fetih geldiğinde;"),
            Ayah(110, 2, "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا", "Ve insanların dalga dalga Allah'ın dinine girdiklerini gördüğünde;"),
            Ayah(110, 3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ ۚ إِنَّهُ كَانَ تَوَّابًا", "Hemen Rabbini hamd ile tesbih et ve O'ndan bağışlanma dile. Muhakkak ki O, tevbeleri çokça kabul edendir.")
        ),

        // 111. Tebbet (5 Âyet)
        111 to listOf(
            Ayah(111, 1, "تَبَّتْ يَدَا أَبِي لَهَبٍ وَتَبَّ", "Ebû Leheb'in elleri kurusun; zaten kurudu da!"),
            Ayah(111, 2, "مَا أَغْنَىٰ عَنْهُ مَالُهُ وَمَا كَسَبَ", "Ona ne malı fayda verdi, ne de kazandıkları."),
            Ayah(111, 3, "سَيَصْلَىٰ نَارًا ذَاتَ لَهَبٍ", "O, alevli bir ateşe girecektir;"),
            Ayah(111, 4, "وَامْرَأَتُهُ حَمَّالَةَ الْحَطَبِ", "Boynunda bükülmüş bir ip olduğu halde, odun taşıyıcı karısı da."),
            Ayah(111, 5, "فِي جِيدِهَا حَبْلٌ مِنْ مَسَدٍ", "Boynunda liften örülmüş bir iple.")
        ),

        // 112. Ihlas (4 Âyet)
        112 to listOf(
            Ayah(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "De ki: O, Allah birdir, tektir."),
            Ayah(112, 2, "اللَّهُ الصَّمَدُ", "Allah Samed'dir (her şey O'na muhtaçtır, O hiçbir şeye muhtaç değildir)."),
            Ayah(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "O, doğurmamış ve doğmamıştır."),
            Ayah(112, 4, "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Hiçbir şey O'nun dengi ve benzeri olmamıştır.")
        ),

        // 113. Felak (5 Âyet)
        113 to listOf(
            Ayah(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "De ki: Sabahın Rabbine sığınırım;"),
            Ayah(113, 2, "مِنْ شَرِّ مَا خَلَقَ", "Yarattığı şeylerin şerrinden,"),
            Ayah(113, 3, "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ", "Karanlığı çöktüğü zaman gecenin şerrinden,"),
            Ayah(113, 4, "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "Düğümlere üfleyen büyücülerin şerrinden,"),
            Ayah(113, 5, "وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ", "Ve haset ettiği zaman hasetçinin şerrinden.")
        ),

        // 114. Nas (6 Âyet)
        114 to listOf(
            Ayah(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "De ki: İnsanların Rabbine sığınırım;"),
            Ayah(114, 2, "مَلِكِ النَّاسِ", "İnsanların yegâne hükümdarına,"),
            Ayah(114, 3, "إِلَٰهِ النَّاسِ", "İnsanların yegâne ilahına;"),
            Ayah(114, 4, "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "O sinsi ve gizlice fısıldayan vesvesecinin şerrinden;"),
            Ayah(114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Ki o, insanların göğüslerine vesvese fısıldar;"),
            Ayah(114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "Gerek cinlerden gerekse insanlardan.")
        )
    )

    /**
     * Sûrenin tüm âyetlerini hemen döndürür (Önbellekten veya yerleşik listeden).
     */
    fun getAllAyahsForSurah(surah: Surah): List<Ayah> {
        val inMem = memoryCache[surah.number]
        if (inMem != null && inMem.size >= surah.ayahCount) {
            return inMem
        }

        val curated = curatedSurahAyahs[surah.number]
        if (curated != null && curated.size >= surah.ayahCount) {
            return curated
        }

        val existing = surah.ayahs
        if (existing.size >= surah.ayahCount) {
            return existing
        }

        val existingMap = (curated ?: existing).associateBy { it.ayahNumber }
        val result = mutableListOf<Ayah>()
        for (i in 1..surah.ayahCount) {
            val found = existingMap[i]
            if (found != null) {
                result.add(found)
            } else {
                result.add(
                    Ayah(
                        surahNumber = surah.number,
                        ayahNumber = i,
                        textAr = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿$i﴾",
                        textTr = "${surah.nameTr} Sûresi, $i. Âyet-i Kerîme yükleniyor..."
                    )
                )
            }
        }
        return result
    }

    /**
     * Kullanıcının hocayı dinlerken ekranda birebir takip edebilmesi için
     * resmi Al-Quran Uthmani Arapça metnini ve Diyanet mealini arka planda yükler,
     * önbelleğe kaydeder ve UI'ı anında günceller.
     */
    fun loadAyahsForSurah(
        context: Context,
        surah: Surah,
        onLoaded: (List<Ayah>) -> Unit
    ) {
        val inMem = memoryCache[surah.number]
        if (inMem != null && inMem.size >= surah.ayahCount) {
            onLoaded(inMem)
            return
        }

        val cacheDir = File(context.cacheDir, "quran_ayahs").apply { if (!exists()) mkdirs() }
        val cacheFile = File(cacheDir, "surah_${surah.number}.json")

        scope.launch {
            // 1. Önce yerel disk önbelleğini kontrol et
            if (cacheFile.exists() && cacheFile.length() > 500) {
                try {
                    val jsonStr = cacheFile.readText()
                    val parsed = parseAlQuranJson(surah.number, jsonStr)
                    if (parsed.isNotEmpty()) {
                        memoryCache[surah.number] = parsed
                        withContext(Dispatchers.Main) {
                            onLoaded(parsed)
                        }
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("QuranAyahProvider", "Disk cache parse error: ${e.message}")
                }
            }

            // 2. İnternetten eksiksiz Uthmani Arapça metin ve Diyanet Meali indir
            try {
                val apiUrl = "https://api.alquran.cloud/v1/surah/${surah.number}/editions/quran-uthmani,tr.diyanet"
                val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 15000
                    setRequestProperty("User-Agent", "Ezanla-Android")
                }
                conn.connect()
                if (conn.responseCode in 200..299) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val parsed = parseAlQuranJson(surah.number, body)
                    if (parsed.isNotEmpty()) {
                        try {
                            cacheFile.writeText(body)
                        } catch (_: Exception) {}

                        memoryCache[surah.number] = parsed
                        withContext(Dispatchers.Main) {
                            onLoaded(parsed)
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.w("QuranAyahProvider", "Online ayah fetch failed for surah ${surah.number}: ${e.message}")
            }
        }
    }

    private fun parseAlQuranJson(surahNumber: Int, jsonString: String): List<Ayah> {
        val result = mutableListOf<Ayah>()
        val root = JSONObject(jsonString)
        if (!root.has("data")) return result

        val dataArr = root.getJSONArray("data")
        if (dataArr.length() < 2) return result

        val arabicObj = dataArr.getJSONObject(0)
        val turkishObj = dataArr.getJSONObject(1)

        val arabicAyahs = arabicObj.getJSONArray("ayahs")
        val turkishAyahs = turkishObj.getJSONArray("ayahs")

        val count = arabicAyahs.length().coerceAtMost(turkishAyahs.length())
        for (i in 0 until count) {
            val aItem = arabicAyahs.getJSONObject(i)
            val tItem = turkishAyahs.getJSONObject(i)

            val ayahNum = aItem.getInt("numberInSurah")
            var aText = aItem.getString("text").trim()
            val tText = tItem.getString("text").trim()

            // 1. Âyetin başındaki tekrarlı Besmeleyi temizle (Fatiha hariç)
            if (surahNumber != 1 && surahNumber != 9 && ayahNum == 1) {
                val bismillahPrefix = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
                val bismillahPrefixAlt = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                if (aText.startsWith(bismillahPrefix)) {
                    aText = aText.removePrefix(bismillahPrefix).trim()
                } else if (aText.startsWith(bismillahPrefixAlt)) {
                    aText = aText.removePrefix(bismillahPrefixAlt).trim()
                }
            }

            result.add(
                Ayah(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNum,
                    textAr = aText,
                    textTr = tText
                )
            )
        }
        return result
    }
}
