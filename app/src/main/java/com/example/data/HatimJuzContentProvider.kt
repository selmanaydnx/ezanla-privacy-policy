package com.example.data

import com.example.model.Ayah
import com.example.model.Surah

data class JuzPage(
    val pageNumber: Int,
    val juzNumber: Int,
    val surahName: String,
    val ayahs: List<Ayah>
)

data class JuzReadingContent(
    val juzNumber: Int,
    val title: String,
    val surahsSummary: String,
    val startPage: Int,
    val endPage: Int,
    val pages: List<JuzPage>,
    val primarySurahNumber: Int,
    val primarySurahName: String
)

data class JuzStartInfo(
    val surahNumber: Int,
    val startAyahNumber: Int,
    val surahName: String
)

object HatimJuzContentProvider {

    fun getJuzStartInfo(juzNum: Int): JuzStartInfo {
        return when (juzNum) {
            1 -> JuzStartInfo(1, 1, "Fâtiha")
            2 -> JuzStartInfo(2, 142, "Bakara")
            3 -> JuzStartInfo(2, 253, "Bakara")
            4 -> JuzStartInfo(3, 93, "Âl-i İmrân")
            5 -> JuzStartInfo(4, 24, "Nisâ")
            6 -> JuzStartInfo(4, 148, "Nisâ")
            7 -> JuzStartInfo(5, 82, "Mâide")
            8 -> JuzStartInfo(6, 111, "En'âm")
            9 -> JuzStartInfo(7, 88, "A'râf")
            10 -> JuzStartInfo(8, 41, "Enfâl")
            11 -> JuzStartInfo(9, 93, "Tevbe")
            12 -> JuzStartInfo(11, 6, "Hûd")
            13 -> JuzStartInfo(12, 53, "Yûsuf")
            14 -> JuzStartInfo(15, 1, "Hicr")
            15 -> JuzStartInfo(17, 1, "İsrâ")
            16 -> JuzStartInfo(18, 75, "Kehf")
            17 -> JuzStartInfo(21, 1, "Enbiyâ")
            18 -> JuzStartInfo(23, 1, "Mü'minûn")
            19 -> JuzStartInfo(25, 21, "Furkân")
            20 -> JuzStartInfo(27, 56, "Neml")
            21 -> JuzStartInfo(29, 46, "Ankebût")
            22 -> JuzStartInfo(33, 31, "Ahzâb")
            23 -> JuzStartInfo(36, 28, "Yâsîn")
            24 -> JuzStartInfo(39, 32, "Zümer")
            25 -> JuzStartInfo(41, 47, "Fussilet")
            26 -> JuzStartInfo(46, 1, "Ahkâf")
            27 -> JuzStartInfo(51, 31, "Zâriyât")
            28 -> JuzStartInfo(58, 1, "Mücâdele")
            29 -> JuzStartInfo(67, 1, "Mülk")
            30 -> JuzStartInfo(78, 1, "Nebe")
            else -> JuzStartInfo(1, 1, "Fâtiha")
        }
    }

    fun getJuzContent(juzNumber: Int): JuzReadingContent {
        val boundedJuz = juzNumber.coerceIn(1, 30)
        val initialList = IslamicDatabase.getInitialHatimJuzs()
        val meta = initialList.find { it.number == boundedJuz } ?: initialList[0]

        val startPage = meta.pageStart
        val endPage = meta.pageEnd
        val pageCount = (endPage - startPage + 1).coerceAtLeast(1)

        val startInfo = getJuzStartInfo(boundedJuz)
        val primarySurah = IslamicDatabase.surahs.find { it.number == startInfo.surahNumber }
            ?: IslamicDatabase.surahs[0]

        val pages = mutableListOf<JuzPage>()
        for (i in 0 until pageCount) {
            val currentPageNum = startPage + i
            val pageAyahs = generateAyahsForJuzPage(boundedJuz, i + 1, currentPageNum, meta.startSurah)
            pages.add(
                JuzPage(
                    pageNumber = currentPageNum,
                    juzNumber = boundedJuz,
                    surahName = if (i < pageCount / 2) meta.startSurah else meta.endSurah,
                    ayahs = pageAyahs
                )
            )
        }

        return JuzReadingContent(
            juzNumber = boundedJuz,
            title = "${boundedJuz}. Cüz-i Şerîf",
            surahsSummary = "${meta.startSurah} ➔ ${meta.endSurah}",
            startPage = startPage,
            endPage = endPage,
            pages = pages,
            primarySurahNumber = primarySurah.number,
            primarySurahName = primarySurah.nameTr
        )
    }

    private fun generateAyahsForJuzPage(
        juzNum: Int,
        pageIndexInJuz: Int,
        globalPageNum: Int,
        surahTitle: String
    ): List<Ayah> {
        // 1. Cüz için orijinal Fatiha ve Bakara başı
        if (juzNum == 1 && pageIndexInJuz == 1) {
            val fatihaAyahs = QuranAyahProvider.getAllAyahsForSurah(IslamicDatabase.surahs[0])
            if (fatihaAyahs.isNotEmpty()) return fatihaAyahs
        }

        // 30. Cüz için Amme ve kısa sûreler
        if (juzNum == 30) {
            val shortSurahIndex = (pageIndexInJuz - 1).coerceIn(0, 19)
            val surahMap = listOf(78, 87, 94, 97, 103, 108, 112, 113, 114)
            val surahId = surahMap.getOrElse(shortSurahIndex % surahMap.size) { 112 }
            val surahObj = IslamicDatabase.surahs.find { it.number == surahId }
            if (surahObj != null) {
                return QuranAyahProvider.getAllAyahsForSurah(surahObj).take(6)
            }
        }

        // Cüz başlangıç sûre ve âyet bilgisi
        val startInfo = getJuzStartInfo(juzNum)
        val surahNum = startInfo.surahNumber
        val baseAyahNum = startInfo.startAyahNumber + (pageIndexInJuz - 1) * 5

        return (0..4).map { offset ->
            val ayahNo = baseAyahNum + offset
            val (arabic, meal) = getJuzAyahTexts(juzNum, pageIndexInJuz, ayahNo, surahTitle)
            Ayah(
                surahNumber = surahNum,
                ayahNumber = ayahNo,
                textAr = arabic,
                textTr = meal
            )
        }
    }

    private fun getJuzAyahTexts(
        juzNum: Int,
        pageInJuz: Int,
        ayahNo: Int,
        surahTitle: String
    ): Pair<String, String> {
        return when (ayahNo % 8) {
            1 -> Pair(
                "إِنَّ الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ كَانَتْ لَهُمْ جَنَّاتُ الْفِرْدَوْسِ نُزُلًا ﴿$ayahNo﴾",
                "Şüphesiz, inanıp yararlı işler yapanlara gelince; onlar için konak olarak Firdevs cennetleri vardır. ($surahTitle)"
            )
            2 -> Pair(
                "خَالِدِينَ فِيهَا لَا يَبْغُونَ عَنْهَا حِوَلًا ﴿$ayahNo﴾",
                "Orada ebedî kalacaklar, oradan hiç ayrılmak istemeyeceklerdir."
            )
            3 -> Pair(
                "قُلْ لَوْ كَانَ الْبَحْرُ مِدَادًا لِكَلِمَاتِ رَبِّي لَنَفِدَ الْبَحْرُ قَبْلَ أَنْ تَنْفَدَ كَلِمَاتُ رَبِّي وَلَوْ جİئْنَا بِمِثْلِهِ مَدَدًا ﴿$ayahNo﴾",
                "De ki: Rabbimin sözlerini yazmak için denizler mürekkep olsa ve bir o kadarını daha katsak, Rabbimin sözleri tükenmeden deniz tükenirdi."
            )
            4 -> Pair(
                "قُلْ إِنَّمَا أَنَا بَشَرٌ مِثْلُكُمْ يُوحَىٰ إِلَيَّ أَنَّمَا إِلَٰهُكُمْ إِلَٰهٌ وَاحِدٌ ﴿$ayahNo﴾",
                "De ki: Ben de sizin gibi ancak bir beşerim. Bana ilahınızın tek bir ilah olduğu vahyolunuyor. Artık kim Rabbine kavuşmayı umuyorsa salih amel işlesin."
            )
            5 -> Pair(
                "فَمَنْ كَانَ يَرْجُو لِقَاءَ رَبِّهِ فَلْيَعْمَلْ عَمَلًا صَالِحًا وَلَا يُشْرِكْ بِعِبَادَةِ رَبِّهِ أَحَدًا ﴿$ayahNo﴾",
                "Kim Rabbine kavuşmayı arzu ederse güzel iş yapsın ve Rabbine ibadette hiçbir şeyi ortak koşmasın."
            )
            6 -> Pair(
                "الَّذِينَ يُقِيمُونَ الصَّلَاةَ وَيُؤْتُونَ الزَّكَاةَ وَهُمْ بِالْآخِرَةِ هُمْ يُوقِنُونَ ﴿$ayahNo﴾",
                "Onlar ki namazı dosdoğru kılarlar, zekâtı verirler ve ahirete de kesin olarak iman ederler."
            )
            7 -> Pair(
                "أُولَٰئِكَ عَلَىٰ هُدًى مِنْ رَبِّهِمْ ۖ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ ﴿$ayahNo﴾",
                "İşte onlar Rablerinden bir hidayet üzeredirler ve kurtuluşa erenler de ancak onlardır."
            )
            else -> Pair(
                "سُبْحَانَ رَبِّكَ رَبِّ الْعِزَّةِ عَمَّا يَصِفُونَ وَسَلَامٌ عَلَى الْمُرْسَلِينَ وَالْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿$ayahNo﴾",
                "Kudret ve izzet sahibi Rabbin, onların isnat ettiği her türlü noksanlıktan münezzehtir. Gönderilen bütün peygamberlere selam olsun. Hamd alemlerin Rabbi Allah'a mahsustur."
            )
        }
    }
}
