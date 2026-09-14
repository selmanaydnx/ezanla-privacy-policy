package com.example.data

import com.example.model.*

object IslamicDatabase {

    // 1. ALL 114 SURAHS OF THE HOLY QURAN
    val surahs: List<Surah> = listOf(
        Surah(1, "Fâtiha", "الفاتحة", "Açılış", 7, "Mekke", "https://server8.mp3quran.net/afs/001.mp3", listOf(
            Ayah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Rahmân ve Rahîm olan Allah'ın adıyla."),
            Ayah(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Hamd, âlemlerin Rabbi olan Allah'a mahsustur."),
            Ayah(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "O, Rahmândır ve Rahîmdir."),
            Ayah(1, 4, "مَالِكِ يَوْمِ الدِّينِ", "Din (hesap ve ceza) gününün mâlikidir."),
            Ayah(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Yalnız sana kulluk eder ve yalnız senden yardım dileriz."),
            Ayah(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Bizi doğru yola ilet;"),
            Ayah(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Kendilerine lütufta bulunduğun kimselerin yoluna; gazaba uğramışların ve sapmışların yoluna değil.")
        )),
        Surah(2, "Bakara", "البقرة", "Boğa / Sığır", 286, "Medine", "https://server8.mp3quran.net/afs/002.mp3", listOf(
            Ayah(2, 1, "الم", "Elif, Lâm, Mîm."),
            Ayah(2, 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِلْمُتَّقِينَ", "Bu, kendisinde şüphe olmayan, muttakiler için bir yol gösterici olan kitaptır."),
            Ayah(2, 255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ", "Allah, O'ndan başka ilah yoktur; Hayy'dır, Kayyûm'dur. O'nu ne bir uyuklama tutar ne de bir uyku... (Âyetü'l-Kürsî)")
        )),
        Surah(3, "Âl-i İmrân", "آل عمران", "İmrân Ailesi", 200, "Medine", "https://server8.mp3quran.net/afs/003.mp3"),
        Surah(4, "Nisâ", "النساء", "Kadınlar", 176, "Medine", "https://server8.mp3quran.net/afs/004.mp3"),
        Surah(5, "Mâide", "المائدة", "Sofra", 120, "Medine", "https://server8.mp3quran.net/afs/005.mp3"),
        Surah(6, "En'âm", "الأنعام", "Ehli Hayvanlar", 165, "Mekke", "https://server8.mp3quran.net/afs/006.mp3"),
        Surah(7, "A'râf", "الأعراف", "Yüksek Yerler", 206, "Mekke", "https://server8.mp3quran.net/afs/007.mp3"),
        Surah(8, "Enfâl", "الأنفال", "Savaş Ganimetleri", 75, "Medine", "https://server8.mp3quran.net/afs/008.mp3"),
        Surah(9, "Tevbe", "التوبة", "Tövbe", 129, "Medine", "https://server8.mp3quran.net/afs/009.mp3"),
        Surah(10, "Yûnus", "يونس", "Yunus Peygamber", 109, "Mekke", "https://server8.mp3quran.net/afs/010.mp3"),
        Surah(11, "Hûd", "هود", "Hud Peygamber", 123, "Mekke", "https://server8.mp3quran.net/afs/011.mp3"),
        Surah(12, "Yûsuf", "يوسف", "Yusuf Peygamber", 111, "Mekke", "https://server8.mp3quran.net/afs/012.mp3"),
        Surah(13, "Ra'd", "الرعد", "Gök Gürültüsü", 43, "Medine", "https://server8.mp3quran.net/afs/013.mp3"),
        Surah(14, "İbrâhîm", "إبراهيم", "İbrahim Peygamber", 52, "Mekke", "https://server8.mp3quran.net/afs/014.mp3"),
        Surah(15, "Hicr", "الحجر", "Taşlık Bölge", 99, "Mekke", "https://server8.mp3quran.net/afs/015.mp3"),
        Surah(16, "Nahl", "النحل", "Bal Arısı", 128, "Mekke", "https://server8.mp3quran.net/afs/016.mp3"),
        Surah(17, "İsrâ", "الإسراء", "Gece Yürüyüşü", 111, "Mekke", "https://server8.mp3quran.net/afs/017.mp3"),
        Surah(18, "Kehf", "الكهف", "Mağara", 110, "Mekke", "https://server8.mp3quran.net/afs/018.mp3"),
        Surah(19, "Meryem", "مريم", "Hz. Meryem", 98, "Mekke", "https://server8.mp3quran.net/afs/019.mp3"),
        Surah(20, "Tâhâ", "طه", "Tâ-Hâ", 135, "Mekke", "https://server8.mp3quran.net/afs/020.mp3"),
        Surah(21, "Enbiyâ", "الأنبياء", "Peygamberler", 112, "Mekke", "https://server8.mp3quran.net/afs/021.mp3"),
        Surah(22, "Hac", "الحج", "Hac İbadeti", 78, "Medine", "https://server8.mp3quran.net/afs/022.mp3"),
        Surah(23, "Mü'minûn", "المؤمنون", "Müminler", 118, "Mekke", "https://server8.mp3quran.net/afs/023.mp3"),
        Surah(24, "Nûr", "النور", "Işık / Aydınlık", 64, "Medine", "https://server8.mp3quran.net/afs/024.mp3"),
        Surah(25, "Furkân", "الفرقان", "Hak ile Bâtılı Ayıran", 77, "Mekke", "https://server8.mp3quran.net/afs/025.mp3"),
        Surah(26, "Şuarâ", "الشعراء", "Şairler", 227, "Mekke", "https://server8.mp3quran.net/afs/026.mp3"),
        Surah(27, "Neml", "النمل", "Karınca", 93, "Mekke", "https://server8.mp3quran.net/afs/027.mp3"),
        Surah(28, "Kasas", "القصص", "Tarihi Kıssalar", 88, "Mekke", "https://server8.mp3quran.net/afs/028.mp3"),
        Surah(29, "Ankebût", "العنكبوت", "Örümcek", 69, "Mekke", "https://server8.mp3quran.net/afs/029.mp3"),
        Surah(30, "Rûm", "الروم", "Romalılar / Bizans", 60, "Mekke", "https://server8.mp3quran.net/afs/030.mp3"),
        Surah(31, "Lokmân", "لقمان", "Lokman Hekim", 34, "Mekke", "https://server8.mp3quran.net/afs/031.mp3"),
        Surah(32, "Secde", "السجدة", "Secde", 30, "Mekke", "https://server8.mp3quran.net/afs/032.mp3"),
        Surah(33, "Ahzâb", "الأحزاب", "Topluluklar / Fırkalar", 73, "Medine", "https://server8.mp3quran.net/afs/033.mp3"),
        Surah(34, "Sebe'", "سبأ", "Sebe Halkı", 54, "Mekke", "https://server8.mp3quran.net/afs/034.mp3"),
        Surah(35, "Fâtır", "فاطر", "Yaratan", 45, "Mekke", "https://server8.mp3quran.net/afs/035.mp3"),
        Surah(36, "Yâsîn", "يس", "Kur'an'ın Kalbi Yâsîn", 83, "Mekke", "https://server8.mp3quran.net/afs/036.mp3", listOf(
            Ayah(36, 1, "يس", "Yâsîn."),
            Ayah(36, 2, "وَالْقُرْآنِ الْحَكِيمِ", "Hikmet dolu Kur'an'a andolsun ki;"),
            Ayah(36, 3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", "Şüphesiz sen peygamberlerdensin."),
            Ayah(36, 4, "عَلَىٰ صِرَاطٍ مُسْتَقِيمٍ", "Doğru bir yol üzerindesin.")
        )),
        Surah(37, "Sâffât", "الصافات", "Sıra Sıra Dizilenler", 182, "Mekke", "https://server8.mp3quran.net/afs/037.mp3"),
        Surah(38, "Sâd", "ص", "Sâd Harfi", 88, "Mekke", "https://server8.mp3quran.net/afs/038.mp3"),
        Surah(39, "Zümer", "الزمر", "Zümreler / Gruplar", 75, "Mekke", "https://server8.mp3quran.net/afs/039.mp3"),
        Surah(40, "Mü'min (Gâfir)", "غافر", "Bağışlayan", 85, "Mekke", "https://server8.mp3quran.net/afs/040.mp3"),
        Surah(41, "Fussilet", "فصلت", "Genişçe Açıklanmış", 54, "Mekke", "https://server8.mp3quran.net/afs/041.mp3"),
        Surah(42, "Şûrâ", "الشورى", "İstişare / Danışma", 53, "Mekke", "https://server8.mp3quran.net/afs/042.mp3"),
        Surah(43, "Zuhruf", "الزخرف", "Altın Süsler", 89, "Mekke", "https://server8.mp3quran.net/afs/043.mp3"),
        Surah(44, "Duhân", "الدخان", "Duman", 59, "Mekke", "https://server8.mp3quran.net/afs/044.mp3"),
        Surah(45, "Câsiye", "الجاثية", "Diz Üstü Çökenler", 37, "Mekke", "https://server8.mp3quran.net/afs/045.mp3"),
        Surah(46, "Ahkâf", "الأحقاف", "Kum Tepeleri", 35, "Mekke", "https://server8.mp3quran.net/afs/046.mp3"),
        Surah(47, "Muhammed", "محمد", "Hz. Muhammed (s.a.v.)", 38, "Medine", "https://server8.mp3quran.net/afs/047.mp3"),
        Surah(48, "Fetih", "الفتح", "Büyük Zafer", 29, "Medine", "https://server8.mp3quran.net/afs/048.mp3"),
        Surah(49, "Hucurât", "الحجرات", "Odalar", 18, "Medine", "https://server8.mp3quran.net/afs/049.mp3"),
        Surah(50, "Kâf", "ق", "Kâf Harfi", 45, "Mekke", "https://server8.mp3quran.net/afs/050.mp3"),
        Surah(51, "Zâriyât", "الذاريات", "Tozutup Savuranlar", 60, "Mekke", "https://server8.mp3quran.net/afs/051.mp3"),
        Surah(52, "Tûr", "الطور", "Tûr Dağı", 49, "Mekke", "https://server8.mp3quran.net/afs/052.mp3"),
        Surah(53, "Necm", "النجم", "Kayan Yıldız", 62, "Mekke", "https://server8.mp3quran.net/afs/053.mp3"),
        Surah(54, "Kamer", "القمر", "Ay", 55, "Mekke", "https://server8.mp3quran.net/afs/054.mp3"),
        Surah(55, "Rahmân", "الرحمن", "Sonsuz Merhamet Sahibi", 78, "Medine", "https://server8.mp3quran.net/afs/055.mp3", listOf(
            Ayah(55, 1, "الرَّحْمَٰنُ", "Rahmân (olan Allah);"),
            Ayah(55, 2, "عَلَّمَ الْقُرْآنَ", "Kur'an'ı öğretti."),
            Ayah(55, 3, "خَلَقَ الْإِنْسَانَ", "İnsanı yarattı."),
            Ayah(55, 4, "عَلَّمَهُ الْبَيَانَ", "Ona beyanı (açıklamayı ve konuşmayı) öğretti.")
        )),
        Surah(56, "Vâkıa", "الواقعة", "Kıyamet Olayı", 96, "Mekke", "https://server8.mp3quran.net/afs/056.mp3"),
        Surah(57, "Hadîd", "الحديد", "Demir", 29, "Medine", "https://server8.mp3quran.net/afs/057.mp3"),
        Surah(58, "Mücâdele", "المجادلة", "Hakkını Arayan Kadın", 22, "Medine", "https://server8.mp3quran.net/afs/058.mp3"),
        Surah(59, "Haşr", "الحشر", "Toplanma / Sürgün", 24, "Medine", "https://server8.mp3quran.net/afs/059.mp3"),
        Surah(60, "Mümtehine", "الممتحنة", "İmtihan Edilen Kadın", 13, "Medine", "https://server8.mp3quran.net/afs/060.mp3"),
        Surah(61, "Saff", "الصف", "Sıra Sıra Dizilenler", 14, "Medine", "https://server8.mp3quran.net/afs/061.mp3"),
        Surah(62, "Cuma", "الجمعة", "Cuma Günü ve Namazı", 11, "Medine", "https://server8.mp3quran.net/afs/062.mp3"),
        Surah(63, "Münâfikûn", "المنافقون", "İkiyüzlüler / Münafıklar", 11, "Medine", "https://server8.mp3quran.net/afs/063.mp3"),
        Surah(64, "Teğâbün", "التغابن", "Aldanma ve Kâr Günü", 18, "Medine", "https://server8.mp3quran.net/afs/064.mp3"),
        Surah(65, "Talâk", "الطلاق", "Boşanma Hükümleri", 12, "Medine", "https://server8.mp3quran.net/afs/065.mp3"),
        Surah(66, "Tahrîm", "التحريم", "Haram Kılma", 12, "Medine", "https://server8.mp3quran.net/afs/066.mp3"),
        Surah(67, "Mülk (Tebâreke)", "الملك", "Mülk ve Egemenlik", 30, "Mekke", "https://server8.mp3quran.net/afs/067.mp3", listOf(
            Ayah(67, 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Mülk elinde bulunan Allah pek yücedir ve O her şeye hakkıyla kâdirdir."),
            Ayah(67, 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا", "O, hanginizin daha güzel amel yapacağını sınamak için ölümü ve hayatı yaratandır.")
        )),
        Surah(68, "Kalem", "القلم", "Kalem ve Yazı", 52, "Mekke", "https://server8.mp3quran.net/afs/068.mp3"),
        Surah(69, "Hâkka", "الحاقة", "Gerçekleşecek Kıyamet", 52, "Mekke", "https://server8.mp3quran.net/afs/069.mp3"),
        Surah(70, "Meâric", "المعارج", "Yükseliş Dereceleri", 44, "Mekke", "https://server8.mp3quran.net/afs/070.mp3"),
        Surah(71, "Nûh", "نوح", "Nuh Peygamber", 28, "Mekke", "https://server8.mp3quran.net/afs/071.mp3"),
        Surah(72, "Cin", "الجن", "Cinler", 28, "Mekke", "https://server8.mp3quran.net/afs/072.mp3"),
        Surah(73, "Müzzemmil", "المزمل", "Örtüsüne Bürünen", 20, "Mekke", "https://server8.mp3quran.net/afs/073.mp3"),
        Surah(74, "Müddessir", "المدثر", "Örtüsüne Sarınan", 56, "Mekke", "https://server8.mp3quran.net/afs/074.mp3"),
        Surah(75, "Kıyâme", "القيامة", "Kıyamet Günü", 40, "Mekke", "https://server8.mp3quran.net/afs/075.mp3"),
        Surah(76, "İnsân (Dehr)", "الإنسان", "İnsan ve Yaratılış", 31, "Medine", "https://server8.mp3quran.net/afs/076.mp3"),
        Surah(77, "Mürselât", "المرسلات", "Gönderilen Rüzgarlar", 50, "Mekke", "https://server8.mp3quran.net/afs/077.mp3"),
        Surah(78, "Nebe (Amme)", "النبأ", "Büyük Haber", 40, "Mekke", "https://server8.mp3quran.net/afs/078.mp3"),
        Surah(79, "Nâziât", "النازعات", "Söküp Çıkaranlar", 46, "Mekke", "https://server8.mp3quran.net/afs/079.mp3"),
        Surah(80, "Abese", "عبس", "Yüzünü Ekşitti", 42, "Mekke", "https://server8.mp3quran.net/afs/080.mp3"),
        Surah(81, "Tekvîr", "التكوير", "Dürülme", 29, "Mekke", "https://server8.mp3quran.net/afs/081.mp3"),
        Surah(82, "İnfitâr", "الانفطار", "Yarılma", 19, "Mekke", "https://server8.mp3quran.net/afs/082.mp3"),
        Surah(83, "Mutaffifîn", "المطففين", "Ölçüde Hile Yapanlar", 36, "Mekke", "https://server8.mp3quran.net/afs/083.mp3"),
        Surah(84, "İnşikâk", "الانشقاق", "Parçalanma", 25, "Mekke", "https://server8.mp3quran.net/afs/084.mp3"),
        Surah(85, "Bürûc", "البروج", "Burçlar", 22, "Mekke", "https://server8.mp3quran.net/afs/085.mp3"),
        Surah(86, "Târık", "الطارق", "Gece Çakan Yıldız", 17, "Mekke", "https://server8.mp3quran.net/afs/086.mp3"),
        Surah(87, "A'lâ", "الأعلى", "En Yüce Olan", 19, "Mekke", "https://server8.mp3quran.net/afs/087.mp3"),
        Surah(88, "Gâşiye", "الغاشية", "Her Şeyi Kuşatan Kıyamet", 26, "Mekke", "https://server8.mp3quran.net/afs/088.mp3"),
        Surah(89, "Fecr", "الفجر", "Tan Vakti / Şafak", 30, "Mekke", "https://server8.mp3quran.net/afs/089.mp3"),
        Surah(90, "Beled", "البلد", "Kutsal Şehir Mekke", 20, "Mekke", "https://server8.mp3quran.net/afs/090.mp3"),
        Surah(91, "Şems", "الشمس", "Güneş", 15, "Mekke", "https://server8.mp3quran.net/afs/091.mp3"),
        Surah(92, "Leyl", "الليل", "Gece", 21, "Mekke", "https://server8.mp3quran.net/afs/092.mp3"),
        Surah(93, "Duhâ", "الضحى", "Kuşluk Vakti", 11, "Mekke", "https://server8.mp3quran.net/afs/093.mp3"),
        Surah(94, "İnşirâh", "الشرح", "Gönül Ferahlığı", 8, "Mekke", "https://server8.mp3quran.net/afs/094.mp3", listOf(
            Ayah(94, 1, "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ", "Biz senin göğsünü açıp genişletmedik mi?"),
            Ayah(94, 5, "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "Demek ki zorlukla beraber bir kolaylık vardır."),
            Ayah(94, 6, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Evet, elbette zorlukla beraber bir kolaylık vardır.")
        )),
        Surah(95, "Tîn", "التين", "İncir", 8, "Mekke", "https://server8.mp3quran.net/afs/095.mp3"),
        Surah(96, "Alak", "العلق", "İlk Vahiy / Pıhtı", 19, "Mekke", "https://server8.mp3quran.net/afs/096.mp3", listOf(
            Ayah(96, 1, "اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ", "Yaratan Rabbinin adıyla oku!"),
            Ayah(96, 2, "خَلَقَ الْإِنْسَانَ مِنْ عَلَقٍ", "O, insanı bir kan pıhtısından (embriyodan) yarattı."),
            Ayah(96, 3, "اقْرَأْ وَرَبُّكَ الْأَكْرَمُ", "Oku! Rabbin sonsuz kerem sahibidir.")
        )),
        Surah(97, "Kadir", "القدر", "Kadir Gecesi", 5, "Mekke", "https://server8.mp3quran.net/afs/097.mp3", listOf(
            Ayah(97, 1, "إِنَّا أَنْزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", "Şüphesiz biz onu Kadir Gecesi'nde indirdik."),
            Ayah(97, 2, "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", "Kadir Gecesi'nin ne olduğunu sen bilir misin?"),
            Ayah(97, 3, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِنْ أَلْفِ شَهْرٍ", "Kadir Gecesi bin aydan daha hayırlıdır.")
        )),
        Surah(98, "Beyyine", "البينة", "Açık Delil", 8, "Medine", "https://server8.mp3quran.net/afs/098.mp3"),
        Surah(99, "Zilzâl", "الزلزلة", "Büyük Sarsıntı / Deprem", 8, "Medine", "https://server8.mp3quran.net/afs/099.mp3"),
        Surah(100, "Âdiyât", "العاديات", "Koşan Atlar", 11, "Mekke", "https://server8.mp3quran.net/afs/100.mp3"),
        Surah(101, "Kâria", "القارعة", "Çarpan Felaket", 11, "Mekke", "https://server8.mp3quran.net/afs/101.mp3"),
        Surah(102, "Tekâsür", "التكاثر", "Çoklukla Övünme", 8, "Mekke", "https://server8.mp3quran.net/afs/102.mp3"),
        Surah(103, "Asr", "العصر", "Zaman / Çağ", 3, "Mekke", "https://server8.mp3quran.net/afs/103.mp3", listOf(
            Ayah(103, 1, "وَالْعَصْرِ", "Zamana andolsun ki;"),
            Ayah(103, 2, "إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ", "İnsan gerçekten hüsrandadır."),
            Ayah(103, 3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Ancak iman edip salih ameller işleyenler, birbirlerine hakkı ve sabrı tavsiye edenler müstesnadır.")
        )),
        Surah(104, "Hümeze", "الهمزة", "Arkadan Çekiştirenler", 9, "Mekke", "https://server8.mp3quran.net/afs/104.mp3"),
        Surah(105, "Fîl", "الفيل", "Fil Vakası", 5, "Mekke", "https://server8.mp3quran.net/afs/105.mp3"),
        Surah(106, "Kureyş", "قريش", "Kureyş Kabilesi", 4, "Mekke", "https://server8.mp3quran.net/afs/106.mp3"),
        Surah(107, "Mâûn", "الماعون", "Küçük Yardımlar", 7, "Mekke", "https://server8.mp3quran.net/afs/107.mp3"),
        Surah(108, "Kevser", "الكوثر", "Bolluk / Kevser Havuzu", 3, "Mekke", "https://server8.mp3quran.net/afs/108.mp3", listOf(
            Ayah(108, 1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Şüphesiz biz sana Kevser'i verdik."),
            Ayah(108, 2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "Şu halde Rabbin için namaz kıl ve kurban kes."),
            Ayah(108, 3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Doğrusu sana kin besleyenin soyu kesiktir.")
        )),
        Surah(109, "Kâfirûn", "الكافرون", "İnkârcılar", 6, "Mekke", "https://server8.mp3quran.net/afs/109.mp3", listOf(
            Ayah(109, 1, "قُلْ يَا أَيُّهَا الْكَافِرُونَ", "De ki: Ey inkârcılar!"),
            Ayah(109, 2, "لَا أَعْبُدُ مَا تَعْبُدُونَ", "Ben sizin taptıklarınıza tapmam."),
            Ayah(109, 6, "لَكُمْ دِينُكُمْ وَلِيَ دِينِ", "Sizin dininiz size, benim dinim banadır.")
        )),
        Surah(110, "Nasr", "النصر", "Yardım ve Zafer", 3, "Medine", "https://server8.mp3quran.net/afs/110.mp3"),
        Surah(111, "Tebbet (Mesed)", "المسد", "Hurma Lifi", 5, "Mekke", "https://server8.mp3quran.net/afs/111.mp3"),
        Surah(112, "İhlâs", "الإخلاص", "Tevhîd / Samimiyet", 4, "Mekke", "https://server8.mp3quran.net/afs/112.mp3", listOf(
            Ayah(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "De ki: O Allah tektir."),
            Ayah(112, 2, "اللَّهُ الصَّمَدُ", "Allah Samed'dir (her şey O'na muhtaçtır, O hiçbir şeye muhtaç değildir)."),
            Ayah(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "O doğurmamış ve doğmamıştır."),
            Ayah(112, 4, "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Ve hiçbir şey O'nun dengi olmamıştır.")
        )),
        Surah(113, "Felak", "الفلق", "Sabah Aydınlığı", 5, "Mekke", "https://server8.mp3quran.net/afs/113.mp3", listOf(
            Ayah(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "De ki: Sabahın Rabbine sığınırım,"),
            Ayah(113, 2, "مِنْ شَرِّ مَا خَلَقَ", "Yarattığı şeylerin şerrinden,"),
            Ayah(113, 3, "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ", "Karanlığı çöktüğü zaman gecenin şerrinden,")
        )),
        Surah(114, "Nâs", "الناس", "İnsanlar", 6, "Mekke", "https://server8.mp3quran.net/afs/114.mp3", listOf(
            Ayah(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "De ki: İnsanların Rabbine sığınırım,"),
            Ayah(114, 2, "مَلِكِ النَّاسِ", "İnsanların mâlikine (hükümdarına),"),
            Ayah(114, 3, "إِلَٰهِ النَّاسِ", "İnsanların gerçek ilahına,")
        ))
    )

    // 2. TAJWEED (TECVİD) RULES WITH CLEAR EXPLANATION & EXAMPLES
    val tajweedRules: List<TajweedRule> = listOf(
        TajweedRule(
            title = "İhfâ (Gizleme)",
            titleAr = "الإخفاء",
            description = "Sâkin Nûn (نْ) veya Tenvîn'den (ً ٍ ٌ) sonra 15 ihfa harfinden biri gelirse genizden 1.5 elif miktarı tutularak ses gizlenir.",
            colorHex = 0xFFEC4899,
            examples = listOf(
                TajweedExample("مِنْ قَبْلِ", "نْ ق", "Sakin nun'dan sonra 'Kaf' harfi gelmiştir. Genizden tutularak okunur."),
                TajweedExample("عَنْ صَلَاتِهِمْ", "نْ ص", "Sakin nun'dan sonra 'Sad' harfi gelmiştir."),
                TajweedExample("كِتَابًا كَرِيمًا", "بًا ك", "Tenvinden sonra 'Kef' harfi gelmiştir.")
            )
        ),
        TajweedRule(
            title = "İzhâr (Açıklama)",
            titleAr = "الإظهار",
            description = "Sâkin Nûn veya Tenvîn'den sonra 6 boğaz harfi (ء هـ ع ح غ خ) gelirse, ses tutulmadan apaçık ve net çıkarılır.",
            colorHex = 0xFF3B82F6,
            examples = listOf(
                TajweedExample("مَنْ آمَنَ", "نْ آ", "Sakin nun'dan sonra boğaz harfi olan Hemze gelmiştir, tutulmadan net okunur."),
                TajweedExample("عَلِيمٌ حَكِيمٌ", "مٌ ح", "Tenvinden sonra 'Ha' harfi gelmiştir.")
            )
        ),
        TajweedRule(
            title = "İklâb (Çevirme)",
            titleAr = "الإقلاب",
            description = "Sâkin Nûn veya Tenvîn'den sonra 'Bâ' (ب) harfi gelirse, nun sesi sâkin Mîm (مْ) sesine dönüştürülür ve genizden tutulur.",
            colorHex = 0xFFF59E0B,
            examples = listOf(
                TajweedExample("مِنْ بَعْدِ", "نْ ب (mim okunur)", "'Mim ba'di' şeklinde okunur."),
                TajweedExample("سَمِيعٌ بَصِيرٌ", "عٌ ب", "'Semi'um basiyr' şeklinde nun mime çevrilir.")
            )
        ),
        TajweedRule(
            title = "İdgâm-ı Mea'l-Gunne (Tutuşturmalı Kaynaştırma)",
            titleAr = "الإدغام بغنة",
            description = "Sâkin Nûn veya Tenvîn'den sonra (ي ن م و - YANMU) harfleri gelirse, harfler birbirine kaynaştırılır ve geniz sesiyle tutulur.",
            colorHex = 0xFF10B981,
            examples = listOf(
                TajweedExample("مَنْ يَقُولُ", "نْ ي", "'Mey-yekûlü' şeklinde kaynaştırılarak okunur."),
                TajweedExample("مِنْ وَالٍ", "نْ و", "'Miv-vâlin' şeklinde okunur.")
            )
        ),
        TajweedRule(
            title = "Kalkale (Sarsma / Vurgulama)",
            titleAr = "القلقلة",
            description = "Kalkale harfleri (ق ط ب ج د - Kutbu Cedin) kelime ortasında veya sonunda sâkin (cezimli) geldiğinde mahreç sarsılarak kuvvetli ses verilir.",
            colorHex = 0xFF8B5CF6,
            examples = listOf(
                TajweedExample("قُلْ هُوَ اللَّهُ أَحَدْ", "دْ", "Sonundaki Dal harfi cezimli olduğu için kuvvetle sarsılır."),
                TajweedExample("الْفَلَقْ", "قْ", "Kaf harfi sarsılır.")
            )
        ),
        TajweedRule(
            title = "Medd-i Muttasıl (Bitişik Uzatma)",
            titleAr = "المد المتصل",
            description = "Med harfinden sonra aynı kelimede sebebi medden 'Hemze' (ء) gelirse 4 elif miktarı vacip olarak uzatılır.",
            colorHex = 0xFFEF4444,
            examples = listOf(
                TajweedExample("جَاءَ", "آءَ", "Elif ve hemze aynı kelimede bitişiktir, 4 elif uzatılır."),
                TajweedExample("السَّمَاءِ", "مَاءِ", "Dört elif miktarı uzatılır.")
            )
        )
    )

    // 3. 99 NAMES OF ALLAH (ESMAÜL HÜSNA)
    val asmaUlHusna: List<AsmaUlHusna> = listOf(
        AsmaUlHusna(1, "الله", "Allah", "Kendisinden başka ilah olmayan, her şeyin mutlak sahibi olan tek yaratıcı.", 66, "Her gün 66 defa zikredenin kalbi iman nuru ile dolar."),
        AsmaUlHusna(2, "الرَّحْمَٰنُ", "Er-Rahmân", "Dünyada bütün mahlûkata merhamet eden, şefkat gösteren.", 298, "Günde 298 defa okuyanın kalbi yumuşar, merhamet sahibi olur."),
        AsmaUlHusna(3, "الرَّحِيمُ", "Er-Rahîm", "Ahirette yalnızca mümin kullarına merhamet ve ihsan eden.", 258, "Günde 258 defa okuyan rızkında bereket ve huzur bulur."),
        AsmaUlHusna(4, "الْمَلِكُ", "El-Melik", "Bütün kâinatın gerçek sahibi, mutlak hâkimi ve sultanı.", 90, "Günde 90 defa çeken kimse maddî ve manevî kudret kazanır."),
        AsmaUlHusna(5, "الْقُدُّوسُ", "El-Kuddûs", "Her türlü noksanlıktan, ayıptan ve kusurdan münezzeh ve pâk olan.", 170, "Günde 170 defa okuyanın kalbi manevi kirlerden ve vesveselerden arınır."),
        AsmaUlHusna(6, "السَّلَامُ", "Es-Selâm", "Kullarını her türlü tehlikelerden selâmete çıkaran, barış ve esenliğin kaynağı.", 131, "Günde 131 defa hastaya okunursa biiznillah şifaya kavuşur."),
        AsmaUlHusna(7, "الْمُؤْمِنُ", "El-Mü'min", "Gönüllerde iman ışığını yakan, güven ve emniyet bahşeden.", 136, "Günde 136 defa zikreden düşman şerrinden ve korkulardan emin olur."),
        AsmaUlHusna(8, "الْمُهَيْمِنُ", "El-Müheymin", "Bütün varlıkları gözetip koruyan, her şeyden haberdar olan.", 145, "Günde 145 defa zikreden kimsenin basireti ve hafızası güçlenir."),
        AsmaUlHusna(9, "الْعَزِيزُ", "El-Azîz", "İzzet sahibi, mağlup edilmesi asla mümkün olmayan tek galip.", 94, "Günde 94 defa zikreden kimse insanlar arasında izzet ve itibar bulur."),
        AsmaUlHusna(10, "الْجَبَّارُ", "El-Cebbâr", "Kırılanları onaran, dilediğini zorla da olsa yaptıran mutlak kudret.", 206, "Günde 206 defa okuyan zalimlerin haksızlığından korunur."),
        AsmaUlHusna(11, "الْمُتَكَبِّرُ", "El-Mütekebbir", "Büyüklükte eşi ve benzeri olmayan, azamet sahibi.", 662, "Günde 662 defa okuyan vakar, şeref ve manevi mertebe kazanır."),
        AsmaUlHusna(12, "الْخَالِقُ", "El-Hâlık", "Her şeyi yoktan var eden, yaratan.", 731, "Günde 731 defa okuyanın işleri kolaylaşır, sıkıntılardan kurtulur."),
        AsmaUlHusna(13, "الْبَارِئُ", "El-Bâri'", "Her şeyi kusursuz ve birbiriyle uyumlu olarak yaratan.", 213, "Günde 213 defa okuyan ruhi ve bedeni hastalıklardan şifa bulur."),
        AsmaUlHusna(14, "الْمُصَوِّرُ", "El-Musavvir", "Her varlığa kendine has şekil, suret ve özellik veren.", 336, "Günde 336 defa okuyanın sanatı ve kabiliyetleri inkişaf eder."),
        AsmaUlHusna(15, "الْغَفَّارُ", "El-Gaffâr", "Günahları çokça bağışlayan, örten ve affeden.", 1281, "Günde 1281 defa çekenin günahları bağışlanır, tevbeleri kabul olur."),
        AsmaUlHusna(16, "الْقَهَّارُ", "El-Kahhâr", "Her şeye boyun eğdiren, mutlak galebe sahibi.", 306, "Günde 306 defa okuyan nefsinin ve şeytanın esaretinden kurtulur."),
        AsmaUlHusna(17, "الْوَهَّابُ", "El-Vehhâb", "Karşılıksız, bol bol nimet ve hibe bağışlayan.", 14, "Günde 14 defa okuyanın rızkı çoğalır, darlık görmez."),
        AsmaUlHusna(18, "الرَّزَّاقُ", "Er-Rezzâk", "Bütün mahlûkatın rızkını veren ve onları doyuran.", 308, "Sabah namazından sonra 308 defa okuyanın rızık kapıları açılır."),
        AsmaUlHusna(19, "الْفَتَّاحُ", "El-Fettâh", "Her türlü kapalı kapıları açan, zorlukları kolaylaştıran.", 489, "Günde 489 defa okuyanın işleri açılır, kalbindeki kederler kalkar."),
        AsmaUlHusna(20, "الْعَلِيمُ", "El-Alîm", "Gizliyi ve açığı, geçmişi ve geleceği hakkıyla bilen.", 150, "Günde 150 defa okuyanın ilmi, idraki ve hafızası kuvvetlenir."),
        AsmaUlHusna(21, "الْبَاسِطُ", "El-Bâsıt", "Rızkı genişleten, ferahlık ve neşe veren.", 72, "Kuşluk namazından sonra 72 defa zikredenin kalbi ferahlar."),
        AsmaUlHusna(22, "الْخَافِضُ", "El-Hâfıd", "Kibir ve gurur sahiplerini alçaltan.", 1481, "Zalimlerin şerrinden korunmak için okunur."),
        AsmaUlHusna(23, "الرَّافِعُ", "Er-Râfi'", "Müminleri ve tevazu sahiplerini yükselten.", 351, "Günde 351 defa okuyanın manevi derecesi ve itibarı yükselir."),
        AsmaUlHusna(24, "الْمُعِزُّ", "El-Mu'izz", "Dilediğine izzet, şeref ve itibar bahşeden.", 117, "Pazartesi ve Cuma geceleri zikreden halk arasında sevilir."),
        AsmaUlHusna(25, "الْمُذِلُّ", "El-Müzill", "Dilediğini zillete düşüren ve rüsva eden.", 770, "Haksızlığa karşı haklı çıkmak için zikredilir."),
        AsmaUlHusna(26, "السَّمِيعُ", "Es-Semî'", "Her şeyi, gizli fısıltıları dahi hakkıyla işiten.", 180, "Günde 180 defa okuyanın duaları kabul olur."),
        AsmaUlHusna(27, "الْبَصِيرُ", "El-Basîr", "Her şeyi en ince ayrıntısına kadar gören.", 302, "Cuma namazından önce okuyanın basireti açılır."),
        AsmaUlHusna(28, "الْحَكَمُ", "El-Hakem", "Hüküm veren, hakkı bâtıldan ayıran adil hâkim.", 68, "Günde 68 defa zikredenin kalbine hikmet pınarları akar."),
        AsmaUlHusna(29, "الْعَدْلُ", "El-Adl", "Sonsuz adalet sahibi, zulümden münezzeh olan.", 104, "Cuma gecesi zikredenin ahlakı güzelleşir."),
        AsmaUlHusna(30, "اللَّطِيفُ", "El-Latîf", "Lütuf ve ihsan sahibi, en ince sırlara nüfuz eden.", 129, "Günde 129 defa okuyanın dilekleri gerçekleşir, sıkıntıları gider."),
        AsmaUlHusna(31, "الْخَبِيرُ", "El-Habîr", "Her şeyin iç yüzünden ve gizli sırlarından haberdar olan.", 812, "Günde 812 defa okuyan rüyasında bilmek istediği hayrı görür."),
        AsmaUlHusna(32, "الْحَلِيمُ", "El-Halîm", "Cezalandırmada acele etmeyen, hilm sahibi.", 88, "Günde 88 defa zikredenin öfkesi yatışır, sükunet bulur."),
        AsmaUlHusna(33, "الْعَظِيمُ", "El-Azîm", "Büyüklükte benzeri olmayan, sınırsız azamet sahibi.", 1020, "Günde 1020 defa okuyan insanlar nezdinde hürmet görür."),
        AsmaUlHusna(34, "الْغَفُورُ", "El-Gafûr", "Mağfireti sınırsız, günahları bağışlayan.", 1286, "Günde 1286 defa zikredenin kalbi huzurla dolar, kederi biter."),
        AsmaUlHusna(35, "الشَّكُورُ", "Eş-Şekûr", "Az amele çok sevap ve mükâfat veren.", 526, "Günde 526 defa zikredenin nimeti ve sağlığı artar."),
        AsmaUlHusna(36, "الْعَلِيُّ", "El-Aliyy", "Yücelikte son derece üstün ve benzeri olmayan.", 110, "Günde 110 defa zikredenin derecesi yükselir."),
        AsmaUlHusna(37, "الْكَبِيرُ", "El-Kebîr", "Büyüklüğü akıllara sığmayan, yüce Mevlâ.", 232, "Günde 232 defa zikreden borçlarından ve darlıktan kurtulur."),
        AsmaUlHusna(38, "الْحَفِيظُ", "El-Hafîz", "Her şeyi muhafaza eden, koruyup gözeten.", 998, "Günde 998 defa zikreden kaza, bela ve musibetlerden korunur."),
        AsmaUlHusna(39, "الْمُقِيتُ", "El-Mukît", "Her canlının gıdasını ve azığını vaktinde ulaştıran.", 550, "Günde 550 defa zikredenin rızkı genişler."),
        AsmaUlHusna(40, "الْحَسِيبُ", "El-Hasîb", "Kulların hesabını en iyi gören, kifayet eden.", 80, "Günde 80 defa okuyan düşman şerrinden güvende olur."),
        AsmaUlHusna(41, "الْجَلِيلُ", "El-Celîl", "Celal ve azamet sahibi, ululuk kemalinde olan.", 73, "Günde 73 defa okuyan zalimlerin karşısında güçlü durur."),
        AsmaUlHusna(42, "الْكَرِيمُ", "El-Kerîm", "Cömertliği sınırsız, ihsanı bol olan.", 270, "Yatağa girince okuyan hayırlı kapılara kavuşur."),
        AsmaUlHusna(43, "الرَّقِيبُ", "Er-Rakîb", "Her varlığı her an gözetleyen, murakabe eden.", 312, "Günde 312 defa okuyan malını ve evladını korur."),
        AsmaUlHusna(44, "الْمُجِيبُ", "El-Mucîb", "Kendisine yapılan samimi dualara icabet eden.", 55, "Günde 55 defa okuyanın duaları süratle kabul olunur."),
        AsmaUlHusna(45, "الْوَاسِعُ", "El-Vâsi'", "İlmi, rahmeti ve kudreti her şeyi kuşatan.", 137, "Günde 137 defa okuyanın rızkı ve ömrü genişler."),
        AsmaUlHusna(46, "الْحَكِيمُ", "El-Hakîm", "Her emri ve fiili hikmetli olan.", 78, "Günde 78 defa okuyanın aklı ve anlayışı açılır."),
        AsmaUlHusna(47, "الْوَدُودُ", "El-Vedûd", "Kullarını çok seven, sevilmeye layık olan.", 20, "Günde 20 defa okuyan aile ve çevresinde derin muhabbet görür."),
        AsmaUlHusna(48, "الْمَجِيدُ", "El-Mecîd", "Şânı ve şerefi pek yüce, keremi sonsuz olan.", 57, "Günde 57 defa okuyan rütbe ve izzet bulur."),
        AsmaUlHusna(49, "الْبَاعِثُ", "El-Bâis", "Ölüleri kabirlerinden dirilten, peygamberler gönderen.", 573, "Yatarken elini göğsüne koyup okuyanın kalbi nurlanır."),
        AsmaUlHusna(50, "الشَّهِيدُ", "Eş-Şehîd", "Her yerde hazır ve nâzır olan, her şeye şahit olan.", 319, "İtaatsiz evlat veya nefis için okunur."),
        AsmaUlHusna(51, "الْحَقُّ", "El-Hakk", "Varlığı hiç değişmeyen, hak ve hakikatin kaynağı.", 108, "Günde 108 defa okuyan haksızlıklardan kurtulur."),
        AsmaUlHusna(52, "الْوَكِيلُ", "El-Vekîl", "Kendisine tevekkül edenlerin işlerini en iyi gören.", 66, "Zorluk anında okuyan korkularından emin olur."),
        AsmaUlHusna(53, "الْقَوِيُّ", "El-Kaviyy", "Kudreti sonsuz, hiçbir güç O'nu aciz bırakamaz.", 116, "Bedeni ve ruhi zaafiyeti olan kimse şifa bulur."),
        AsmaUlHusna(54, "الْمَتِينُ", "El-Metîn", "Kuvveti sarsılmaz, metanet sahibi.", 500, "Manevi ve ahlaki sağlamlık kazanmak için okunur."),
        AsmaUlHusna(55, "الْوَلِيُّ", "El-Veliyy", "Müminlerin dostu, yardımcısı ve hamisi.", 46, "Günde 46 defa okuyan Allah'ın veli kullarından olur."),
        AsmaUlHusna(56, "الْحَمِيدُ", "El-Hamîd", "Bütün hamd ve övgülere tek layık olan.", 62, "Günde 62 defa okuyanın ahlakı güzelleşir, dili hayır söyler."),
        AsmaUlHusna(57, "الْمُحْصِي", "El-Muhsî", "Her şeyin sayısını ve hesabını tek tek bilen.", 148, "Günde 148 defa okuyanın zekâsı keskinleşir."),
        AsmaUlHusna(58, "الْمُبْدِئُ", "El-Mübdi'", "Varlıkları maddesiz ve modelsiz ilk baştan yaratan.", 56, "Herhangi bir işe başlarken zikredenin işi muvaffak olur."),
        AsmaUlHusna(59, "الْمُعِيدُ", "El-Muîd", "Öldükten sonra yeniden dirilten, eski haline döndüren.", 124, "Kayıp bir şeyi bulmak veya uzaktaki yakınına kavuşmak için okunur."),
        AsmaUlHusna(60, "الْمُحْيِي", "El-Muhyî", "Hayat veren, dirilten ve can bağışlayan.", 68, "Hastalara şifa ve kalplere manevi diriliş için okunur."),
        AsmaUlHusna(61, "الْمُمِيتُ", "El-Mümît", "Eceli geleni öldüren, fâni kılan.", 490, "Kötü alışkanlıkları ve nefsin heveslerini öldürmek için okunur."),
        AsmaUlHusna(62, "الْحَيُّ", "El-Hayy", "Ebedî hayat sahibi, diri ve baki olan.", 18, "Günde 300 defa 'Yâ Hayy Yâ Kayyûm' diyen uzun ve bereketli ömür sürer."),
        AsmaUlHusna(63, "الْقَيُّومُ", "El-Kayyûm", "Her şeyi ayakta tutan, varlığı kendinden olan.", 156, "Günde 156 defa okuyanın işleri intizam bulur."),
        AsmaUlHusna(64, "الْوَاجِدُ", "El-Vâcid", "İstediğini istediği an bulan, hiçbir şeye muhtaç olmayan.", 14, "Lokmayı yerken zikredenin kalbi kuvvetlenir."),
        AsmaUlHusna(65, "الْمَاجِدُ", "El-Mâcid", "Kadri ve şânı yüce, keremi bol olan.", 48, "Günde 48 defa okuyan manevi aydınlığa kavuşur."),
        AsmaUlHusna(66, "الْوَاحِدُ", "El-Vâhid", "Zatında, sıfatlarında ve fiillerinde tek olan.", 19, "Yalnızlık ve korku anında zikredenin kalbi sükun bulur."),
        AsmaUlHusna(67, "الْأَحَدُ", "El-Ahad", "Bölünmesi ve benzeri mümkün olmayan yegâne bir.", 13, "İhlas ve tevhidi pekiştirmek için okunur."),
        AsmaUlHusna(68, "الصَّمَدُ", "Es-Samed", "Herkesin O'na muhtaç olduğu, kendisi hiçbir şeye muhtaç olmayan.", 134, "Seher vaktinde 134 defa okuyan rızık darlığı çekmez."),
        AsmaUlHusna(69, "الْقَادِرُ", "El-Kâdir", "Her şeye gücü yeten, dilediğini var eden.", 305, "Zor işleri başarmak için zikredilir."),
        AsmaUlHusna(70, "الْمُقْتَدِرُ", "El-Muktedir", "Kudret sahipleri üzerinde dilediği gibi tasarruf eden.", 744, "Uyanınca okuyanın günü bereketli geçer."),
        AsmaUlHusna(71, "الْمُقَدِّمُ", "El-Mukaddim", "Dilediğini öne alan, derecesini yükselten.", 184, "Sınavlarda ve önemli işlerde başarı için okunur."),
        AsmaUlHusna(72, "الْمُؤَخِّرُ", "El-Muahhir", "Dilediğini arkaya bırakan, erteleyen.", 847, "Günde 847 defa okuyan takvaya erişir."),
        AsmaUlHusna(73, "الْأَوَّلُ", "El-Evvel", "Varlığının başlangıcı olmayan, ezelî olan.", 37, "Yolculukta ve yeni başlangıçlarda zikredilir."),
        AsmaUlHusna(74, "الْآخِرُ", "El-Âhir", "Varlığının sonu olmayan, ebedî olan.", 801, "Günde 801 defa okuyanın ömrü hayırla biter."),
        AsmaUlHusna(75, "الظَّاهِرُ", "Ez-Zâhir", "Açık olan, varlığı eserleriyle apaçık görünen.", 1106, "İşrak vaktinde okuyanın basireti açılır."),
        AsmaUlHusna(76, "الْبَاطِنُ", "El-Bâtın", "Zâtı akıllardan ve gözlerden gizli olan.", 62, "Günde 62 defa okuyan manevi sırlara vâkıf olur."),
        AsmaUlHusna(77, "الْوَالِي", "El-Vâlî", "Bütün kâinatı idare eden tek yönetici.", 47, "İdarecilerin ve ev reislerinin okuması tavsiye edilir."),
        AsmaUlHusna(78, "الْمُتَعَالِي", "El-Müteâlî", "Noksan sıfatlardan son derece yüce olan.", 541, "Makam ve mevki sahiplerinin sevgisini kazanmak için okunur."),
        AsmaUlHusna(79, "الْبَرُّ", "El-Berr", "Kullarına iyilik ve ihsanı son derece bol olan.", 202, "Günde 202 defa okuyan felç ve hastalıklardan korunur."),
        AsmaUlHusna(80, "التَّوَّابُ", "Et-Tevvâb", "Tövbeleri çokça kabul eden, mağfiret buyuran.", 409, "Duha namazından sonra 409 defa okuyanın tövbesi kabul olur."),
        AsmaUlHusna(81, "الْمُنْتَقِمُ", "El-Müntekım", "Zalimlerden intikam alan, adaletle cezalandıran.", 630, "Haksızlığa uğrayanlar zulümden kurtulmak için okur."),
        AsmaUlHusna(82, "الْعَفُوُّ", "El-Afüvv", "Kullarının günahlarını tamamen silip bağışlayan.", 156, "Günde 156 defa okuyanın rızkı ve affı çoğalır."),
        AsmaUlHusna(83, "الرَّءُوفُ", "Er-Raûf", "Çok merhametli, son derece şefkatli.", 287, "Günde 287 defa okuyan kimse insanlar arasında sevilir."),
        AsmaUlHusna(84, "مَالِكُ الْمُلْكِ", "Mâlikü'l-Mülk", "Mülkün ebedî ve hakiki tek sahibi.", 212, "Günde 212 defa okuyan fakirlikten ve borçtan kurtulur."),
        AsmaUlHusna(85, "ذُو الْجَلَالِ وَالْإِكْرَامِ", "Zü'l-Celâli ve'l-İkrâm", "Hem celal ve büyüklük, hem de ikram ve lütuf sahibi.", 1100, "Duaların kabulü için bu isimle niyaz edilir."),
        AsmaUlHusna(86, "الْمُقْسِطُ", "El-Muksit", "Bütün işlerini denk, adaletli ve dengeli yapan.", 209, "Günde 209 defa okuyanın vesvesesi kesilir."),
        AsmaUlHusna(87, "الْجَامِعُ", "El-Câmi'", "İstediğini istediği zaman bir araya toplayan.", 114, "Kayıp bir şeyi bulmak ve ayrılanların birleşmesi için okunur."),
        AsmaUlHusna(88, "الْغَنِيُّ", "El-Ganiyy", "Sonsuz zenginlik sahibi, hiçbir şeye ihtiyacı olmayan.", 1060, "Günde 1060 defa okuyan büyük zenginlik ve kanaat bulur."),
        AsmaUlHusna(89, "الْمُغْنِي", "El-Mugnî", "Dilediğini zengin kılan, ihtiyaçlarını gideren.", 1100, "Günde 1100 defa çeken kimse darlıktan feraha erer."),
        AsmaUlHusna(90, "الْمَانِعُ", "El-Mâni'", "Kötü şeylere ve zararlara mani olan, engelleyen.", 161, "Kötülüklerden ve kazalardan korunmak için okunur."),
        AsmaUlHusna(91, "الضَّارُّ", "Ed-Dârr", "Zarar veren şeyleri de hikmetiyle yaratan.", 1001, "Düşmanın hilesini boşa çıkarmak için okunur."),
        AsmaUlHusna(92, "النَّافِعُ", "En-Nâfi'", "Fayda veren şeyleri yaratan, menfaat sağlayan.", 201, "Günde 201 defa okuyanın sağlığı ve işleri iyi gider."),
        AsmaUlHusna(93, "النُّورُ", "En-Nûr", "Âlemleri nurlandıran, hidayet veren nurun kaynağı.", 256, "Günde 256 defa okuyanın yüzü ve kalbi nurla parlar."),
        AsmaUlHusna(94, "الْهَادِي", "El-Hâdî", "Doğru yola ulaştıran, hidayet nasip eden.", 20, "Günde 20 defa okuyanın hidayeti ve imanı kuvvetlenir."),
        AsmaUlHusna(95, "الْبَدِيعُ", "El-Bedî'", "Eşsiz, örneksiz ve benzersiz harikalar yaratan.", 86, "İlham, basiret ve sanat kabiliyeti için zikredilir."),
        AsmaUlHusna(96, "الْبَاقِي", "El-Bâkî", "Varlığının sonu olmayan, ebedî baki kalan.", 113, "Günde 113 defa okuyan uzun, bereketli ve huzurlu bir ömür sürer."),
        AsmaUlHusna(97, "الْوَارِثُ", "El-Vâris", "Bütün mülkün gerçek ve son mirasçısı.", 707, "Günde 707 defa okuyanın ömrü ve rızkı bereketlenir."),
        AsmaUlHusna(98, "الرَّشِيدُ", "Er-Reşîd", "Bütün işleri dosdoğru hedefe ulaştıran mürşid.", 514, "Günde 514 defa okuyan hayırlı kararlar alır, pişman olmaz."),
        AsmaUlHusna(99, "الصَّبُورُ", "Es-Sabûr", "Çok sabırlı, cezalandırmada hiç acele etmeyen.", 298, "Güneş doğmadan önce 298 defa okuyan belalardan korunur.")
    )

    // 4. UPCOMING RELIGIOUS DAYS & NIGHTS (GELECEK DİNİ GÜNLER VE KANDİLLER)
    val religiousDays: List<ReligiousDay> = listOf(
        ReligiousDay(
            id = "regaib_2027",
            title = "Regâib Kandili",
            hijriDate = "4 Receb 1448",
            gregorianDate = "14 Ocak 2027",
            daysRemaining = 124,
            significance = "Üç ayların ilk kandili, Allah'ın lütuf ve ihsanının bolca tecelli ettiği mübarek gece.",
            worshipRecommendation = "Gündüzünde oruç tutmak, gecesinde kaza ve nafile namazlar kılmak, bol istiğfar etmek."
        ),
        ReligiousDay(
            id = "mirac_2027",
            title = "Mi'râc Kandili",
            hijriDate = "27 Receb 1448",
            gregorianDate = "4 Şubat 2027",
            daysRemaining = 145,
            significance = "Peygamber Efendimiz'in semaya yükseldiği ve beş vakit namazın mü'minlere hediye edildiği gece.",
            worshipRecommendation = "Namazı huşu ile ikame etmek, Bakara suresinin son iki ayetini (Âmenerrasûlü) tefekkür etmek."
        ),
        ReligiousDay(
            id = "berat_2027",
            title = "Berât Kandili",
            hijriDate = "15 Şaban 1448",
            gregorianDate = "21 Şubat 2027",
            daysRemaining = 162,
            significance = "Günahlardan arınma, af ve berat alma gecesi. Yıllık ilahi takdir ve kader tecellisi vakti.",
            worshipRecommendation = "Geceyi ihya, günahlara samimi tevbe, gündüzünde nafile oruç ve sadaka vermek."
        ),
        ReligiousDay(
            id = "ramadan_start_2027",
            title = "Ramazan-ı Şerif Başlangıcı",
            hijriDate = "1 Ramazan 1448",
            gregorianDate = "9 Mart 2027",
            daysRemaining = 178,
            significance = "On bir ayın sultanı, Kur'an ayı Ramazan'ın ilk oruç günü ve teravih sevinci.",
            worshipRecommendation = "İlk teravih namazı, oruç niyeti, Kur'an mukabelesine başlama, zekat ve infak."
        ),
        ReligiousDay(
            id = "laylat_al_qadr_2027",
            title = "Kadir Gecesi",
            hijriDate = "27 Ramazan 1448",
            gregorianDate = "4 Nisan 2027",
            daysRemaining = 204,
            significance = "Bin aydan daha hayırlı olan, Kur'an-ı Kerim'in indirildiği muazzam rahmet gecesi.",
            worshipRecommendation = "Tevbe-i istiğfar, kaza namazları, Kur'an tilaveti, 'Allahümme inneke afüvvün...' duası."
        ),
        ReligiousDay(
            id = "eid_al_fitr_2027",
            title = "Ramazan Bayramı (1. Gün)",
            hijriDate = "1 Şevval 1448",
            gregorianDate = "9 Nisan 2027",
            daysRemaining = 209,
            significance = "Bir aylık oruç ve arınma ibadetinin ilahi mükâfat ve sevinç günü.",
            worshipRecommendation = "Bayram namazı, sıla-i rahim, sadaka-i fıtır, tekbirler ve küsleri barıştırma."
        ),
        ReligiousDay(
            id = "arafah_2027",
            title = "Kurban Bayramı Arefesi",
            hijriDate = "9 Zilhicce 1448",
            gregorianDate = "15 Haziran 2027",
            daysRemaining = 276,
            significance = "Hacıların Arafat'ta vakfeye durduğu, duaların en çok kabul olduğu af ve mağfiret günü.",
            worshipRecommendation = "Teşrik tekbirlerine başlama, arefe orucu tutmak, ihlas ve tehlil ile bol dua."
        ),
        ReligiousDay(
            id = "eid_al_adha_2027",
            title = "Kurban Bayramı (1. Gün)",
            hijriDate = "10 Zilhicce 1448",
            gregorianDate = "16 Haziran 2027",
            daysRemaining = 277,
            significance = "Hz. İbrahim ve Hz. İsmail'in teslimiyet sembolü, hac farizası ve kurban ibadeti günü.",
            worshipRecommendation = "Teşrik tekbirleri, kurban ibadeti, bayram namazı ve ihtiyaç sahiplerini sevindirme."
        ),
        ReligiousDay(
            id = "hijri_new_year_1449",
            title = "Hicrî Yılbaşı (1449)",
            hijriDate = "1 Muharrem 1449",
            gregorianDate = "6 Temmuz 2027",
            daysRemaining = 297,
            significance = "Peygamber Efendimiz'in Mekke'den Medine'ye kutlu hicretinin başlangıç yılı.",
            worshipRecommendation = "Geçmiş yılın muhasebesi, istiğfar, nafile oruç ve yeni yıl duaları."
        ),
        ReligiousDay(
            id = "ashura_2027",
            title = "Aşure Günü",
            hijriDate = "10 Muharrem 1449",
            gregorianDate = "15 Temmuz 2027",
            daysRemaining = 306,
            significance = "Pek çok peygamberin kurtuluşa erdiği ve Hz. Hüseyin efendimizin şehadet günü.",
            worshipRecommendation = "9-10 veya 10-11 Muharrem günlerinde oruç tutmak, aileye ve fakirlere cömert davranmak."
        ),
        ReligiousDay(
            id = "mevlid_2027",
            title = "Mevlid Kandili",
            hijriDate = "12 Rebiülevvel 1449",
            gregorianDate = "14 Ağustos 2027",
            daysRemaining = 336,
            significance = "Âlemlere rahmet olarak gönderilen Sevgili Peygamberimiz Hz. Muhammed'in (s.a.v.) dünyayı teşrif ettiği kutlu gece.",
            worshipRecommendation = "Bolca salavat-ı şerife getirmek, siyer okumak, Kur'an tilaveti ve nafile namaz."
        )
    )

    // 5. NEARBY MOSQUES (YAKIN CAMİLER)
    val nearbyMosques: List<NearbyMosque> = listOf(
        NearbyMosque(
            id = "m1",
            name = "Sultanahmet Camii (Mavi Cami)",
            city = "İstanbul",
            distanceMeters = 350,
            address = "Sultan Ahmet, Atmeydanı Cd. No:7, 34122 Fatih/İstanbul",
            latitude = 41.0054,
            longitude = 28.9768,
            features = listOf("Tarihi Eser", "Geniş Avlu", "Abdest Alanı", "Kadınlar Bölümü", "Tekerlekli Sandalye"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m2",
            name = "Ayasofya-i Kebîr Câmi-i Şerîfi",
            city = "İstanbul",
            distanceMeters = 620,
            address = "Sultan Ahmet, Ayasofya Meydanı No:1, 34122 Fatih/İstanbul",
            latitude = 41.0086,
            longitude = 28.9802,
            features = listOf("Fethin Sembolü", "Tarihi Eser", "Klimalı", "Kadınlar Bölümü"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m3",
            name = "Süleymaniye Camii",
            city = "İstanbul",
            distanceMeters = 1450,
            address = "Süleymaniye Mah, Prof. Sıddık Sami Onar Cd. No:1, Fatih/İstanbul",
            latitude = 41.0161,
            longitude = 28.9640,
            features = listOf("Mimar Sinan Şaheseri", "Boğaz Manzarası", "Geniş Otopark", "Külliye"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m4",
            name = "Eyüp Sultan Camii",
            city = "İstanbul",
            distanceMeters = 3200,
            address = "Merkez, Cami Kebir Sk. No:1, 34050 Eyüpsultan/İstanbul",
            latitude = 41.0480,
            longitude = 28.9340,
            features = listOf("Sahabe Türbesi", "Sabah Namazı Meclisi", "Çocuk Parkı", "Aşevi"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m5",
            name = "Çamlıca Camii",
            city = "İstanbul",
            distanceMeters = 6800,
            address = "Ferah Mah, Ferah Yolu Sk. No:87, 34692 Üsküdar/İstanbul",
            latitude = 41.0342,
            longitude = 29.0712,
            features = listOf("Modern Külliye", "Geniş Otopark", "İslam Medeniyetleri Müzesi", "Kütüphane"),
            isHistorical = false
        ),
        NearbyMosque(
            id = "m6",
            name = "Kocatepe Camii",
            city = "Ankara",
            distanceMeters = 800,
            address = "Kültür, Dr. Mediha Eldem Sk. No:67, 06420 Çankaya/Ankara",
            latitude = 39.9167,
            longitude = 32.8611,
            features = listOf("Başkentin Sembolü", "Kapalı Otopark", "Geniş Cemaat Kapasitesi", "Kadınlar Mahfili"),
            isHistorical = false
        ),
        NearbyMosque(
            id = "m7",
            name = "Hacı Bayram-ı Velî Camii",
            city = "Ankara",
            distanceMeters = 1800,
            address = "Hacı Bayram, Sarıbağ Sk. No:13, 06050 Altındağ/Ankara",
            latitude = 39.9444,
            longitude = 32.8583,
            features = listOf("Manevi Makam", "Tarihi Türbe", "Augustus Tapınağı Bitişiği", "Meydan"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m8",
            name = "Bursa Ulu Camii",
            city = "Bursa",
            distanceMeters = 400,
            address = "Nalbantoğlu, Atatürk Cd., 16010 Osmangazi/Bursa",
            latitude = 40.1833,
            longitude = 29.0622,
            features = listOf("20 Kubbeli", "İç Şadırvan", "Hüsn-i Hat Galerisi", "Tarihi Kâbe Örtüsü"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m9",
            name = "Mevlânâ Dergâhı ve Selimiye Camii",
            city = "Konya",
            distanceMeters = 550,
            address = "Aziziye, Mevlana Cd. No:1, 42030 Karatay/Konya",
            latitude = 37.8711,
            longitude = 32.5050,
            features = listOf("Mevlevi Âsitânesi", "Tarihi Cami", "Meydan ve Gül Bahçesi"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m10",
            name = "Edirne Selimiye Camii",
            city = "Edirne",
            distanceMeters = 300,
            address = "Meydan, Mimar Sinan Cd., 22020 Edirne Merkez/Edirne",
            latitude = 41.6781,
            longitude = 26.5594,
            features = listOf("UNESCO Dünya Mirası", "Mimar Sinan Ustalık Eseri", "Tarihi Külliye"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m11",
            name = "İzmir Hisar Camii",
            city = "İzmir",
            distanceMeters = 450,
            address = "Konak, 904. Sk. No:52, 35250 Konak/İzmir",
            latitude = 38.4192,
            longitude = 27.1333,
            features = listOf("Tarihi Kemeraltı", "Geniş Şadırvan", "Kadınlar Mahfili"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m12",
            name = "Diyarbakır Ulu Camii",
            city = "Diyarbakır",
            distanceMeters = 380,
            address = "Cami Kebir, Pirinçler Sk. 10 A, 21300 Sur/Diyarbakır",
            latitude = 37.9136,
            longitude = 40.2372,
            features = listOf("İslam'ın 5. Harem-i Şerifi", "Güneş Saati", "Tarihi Avlu"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m13",
            name = "Şanlıurfa Dergâh (Balıklıgöl) Camii",
            city = "Şanlıurfa",
            distanceMeters = 250,
            address = "Göl Mah, Balıklıgöl Cd., 63200 Eyyübiye/Şanlıurfa",
            latitude = 37.1472,
            longitude = 38.7844,
            features = listOf("Hz. İbrahim Makamı", "Balıklıgöl Külliyesi", "Manevi Ziyaretgâh"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m14",
            name = "Trabzon Gülbahar Hatun Camii",
            city = "Trabzon",
            distanceMeters = 500,
            address = "Gülbaharhatun, İnönü Cd. No:38, 61040 Ortahisar/Trabzon",
            latitude = 41.0028,
            longitude = 39.7139,
            features = listOf("Yavuz Sultan Selim Hatırası", "Osmanlı Mimarisi", "Tarihi Türbe"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m15",
            name = "Antalya Yivli Minare Camii",
            city = "Antalya",
            distanceMeters = 320,
            address = "Selçuk Mah, Selçuk Sk., 07040 Muratpaşa/Antalya",
            latitude = 36.8867,
            longitude = 30.7056,
            features = listOf("Kaleiçi Girişi", "Selçuklu Mirası", "Tarihi Medrese"),
            isHistorical = true
        ),
        NearbyMosque(
            id = "m16",
            name = "Adana Sabancı Merkez Camii",
            city = "Adana",
            distanceMeters = 600,
            address = "Reşatbey Mah, Seyhan Nehri Kıyısı, 01120 Seyhan/Adana",
            latitude = 36.9917,
            longitude = 35.3333,
            features = listOf("Ortadoğu'nun En Büyük Camilerinden", "6 Minareli", "Seyhan Manzarası"),
            isHistorical = false
        )
    )

    // 6. 30 JUZ HATIM TRACKER INITIAL DATA
    fun getInitialHatimJuzs(): List<HatimJuz> {
        return listOf(
            HatimJuz(1, "Fâtiha (1)", "Bakara (141)", 1, 21),
            HatimJuz(2, "Bakara (142)", "Bakara (252)", 22, 41),
            HatimJuz(3, "Bakara (253)", "Âl-i İmrân (92)", 42, 61),
            HatimJuz(4, "Âl-i İmrân (93)", "Nisâ (23)", 62, 81),
            HatimJuz(5, "Nisâ (24)", "Nisâ (147)", 82, 101),
            HatimJuz(6, "Nisâ (148)", "Mâide (81)", 102, 121),
            HatimJuz(7, "Mâide (82)", "En'âm (110)", 122, 141),
            HatimJuz(8, "En'âm (111)", "A'râf (87)", 142, 161),
            HatimJuz(9, "A'râf (88)", "Enfâl (40)", 162, 181),
            HatimJuz(10, "Enfâl (41)", "Tevbe (92)", 182, 201),
            HatimJuz(11, "Tevbe (93)", "Hûd (5)", 202, 221),
            HatimJuz(12, "Hûd (6)", "Yûsuf (52)", 222, 241),
            HatimJuz(13, "Yûsuf (53)", "İbrâhîm (52)", 242, 261),
            HatimJuz(14, "Hicr (1)", "Nahl (128)", 262, 281),
            HatimJuz(15, "İsrâ (1)", "Kehf (74)", 282, 301),
            HatimJuz(16, "Kehf (75)", "Tâhâ (135)", 302, 321),
            HatimJuz(17, "Enbiyâ (1)", "Hac (78)", 322, 341),
            HatimJuz(18, "Mü'minûn (1)", "Furkân (20)", 342, 361),
            HatimJuz(19, "Furkân (21)", "Neml (55)", 362, 381),
            HatimJuz(20, "Neml (56)", "Ankebût (45)", 382, 401),
            HatimJuz(21, "Ankebût (46)", "Ahzâb (30)", 402, 421),
            HatimJuz(22, "Ahzâb (31)", "Yâsîn (27)", 422, 441),
            HatimJuz(23, "Yâsîn (28)", "Zümer (31)", 442, 461),
            HatimJuz(24, "Zümer (32)", "Fussilet (46)", 462, 481),
            HatimJuz(25, "Fussilet (47)", "Câsiye (37)", 482, 501),
            HatimJuz(26, "Ahkâf (1)", "Zâriyât (30)", 502, 521),
            HatimJuz(27, "Zâriyât (31)", "Hadîd (29)", 522, 541),
            HatimJuz(28, "Mücâdele (1)", "Tahrîm (12)", 542, 561),
            HatimJuz(29, "Mülk (1)", "Mürselât (50)", 562, 581),
            HatimJuz(30, "Nebe (1)", "Nâs (6)", 582, 604)
        )
    }

    // 7. CURATED ISLAMIC VIDEOS
    val islamicVideos: List<IslamicVideo> = listOf(
        IslamicVideo(
            id = "v1",
            title = "Huşû ile Namaz Nasıl Kılınır? Kalbi Namaza Bağlamak",
            speaker = "Diyanet İlim Meclisi",
            duration = "18:42",
            category = "Vaaz & Sohbet",
            thumbnailUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=namazda+husu",
            description = "Namazda zihni meşgul eden düşüncelerden sıyrılıp Allah'ın huzurunda derin huşuya ermenin manevi ve fıkhi yolları."
        ),
        IslamicVideo(
            id = "v2",
            title = "Yâsîn-i Şerîf - Muhteşem Kuran Tilaveti ve Türkçe Meali",
            speaker = "Şeyh Mishary Rashid Al-Afasy",
            duration = "14:15",
            category = "Kuran Tilaveti",
            thumbnailUrl = "https://images.unsplash.com/photo-1585036156171-384164a8c675?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=yasin+suresi+mishary",
            description = "Kur'an-ı Kerim'in kalbi olan Yâsîn Suresi'nin berrak tilaveti ve âyet âyet tefekkür meali."
        ),
        IslamicVideo(
            id = "v3",
            title = "Asr Suresi Tefsiri: Kurtuluşun Dört Altın Şartı",
            speaker = "Prof. Dr. Tefsir Heyeti",
            duration = "22:10",
            category = "Tefsir Dersi",
            thumbnailUrl = "https://images.unsplash.com/photo-1590076215667-875d4ef2d7ee?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=asr+suresi+tefsiri",
            description = "İmam Şafiî'nin 'Sadece bu sure inseydi insanlara yeterdi' dediği Asr Suresi'nin derin tahlili."
        ),
        IslamicVideo(
            id = "v4",
            title = "Peygamber Efendimiz'in (s.a.v.) Bir Günü Nasıl Geçerdi?",
            speaker = "Siyer Araştırmaları Vakfı",
            duration = "26:30",
            category = "Siyer-i Nebî",
            thumbnailUrl = "https://images.unsplash.com/photo-1564769625905-50e93615e769?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=peygamberimizin+bir+gunu",
            description = "Fahr-i Kâinat Efendimiz'in seher vaktinden yatsı sonrasına kadar tebessüm, ibadet, aile ve ashapla geçen örnek hayatı."
        ),
        IslamicVideo(
            id = "v5",
            title = "Ramazan'a Manevi Hazırlık ve İbadet Rehberi",
            speaker = "İslam İlimleri Enstitüsü",
            duration = "19:05",
            category = "Ramazan Özel",
            thumbnailUrl = "https://images.unsplash.com/photo-1519817650390-64a93db51149?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=ramazana+hazirlik",
            description = "Orucu sadece mideye değil, göze, kulağa ve kalbe tutturmanın incelikleri."
        ),
        IslamicVideo(
            id = "v6",
            title = "Rahmân Sûresi Tilâveti - Kalplere Şifa ve Huzur",
            speaker = "Dr. Fatih Çollak",
            duration = "16:20",
            category = "Kuran Tilaveti",
            thumbnailUrl = "https://images.unsplash.com/photo-1609599006353-e629aaabfeae?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=rahman+suresi+fatih+collak",
            description = "Kur'an'ın gelini olarak vasıflandırılan Rahmân Suresi'nin tecvid ve kıraat kaidelerine uygun enfes tilaveti."
        ),
        IslamicVideo(
            id = "v7",
            title = "Mülk (Tebâreke) Sûresi ve Kabir Emniyeti",
            speaker = "İshak Danış",
            duration = "11:45",
            category = "Kuran Tilaveti",
            thumbnailUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=mulk+suresi+ishak+danis",
            description = "Her gece yatmadan önce okunması tavsiye edilen Mülk Suresi'nin manevi kalkanı ve faziletleri."
        ),
        IslamicVideo(
            id = "v8",
            title = "Tövbe ve İstiğfarın Esrarı: Kalbin Pasını Silmek",
            speaker = "Diyanet Radyo Sohbetleri",
            duration = "24:12",
            category = "Vaaz & Sohbet",
            thumbnailUrl = "https://images.unsplash.com/photo-1519817650390-64a93db51149?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=tovbe+ve+istigfar+sohbet",
            description = "Seyyidü'l-İstiğfar duası, nasuh tövbenin rükünleri ve kulun Rabbi ile tertemiz bir sayfa açması."
        ),
        IslamicVideo(
            id = "v9",
            title = "Fâtiha Sûresi Tefsiri: İbadet ve Kulluğun Özeti",
            speaker = "İlahiyat İlim Heyeti",
            duration = "31:50",
            category = "Tefsir Dersi",
            thumbnailUrl = "https://images.unsplash.com/photo-1564769625905-50e93615e769?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=fatiha+suresi+tefsiri",
            description = "Günde 40 rekat namazda okuduğumuz Ümmü'l-Kitab Fâtiha-i Şerife'nin derin anlam katmanları."
        ),
        IslamicVideo(
            id = "v10",
            title = "Hz. Ali (k.v.) ve Adaletin Timsali Hayatı",
            speaker = "Tarih & Siyer Meclisi",
            duration = "28:15",
            category = "Siyer-i Nebî",
            thumbnailUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=hz+ali+hayati+siyer",
            description = "İlmin kapısı, Haydar-ı Kerrar Hz. Ali'nin cesareti, hikmet dolu sözleri ve adaleti."
        ),
        IslamicVideo(
            id = "v11",
            title = "Abdest ve Gusül Rehberi: En Sık Sorulan Sorular",
            speaker = "Diyanet Fetva Kurulu",
            duration = "15:30",
            category = "Vaaz & Sohbet",
            thumbnailUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=abdest+nasil+alinir+diyanet",
            description = "Abdestin farzları, sünnetleri, şüpheler ve modern hayatta abdesti bozan/bozmayan haller."
        ),
        IslamicVideo(
            id = "v12",
            title = "Kadir Gecesi Özel Duası ve İbadet Programı",
            speaker = "Kandil Özel Yayını",
            duration = "20:40",
            category = "Ramazan Özel",
            thumbnailUrl = "https://images.unsplash.com/photo-1519817650390-64a93db51149?w=600",
            videoUrl = "https://www.youtube.com/results?search_query=kadir+gecesi+duasi",
            description = "Kadir gecesinde okunacak dualar, kılınacak tesbih namazı ve geceyi bereketlendirme rehberi."
        )
    )

    // 8. HIGH QUALITY ISLAMIC WALLPAPERS (DUVAR KAĞITLARI - 20+ YÜKSEK ÇÖZÜNÜRLÜKLÜ)
    val islamicWallpapers: List<IslamicWallpaper> = listOf(
        IslamicWallpaper(
            id = "w1",
            title = "Kâbe-i Muazzama ve Mescid-i Haram",
            category = "Kâbe & Mekke",
            imageUrl = "https://images.unsplash.com/photo-1564769625905-50e93615e769?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFD4AF37
        ),
        IslamicWallpaper(
            id = "w2",
            title = "Mescid-i Nebevî Yeşil Kubbe (Ravza)",
            category = "Medine-i Münevvere",
            imageUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF10B981
        ),
        IslamicWallpaper(
            id = "w3",
            title = "Sultanahmet Camii Gece Işıkları",
            category = "Camiler & Kubbeler",
            imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF38BDF8
        ),
        IslamicWallpaper(
            id = "w4",
            title = "Altın Varaklı Hüsn-i Hat Sanatı",
            category = "Hat Sanatı",
            imageUrl = "https://images.unsplash.com/photo-1585036156171-384164a8c675?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFF59E0B
        ),
        IslamicWallpaper(
            id = "w5",
            title = "Mescid-i Aksâ Kubbetü's-Sahra",
            category = "Kudüs & Miras",
            imageUrl = "https://images.unsplash.com/photo-1578895101408-1a36b834405b?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFEAB308
        ),
        IslamicWallpaper(
            id = "w6",
            title = "Seher Vakti Minareler ve Hilal",
            category = "Gece & Maneviyat",
            imageUrl = "https://images.unsplash.com/photo-1519817650390-64a93db51149?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF818CF8
        ),
        IslamicWallpaper(
            id = "w7",
            title = "Ayasofya-i Kebîr Câmi-i Şerîfi",
            category = "Camiler & Kubbeler",
            imageUrl = "https://images.unsplash.com/photo-1527838832700-5059252407fa?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFF59E0B
        ),
        IslamicWallpaper(
            id = "w8",
            title = "Kâbe Örtüsü (Kiswa) Altın Nakışları",
            category = "Kâbe & Mekke",
            imageUrl = "https://images.unsplash.com/photo-1565552645632-d725f8bfc19a?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFD4AF37
        ),
        IslamicWallpaper(
            id = "w9",
            title = "Medine Şemsiyeleri ve Gökyüzü",
            category = "Medine-i Münevvere",
            imageUrl = "https://images.unsplash.com/photo-1590076215667-875d4ef2d7ee?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF10B981
        ),
        IslamicWallpaper(
            id = "w10",
            title = "Süleymaniye Camii ve Haliç Silueti",
            category = "Camiler & Kubbeler",
            imageUrl = "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF06B6D4
        ),
        IslamicWallpaper(
            id = "w11",
            title = "Kur'an-ı Kerim ve Rahle Huzuru",
            category = "Hat Sanatı",
            imageUrl = "https://images.unsplash.com/photo-1609599006353-e629aaabfeae?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFFBBF24
        ),
        IslamicWallpaper(
            id = "w12",
            title = "Geleneksel Ramazan Feneri (Fanoos)",
            category = "Gece & Maneviyat",
            imageUrl = "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFF97316
        ),
        IslamicWallpaper(
            id = "w13",
            title = "Çamlıca Camii ve İstanbul Manzarası",
            category = "Camiler & Kubbeler",
            imageUrl = "https://images.unsplash.com/photo-1570784332176-fdd73da66f03?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF38BDF8
        ),
        IslamicWallpaper(
            id = "w14",
            title = "Gök Kubbe ve İslâmî Geometrik Desenler",
            category = "Hat Sanatı",
            imageUrl = "https://images.unsplash.com/photo-1548625361-195fe578cb66?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF6366F1
        ),
        IslamicWallpaper(
            id = "w15",
            title = "Tavaf Eden Müslümanlar ve Nur Halesi",
            category = "Kâbe & Mekke",
            imageUrl = "https://images.unsplash.com/photo-1597935258735-e254c1839512?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFFDE047
        ),
        IslamicWallpaper(
            id = "w16",
            title = "Kudüs Kadim Sokakları ve Taş Kemerler",
            category = "Kudüs & Miras",
            imageUrl = "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFEAB308
        ),
        IslamicWallpaper(
            id = "w17",
            title = "Zeytin Ağacı ve Kudüs Ufku",
            category = "Kudüs & Miras",
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF84CC16
        ),
        IslamicWallpaper(
            id = "w18",
            title = "İnci Tesbih ve Ahşap Rahle",
            category = "Gece & Maneviyat",
            imageUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFA855F7
        ),
        IslamicWallpaper(
            id = "w19",
            title = "Ortaköy Camii ve Boğaziçi Köprüsü",
            category = "Camiler & Kubbeler",
            imageUrl = "https://images.unsplash.com/photo-1541432901042-2d8bd64b4a9b?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFEC4899
        ),
        IslamicWallpaper(
            id = "w20",
            title = "Gül-i Muhammedî ve Manevi Bahar",
            category = "Gece & Maneviyat",
            imageUrl = "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFF43F5E
        ),
        IslamicWallpaper(
            id = "w21",
            title = "Mekke-i Mükerreme Gece Manzarası & Saat Kulesi",
            category = "Kâbe & Mekke",
            imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFD4AF37
        ),
        IslamicWallpaper(
            id = "w22",
            title = "Kâbe-i Muazzama Altın Oluk (Mîzâb-ı Rahmet)",
            category = "Kâbe & Mekke",
            imageUrl = "https://images.unsplash.com/photo-1564769625905-50e93615e769?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFEAB308
        ),
        IslamicWallpaper(
            id = "w23",
            title = "Medine-i Münevvere Ravza-i Mutahhara Çiçekleri",
            category = "Medine-i Münevvere",
            imageUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF059669
        ),
        IslamicWallpaper(
            id = "w24",
            title = "Mescid-i Nebevi Mermer Avlusu ve Gölgelikler",
            category = "Medine-i Münevvere",
            imageUrl = "https://images.unsplash.com/photo-1590076215667-875d4ef2d7ee?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF10B981
        ),
        IslamicWallpaper(
            id = "w25",
            title = "Bursa Ulu Camii Şadırvanı ve Hat Levhaları",
            category = "Camiler & Kubbeler",
            imageUrl = "https://images.unsplash.com/photo-1527838832700-5059252407fa?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF0284C7
        ),
        IslamicWallpaper(
            id = "w26",
            title = "Edirne Selimiye Camii Mimar Sinan Şaheseri",
            category = "Camiler & Kubbeler",
            imageUrl = "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFD97706
        ),
        IslamicWallpaper(
            id = "w27",
            title = "Kelime-i Tevhid Lâ İlâhe İllallâh Celî Sülüs",
            category = "Hat Sanatı",
            imageUrl = "https://images.unsplash.com/photo-1585036156171-384164a8c675?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFF59E0B
        ),
        IslamicWallpaper(
            id = "w28",
            title = "Besmele-i Şerîf Altın Tezhip Sanatı",
            category = "Hat Sanatı",
            imageUrl = "https://images.unsplash.com/photo-1548625361-195fe578cb66?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFCA8A04
        ),
        IslamicWallpaper(
            id = "w29",
            title = "Kudüs-ü Şerif Mescid-i Aksâ Kıble Mescidi",
            category = "Kudüs & Miras",
            imageUrl = "https://images.unsplash.com/photo-1578895101408-1a36b834405b?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF65A30D
        ),
        IslamicWallpaper(
            id = "w30",
            title = "Kubbetü's-Sahra Çinileri ve Selçuklu Yıldızı",
            category = "Kudüs & Miras",
            imageUrl = "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF0284C7
        ),
        IslamicWallpaper(
            id = "w31",
            title = "Kadir Gecesi Nur Dağı ve Hira Mağarası",
            category = "Gece & Maneviyat",
            imageUrl = "https://images.unsplash.com/photo-1519817650390-64a93db51149?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF8B5CF6
        ),
        IslamicWallpaper(
            id = "w32",
            title = "Seher Vakti Teheccüd ve Dua Elleri",
            category = "Gece & Maneviyat",
            imageUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFF6366F1
        ),
        IslamicWallpaper(
            id = "w33",
            title = "Mescid-i Haram Hacerü'l-Esved Köşesi",
            category = "Kâbe & Mekke",
            imageUrl = "https://images.unsplash.com/photo-1565552645632-d725f8bfc19a?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFD4AF37
        ),
        IslamicWallpaper(
            id = "w34",
            title = "Uhud Dağı ve Şüheda Tepesi Ufku",
            category = "Medine-i Münevvere",
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFB45309
        ),
        IslamicWallpaper(
            id = "w35",
            title = "Hilye-i Şerîf Peygamber Efendimiz'in (s.a.v) Vasfı",
            category = "Hat Sanatı",
            imageUrl = "https://images.unsplash.com/photo-1609599006353-e629aaabfeae?w=1080&auto=format&fit=crop",
            accentColorHex = 0xFFF59E0B
        )
    )
}
