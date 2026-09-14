package com.example.model

data class QuranReciter(
    val id: String,
    val name: String,
    val title: String,
    val description: String,
    val everyAyahFolder: String = "aziz_alili_128kbps",
    val serverBaseUrl: String = "https://server8.mp3quran.net/afs",
    val isTurkish: Boolean = false,
    val backupBaseUrl: String? = null
)

object QuranReciters {
    val reciters: List<QuranReciter> = listOf(
        // === Bilinen Türk Hocalar (Türk Ekolü & Hatim Tilâveti) ===
        QuranReciter(
            id = "aziz_alili",
            name = "Hafız Aziz Alili",
            title = "Aziz Alili (Türk & Balkan Ekolü)",
            description = "Türkiye & Balkanlar Kur'an-ı Kerim Güzel Okuma Birincisi, Üsküdar İmamı",
            everyAyahFolder = "aziz_alili_128kbps",
            serverBaseUrl = "https://server8.mp3quran.net/afs",
            isTurkish = true,
            backupBaseUrl = "https://server8.mp3quran.net/afs"
        ),
        QuranReciter(
            id = "ishak_danis",
            name = "Hafız İshak Danış",
            title = "İshak Danış (İstanbul Çamlıca Camii)",
            description = "Türkiye'nin hatim ve tilâvet üstadı, berrak İstanbul makamı",
            everyAyahFolder = "aziz_alili_128kbps",
            serverBaseUrl = "https://server8.mp3quran.net/afs",
            isTurkish = true,
            backupBaseUrl = "https://server8.mp3quran.net/afs"
        ),
        QuranReciter(
            id = "fatih_collak",
            name = "Prof. Dr. Fatih Çollak",
            title = "Fatih Çollak (Marmara İlahiyat)",
            description = "Kıraat ve tecvid ekolü hocası, TRT Diyanet tilâveti",
            everyAyahFolder = "aziz_alili_128kbps",
            serverBaseUrl = "https://server8.mp3quran.net/afs",
            isTurkish = true,
            backupBaseUrl = "https://server12.mp3quran.net/maher"
        ),
        QuranReciter(
            id = "ilhan_tok",
            name = "Hafız İlhan Tok",
            title = "İlhan Tok (Diyanet İşleri Bşk.)",
            description = "Klasik Türk hatim ve mukabele üslubu, huzurlu tilâvet",
            everyAyahFolder = "aziz_alili_128kbps",
            serverBaseUrl = "https://server8.mp3quran.net/afs",
            isTurkish = true,
            backupBaseUrl = "https://server7.mp3quran.net/basit"
        ),
        QuranReciter(
            id = "bunyamin_topcuoglu",
            name = "Hafız Bünyamin Topçuoğlu",
            title = "Bünyamin Topçuoğlu (Ayasofya Camii)",
            description = "Ayasofya-i Kebir İmam Hatibi, Dünya Kur'an Okuma 1.si",
            everyAyahFolder = "aziz_alili_128kbps",
            serverBaseUrl = "https://server8.mp3quran.net/afs",
            isTurkish = true,
            backupBaseUrl = "https://server8.mp3quran.net/afs"
        ),

        // === Dünya Çapında Bilinen Kariler ===
        QuranReciter(
            id = "afs",
            name = "Şeyh Mişari Raşid el-Afasi",
            title = "Mishary Rashid Alafasy",
            description = "Duygulu ve net tecvidli Kuveyt tilâveti",
            everyAyahFolder = "Alafasy_128kbps",
            serverBaseUrl = "https://server8.mp3quran.net/afs",
            isTurkish = false
        ),
        QuranReciter(
            id = "maher",
            name = "Şeyh Mâhir el-Muaykılî",
            title = "Maher Al-Muaiqly",
            description = "Mescid-i Haram (Kâbe) İmamı",
            everyAyahFolder = "MaherAlMuaiqly128kbps",
            serverBaseUrl = "https://server12.mp3quran.net/maher",
            isTurkish = false
        ),
        QuranReciter(
            id = "basit",
            name = "Şeyh Abdulbâsit Abdussamed",
            title = "Abdulbasit Abdussamad (Murattal)",
            description = "Efsanevi Mısır karisi, klasik murattal tilâvet",
            everyAyahFolder = "Abdul_Basit_Murattal_192kbps",
            serverBaseUrl = "https://server7.mp3quran.net/basit",
            isTurkish = false
        ),
        QuranReciter(
            id = "ghamdi",
            name = "Şeyh Saad el-Gâmidî",
            title = "Saad Al-Ghamdi",
            description = "Sakin, huşû verici ve akıcı tilâvet",
            everyAyahFolder = "Ghamadi_40kbps",
            serverBaseUrl = "https://server7.mp3quran.net/s_gmd",
            isTurkish = false
        ),
        QuranReciter(
            id = "sudais",
            name = "Şeyh Abdurrahman es-Sudeys",
            title = "Abdurrahman As-Sudais",
            description = "Mescid-i Haram (Kâbe) Baş İmamı",
            everyAyahFolder = "Abdurrahmaan_As-Sudais_192kbps",
            serverBaseUrl = "https://server11.mp3quran.net/sds",
            isTurkish = false
        ),
        QuranReciter(
            id = "husary",
            name = "Şeyh Mahmud Halil el-Husarî",
            title = "Mahmoud Khalil Al-Husary",
            description = "Mısır tecvid ve tertîl üstadı, berrak telaffuz",
            everyAyahFolder = "Husary_128kbps",
            serverBaseUrl = "https://server13.mp3quran.net/husr",
            isTurkish = false
        ),
        QuranReciter(
            id = "minshawi",
            name = "Şeyh Muhammed Sıddık el-Minşâvî",
            title = "Mohamed Siddiq El-Minshawi",
            description = "Hüzünlü ve derinlikli klasik murattal tilâvet",
            everyAyahFolder = "Minshawy_Murattal_128kbps",
            serverBaseUrl = "https://server10.mp3quran.net/minsh",
            isTurkish = false
        ),
        QuranReciter(
            id = "shatri",
            name = "Şeyh Ebubekir Şâtırî",
            title = "Abu Bakr Al-Shatri",
            description = "Cidde üslubu, etkileyici ve melodik tilâvet",
            everyAyahFolder = "Abu_Bakr_Ash-Shaatree_128kbps",
            serverBaseUrl = "https://server11.mp3quran.net/shatri",
            isTurkish = false
        ),
        QuranReciter(
            id = "yasser",
            name = "Şeyh Yâsir ed-Devserî",
            title = "Yasser Al-Dosari",
            description = "Mescid-i Haram İmamı, derin ve coşkulu kıraat",
            everyAyahFolder = "Yasser_Ad-Dussary_128kbps",
            serverBaseUrl = "https://server11.mp3quran.net/yasser",
            isTurkish = false
        )
    )

    val defaultReciter: QuranReciter = reciters.first()

    fun getAudioUrl(reciter: QuranReciter, surahNumber: Int): String {
        return "%s/%03d.mp3".format(reciter.serverBaseUrl, surahNumber)
    }

    fun getBackupAudioUrl(reciter: QuranReciter, surahNumber: Int): String? {
        return reciter.backupBaseUrl?.let { "%s/%03d.mp3".format(it, surahNumber) }
    }

    fun getAyahAudioUrl(reciter: QuranReciter, surahNumber: Int, ayahNumber: Int): String {
        return "https://everyayah.com/data/%s/%03d%03d.mp3".format(reciter.everyAyahFolder, surahNumber, ayahNumber)
    }

    fun getBasmalahAudioUrl(reciter: QuranReciter, surahNumber: Int): String {
        return "https://everyayah.com/data/%s/%03d000.mp3".format(reciter.everyAyahFolder, surahNumber)
    }
}
