// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 19.08.2021.
//

#ifndef GNSERVICE_HELPER_COUNTRYCODETABLE_HXX_
#define GNSERVICE_HELPER_COUNTRYCODETABLE_HXX_

struct countryCode {
    const char* mcc;
    const char* iso2; // iso-alpha-2 country code
    const char* iso3; // iso-alpha-3 country code
};

static const countryCode countryCodeTable[] = { {"213","ad", "and"},
                                                {"424","ae", "are"},
                                                {"430","ae", "are"},
                                                {"431","ae", "are"},
                                                {"412","af", "afg"},
                                                {"344","ag", "atg"},
                                                {"365","ai", "aia"},
                                                {"276","al", "alb"},
                                                {"283","am", "arm"},
                                                {"362","an", "ant"},
                                                {"631","ao", "ago"},
                                                {"722","ar", "arg"},
                                                {"544","as", "asm"},
                                                {"232","at", "aut"},
                                                {"505","au", "aus"},
                                                {"363","aw", "abw"},
                                                {"400","az", "aze"},
                                                {"218","ba", "bih"},
                                                {"342","bb", "brb"},
                                                {"470","bd", "bgd"},
                                                {"206","be", "bel"},
                                                {"613","bf", "bfa"},
                                                {"284","bg", "bgr"},
                                                {"426","bh", "bhr"},
                                                {"642","bi", "bdi"},
                                                {"616","bj", "ben"},
                                                {"350","bm", "bmu"},
                                                {"528","bn", "brn"},
                                                {"736","bo", "bol"},
                                                {"724","br", "bra"},
                                                {"364","bs", "bhs"},
                                                {"402","bt", "btn"},
                                                {"652","bw", "bwa"},
                                                {"257","by", "blr"},
                                                {"702","bz", "blz"},
                                                {"302","ca", "can"},
                                                {"630","cd", "cod"},
                                                {"623","cf", "caf"},
                                                {"629","cg", "cog"},
                                                {"228","ch", "che"},
                                                {"612","ci", "civ"},
                                                {"548","ck", "cok"},
                                                {"730","cl", "chl"},
                                                {"624","cm", "cmr"},
                                                {"460","cn", "chn"},
                                                {"732","co", "col"},
                                                {"712","cr", "cri"},
                                                {"368","cu", "cub"},
                                                {"625","cv", "cpv"},
                                                {"362","cw", "cuw"},
                                                {"280","cy", "cyp"},
                                                {"230","cz", "cze"},
                                                {"262","de", "deu"},
                                                {"638","dj", "dji"},
                                                {"238","dk", "dnk"},
                                                {"366","dm", "dma"},
                                                {"370","do", "dom"},
                                                {"603","dz", "dza"},
                                                {"740","ec", "ecu"},
                                                {"248","ee", "est"},
                                                {"602","eg", "egy"},
                                                {"657","er", "eri"},
                                                {"214","es", "esp"},
                                                {"636","et", "eth"},
                                                {"340","fg", "mtq"},
                                                {"244","fi", "fin"},
                                                {"542","fj", "fji"},
                                                {"750","fk", "flk"},
                                                {"550","fm", "fsm"},
                                                {"288","fo", "fro"},
                                                {"208","fr", "fra"},
                                                {"628","ga", "gab"},
                                                {"234","gb", "gbr"},
                                                {"235","gb", "gbr"},
                                                {"352","gd", "grd"},
                                                {"282","ge", "geo"},
                                                {"289","ge", "geo"},
                                                {"620","gh", "gha"},
                                                {"266","gi", "gib"},
                                                {"290","gl", "grl"},
                                                {"607","gm", "gmb"},
                                                {"611","gn", "gin"},
                                                {"340","gp", "glp"},
                                                {"627","gq", "gnq"},
                                                {"202","gr", "grc"},
                                                {"704","gt", "gtm"},
                                                {"535","gu", "gum"},
                                                {"632","gw", "gnb"},
                                                {"738","gy", "guy"},
                                                {"454","hk", "hkg"},
                                                {"708","hn", "hnd"},
                                                {"219","hr", "hrv"},
                                                {"372","ht", "hti"},
                                                {"216","hu", "hun"},
                                                {"510","id", "idn"},
                                                {"272","ie", "irl"},
                                                {"425","il", "isr"},
                                                {"404","in", "ind"},
                                                {"405","in", "ind"},
                                                {"418","iq", "irq"},
                                                {"432","ir", "irn"},
                                                {"274","is", "isl"},
                                                {"222","it", "ita"},
                                                {"338","jm", "jam"},
                                                {"416","jo", "jor"},
                                                {"440","jp", "jpn"},
                                                {"441","jp", "jpn"},
                                                {"639","ke", "ken"},
                                                {"437","kg", "kgz"},
                                                {"456","kh", "khm"},
                                                {"545","ki", "kir"},
                                                {"654","km", "com"},
                                                {"356","kn", "kna"},
                                                {"467","kp", "prk"},
                                                {"450","kr", "kor"},
                                                {"419","kw", "kwt"},
                                                {"346","ky", "cym"},
                                                {"401","kz", "kaz"},
                                                {"457","la", "lao"},
                                                {"415","lb", "lbn"},
                                                {"358","lc", "lca"},
                                                {"295","li", "lie"},
                                                {"413","lk", "lka"},
                                                {"618","lr", "lbr"},
                                                {"651","ls", "lso"},
                                                {"246","lt", "ltu"},
                                                {"270","lu", "lux"},
                                                {"247","lv", "lva"},
                                                {"606","ly", "lby"},
                                                {"604","ma", "mar"},
                                                {"212","mc", "mco"},
                                                {"259","md", "mda"},
                                                {"297","me", "mne"},
                                                {"646","mg", "mdg"},
                                                {"294","mk", "mkd"},
                                                {"610","ml", "mli"},
                                                {"414","mm", "mmr"},
                                                {"428","mn", "mng"},
                                                {"455","mo", "mac"},
                                                {"340","mq", "mtq"},
                                                {"609","mr", "mrt"},
                                                {"354","ms", "msr"},
                                                {"278","mt", "mlt"},
                                                {"617","mu", "mus"},
                                                {"472","mv", "mdv"},
                                                {"650","mw", "mwi"},
                                                {"334","mx", "mex"},
                                                {"502","my", "mys"},
                                                {"643","mz", "moz"},
                                                {"901","n/a", "n/a"},
                                                {"649","na", "nam"},
                                                {"546","nc", "ncl"},
                                                {"614","ne", "ner"},
                                                {"621","ng", "nga"},
                                                {"710","ni", "nic"},
                                                {"204","nl", "nld"},
                                                {"242","no", "nor"},
                                                {"429","np", "npl"},
                                                {"555","nu", "niu"},
                                                {"530","nz", "nzl"},
                                                {"422","om", "omn"},
                                                {"714","pa", "pan"},
                                                {"716","pe", "per"},
                                                {"547","pf", "pyf"},
                                                {"537","pg", "png"},
                                                {"515","ph", "phl"},
                                                {"410","pk", "pak"},
                                                {"260","pl", "pol"},
                                                {"308","pm", "spm"},
                                                {"330","pr", "pri"},
                                                {"425","ps", "pse"},
                                                {"268","pt", "prt"},
                                                {"552","pw", "plw"},
                                                {"744","py", "pry"},
                                                {"427","qa", "qat"},
                                                {"647","re", "reu"},
                                                {"226","ro", "rou"},
                                                {"220","rs", "srb"},
                                                {"250","ru", "rus"},
                                                {"635","rw", "rwa"},
                                                {"420","sa", "sau"},
                                                {"540","sb", "slb"},
                                                {"633","sc", "syc"},
                                                {"634","sd", "sdn"},
                                                {"240","se", "swe"},
                                                {"525","sg", "sgp"},
                                                {"293","si", "svn"},
                                                {"231","sk", "svk"},
                                                {"619","sl", "sle"},
                                                {"292","sm", "smr"},
                                                {"608","sn", "sen"},
                                                {"637","so", "som"},
                                                {"746","sr", "sur"},
                                                {"659","ss", "ssd"},
                                                {"626","st", "stp"},
                                                {"706","sv", "slv"},
                                                {"417","sy", "syr"},
                                                {"653","sz", "swz"},
                                                {"376","tc", "tca"},
                                                {"622","td", "tcd"},
                                                {"615","tg", "tgo"},
                                                {"520","th", "tha"},
                                                {"436","tk", "tjk"},
                                                {"438","tm", "tkm"},
                                                {"605","tn", "tun"},
                                                {"539","to", "ton"},
                                                {"514","tp", "tls"},
                                                {"286","tr", "tur"},
                                                {"374","tt", "tto"},
                                                {"553","tv", "tuv"},
                                                {"466","tw", "twn"},
                                                {"640","tz", "tza"},
                                                {"255","ua", "ukr"},
                                                {"641","ug", "uga"},
                                                {"310","us", "usa"},
                                                {"311","us", "usa"},
                                                {"312","us", "usa"},
                                                {"316","us", "usa"},
                                                {"748","uy", "ury"},
                                                {"434","uz", "uzb"},
                                                {"360","vc", "vct"},
                                                {"734","ve", "ven"},
                                                {"348","vg", "vgb"},
                                                {"376","vi", "vir"},
                                                {"452","vn", "vnm"},
                                                {"541","vu", "vut"},
                                                {"549","ws", "wsm"},
                                                {"421","ye", "yem"},
                                                {"655","za", "zaf"},
                                                {"645","zm", "zmb"},
                                                {"648","zw", "zwe"} };

class CountryCodeTable
{
public:

private:
    static const uint32_t countryCodeTableSize = sizeof(countryCodeTable)/sizeof(countryCodeTable[0]);

public:
    static int32_t getIsoAlpha2CountryCodeFromMCC(const char* mcc, const char** iso);
    static int32_t getIsoAlpha3CountryCodeFromMCC(const char* mcc, const char** iso);
    static int32_t getIsoAlpha3CountryCodeFromIsoAlpha2(const char* iso2, const char** iso3);

};

int32_t CountryCodeTable::getIsoAlpha2CountryCodeFromMCC(const char* mcc, const char** iso)
{
    for ( uint32_t i = 0; i < countryCodeTableSize; i++ )
    {
        if( strcmp(countryCodeTable[i].mcc, mcc) == 0 )
        {
            *iso = countryCodeTable[i].iso2;
            return 0;
        }
    }
    *iso = "n/a";
    return 1;
}

int32_t CountryCodeTable::getIsoAlpha3CountryCodeFromMCC(const char* mcc, const char** iso)
{
    for ( uint32_t i = 0; i < countryCodeTableSize; i++ )
    {
        if( strcmp(countryCodeTable[i].mcc, mcc) == 0 )
        {
            *iso = countryCodeTable[i].iso3;
            return 0;
        }
    }
    *iso = "n/a";
    return 1;
}

int32_t CountryCodeTable::getIsoAlpha3CountryCodeFromIsoAlpha2(const char *iso2,
                                                               const char **iso3)
{
    for ( uint32_t i = 0; i < countryCodeTableSize; i++ )
    {
        if ( strcmp(countryCodeTable[i].iso2, iso2) == 0 )
        {
            *iso3 = countryCodeTable[i].iso3;
            return 0;
        }
    }
    *iso3 = "";
    return 1;
}

#endif //GNSERVICE_HELPER_COUNTRYCODETABLE_HXX_
