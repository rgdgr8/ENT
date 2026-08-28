// Copyright 2022 <e.solutions GmbH>
// Created by mir1367 on 15.12.22.
//

#ifndef GNSERVICE_HELPER_GNSERVICELOCALE_HXX
#define GNSERVICE_HELPER_GNSERVICELOCALE_HXX

#include <string>
#include "gnsdk.hpp"

using std::string;
using gracenote::GnLanguage;
using gracenote::GnRegion;

struct gnServiceLocale {
    string systemLocale;
    GnLanguage gnLang;
    GnRegion gnRegion;
};

static const gnServiceLocale gnServiceLocaleTable[] = {
        { "en_US", gracenote::kLanguageEnglish, gracenote::kRegionDefault},
        { "cs_CZ", gracenote::kLanguageCzech, gracenote::kRegionEurope },
        { "nl_NL", gracenote::kLanguageDutch, gracenote::kRegionEurope },
        { "en_GB", gracenote::kLanguageEnglish, gracenote::kRegionEurope },
        { "en_SA", gracenote::kLanguageEnglish, gracenote::kRegionEurope },
        { "fr_FR", gracenote::kLanguageFrench, gracenote::kRegionEurope },
        { "de_DE", gracenote::kLanguageGerman, gracenote::kRegionEurope},
        { "it_IT", gracenote::kLanguageItalian, gracenote::kRegionEurope },
        { "pl_PL", gracenote::kLanguagePolish, gracenote::kRegionEurope },
        { "pt_PT", gracenote::kLanguagePortuguese, gracenote::kRegionEurope },
        { "ru_RU", gracenote::kLanguageRussian, gracenote::kRegionGlobal },
        { "es_ES", gracenote::kLanguageSpanish, gracenote::kRegionEurope },
        { "sv_SE", gracenote::kLanguageSwedish, gracenote::kRegionEurope },
        { "tr_TR", gracenote::kLanguageTurkish, gracenote::kRegionGlobal },
        { "no_NO", gracenote::kLanguageNorwegian, gracenote::kRegionEurope },
        { "hu_HU", gracenote::kLanguageHungarian, gracenote::kRegionEurope },
        { "ar_SA", gracenote::kLanguageArabic, gracenote::kRegionGlobal },
        { "fr_CA", gracenote::kLanguageFrench, gracenote::kRegionUS },
        { "pt_BR", gracenote::kLanguagePortuguese, gracenote::kRegionEurope },
        { "es_MX", gracenote::kLanguageSpanish, gracenote::kRegionGlobal },
        { "zh_CN", gracenote::kLanguageChineseSimplified, gracenote::kRegionChina },
        { "zh_HK", gracenote::kLanguageChineseTraditional, gracenote::kRegionChina },
        { "en_CN", gracenote::kLanguageEnglish, gracenote::kRegionChina },
        { "ja_JP", gracenote::kLanguageJapanese, gracenote::kRegionJapan },
        { "en_JP", gracenote::kLanguageEnglish, gracenote::kRegionJapan },
        { "ko_KR", gracenote::kLanguageKorean, gracenote::kRegionKorea },
        { "en_KR", gracenote::kLanguageEnglish, gracenote::kRegionKorea },
        { "zh_TW", gracenote::kLanguageChineseTraditional, gracenote::kRegionTaiwan },
        { "en_TW", gracenote::kLanguageEnglish, gracenote::kRegionTaiwan },
        { "bs_BA", gracenote::kLanguageEnglish, gracenote::kRegionEurope },
        { "ro_RO", gracenote::kLanguageRomanian, gracenote::kRegionEurope },
        { "sk_SK", gracenote::kLanguageSlovak, gracenote::kRegionEurope },
        { "sl_SI", gracenote::kLanguageEnglish, gracenote::kRegionEurope },
        { "sr_RS", gracenote::kLanguageSerbian, gracenote::kRegionEurope },
        { "da_DK", gracenote::kLanguageDanish, gracenote::kRegionEurope },
        { "el_GR", gracenote::kLanguageGreek, gracenote::kRegionEurope },
        { "hr_HR", gracenote::kLanguageCroatian, gracenote::kRegionEurope },
        { "fi_FI", gracenote::kLanguageFinnish, gracenote::kRegionEurope },
        { "bg_BG", gracenote::kLanguageBulgarian, gracenote::kRegionEurope },
        { "lv_LV", gracenote::kLanguageLatvian, gracenote::kRegionEurope },
        { "et_EE", gracenote::kLanguageEnglish, gracenote::kRegionEurope },
        { "lt_LT", gracenote::kLanguageLithuanian, gracenote::kRegionEurope },
        { "uk_UA", gracenote::kLanguageUkrainian, gracenote::kRegionEurope }
};

class GnServiceLocaleTable
{
public:

private:
    static const uint32_t localeTableSize = sizeof(gnServiceLocaleTable)/sizeof(gnServiceLocaleTable[0]);

public:
    static int32_t getGnServiceLocaleFromSystemLanguage(const string lang, gnServiceLocale* locale);

};

int32_t GnServiceLocaleTable::getGnServiceLocaleFromSystemLanguage(const string lang,
                                                                   gnServiceLocale* locale)
{
    for ( uint32_t i = 0; i < localeTableSize; i++ )
    {
        if ( gnServiceLocaleTable[i].systemLocale == lang )
        {
            *locale = gnServiceLocaleTable[i];
            return 0;
        }
    }
    return 1;
}

#endif //GNSERVICE_HELPER_GNSERVICELOCALE_HXX
