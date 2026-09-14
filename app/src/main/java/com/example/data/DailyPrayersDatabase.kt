package com.example.data

import com.example.model.CategorizedDua
import com.example.model.DuaCategory

object DailyPrayersDatabase {

    val allPrayers: List<CategorizedDua> = listOf(
        // =========================================================================
        // 1. SABAH & AKŞAM DUALARI
        // =========================================================================
        CategorizedDua(
            id = "dua_seyyidul_istigfar",
            title = "Seyyidü'l-İstiğfâr (Tevbelerin Efendisi)",
            category = DuaCategory.SABAH_AKSAM,
            arabic = "اَللّٰهُمَّ أَنْتَ رَبِّي لَا إِلٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transcription = "Allâhümme ente Rabbî lâ ilâhe illâ ente halaktenî ve ene abdüke ve ene alâ ahdike ve va'dike mesteta'tü, e'ûzü bike min şerri mâ sana'tü, ebû'u leke bi-ni'metike aleyye ve ebû'u bi-zenbî fağfirlî fe-innehû lâ yağfiru'z-zünûbe illâ ente.",
            turkish = "Allah'ım! Sen benim Rabbimsin. Senden başka ilah yoktur. Beni sen yarattın, ben senin kulunum. Gücüm yettiğince ezelde verdiğim ahdime ve vaadime sadığım. İşlediğim günahların şerrinden sana sığınırım. Bana lütfettiğin nimetlerini itiraf eder, günahımı da ikrar ederim. Beni bağışla; zira günahları senden başka hiç kimse bağışlayamaz.",
            sourceOrVirtue = "Sahîh-i Buhârî. 'Her kim bu duayı inanarak gündüz okur ve o gün akşam olmadan ölürse cennetliktir. Gece okuyup sabah olmadan ölürse yine cennet ehlindendir.'",
            repeatCount = 1
        ),
        CategorizedDua(
            id = "dua_bismillahillezi",
            title = "Zararlardan Korunma Duası",
            category = DuaCategory.SABAH_AKSAM,
            arabic = "بِسْمِ اللّٰهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transcription = "Bismillâhillezî lâ yedurru mea'smihî şey'ün fi'l-ardı ve lâ fi's-semâi ve hüve's-semîu'l-alîm.",
            turkish = "İsmiyle yerde ve gökte hiçbir şeyin zarar veremeyeceği Allah'ın yüce adıyla başlarım. O hakkıyla işiten ve kemaliyle bilendir.",
            sourceOrVirtue = "Ebû Dâvûd, Tirmizî. 'Sabah ve akşam üçer defa okuyan kimseye hiçbir şey zarar veremez.'",
            repeatCount = 3
        ),
        CategorizedDua(
            id = "dua_hasbiyallah",
            title = "Hasbiyallâh Kifâyet Zikri",
            category = DuaCategory.SABAH_AKSAM,
            arabic = "حَسْبِيَ اللّٰهُ لَا إِلٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transcription = "Hasbiyallâhü lâ ilâhe illâ hû, aleyhi tevekkeltü ve hüve Rabbü'l-Arşi'l-Azîm.",
            turkish = "Allah bana yeter! O'ndan başka hiçbir ilah yoktur. Ben yalnız O'na tevekkül ettim. O, Yüce Arş'ın sahibidir. (Tevbe Suresi 129)",
            sourceOrVirtue = "Ebû Dâvûd. 'Sabah ve akşam yedişer defa bu ayeti okuyanın dünya ve ahiret gamına Allah kâfi gelir.'",
            repeatCount = 7
        ),
        CategorizedDua(
            id = "dua_sabah_mulk",
            title = "Sabah Vakti Hamd ve Tevekkül Duası",
            category = DuaCategory.SABAH_AKSAM,
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلّٰهِ، وَالْحَمْدُ لِلّٰهِ، لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transcription = "Asbahnâ ve asbaha'l-mülkü lillâh, ve'l-hamdü lillâh, lâ ilâhe illallâhü vahdehû lâ şerîke leh.",
            turkish = "Sabaha çıktık; mülk de bütünüyle Allah'ın olarak sabaha erdi. Hamd Allah'a mahsustur. O'ndan başka hiçbir ilah yoktur; tektir, şeriki ve ortağı yoktur.",
            sourceOrVirtue = "Sahîh-i Müslim. Resûlullah (s.a.v.) sabah ve akşam olduğunda bu zikri yapardı.",
            repeatCount = 1
        ),

        // =========================================================================
        // 2. NAMAZ SONRASI DUALARI
        // =========================================================================
        CategorizedDua(
            id = "dua_namaz_selam",
            title = "Selâm ve Esenlik Duası (Selâm Duası)",
            category = DuaCategory.NAMAZ_SONRASI,
            arabic = "اَللّٰهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            transcription = "Allâhümme ente's-Selâmü ve minke's-selâm, tebârekte yâ Ze'l-Celâli ve'l-İkrâm.",
            turkish = "Allah'ım! Selâm (tüm eksikliklerden münezzeh, esenlik veren) Sensin; selâmet ve barış da yalnız Sendedir. Ey celâl ve ikram sahibi Rabbimiz, Sen ne yücesin!",
            sourceOrVirtue = "Sahîh-i Müslim. Peygamber Efendimiz namazdan selâm verip üç defa istiğfar ettikten hemen sonra bu duayı okurdu.",
            repeatCount = 1
        ),
        CategorizedDua(
            id = "dua_namaz_sukurbagis",
            title = "İbâdete Yardım ve Şükür Talebi",
            category = DuaCategory.NAMAZ_SONRASI,
            arabic = "اَللّٰهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
            transcription = "Allâhümme e'ınnî alâ zikrike ve şükrike ve husni ibâdetik.",
            turkish = "Allah'ım! Seni anmak, Sana şükretmek ve Sana en güzel şekilde ibadet edebilmek için bana yardım eyle.",
            sourceOrVirtue = "Ebû Dâvûd, Nesâî. Peygamberimiz Hz. Muâz'a 'Her namazın peşinden bu duayı okumayı sakın terk etme' buyurmuştur.",
            repeatCount = 1
        ),
        CategorizedDua(
            id = "dua_ezan_sonrasi",
            title = "Ezan-ı Şerîf'ten Sonra Okunacak Dua",
            category = DuaCategory.NAMAZ_SONRASI,
            arabic = "اَللّٰهُمَّ رَبَّ هٰذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
            transcription = "Allâhümme Rabbe hâzihi'd-da'veti't-tâmmeh, ve's-salâti'l-kâimeh, âti Seyyidenâ Muhammedenil-vesîlete ve'l-fazîleh, veb'ashü makâmen Mahmûdenillezî va'adteh.",
            turkish = "Ey bu mükemmel çağrının ve kılınacak namazın Rabbi olan Allah'ım! Muhammed'e (s.a.v.) vesileyi ve fazileti ver. O'nu vaad buyurduğun Makâm-ı Mahmûd'a ulaştır.",
            sourceOrVirtue = "Sahîh-i Buhârî. 'Bu duayı okuyana kıyamet gününde şefaatim vacip olur.'",
            repeatCount = 1
        ),

        // =========================================================================
        // 3. ŞİFÂ & KORUNMA DUALARI
        // =========================================================================
        CategorizedDua(
            id = "dua_hastalara_sifa",
            title = "Hastalar İçin Şifâ Duası",
            category = DuaCategory.SIFA_KORUNMA,
            arabic = "أَذْهِبِ الْبَأْسَ رَبَّ النَّاسِ، وَاشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
            transcription = "Ezhibi'l-be'se Rabbe'n-nâs, ve'şfi ente'ş-Şâfî, lâ şifâe illâ şifâüke, şifâen lâ yüğâdiru sekamâ.",
            turkish = "Ey insanların Rabbi olan Allah'ım! Sıkıntıyı gider, şifâ ihsan eyle; Şâfî ancak Sensin. Senin vereceğin şifâdan başka şifâ yoktur. Öyle bir şifâ ver ki geride hiçbir hastalık ve illet bırakmasın.",
            sourceOrVirtue = "Sahîh-i Buhârî, Müslim. Peygamber Efendimiz (s.a.v.) hasta ziyaretlerinde bu duayı okuyup meshederdi.",
            repeatCount = 3
        ),
        CategorizedDua(
            id = "dua_nazar_korunma",
            title = "Nazar ve Kötü Gözden Korunma Duası",
            category = DuaCategory.SIFA_KORUNMA,
            arabic = "أُعِيذُكُمْ بِكَلِمَاتِ اللّٰهِ التَّامَّةِ مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ",
            transcription = "Ü'îzüküm bi-kelimâtillâhi't-tâmmeti min külli şeytânin ve hâmmetin ve min külli aynin lâmmeh.",
            turkish = "Her türlü şeytandan, zararlı haşerattan ve kem gözlerden Allah'ın noksansız ve eksiksiz kelimelerine sığınırım.",
            sourceOrVirtue = "Sahîh-i Buhârî. Hz. İbrahim'in İsmail ve İshak'ı, Peygamberimiz'in de Hasan ve Hüseyin'i bu dua ile koruduğu rivayet edilmiştir.",
            repeatCount = 3
        ),
        CategorizedDua(
            id = "dua_kul_euzu",
            title = "Kelimât-ı Tâmmât ile Sığınma Duası",
            category = DuaCategory.SIFA_KORUNMA,
            arabic = "أَعُوذُ بِكَلِمَاتِ اللّٰهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transcription = "E'ûzü bi-kelimâtillâhi't-tâmmâti min şerri mâ halak.",
            turkish = "Yarattığı şeylerin bütün şerrinden Allah'ın noksansız, şifalı kelimelerine sığınırım.",
            sourceOrVirtue = "Sahîh-i Müslim. 'Kim bir yerde konaklar da bu duayı söylerse, oradan ayrılıncaya kadar ona hiçbir şey zarar veremez.'",
            repeatCount = 3
        ),

        // =========================================================================
        // 4. GÜNLÜK HAYAT & RIZIK DUALARI
        // =========================================================================
        CategorizedDua(
            id = "dua_evden_cikarken",
            title = "Evden Çıkarken Okunacak Tevekkül Duası",
            category = DuaCategory.GUNLUK_HAYAT,
            arabic = "بِسْمِ اللّٰهِ تَوَكَّلْتُ عَلَى اللّٰهِ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ",
            transcription = "Bismillâhi tevekkeltü alallâh, lâ havle ve lâ kuvvete illâ billâh.",
            turkish = "Allah'ın ismiyle, Allah'a tevekkül edip güvendim. Güç ve kuvvet ancak ve ancak Allah iledir.",
            sourceOrVirtue = "Ebû Dâvûd, Tirmizî. 'Evinden çıkarken bu duayı söyleyen kimseye melekler: Doğru yola iletildin, kötülüklerden korundun ve muhafaza edildin derler.'",
            repeatCount = 1
        ),
        CategorizedDua(
            id = "dua_eve_girerken",
            title = "Eve Girerken Okunacak Selâm ve Bereket Duası",
            category = DuaCategory.GUNLUK_HAYAT,
            arabic = "اَللّٰهُمَّ إِنِّي أَسْأَلُكَ خَيْرَ الْمَوْلِجِ وَخَيْرَ الْمَخْرَجِ، بِسْمِ اللّٰهِ وَلَجْنَا، وَبِسْمِ اللّٰهِ خَرَجْنَا، وَعَلَى اللّٰهِ رَبِّنَا تَوَكَّلْنَا",
            transcription = "Allâhümme innî es'elüke hayra'l-mevlici ve hayra'l-mahrac, bismillâhi velecnâ, ve bismillâhi haracnâ, ve alallâhi Rabbinâ tevekkelnâ.",
            turkish = "Allah'ım! Senden girişin de çıkışın da hayırlısını isterim. Allah'ın adıyla girdik, Allah'ın adıyla çıktık ve yalnız Rabbimiz olan Allah'a tevekkül ettik.",
            sourceOrVirtue = "Ebû Dâvûd. Eve girildiğinde bu dua okunup aile halkına selâm verilir.",
            repeatCount = 1
        ),
        CategorizedDua(
            id = "dua_yemek_sonu",
            title = "Yemekten Sonra Şükür Duası",
            category = DuaCategory.GUNLUK_HAYAT,
            arabic = "اَلْحَمْدُ لِلّٰهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
            transcription = "Elhamdü lillâhillezî et'amenâ ve sekânâ ve cealenâ müslimîn.",
            turkish = "Bizi yediren, bizi içiren ve bizi Müslümanlardan kılan Yüce Allah'a hamdolsun.",
            sourceOrVirtue = "Ebû Dâvûd, Tirmizî. Yemek sonrasında nimete şükür amacıyla Resûl-i Ekrem tarafından okunurdu.",
            repeatCount = 1
        ),
        CategorizedDua(
            id = "dua_uyku_oncesi",
            title = "Yatarken / Uykudan Önce Okunacak Dua",
            category = DuaCategory.GUNLUK_HAYAT,
            arabic = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي وَبِكَ أَرْفَعُهُ، إِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
            transcription = "Bismike Rabbî vada'tü cenbî ve bike erfe'uh, in emsekte nefsî ferhamhâ, ve in erseltehâ fahfazhâ bimâ tahfezu bihî ibâdeke's-sâlihîn.",
            turkish = "Rabbim! Senin isminle yanımı yatağa koydum, yine Senin inayetinle kaldırırım. Eğer canımı alacaksan ona merhamet eyle; şayet geri göndereceksen salih kullarını koruduğun gibi onu da muhafaza eyle.",
            sourceOrVirtue = "Sahîh-i Buhârî, Müslim. Gece yatağa girildiğinde sağ tarafına uzanılarak okunması sünnettir.",
            repeatCount = 1
        ),

        // =========================================================================
        // 5. PEYGAMBERLERİN KUR'ÂN'DAKİ DUALARI
        // =========================================================================
        CategorizedDua(
            id = "dua_yunus_nebi",
            title = "Hz. Yûnus'un (a.s.) Sıkıntı ve Felah Duası",
            category = DuaCategory.PEYGAMBER_DUALARI,
            arabic = "لَا إِلٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            transcription = "Lâ ilâhe illâ ente sübhâneke innî küntü mine'z-zâlimîn.",
            turkish = "Senden başka hiçbir ilah yoktur. Seni her türlü eksiklikten tenzih ederim. Gerçekten ben kendi nefsine zulmedenlerden oldum. (Enbiyâ Suresi 87)",
            sourceOrVirtue = "Tirmizî. 'Balığın karnındayken yaptığı bu duayı bir Müslüman herhangi bir sıkıntısında okursa Allah Teâlâ mutlaka onun duasını kabul buyurur.'",
            repeatCount = 40
        ),
        CategorizedDua(
            id = "dua_ibrahim_namaz",
            title = "Hz. İbrâhîm'in (a.s.) Nesil ve Namaz Duası",
            category = DuaCategory.PEYGAMBER_DUALARI,
            arabic = "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِنْ ذُرِّيَّتِي رَبَّنَا وَتَقَبَّلْ دُعَاءِ ۞ رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ",
            transcription = "Rabbic'alnî mukîme's-salâti ve min zürriyyetî, Rabbenâ ve tekabbel düâ. Rabbenâğfirlî ve li-vâlideyye ve li'l-mü'minîne yevme yekûmu'l-hisâb.",
            turkish = "Rabbim! Beni ve neslimden gelenleri namazı dosdoğru kılanlardan eyle. Rabbimiz! Duamı kabul buyur. Rabbimiz! Hesabın görüleceği gün beni, anne-babamı ve bütün inananları bağışla. (İbrâhîm Suresi 40-41)",
            sourceOrVirtue = "Kur'an-ı Kerim. Her namazın son kaidesinde 'Rabbenâğfirlî' olarak okunması sünnettir.",
            repeatCount = 1
        ),
        CategorizedDua(
            id = "dua_eyyub_sifa",
            title = "Hz. Eyyûb'un (a.s.) Sabır ve Şifâ Niyazı",
            category = DuaCategory.PEYGAMBER_DUALARI,
            arabic = "أَنِّي مَسَّنِيَ الضُّرُّ وَأَنْتَ أَرْحَمُ الرَّاحِمِينَ",
            transcription = "Ennî messeniye'd-durru ve ente Erhamü'r-râhimîn.",
            turkish = "Şüphesiz bu dert ve hastalık bana dokundu; Sen ise merhametlilerin en merhametlisisin! (Enbiyâ Suresi 83)",
            sourceOrVirtue = "Kur'an-ı Kerim. Ağır hastalıklarda, dert ve musibet anlarında sabır ve acil şifâ niyazıyla okunur.",
            repeatCount = 7
        ),
        CategorizedDua(
            id = "dua_peygamberimiz_hidayet",
            title = "Resûlullah'ın (s.a.v.) Hidayet ve İffet Duası",
            category = DuaCategory.PEYGAMBER_DUALARI,
            arabic = "اَللّٰهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى",
            transcription = "Allâhümme innî es'elüke'l-hüdâ ve't-tukâ ve'l-afâfe ve'l-ğınâ.",
            turkish = "Allah'ım! Senden dosdoğru hidayet, takvâ (günahlardan korunma hassasiyeti), iffet ve gönül zenginliği diliyorum.",
            sourceOrVirtue = "Sahîh-i Müslim. Peygamber Efendimiz'in en çok tekrar ettiği veciz ve kapsamlı dualarındandır.",
            repeatCount = 3
        ),

        // =========================================================================
        // 6. TEVBE & İSTİĞFÂR DUALARI
        // =========================================================================
        CategorizedDua(
            id = "dua_adem_tevbe",
            title = "Hz. Âdem ve Havvâ'nın Bağışlanma Duası",
            category = DuaCategory.TEVBE_ISTIGFAR,
            arabic = "رَبَّنَا ظَلَمْنَا أَنْفُسَنَا وَإِنْ لَمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
            transcription = "Rabbenâ zalemnâ enfüsenâ ve in lem tağfir lenâ ve terhamnâ le-nekûnenne mine'l-hâsirîn.",
            turkish = "Ey Rabbimiz! Biz kendimize zulmettik. Eğer bizi bağışlamaz ve bize merhamet etmezsen elbette hüsrana uğrayanlardan oluruz. (A'râf Suresi 23)",
            sourceOrVirtue = "Kur'an-ı Kerim. İnsanlığın atası Hz. Âdem ve Havvâ'nın tövbesi; samimi tövbelerin anahtarıdır.",
            repeatCount = 3
        ),
        CategorizedDua(
            id = "dua_genel_istigfar",
            title = "Büyük İstiğfâr Zikri",
            category = DuaCategory.TEVBE_ISTIGFAR,
            arabic = "أَسْتَغْفِرُ اللّٰهَ الْعَظِيمَ الَّذِي لَا إِلٰهَ إِلَّا هُوَ الْحَيَّ الْقَيُّومَ وَأَتُوبُ إِلَيْهِ",
            transcription = "Estağfirullâhe'l-Azîmellezî lâ ilâhe illâ hüve'l-Hayye'l-Kayyûme ve etûbü ileyh.",
            turkish = "Kendisinden başka hiçbir ilah bulunmayan, Hayy (ebedî diri) ve Kayyûm (her şeyi ayakta tutan) olan Yüce Allah'tan bağışlanma diler ve O'na tövbe ederim.",
            sourceOrVirtue = "Tirmizî, Ebû Dâvûd. 'Her kim bu istiğfarı söylerse, savaştan kaçmış bile olsa günahları bağışlanır.'",
            repeatCount = 3
        )
    )
}
