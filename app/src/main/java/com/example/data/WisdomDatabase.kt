package com.example.data

import com.example.model.DailyWisdom
import com.example.model.DhikrPreset

object WisdomDatabase {
    val dhikrPresets: List<DhikrPreset> = listOf(
        DhikrPreset(
            id = "subhanallah",
            title = "Sübhânallâh",
            arabic = "سُبْحَانَ اللَّهِ",
            meaning = "Allah her türlü eksiklikten ve noksanlıktan münezzehtir.",
            target = 33,
            virtues = "Günde 100 defa söyleyenin günahları deniz köpüğü kadar da olsa bağışlanır."
        ),
        DhikrPreset(
            id = "elhamdulillah",
            title = "Elhamdülillâh",
            arabic = "الْحَمْدُ لِلَّهِ",
            meaning = "Hamd ve övgülerin tamamı yalnızca Allah'a aittir.",
            target = 33,
            virtues = "Mizânı dolduran en faziletli şükür zikridir."
        ),
        DhikrPreset(
            id = "allahuekber",
            title = "Allâhuekber",
            arabic = "اللَّهُ أَكْبَرُ",
            meaning = "Allah her şeyden en büyüktür, en yücedir.",
            target = 33,
            virtues = "Gökler ve yer arasını dolduracak kadar büyük ecri vardır."
        ),
        DhikrPreset(
            id = "lailaheillallah",
            title = "Lâ ilâhe illallâh",
            arabic = "لَا إِلٰهَ إِلَّا اللَّهُ",
            meaning = "Allah'tan başka hiçbir ilah yoktur.",
            target = 100,
            virtues = "Zikirlerin en faziletlisi Kelime-i Tevhid'dir."
        ),
        DhikrPreset(
            id = "astaghfirullah",
            title = "Estağfirullâh el-Azîm",
            arabic = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ",
            meaning = "Şanı yüce olan Allah'tan bağışlanma ve af dilerim.",
            target = 100,
            virtues = "İstiğfara devam edene Allah her darlıktan bir çıkış yolu ihsan eder."
        ),
        DhikrPreset(
            id = "salavat",
            title = "Salavât-ı Şerîfe",
            arabic = "اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ",
            meaning = "Allah'ım! Efendimiz Hz. Muhammed'e ve âline salât ve selâm eyle.",
            target = 100,
            virtues = "Bana bir salavat getirene Allah on rahmet eder, on günahını siler."
        ),
        DhikrPreset(
            id = "havle",
            title = "Lâ havle ve lâ kuvvete illâ billâh",
            arabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            meaning = "Güç ve kuvvet ancak şanı yüce olan Allah'a aittir.",
            target = 33,
            virtues = "Cennet hazinelerinden bir hazinedir, 99 derde devadır."
        ),
        DhikrPreset(
            id = "hasbunallah",
            title = "Hasbünallâhu ve ni'me'l-vekîl",
            arabic = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
            meaning = "Allah bize yeter, O ne güzel vekildir.",
            target = 100,
            virtues = "Her türlü korku, gam, keder ve imtihanda en büyük sığınaktır."
        ),
        DhikrPreset(
            id = "subhanallahi_vebihamdihi",
            title = "Sübhânallâhi ve bi-hamdihî",
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            meaning = "Allah'ı hamdiyle tesbih ederim, noksan sıfatlardan tenzih ederim.",
            target = 100,
            virtues = "Dilde hafif, mizanda pek ağır ve Rahman'a pek sevimli iki kelimedir."
        ),
        DhikrPreset(
            id = "yunus_dua",
            title = "Hz. Yûnus (a.s) Zikri",
            arabic = "لَا إِلٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            meaning = "Senden başka ilah yoktur. Seni tenzih ederim. Şüphesiz ben zalimlerden oldum.",
            target = 40,
            virtues = "Keder ve darlık anında bu dua ile yapılan niyazlar geri çevrilmez."
        ),
        DhikrPreset(
            id = "ya_hayyu_ya_qayyum",
            title = "Yâ Hayyü Yâ Kayyûm",
            arabic = "يَا حَيُّ يَا قَيُّومُ",
            meaning = "Ey daima diri olan ve her şeyi ayakta tutan Yüce Allah!",
            target = 100,
            virtues = "İsm-i Âzam'ı içeren mübarek zikirlerdendir, gönüllere ferahlık verir."
        ),
        DhikrPreset(
            id = "ya_fettah",
            title = "Yâ Fettâh",
            arabic = "يَا فَتَّاحُ",
            meaning = "Ey bütün kapıları ve hayır yollarını açan!",
            target = 100,
            virtues = "Maddi ve manevi kilitli kapıların açılması için çekilir."
        ),
        DhikrPreset(
            id = "ya_shafi",
            title = "Yâ Şâfî",
            arabic = "يَا شَافِي",
            meaning = "Ey maddi ve manevi hastalıklara şifa veren!",
            target = 100,
            virtues = "Hastalık ve sıkıntı anlarında şifa niyetiyle çekilir."
        )
    )

    // 99 Esmâü'l-Hüsnâ Zikir Listesi (Ebced Değerleri ve Faziletleri İle)
    val asmaUlHusnaPresets: List<DhikrPreset> by lazy {
        IslamicDatabase.asmaUlHusna.map { asma ->
            DhikrPreset(
                id = "asma_${asma.number}",
                title = "Yâ ${asma.transliteration}",
                arabic = asma.arabic,
                meaning = asma.turkishMeaning,
                target = asma.ebcedValue,
                virtues = asma.virtue
            )
        }
    }

    // Hem Klasik Tesbihatları Hem de 99 Esmâü'l-Hüsnâ'yı İçeren Tam Liste
    val allDhikrPresets: List<DhikrPreset> by lazy {
        dhikrPresets + asmaUlHusnaPresets
    }

    val dailyWisdomList: List<DailyWisdom> = listOf(
        DailyWisdom(
            ayahArabic = "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَوْقُوتًا",
            ayahTurkish = "Şüphesiz namaz, mü'minler üzerine vakitleri belirlenmiş bir farzdır.",
            ayahSurah = "Nisâ Sûresi, 103. Âyet",
            hadithTurkish = "Kıyamet gününde kulun ilk hesaba çekileceği amel, namazıdır. Eğer o düzgün olursa, kurtulmuş ve kazanmıştır.",
            hadithSource = "Tirmizî, Salât, 188",
            duaArabic = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ",
            duaTurkish = "Ey bu tam davetin ve kılınacak namazın Rabbi olan Allah'ım! Muhammed'e vesileyi ve fazileti ihsan eyle."
        ),
        DailyWisdom(
            ayahArabic = "وَأَقِمِ الصَّلَاةَ لِذِكْرِي",
            ayahTurkish = "Beni anmak ve hatırlamak için namazı dosdoğru kıl.",
            ayahSurah = "Tâhâ Sûresi, 14. Âyet",
            hadithTurkish = "İki serinlik namazını (sabah ve ikindiyi) kılan kimse cennete girer.",
            hadithSource = "Buhârî, Mevâkît, 26",
            duaArabic = "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِن ذُرِّيَّتِي ۚ رَبَّنَا وَتَقَبَّلْ دُعَاءِ",
            duaTurkish = "Rabbim! Beni ve neslimi namazı dosdoğru kılanlardan eyle. Rabbimiz, dualarımızı kabul buyur."
        ),
        DailyWisdom(
            ayahArabic = "حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ وَقُومُوا لِلَّهِ قَانِتِينَ",
            ayahTurkish = "Namazlara ve orta namaza devam edin; gönülden boyun eğerek Allah'ın huzurunda durun.",
            ayahSurah = "Bakara Sûresi, 238. Âyet",
            hadithTurkish = "Namaz dinin direğidir. Onu ikame eden dinini ikame etmiş, terk eden ise dinini yıkmış olur.",
            hadithSource = "Beyhakî, Şu'abü'l-Îmân",
            duaArabic = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
            duaTurkish = "Ey kalpleri evirip çeviren Allah'ım! Kalbimi dinin ve rızan üzere sabit kıl."
        ),
        DailyWisdom(
            ayahArabic = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            ayahTurkish = "Öyleyse siz beni anın ki, ben de sizi anayım. Bana şükredin ve nankörlük etmeyin.",
            ayahSurah = "Bakara Sûresi, 152. Âyet",
            hadithTurkish = "Rabbini zikreden kimse ile zikretmeyen kimsenin misali, diri ile ölü gibidir.",
            hadithSource = "Buhârî, Deavât, 66",
            duaArabic = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
            duaTurkish = "Allah'ım! Seni zikretmek, sana şükretmek ve sana güzelce ibadet etmek hususunda bana yardım eyle."
        ),
        DailyWisdom(
            ayahArabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            ayahTurkish = "Bilesiniz ki, kalpler ancak Allah'ı anmakla huzur ve sükûnet bulur.",
            ayahSurah = "Ra'd Sûresi, 28. Âyet",
            hadithTurkish = "Kim sabah ve akşam yüz defa 'Sübhanallahi ve bihamdihi' derse, günahları deniz köpüğü kadar da olsa bağışlanır.",
            hadithSource = "Müslim, Zikir, 28",
            duaArabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ",
            duaTurkish = "Allah'ım! Senden dünyada da ahirette de af ve afiyet dilerim."
        ),
        DailyWisdom(
            ayahArabic = "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ وَإِنَّهَا لَكَبِيرَةٌ إِلَّا عَلَى الْخَاشِعِينَ",
            ayahTurkish = "Sabır ve namazla Allah'tan yardım dileyin. Şüphesiz o, huşû duyanlardan başkasına pek ağır gelir.",
            ayahSurah = "Bakara Sûresi, 45. Âyet",
            hadithTurkish = "Kulun Rabbine en yakın olduğu an, secde ânıdır. Öyleyse secdede duayı çok yapınız.",
            hadithSource = "Müslim, Salât, 215",
            duaArabic = "سُبْحَانَ رَبِّيَ الْأَعْلَى وَبِحَمْدِهِ",
            duaTurkish = "En Yüce olan Rabbimi noksan sıfatlardan tenzih ederim ve O'na hamd ederim."
        ),
        DailyWisdom(
            ayahArabic = "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            ayahTurkish = "Elbette her zorlukla beraber bir kolaylık vardır.",
            ayahSurah = "İnşirâh Sûresi, 6. Âyet",
            hadithTurkish = "Mü'minin durumu ne hoştur! Her hâli kendisi için bir hayırdır. Başına bir sevinç gelirse şükreder kazanır, bir darlık gelirse sabreder kazanır.",
            hadithSource = "Müslim, Zühd, 64",
            duaArabic = "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
            duaTurkish = "Rabbim! Gönlüme ferahlık ver ve işimi bana kolaylaştır."
        ),
        DailyWisdom(
            ayahArabic = "وَقُلْ رَبِّ زِدْنِي عِلْمًا",
            ayahTurkish = "De ki: Rabbim, benim ilmimi ve anlayışımı artır!",
            ayahSurah = "Tâhâ Sûresi, 114. Âyet",
            hadithTurkish = "Kim ilim tahsil etmek için bir yola girerse, Allah ona cennete giden yolu kolaylaştırır.",
            hadithSource = "Müslim, Zikir, 38",
            duaArabic = "اللَّهُمَّ انْفَعْنِي بِمَا عَلَّمْتَنِي وَعَلِّمْنِي مَا يَنْفَعُنِي",
            duaTurkish = "Allah'ım! Bana öğrettiğin ilim ile beni faydalandır, bana fayda verecek ilmi öğret."
        ),
        DailyWisdom(
            ayahArabic = "وَتَوَكَّلْ عَلَى الْحَيِّ الَّذِي لَا يَمُوتُ",
            ayahTurkish = "Ölmeyen ve daima diri olan Allah'a tevekkül et.",
            ayahSurah = "Furkân Sûresi, 58. Âyet",
            hadithTurkish = "Eğer siz Allah'a hakkıyla tevekkül etseydiniz, sabahleyin aç çıkıp akşamleyin tok dönen kuşlar gibi rızıklanırdınız.",
            hadithSource = "Tirmizî, Zühd, 33",
            duaArabic = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
            duaTurkish = "Allah bize yeter, O ne güzel vekildir!"
        ),
        DailyWisdom(
            ayahArabic = "إِنَّ اللَّهَ يُحِبُّ الْمُحْسِنِينَ",
            ayahTurkish = "Şüphesiz Allah, iyilik ve ihsanda bulunanları sever.",
            ayahSurah = "Bakara Sûresi, 195. Âyet",
            hadithTurkish = "İnsanların en hayırlısı, insanlara en çok faydası dokunan kimsedir.",
            hadithSource = "Taberânî, el-Mu'cemü'l-Evsat",
            duaArabic = "اللَّهُمَّ اهْدِنِي لِأَحْسَنِ الْأَخْلَاقِ لَا يَهْدِي لِأَحْسَنِهَا إِلَّا أَنْتَ",
            duaTurkish = "Allah'ım! Beni ahlakın en güzeline ilet; zira en güzeline ancak Sen ulaştırırsın."
        ),
        DailyWisdom(
            ayahArabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            ayahTurkish = "Rabbimiz! Bize dünyada da iyilik ve güzellik ver, ahirette de iyilik ve güzellik ver ve bizi cehennem azabından koru.",
            ayahSurah = "Bakara Sûresi, 201. Âyet",
            hadithTurkish = "Dua, ibadetin ta kendisi ve özüdür.",
            hadithSource = "Ebû Dâvûd, Vitir, 23",
            duaArabic = "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا",
            duaTurkish = "Rabbimiz! Bizi doğru yola ilettikten sonra kalplerimizi eğriltme!"
        ),
        DailyWisdom(
            ayahArabic = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنْفُسِهِمْ لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ",
            ayahTurkish = "De ki: Ey nefisleri aleyhine haddi aşan kullarım! Allah'ın rahmetinden ümidinizi kesmeyin. Muhakkak ki Allah bütün günahları bağışlar.",
            ayahSurah = "Zümer Sûresi, 53. Âyet",
            hadithTurkish = "Günahtan samimiyetle tevbe eden kimse, hiç günah işlememiş gibidir.",
            hadithSource = "İbn Mâce, Zühd, 30",
            duaArabic = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ وَأَتُوبُ إِلَيْهِ",
            duaTurkish = "Yüce Allah'tan bağışlanma diler ve O'na tövbe ederim."
        ),
        DailyWisdom(
            ayahArabic = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
            ayahTurkish = "Kullarım sana beni sorduklarında bilsinler ki ben şüphesiz onlara çok yakınım. Bana dua edenin duasına icabet ederim.",
            ayahSurah = "Bakara Sûresi, 186. Âyet",
            hadithTurkish = "Şüphesiz Rabbiniz hayâ ve kerem sahibidir. Kulu ellerini kaldırıp dua ettiğinde onları boş çevirmekten hayâ buyurur.",
            hadithSource = "Ebû Dâvûd, Vitir, 23",
            duaArabic = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ",
            duaTurkish = "Ey Hayy ve Kayyûm olan Rabbim! Rahmetinle yardımını niyaz ederim."
        ),
        DailyWisdom(
            ayahArabic = "مَنْ جَاءَ بِالْحَسَنَةِ فَلَهُ عَشْرُ أَمْثَالِهَا",
            ayahTurkish = "Kim bir iyilikle gelirse, ona getirdiğinin on katı vardır.",
            ayahSurah = "En'âm Sûresi, 160. Âyet",
            hadithTurkish = "Yarım hurmayla dahi olsa kendinizi ateşten koruyun; onu da bulamazsanız güzel bir sözle!",
            hadithSource = "Buhârî, Zekât, 10",
            duaArabic = "اللَّهُمَّ بَارِكْ لَنَا فِي رِزْقِنَا وَقِنَا عَذَابَ النَّارِ",
            duaTurkish = "Allah'ım! Rızkımızı bereketli kıl ve bizi cehennem azabından koru."
        ),
        DailyWisdom(
            ayahArabic = "وَقُولُوا لِلنَّاسِ حُسْنًا",
            ayahTurkish = "İnsanlara güzel ve tatlı söz söyleyin.",
            ayahSurah = "Bakara Sûresi, 83. Âyet",
            hadithTurkish = "Müslüman, elinden ve dilinden diğer Müslümanların emniyette olduğu kimsedir.",
            hadithSource = "Buhârî, Îmân, 4",
            duaArabic = "اللَّهُمَّ كَمَا حَسَّنْتَ خَلْقِي فَحَسِّنْ خُلُقِي",
            duaTurkish = "Allah'ım! Yaratılışımı güzel kıldığın gibi ahlakımı da güzelleştir."
        )
    )

    fun getTodayWisdom(dayOfYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)): DailyWisdom {
        val index = (dayOfYear % dailyWisdomList.size + dailyWisdomList.size) % dailyWisdomList.size
        return dailyWisdomList[index]
    }

    fun getWisdomByIndex(index: Int): DailyWisdom {
        val safeIndex = (index % dailyWisdomList.size + dailyWisdomList.size) % dailyWisdomList.size
        return dailyWisdomList[safeIndex]
    }
}
