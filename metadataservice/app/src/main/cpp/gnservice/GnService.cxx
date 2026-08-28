// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 29.11.19.
//

#include <jni.h>

#include <sys/stat.h>
#include <unistd.h>
#include <thread> // NOLINT [build/c++11], google style guide sees <thread> as an unapproved c++11 header
#include <cerrno>
#include <fstream>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>
#include <memory>
#include <utility>

#include "event/GnServiceStatusEventHandler.hxx"

#include "helper/GnServiceLogging.hxx"
#include "user/GnUserStore.hxx"
#include "helper/GnsdkLogEvents.hxx"
#include "helper/CountryCodeTable.hxx"
#include "helper/GnServiceLocale.hxx"
#include "resultdb/GnLocalLookupDb.hxx"
#include "request/GnRequestCoverArtByMetadata.hxx"
#include "request/GnRequestRadioStationData.hxx"

#include "GnService.hxx"
#include "GnServiceConstants.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ Service
DECLARE_CHANNEL();
#define LOG_TAG "GnService"

using std::string;
using std::mutex;
using std::recursive_mutex;
using std::lock_guard;
using std::make_unique;
using std::stringstream;
using std::unique_lock;
using std::vector;
using gracenote::storage_sqlite::GnStorageSqlite;
using gracenote::lookup_local::GnLookupLocal;
using gracenote::musicid_stream::GnMusicIdStream;
using gracenote::metadata::GnTrack;
using gracenote::GnLog;
using gracenote::GnLogFilters;
using gracenote::GnLogColumns;
using gracenote::GnLogOptions;
using gracenote::GnLogMessageType;
using gracenote::GnUser;
using gracenote::GnUserOptions;
using gracenote::GnLocale;
using gracenote::GnList;
using gracenote::GnError;
using gracenote::GnLookupMode;
using gracenote::GnManager;
using gnservice::GnLocalLookupDB;


GnService::GnService() :
        m_gnJavaContext(make_unique<GnServiceJavaContext>()),
        m_filePath(),
        m_systemLang(),
        m_iso3CountryCode(),
        m_countryOfSale(),
        m_gnManager(nullptr),
        m_gnUser(nullptr),
        m_curPlayingTrack(GNSDK_NULL),
        m_resultDb(nullptr),
        m_radioResultDb(nullptr),
        m_gnsdkLogEvents(nullptr),
        m_apnConfigTable(),
        m_callbackTable(),
        m_reqLyrics(nullptr),
        m_songRecogJobId(0),
        m_lyricsJobId(0),
        m_initialized(false),
        m_jniContextInitialized(false),
        m_lyricsActivated(false),
        m_lyricsPending(false),
        m_esoTracingInitiazed(false)
#ifdef GN_INTEGRATE_FRAMEWORK
        ,
        m_errstor(),
        m_osal(),
        m_util(nullptr)
#endif // GN_INTEGRATE_FRAMEWORK
{
}

GnService::~GnService() = default;

#ifdef GN_INTEGRATE_FRAMEWORK
void GnService::initEsoTracing(const std::string& path)
{
    if (!isEsoTracingInitialized())
    {
        const auto envVariablesSet = configureTracingEnvVariables(path);
        if (envVariablesSet == false)
        {
            USE_CHANNEL();
            ALOG_ERR("Environment variable for tracing config files were not properly set, tracing might now work for Gracenote Service");
            return;
        }

        auto tempUtil = std::make_unique<util::Util>(m_osal, "gracenoteservice");
        m_util.reset(tempUtil.release());

        m_esoTracingInitiazed = true;
    }
}
#endif // GN_INTEGRATE_FRAMEWORK

void GnService::enableLogging()
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    string logPath = m_filePath;
    logPath+="/gnsdk.log";

    ALOG_DEBUG_CV("creating log path in '%s'\n", logPath.c_str());
    mkdir(m_filePath.c_str(), 0770);

    ALOG_INFO_CV("logging is configured (currently disabled) for path '%s'\n", logPath.c_str());

    /* Enable GNSDK logging */
    GnLog gnServiceLog("",                           /* File to write log to (optional if using delegate) */
                       GnLogFilters().Error().Warning().Info(),    /* Include error, warning and info entries */
                       GnLogColumns().All(),                       /* Add all columns to log: timestamps, thread IDs, etc */
                       GnLogOptions().MaxSize(0).Archive(false),   /* Max size of log: 0 means a new log file will be created each run. Archiving of logs disabled. */
                       this  // m_gnsdkLogEvents               // for callbacks (or set to GNSDK_NULL if not wanted) - Optional callback delegate for logging message
                       );

    /* To enable logging package call enable with the package. Enable can be called multiple
     * times to turn on multiple packages */
    gnServiceLog.Enable(gracenote::kLogPackageAllGNSDK);          /* Enable for all GNSDK packages */
    ALOG_DEBUG("EXIT");
}

void GnService::enableEncryption()
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    // set gnsdk's global encryption algorithm to TLS
    GnUserOptions().OnlineEncryption().UseTLSAlgorithm(gracenote::kOnlineEncryptionTLSVersion1_2,
                                                       gracenote::kOnlineEncryptionTLSVersion1_3);
    string certPath = m_filePath;
#ifdef GN_USE_DEFAULT_CERTFILE
    certPath += kCertificateFilePath;
#else
    certPath += kCertificateOtaFilePath;
#endif

    if ( FILE *file = fopen(certPath.c_str(), "r"))
    {
        ALOG_INFO_CV("certificate file '%s' exists and is readable", certPath.c_str());
        fclose(file);
    }
    else
    {
        ALOG_WARN_CV("certificate file '%s' cannot be read", certPath.c_str());
#ifdef GN_USE_DEFAULT_CERTFILE
        return;
#endif
        if ( remove(certPath.c_str()) )
        {
            ALOG_WARN("cannot remove OTA certificate file");
        }
        certPath = m_filePath + kCertificateFilePath;
    }

    ALOG_INFO_CV("using certificate file in '%s'", certPath.c_str());
    try {
        GnUserOptions().OnlineEncryption().AddTLSCertificates(certPath.c_str());
    }
    catch (GnError& e)
    {
        ALOG_ERR_CV("%s 0x%x : %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription() );
    }

    ALOG_DEBUG("EXIT");
}

void GnService::init(string path)
{
    USE_CHANNEL();
    {  // avoid race conditions for init state
        lock_guard<mutex> initLock(m_initMtx);
        if ( isInitialized() )
        {
            ALOG_DEBUG("GnService already initialized");
            return;
        }
    }

    ALOG_WARN("no language given for init, initializing locale for gnsdk with default language 'en_US'");
    init(path, "en_US");
}

void GnService::init(string path, string lang)
{
    USE_CHANNEL();

    if ( !preconditionsFulfilled() )
    {
        return;
    }
    // make sure gnsdk gets initialize only once and avoid race conditions
    lock_guard<mutex> initLock(m_initMtx);
    if ( isInitialized() )
    {
        ALOG_DEBUG("GnService already initialized");
        return;
    }

    ALOG_DEBUG("initializing GnService");
    m_filePath = std::move(path);
    m_systemLang = std::move(lang);
    initGnsdk();
    m_initialized = true;
}

void GnService::initGnsdk()
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");

    initGnManager();

    enableLogging();

    enableEncryption();

    setupGnUser();

    /**
     * Enable all necessary modules
     */
    /* Enable the SQlite storage module to be used as our database engine. */
    GnStorageSqlite::Enable();
    /* Enable local lookup */
    GnLookupLocal::Enable();
    /* Enable musicid stream */
    GnMusicIdStream::Enable();

    initResultDb();

    /* load locale asynchronously in a separate thread in order to speed up startup and initialization
     * of gnsdk */
    std::thread th(&GnService::loadLocale, this);
    th.detach();

    ALOG_DEBUG("EXIT");
}

/*-----------------------------------------------------------------------------
 *  loadLocale
 *
 *  Set locale. Note that this is only necessary if you are using
 *  locale-dependant fields such as genre, mood, origin, era, etc. Your app
 *  may or may not be accessing locale_dependent fields, but it does not hurt
 *  to do this initialization as a matter of course.
 */
void GnService::loadLocale()
{
    USE_CHANNEL();
    ALOG_DEBUG("trying to configure locale");

    gracenote::GnConfig config;
    string resultDbPath = m_filePath + "/" + string(kResultDbMainLocation) + "/" + resultDbConfig[0].subPath;

    /* get language setting and set up gnsdk langage and region respectively */
    ALOG_INFO_CV("loading language and region mapping for gnsdk from system language '%s", m_systemLang.c_str());
    gnServiceLocale gnServiceLocaleVal;
    if ( GnServiceLocaleTable::getGnServiceLocaleFromSystemLanguage(m_systemLang, &gnServiceLocaleVal) )
    {
        ALOG_WARN_CV("could not find language region mapping for gnsdk from system language (%s), using default '%s'",
                     m_systemLang.c_str(),
                     gnServiceLocaleTable[0].systemLocale.c_str());
        gnServiceLocaleVal = gnServiceLocaleTable[0];
    }

    try
    {
        config.ManagerStorage().StorageLocation(gracenote::kConfigManagerStorageLocationLists,
                                                resultDbPath.c_str());
        m_gnManager->LocalesStore().Configure(config);

        auto *pStatusEventHandler = new GnServiceStatusEventHandler( const_cast<gnsdk_str_t>("Load Locale"));
        // set locale with desired Group, Language, Region and Descriptor
        GnLocale locale(gracenote::kLocaleGroupMusic,
                        gnServiceLocaleVal.gnLang,
                        gnServiceLocaleVal.gnRegion,
                        gracenote::kDescriptorSimplified,
                        *m_gnUser,
                        pStatusEventHandler);
        // set this locale as default for the duration of gnsdk
        locale.SetGroupDefault();

        // check if an update is needed
        if ( locale.UpdateCheck(*m_gnUser) )
        {
            ALOG_INFO("Locale needs updating");
            locale.Update(*m_gnUser);
            ALOG_INFO("Locale updated and persisted");
        }
        ALOG_INFO("Locale set successfully");
    }
    catch (GnError &e)
    {
        ALOG_INFO_CV("%s 0x%x: %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription() );

#ifndef GN_USE_DEFAULT_CERTFILE
        if ( e.ErrorCode() & GNSDKERR_TLSPeerCertInvalid )
        {
            ALOG_WARN("used TLS certificate is invalid, need to use fallback cert file");
            useFallbackCertFileRetryLocale();
        }
#endif
    }
}


string GnService::getGnsdkVersionInfo()
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    string retString{"no version"};

    initGnManager();

    if ( m_gnManager != nullptr )
    {
        retString = string{m_gnManager->ProductVersion()};
        retString.append(" (built ");
        retString.append(m_gnManager->BuildDate());
        retString.append(")");
    }
    else
    {
        ALOG_WARN("no manager object available\n");
    }

    ALOG_DEBUG("EXIT");
    return retString;
}

void GnService::initGnManager()
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    try
    {
        if ( m_gnManager == nullptr )
        {
            m_gnManager = make_unique<GnManager>(gnLicenseString,
                                                 gracenote::kLicenseInputModeString);
        }
        ALOG_INFO_CV("GNSDK Product Version: %s (built %s)\n", m_gnManager->ProductVersion(),
                     m_gnManager->BuildDate());
    }
    catch (GnError& e)
    {
        ALOG_ERR_CV("%s 0x%x : %s\n", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
    }

    ALOG_DEBUG("EXIT");
}

void GnService::initResultDb()
{
    USE_CHANNEL();
    if ( m_resultDb == nullptr )
    {
        ALOG_INFO("initialize result db");
        m_resultDb = make_unique<gnservice::GnLocalLookupDB>();
    }

    if ( m_resultDb->init(m_filePath) != 0 )
    {
        ALOG_ERR("result db could not be initialized");
    }
}

uint32_t GnService::initRadioResultDb()
{
    USE_CHANNEL();
    lock_guard<mutex> initLock(m_rdbMtx);
    if ( m_radioResultDb == nullptr )
    {
        ALOG_INFO("initialize radio result db");
        m_radioResultDb = make_unique<GnRadioResultDb>();
    }

    if ( m_radioResultDb->init(m_filePath) != 0)
    {
        ALOG_ERR("radio result db could not be initialized");
        return 1;
    }
    return 0;
}

void GnService::requestCoverArtFromMetadata(const jobject& callback,
                                            const string &albumName, const string &artistName,
                                            const string &titleName)
{
    USE_CHANNEL();
    if ( !preconditionsFulfilled() )
    {
        return;
    }

    ALOG_INFO_CV("input: titleName='%s', artistName='%s', albumName='%s'",
                 albumName.c_str(), artistName.c_str(), titleName.c_str());

    std::string networkDevice;
    std::unique_lock<mutex> apnConfigTableLock(m_apnTableMtx);
    if ( m_apnConfigTable.empty()
         || m_apnConfigTable.count(GnServiceRequestNr::REQUEST_COVERART) == 0 )
    {
        ALOG_INFO_CV("no net device set for %s, using default network for emulator compatibility",
                     getRequestName(GnServiceRequestNr::REQUEST_COVERART).c_str() );
        networkDevice = ""; // Use default network
    }
    else
    {
        networkDevice = m_apnConfigTable.at(GnServiceRequestNr::REQUEST_COVERART);

        // Allow empty network device for emulator/dev environments
        // Gracenote SDK will use default network routing
        if ( networkDevice.empty() )
        {
            ALOG_INFO_CV("net device string is empty for %s, using default network",
                         getRequestName(GnServiceRequestNr::REQUEST_COVERART).c_str() );
        }
    }

    apnConfigTableLock.unlock();
    ALOG_INFO_CV("for %s net device '%s' will be used (empty = default network)",
                 getRequestName(GnServiceRequestNr::REQUEST_COVERART).c_str(),
                 networkDevice.c_str());

    auto albumInfo = GnServiceAlbumInfo();
    albumInfo.Track = string(albumName);
    albumInfo.Artist = string(artistName);
    albumInfo.Name = string(titleName);

    auto req = new GnRequestCoverArtByMetadata(this,
                                               m_gnUser.get(),
                                               m_resultDb.get(),
                                               albumInfo);
    req->setNetworkInterface(networkDevice);
    uint64_t jobId = req->getJobId();
    addCallbackForJob(jobId, callback);

    req->execute();
    ALOG_DEBUG("EXIT");
}

void GnService::requestLyrics(const jobject& callback,
                              const GnServiceLyricsType type,
                              const bool familyFriendly)
{
    USE_CHANNEL();

    if ( !preconditionsFulfilled() )
    {
        return;
    }

    if ( m_songRecogJobId == 0 )
    {
        ALOG_WARN("no song recognition active, request ignored");
        return;
    }
    ALOG_INFO_CV("type=%u, %sclean", type, familyFriendly ? "" : "not ");

    std::string networkDevice;
    if ( m_apnConfigTable.empty()
         || m_apnConfigTable.count(GnServiceRequestNr::REQUEST_LYRICS) == 0 )
    {
        ALOG_INFO_CV("no net device set for %s, cannot execute request",
                     getRequestName(GnServiceRequestNr::REQUEST_LYRICS).c_str() );
        return;
    }
    else
    {
        networkDevice = m_apnConfigTable.at(GnServiceRequestNr::REQUEST_LYRICS);
        if ( networkDevice.empty() )
        {
            ALOG_INFO_CV("the net device string is empty for %s, cannot execute request",
                         getRequestName(GnServiceRequestNr::REQUEST_LYRICS).c_str() );
            return;
        }
    }
    ALOG_INFO_CV("for %s net device '%s' will be used",
                 getRequestName(GnServiceRequestNr::REQUEST_LYRICS).c_str(),
                 networkDevice.c_str());

    if ( m_lyricsActivated )
    {
        GnRequestLyrics::abort(m_reqLyrics->getJobId());
    }

    m_reqLyrics = new GnRequestLyrics(this,
                                      m_gnUser.get(),
                                      m_resultDb.get(),
                                      m_curPlayingTrack,
                                      type,
                                      familyFriendly
                                      );
    m_reqLyrics->setNetworkInterface(networkDevice);
    m_lyricsJobId = m_reqLyrics->getJobId();
    addCallbackForJob(m_lyricsJobId, callback);

    m_lyricsActivated = true;
    if ( !m_curPlayingTrack.IsNull() )
    {
        m_reqLyrics->execute();
        return;
    }

    m_lyricsPending = true;
    ALOG_WARN("no song currently recognized, lyrics not available yet, waiting for recognized result");
}

void GnService::requestRadioStationMetadata(const uint32_t frequency,
                                            const std::string& band,
                                            const uint32_t country,
                                            const GnServicePositionData& posData)
{
    USE_CHANNEL();

    if ( !preconditionsFulfilled() )
    {
        return;
    }

    ALOG_INFO_CV("input: frequency=%i, band='%s', country='%i'",
                 frequency, band.c_str(), country);

    std::string networkDevice;
    std::unique_lock<mutex> apnConfigTableLock(m_apnTableMtx);
    if ( m_apnConfigTable.empty()
         || m_apnConfigTable.count(GnServiceRequestNr::REQUEST_RADIOSTATION) == 0 )
    {
        ALOG_INFO_CV("no net device set for %s, cannot execute request",
                     getRequestName(GnServiceRequestNr::REQUEST_RADIOSTATION).c_str() );
        return;
    }
    else
    {
        networkDevice = m_apnConfigTable.at(GnServiceRequestNr::REQUEST_RADIOSTATION);
        if ( networkDevice.empty() )
        {
            ALOG_INFO_CV("the net device string is empty for %s, cannot execute request",
                         getRequestName(GnServiceRequestNr::REQUEST_RADIOSTATION).c_str() );
            return;
        }
    }

    apnConfigTableLock.unlock();
#ifdef GN_INTEGRATE_FRAMEWORK
    TBIRC::mmx::MetadataService::PositioningData(TBIRC::mmx::MetadataService::EPositioningData::PositioningDataInUse);
#endif
    ALOG_INFO_CV("for %s net device '%s' will be used",
                 getRequestName(GnServiceRequestNr::REQUEST_RADIOSTATION).c_str(),
                 networkDevice.c_str());
    if ( initRadioResultDb() )
    {
        ALOG_ERR("radio result db could not be initialized, aborting");
        return;
    }

    const char* countryCode;
    stringstream sCountry;
    sCountry << country;
    if ( CountryCodeTable::getIsoAlpha2CountryCodeFromMCC(sCountry.str().c_str(), &countryCode) )
    {
        ALOG_WARN_CV("could not find iso country code from mcc (%i)", country);
    }
    auto radioStation = GnServiceRadioStation(frequency, band, countryCode);

    auto req = new GnRequestRadioStationData(this,
                                             m_gnUser.get(),
                                             m_radioResultDb.get(),
                                             radioStation,
                                             posData);
    req->setNetworkInterface(networkDevice);
    req->execute();
}

void GnService::setMcc(uint32_t countryCode)
{
    USE_CHANNEL();
    ALOG_DEBUG("mcc not used anymore, country of sale is used instead");
}

void GnService::sendSongRecognitionStatusEvent(const GnServiceSongRecognitionStatus status) const
{
    USE_CHANNEL();
    ALOG_INFO_CV("sending status %i", status);
    sendJavaEvent("onRecognitionStatus", status);
}

void GnService::sendClearResultEvent() const
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    sendJavaEvent("onRecognitionClearResult");
    ALOG_DEBUG("EXIT");
}

void GnService::sendRadioStationEvent(const GnServiceRadioStation &station,
        const std::vector<unsigned char> &imageData) const
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    const GnServiceJavaContext *pctx = m_gnJavaContext.get();
    JavaVM* javaVM = pctx->javaVM;
    JNIEnv* env;

    jint res = javaVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if ( res != JNI_OK )
    {
        res = javaVM->AttachCurrentThread(&env, nullptr);
        if ( res != JNI_OK )
        {
            ALOG_ERR_CV("failed to AttachCurrentThread, error=%d", res);
            return;
        }
    }

    jclass jRadioStationInfoCls = getJavaClass("technology/cariad/entertainment/metadataservice/data/provider/gracenote/RadioStationInfo");
    jobject jRadioStationInfo = env->AllocObject(jRadioStationInfoCls);

    jfieldID stationNameId = env->GetFieldID(jRadioStationInfoCls, "Name", "Ljava/lang/String;");
    jstring javaStationName = env->NewStringUTF(station.getName().c_str());
    env->SetObjectField(jRadioStationInfo, stationNameId, javaStationName);

    stringstream sFreq;
    sFreq << station.getFrequency();
    jfieldID frequencyId = env->GetFieldID(jRadioStationInfoCls, "Frequency", "Ljava/lang/String;");
    jstring javaFrequency = env->NewStringUTF((sFreq.str().c_str()));
    env->SetObjectField(jRadioStationInfo, frequencyId, javaFrequency);

    jfieldID bandId = env->GetFieldID(jRadioStationInfoCls, "Band", "Ljava/lang/String;");
    jstring javaBand = env->NewStringUTF(station.getBand().c_str());
    env->SetObjectField(jRadioStationInfo, bandId, javaBand);

    jfieldID gnIdentifierId = env->GetFieldID(jRadioStationInfoCls, "StationId", "Ljava/lang/String;");
    jstring javaGnId = env->NewStringUTF(station.getGracenoteId().c_str());
    env->SetObjectField(jRadioStationInfo, gnIdentifierId, javaGnId);

    jbyteArray array = env->NewByteArray(imageData.size());
    env->SetByteArrayRegion(array, 0, imageData.size(), reinterpret_cast<const jbyte*>(imageData.data()));

    jmethodID callbackId = env->GetMethodID(pctx->mainActivityCls,
                                            "onRadioStationDataReceived",
                                            "(Ltechnology/cariad/entertainment/metadataservice/data/provider/gracenote/RadioStationInfo;[B)V");
    env->CallVoidMethod(pctx->mainActivityObj, callbackId, jRadioStationInfo, array);

    env->DeleteLocalRef(javaStationName);
    env->DeleteLocalRef(javaFrequency);
    env->DeleteLocalRef(javaBand);
    env->DeleteLocalRef(javaGnId);
    env->DeleteLocalRef(array);
    env->DeleteLocalRef(jRadioStationInfo);
}

void GnService::sendLyricsDataEvent(const uint32_t playPosition,
                                    const std::vector<unsigned char> &lyricsData,
                                    const jobject callback) const
{
    USE_CHANNEL();
    JNIEnv* env = getJniEnv();
    if ( env == nullptr )
    {
        ALOG_WARN("could not get java env pointer");
        return;
    }

    jclass implCls = env->GetObjectClass(callback);
    jboolean flag = env->ExceptionCheck();
    if ( flag )
    {
        env->ExceptionClear();
        ALOG_ERR("could not find java class for callback");
        return;
    }

    jmethodID callbackId = env->GetMethodID( implCls,
                                             "onLyricsReceived",
                                             "(ILjava/lang/String;)V");
    flag = env->ExceptionCheck();
    if ( flag )
    {
        env->ExceptionClear();
        ALOG_ERR("could not find java method");
        return;
    }

    string lyricsString = { lyricsData.begin(), lyricsData.end() };
    jstring javaString = env->NewStringUTF(lyricsString.c_str());
    jint jRequestId = playPosition;

    env->CallVoidMethod(callback, callbackId, jRequestId, javaString);
    env->DeleteLocalRef(javaString);
}

void GnService::sendJavaEvent(const string &funcName) const
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("ENTER, '%s'", funcName.c_str());
    GnServiceJavaContext *pctx = m_gnJavaContext.get();
    JavaVM * javaVM = pctx->javaVM;
    JNIEnv *env;

    jint res = javaVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if ( res != JNI_OK )
    {
        res = javaVM->AttachCurrentThread(&env, nullptr);
        if ( res != JNI_OK )
        {
            ALOG_ERR_CV("failed to AttachCurrentThread, error=%d", res);
            return;
        }
    }

    jmethodID callbackId = env->GetMethodID(pctx->mainActivityCls,
                                            funcName.c_str(),
                                            "()V");
    env->CallVoidMethod(pctx->mainActivityObj, callbackId);
}

void GnService::sendJavaEvent(const string &funcName, const GnServiceSongRecognitionStatus status) const
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("ENTER, '%s', status=%i", funcName.c_str(), status);
    GnServiceJavaContext *pctx = m_gnJavaContext.get();
    JavaVM* javaVM = pctx->javaVM;
    JNIEnv* env;

    jint res = javaVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if ( res != JNI_OK )
    {
        res = javaVM->AttachCurrentThread(&env, nullptr);
        if ( res != JNI_OK )
        {
            ALOG_ERR_CV("failed to AttachCurrentThread, error=%d", res);
            return;
        }
    }

    jmethodID callbackId = env->GetMethodID(pctx->mainActivityCls,
                                            funcName.c_str(),
                                            "(I)V");
    jint param = static_cast<jint>(status);
    env->CallVoidMethod(pctx->mainActivityObj, callbackId, param);
}

void GnService::startSongRecognition(const int32_t audioFd,
                                     const uint32_t sampleRate,
                                     const uint32_t bitRate,
                                     const uint32_t channels)
{
    USE_CHANNEL();

    if ( !preconditionsFulfilled() )
    {
        return;
    }

    std::string networkDevice;

    std::unique_lock<mutex> apnConfigTableLock(m_apnTableMtx);
    if ( m_apnConfigTable.empty()
         || m_apnConfigTable.count(GnServiceRequestNr::REQUEST_SONGRECOGNITION) == 0 )
    {
        ALOG_INFO_CV("no net device set for %s, cannot execute request",
                     getRequestName(GnServiceRequestNr::REQUEST_SONGRECOGNITION).c_str() );
        return;
    }
    else
    {
        networkDevice = m_apnConfigTable.at(GnServiceRequestNr::REQUEST_SONGRECOGNITION);

        if ( networkDevice.empty() )
        {
            ALOG_INFO_CV("the net device string is empty for %s, cannot execute request",
                         getRequestName(GnServiceRequestNr::REQUEST_SONGRECOGNITION).c_str() );
            return;
        }
    }

    apnConfigTableLock.unlock();
    GnServiceAudioFormat audioFormat{sampleRate, bitRate, channels};

    if (m_songRecogJobId != 0)
    {
        ALOG_WARN_CV("a song recognition request %lu was running, stopping", m_songRecogJobId);
        stopSongRecognition();
    }

    ALOG_INFO("starting song recognition");
    auto reqPtr = new GnRequestSongRecognition(this,
                                               m_gnUser.get(),
                                               m_resultDb.get(),
                                               m_filePath);
    reqPtr->setAudioFormat(audioFormat);
    reqPtr->setAudioFileDescriptor(audioFd);
    reqPtr->setNetworkInterface(networkDevice);
    m_songRecogJobId = reqPtr->getJobId();
    reqPtr->setCountryCode(m_iso3CountryCode.c_str());
    reqPtr->execute();
}

void GnService::stopSongRecognition()
{
    USE_CHANNEL();
    ALOG_INFO("stopping song recognition");
    if (m_songRecogJobId != 0)
    {
        GnRequest::abort(m_songRecogJobId);
        m_songRecogJobId = 0;
    }
    else
    {
        ALOG_WARN("no song recognition request is currently running, nothing to stop");
        sendSongRecognitionStatusEvent(GnServiceSongRecognitionStatus::STATUS_STOPPED);
    }
    m_lyricsActivated = false;
}

void GnService::setApnConfig(std::map<GnServiceRequestNr, std::string> config)
{
    USE_CHANNEL();
    ALOG_INFO("setting APN config table for requests");
    lock_guard<mutex> apnConfigTableLock(m_apnTableMtx);
    m_apnConfigTable = std::move(config);

    auto it = m_apnConfigTable.begin();

    while ( it != m_apnConfigTable.end() )
    {
        ALOG_INFO_CV("   %s is using net interface '%s'", getRequestName(it->first).c_str(), it->second.c_str());
        it++;
    }
}

std::string GnService::getRequestName(GnServiceRequestNr request)
{
    switch (request)
    {
        case GnServiceRequestNr::REQUEST_COVERART:
            return {"REQUEST_COVERART"};
            break;
        case GnServiceRequestNr::REQUEST_SONGRECOGNITION:
            return {"REQUEST_SONGRECOGNITION"};
            break;
        case GnServiceRequestNr::REQUEST_RADIOSTATION:
            return {"REQUEST_RADIOSTATION"};
            break;
        case GnServiceRequestNr::REQUEST_LYRICS:
            return {"REQUEST_LYRICS"};
            break;
        default:
            break;
    }
}

void GnService::initializeJniContext(JavaVM* vm, JNIEnv *env, jobject instance, jobject classLoader, jmethodID methodId)
{
    USE_CHANNEL();
    if ( !isJniContextInitialized() )
    {
        m_gnJavaContext->javaVM = vm;

        ALOG_DEBUG("initializing java context");
        jclass clz = env->GetObjectClass(instance);
        m_gnJavaContext->mainActivityCls = reinterpret_cast<jclass>(env->NewGlobalRef(clz));
        m_gnJavaContext->mainActivityObj = env->NewGlobalRef(instance);
        m_gnJavaContext->classLoader = classLoader;
        m_gnJavaContext->findClassMethod = methodId;
        m_gnJavaContext->initialized = 1;

        ALOG_DEBUG_CV("mainActivityCls = %p, mainActivityObj = %p",
                       m_gnJavaContext->mainActivityCls,
                       m_gnJavaContext->mainActivityObj);

        m_jniContextInitialized = true;
    }
}

bool GnService::isInitialized() const
{
    return m_initialized;
}

bool GnService::isJniContextInitialized() const
{
    return m_jniContextInitialized;
}

bool GnService::isEsoTracingInitialized() const
{
    return m_esoTracingInitiazed;
}

bool GnService::configureTracingEnvVariables(const std::string& path)
{
    USE_CHANNEL();
    std::string configPath (path + "/config");
    std::string key ("IPL_CONFIG_DIR");
    if (setEnvVaribale(key, configPath) != 0)
    {
        ALOG_ERR_CV("setenv(%s,%s) failed.", key.c_str(), configPath.c_str());
        return false;
    }

    std::string tracingJsonPath(path + "/tracing.json");
    key = "IPL_CONFIG_FILE_TRACING";
    if (setEnvVaribale(key, tracingJsonPath) != 0) {
        ALOG_ERR_CV("setenv(%s,%s) failed.", key.c_str(), tracingJsonPath.c_str());
        return false;
    }

    std::string verboseValue("1");
    key = "IPL_CONFIG_VERBOSE";
    if (setEnvVaribale(key, verboseValue) != 0) {
        ALOG_ERR_CV("setenv(%s,%s) failed.", key.c_str(), verboseValue.c_str());
        return false;
    }

    return true;
}

bool GnService::isCNProxyNeeded() const
{
    if ( m_countryOfSale == kCountryToUseChinaProxy )
    {
        return true;
    }
    return false;
}

int GnService::setEnvVaribale(const std::string& key, const std::string& value, const bool overwrite /* = true */)
{
    int err = setenv(key.c_str(), value.c_str(), overwrite /*=overwrite_existing*/);
    return err;
}

void GnService::logGnsdkErrorToIRC(const gnsdk_uint16_t packageId, const gnsdk_uint32_t errorCode, const gnsdk_cstr_t message)
{
    std::ostringstream environmentData;
    environmentData << std::hex << " - gnsdk: 0x" << packageId << ", 0x" << errorCode << std::dec;
#ifdef GN_INTEGRATE_FRAMEWORK
    TBIRC::mmx::MetadataService::GnSDKError(message, environmentData.str());
#endif
}

void GnService::onCoverArtEvent_dataReceived(const GnServiceAlbumInfo &albumInfo,
                                             const std::vector<unsigned char>& imageData,
                                             const uint64_t jobId)
{
    USE_CHANNEL();
    ALOG_INFO_CV("result received, img size=%lu, jobId=%lu", imageData.size(), jobId);

    try
    {
        lock_guard<recursive_mutex> callbackMutex(m_callbackTableMtx);
        jobject callback = m_callbackTable.at(jobId);

        sendCoverArtCallback(albumInfo, imageData, callback);

        deleteCallbackForJob(jobId);
    }
    catch (const std::out_of_range& e)
    {
        ALOG_ERR_CV("no callback available for this jobId, ignoring (error='%s')", e.what());
    }
}

void GnService::deleteCallbackForJob(uint64_t jobId)
{
    lock_guard<recursive_mutex> callbackMutex(m_callbackTableMtx);
    auto it = m_callbackTable.begin();
    while ( it != m_callbackTable.end() )
    {
        if ( it->first == jobId )
        {
            m_callbackTable.erase(it);
            break;
        }
        it++;
    }
}

void GnService::addCallbackForJob(uint64_t jobId, jobject callback)
{
    USE_CHANNEL();
    const GnServiceJavaContext *pctx = m_gnJavaContext.get();
    JavaVM* javaVm = pctx->javaVM;
    JNIEnv *env;

    jint res = javaVm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if ( res != JNI_OK )
    {
        res = javaVm->AttachCurrentThread(&env, nullptr);
        if ( res != JNI_OK )
        {
            ALOG_ERR_CV("failed to AttachCurrrentThread, error=%d", res);
            return;
        }
    }

    lock_guard<recursive_mutex> callbackMutex(m_callbackTableMtx);
    jobject callbackRef = env->NewGlobalRef(callback);
    m_callbackTable.insert(std::make_pair(jobId, callbackRef));
}
/**
 * event handler from IGnSongRecognitionEventListener
 */
void GnService::onRecognitionEvent_generalStatus(const uint64_t jobId,
                                                 const GnServiceSongRecognitionStatus &status)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("jobId-%lu: general status received, status=%i", jobId, status);
    bool sendStatusUpdate = true;
    if ( status != GnServiceSongRecognitionStatus::STATUS_RUNNING )
    {
        sendStatusUpdate = cleanupRecognitionRequest(jobId);
        if ( m_lyricsActivated )
        {
            GnRequestLyrics::abort(m_reqLyrics->getJobId());
            m_lyricsActivated = false;
        }
    }

    if (sendStatusUpdate)
    {
        sendSongRecognitionStatusEvent(status);
    }
    else
    {
        ALOG_WARN_CV("Ignore request %lu status %i, request %lu is already running", jobId, status, m_songRecogJobId);
    }
}

void GnService::onRecognitionEvent_clearResult(const uint64_t jobId)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("jobId-%lu: clear result received", jobId );
    m_curPlayingTrack = GNSDK_NULL;
    sendClearResultEvent();

    if ( m_lyricsActivated )
    {
        const vector<unsigned char> lyrics{};
        onLyricsEvent_dataReceived(lyrics, m_lyricsJobId);
    }
}

void GnService::onRecognitionEvent_dataReceived(const uint64_t jobId,
                                                const GnServiceTrackInfo& trackInfo,
                                                const unsigned char* imageData,
                                                const uint32_t length) const
{
    USE_CHANNEL();
    if ( jobId != m_songRecogJobId )
    {
        ALOG_INFO_CV("jobId-%lu: result from aborted request received, discarding", jobId);
        return;
    }
    ALOG_DEBUG_CV("jobId-%lu: recognition data received, forwarding to app", jobId);
    sendRecognitionDataEvent(trackInfo, imageData, length);
}

void GnService::onRecognitionEvent_currentlyPlayingTrack(const GnTrack &track)
{
    USE_CHANNEL();
    m_curPlayingTrack = track;
    ALOG_INFO_CV("currently recognized track: '%s', gnid='%s'",
                 m_curPlayingTrack.Title().Display(),
                 m_curPlayingTrack.GnId());

    if ( m_lyricsPending )
    {
        m_reqLyrics->setCurrentTrack(track);
        m_reqLyrics->execute();
        m_lyricsPending = false;
        return;
    }

    if ( m_lyricsActivated )
    {
        m_reqLyrics->setCurrentTrack(track);
    }
}

void GnService::onRecognitionEvent_identifierStatus(const uint64_t jobId,
                                                    const std::string &status) const
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("jobId-%lu: identifier status='%s'", jobId, status.c_str());
}

void GnService::onRecognitionEvent_matchStatus(const uint64_t jobId,
                                               const std::string &status) const
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("jobId-%lu: match status='%s'", jobId, status.c_str());
}

void GnService::onRadioStationEvent_status(const GnServiceRadioStationStatus status) const
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("status=%i", status);
}

void GnService::onRadioStationEvent_dataReceived(const GnServiceRadioStation &radioStation,
                                                 const vector<unsigned char>& imageData) const
{
    USE_CHANNEL();
    ALOG_INFO_CV("station name:'%s', id:'%s",
                 radioStation.getName().c_str(), radioStation.getGracenoteId().c_str());

    sendRadioStationEvent(radioStation, const_cast<vector<unsigned char>&>(imageData));
}

void GnService::onLyricsEvent_dataReceived(const std::vector<unsigned char> &lyricsData,
                                           const uint64_t jobId)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("jobId=%lu", jobId);
    try
    {
        jobject callback = m_callbackTable.at(m_lyricsJobId);
        uint32_t playPosition = m_curPlayingTrack.IsNull() ? 0 : m_curPlayingTrack.CurrentPosition();
        sendLyricsDataEvent(playPosition, lyricsData, callback);
    }
    catch (const std::out_of_range& e)
    {
        ALOG_ERR("no callback available for this job, ignoring");
    }
}

void GnService::onLyricsEvent_finished(const uint64_t jobId)
{
    USE_CHANNEL();
    cleanupLyricsRequest(jobId);
    ALOG_INFO_CV("lyrics request is finished, jobId=%lu", jobId);
}

void GnService::SystemMemoryWarning(gnsdk_size_t curMemSize, gnsdk_size_t memoryWarnSize)
{
    USE_CHANNEL();
    ALOG_ERR_CV("Memory warning received. Current Heap size usage: %ld, memory warning size set to: %ld",
                 curMemSize, memoryWarnSize);
}

void GnService::LocaleUpdateNeeded(GnLocale &locale)
{
    USE_CHANNEL();
    ALOG_WARN("the locale needs to be updated");
    // TODO(miir1367): perform locale update
}

void GnService::ListUpdateNeeded(GnList &list)
{
    USE_CHANNEL();
    ALOG_WARN("a list update is needed");
    // TODO(miir1367): perform list update
}

bool GnService::LogMessage(gnsdk_uint16_t packageId, gracenote::GnLogMessageType messageType,
                           gnsdk_uint32_t errorCode, gnsdk_cstr_t message)
{
    USE_CHANNEL();
    switch (messageType) {
        case gracenote::kLoggingMessageTypeError:
        {
            logGnsdkErrorToIRC(packageId, errorCode, message);
            ALOG_ERR_CV("gnsdk: 0x%x, 0x%x - %s", packageId, errorCode, message);
            break;
        }
        case gracenote::kLoggingMessageTypeWarning:
            ALOG_WARN_CV("gnsdk: 0x%x, 0x%x - %s", packageId, errorCode, message);
            break;
        case gracenote::kLoggingMessageTypeInfo:
            ALOG_INFO_CV("gnsdk: 0x%x, 0x%x - %s", packageId, errorCode, message);
            break;
        case gracenote::kLoggingMessageTypeDebug:
            ALOG_DEBUG_CV("gnsdk: 0x%x, 0x%x - %s", packageId, errorCode, message);
            break;
        default:
            break;
    }
    return true;
}

jclass GnService::getJavaClass(const char *name) const
{
    USE_CHANNEL();
    const GnServiceJavaContext *pctx = m_gnJavaContext.get();
    JavaVM* javaVM = pctx->javaVM;
    JNIEnv* env;

    jint res = javaVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if ( res != JNI_OK )
    {
        res = javaVM->AttachCurrentThread(&env, nullptr);
        if ( res != JNI_OK )
        {
            ALOG_ERR_CV("failed to AttachCurrentThread, error=%d", res);
        }
    }
    ALOG_DEBUG_CV("trying to get java class for %s", name);
    auto javaClass = reinterpret_cast<jclass>(env->CallObjectMethod(pctx->classLoader,
                                                                    pctx->findClassMethod,
                                                                    env->NewStringUTF(name)));
    jboolean flag = env->ExceptionCheck();
    if ( flag )
    {
        env->ExceptionClear();
        ALOG_ERR_CV("could not find java class %s", name);
    }
    return javaClass;
}

bool GnService::cleanupRecognitionRequest(const uint64_t jobId)
{
    deleteCallbackForJob(jobId);
    if (m_songRecogJobId == jobId || m_songRecogJobId == 0)
    {
        // Reset only if current jobID is same as the one being stopped or 0
        m_songRecogJobId = 0;
        return true;

    }

    return false;
}

void GnService::cleanupLyricsRequest(const uint64_t jobId)
{
    deleteCallbackForJob(jobId);
}

void GnService::sendRecognitionDataEvent(const GnServiceTrackInfo &trackInfo,
                                         const unsigned char *data,
                                         const uint32_t length) const
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");

    const GnServiceJavaContext *pctx = m_gnJavaContext.get();
    JavaVM* javaVM = pctx->javaVM;
    JNIEnv* env;

    jint res = javaVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if ( res != JNI_OK )
    {
        res = javaVM->AttachCurrentThread(&env, nullptr);
        if ( res != JNI_OK )
        {
            ALOG_ERR_CV("failed to AttachCurrentThread, error=%d", res);
            return;
        }
    }

    jclass jTrackInfoCls = getJavaClass("technology/cariad/entertainment/metadataservice/data/provider/gracenote/TrackInfo");
    jmethodID constr = env->GetMethodID(jTrackInfoCls, "<init>", "()V");
    jobject jTrackInfo = env->NewObject(jTrackInfoCls, constr);
    jboolean flag = env->ExceptionCheck();
    if (flag)
    {
        env->ExceptionClear();
        ALOG_ERR("could not call constructor for TrackInfo");
        return;
    }

    jfieldID trackNameId = env->GetFieldID(jTrackInfoCls, "trackName", "Ljava/lang/String;");
    jstring jTrackName = env->NewStringUTF(trackInfo.getTrackName().c_str());
    env->SetObjectField(jTrackInfo, trackNameId, jTrackName);

    jfieldID artistId = env->GetFieldID(jTrackInfoCls, "artist", "Ljava/lang/String;");
    jstring jArtist = env->NewStringUTF(trackInfo.getArtistName().c_str());
    env->SetObjectField(jTrackInfo, artistId, jArtist);

    jfieldID albumId = env->GetFieldID(jTrackInfoCls, "album", "Ljava/lang/String;");
    jstring jAlbum = env->NewStringUTF(trackInfo.getAlbumName().c_str());
    env->SetObjectField(jTrackInfo, albumId, jAlbum);

    jfieldID genreId = env->GetFieldID(jTrackInfoCls, "genre", "Ljava/lang/String;");
    jstring jGenre = env->NewStringUTF(trackInfo.getAlbumGenre().c_str());
    env->SetObjectField(jTrackInfo, genreId, jGenre);

    jfieldID yearId = env->GetFieldID(jTrackInfoCls, "year", "Ljava/lang/String;");
    jstring jYear = env->NewStringUTF(trackInfo.getAlbumYear().c_str());
    env->SetObjectField(jTrackInfo, yearId, jYear);

    jfieldID gnIdId = env->GetFieldID(jTrackInfoCls, "gnId", "Ljava/lang/String;");
    jstring jGnId = env->NewStringUTF(trackInfo.getGnId().c_str());
    env->SetObjectField(jTrackInfo, gnIdId, jGnId);

    jfieldID gnUIdId = env->GetFieldID(jTrackInfoCls, "gnUId", "Ljava/lang/String;");
    jstring jGnUId = env->NewStringUTF(trackInfo.getGnUId().c_str());
    env->SetObjectField(jTrackInfo, gnUIdId, jGnUId);

    jfieldID lengthId = env->GetFieldID(jTrackInfoCls, "length", "I");
    jint jLength = trackInfo.getTrackLength();
    env->SetIntField(jTrackInfo, lengthId, jLength);

    jfieldID playPosId = env->GetFieldID(jTrackInfoCls, "playposition", "I");
    jint jPlaypos = trackInfo.getPlayPosition();
    env->SetIntField(jTrackInfo, playPosId, jPlaypos);

    jfieldID moodId = env->GetFieldID(jTrackInfoCls, "mood", "Ljava/lang/String;");
    jstring jMood = env->NewStringUTF(trackInfo.getMood().c_str());
    env->SetObjectField(jTrackInfo, moodId, jMood);

    jfieldID tempoId = env->GetFieldID(jTrackInfoCls, "tempo", "Ljava/lang/String;");
    jstring jTempo = env->NewStringUTF(trackInfo.getTempo().c_str());
    env->SetObjectField(jTrackInfo, tempoId, jTempo);

    jclass jExternalIdCls = getJavaClass("technology/cariad/entertainment/metadataservice/data/provider/gracenote/ExternalId");
    flag = env->ExceptionCheck();
    if ( flag )
    {
        env->ExceptionClear();
        ALOG_ERR("could not find java class ExternalID");
        return;
    }
    jmethodID addMethodId = env->GetMethodID(jTrackInfoCls,
                                             "addExternalId",
                                             "(Ltechnology/cariad/entertainment/metadataservice/data/provider/gracenote/ExternalId;)V");

    for ( auto &it : trackInfo.getExternalIdList() )
    {
        jobject jExternalId = env->AllocObject(jExternalIdCls);
        jfieldID sourceId = env->GetFieldID(jExternalIdCls, "source", "Ljava/lang/String;");
        jfieldID valueId = env->GetFieldID(jExternalIdCls, "value", "Ljava/lang/String;");
        jstring jSource = env->NewStringUTF(it.getSource().c_str());
        jstring jValue = env->NewStringUTF(it.getValue().c_str());
        env->SetObjectField(jExternalId, sourceId, jSource);
        env->SetObjectField(jExternalId, valueId, jValue);

        env->CallVoidMethod(jTrackInfo, addMethodId, jExternalId);
        flag = env->ExceptionCheck();
        if ( flag )
        {
            env->ExceptionClear();
            ALOG_ERR("could not add ExternalId to TrackInfo");
        }
        env->DeleteLocalRef(jSource);
        env->DeleteLocalRef(jValue);
        env->DeleteLocalRef(jExternalId);
    }

    jclass jExternalAlbumIdCls = getJavaClass("technology/cariad/entertainment/metadataservice/data/provider/gracenote/ExternalId");
    flag = env->ExceptionCheck();
    if ( flag )
    {
        env->ExceptionClear();
        ALOG_ERR("could not find java class ExternalId");
        return;
    }
    jmethodID addAlbumMethodId = env->GetMethodID(jTrackInfoCls,
                                                  "addExternalAlbumId",
                                                  "(Ltechnology/cariad/entertainment/metadataservice/data/provider/gracenote/ExternalId;)V");
    for ( auto &it : trackInfo.getExternalAlbumIdList() )
    {
        jobject jExternalAlbumId = env->AllocObject(jExternalAlbumIdCls);
        jfieldID sourceId = env->GetFieldID(jExternalAlbumIdCls, "source", "Ljava/lang/String;");
        jfieldID valueId = env->GetFieldID(jExternalAlbumIdCls, "value", "Ljava/lang/String;");
        jstring jSource = env->NewStringUTF(it.getSource().c_str());
        jstring jValue = env->NewStringUTF(it.getValue().c_str());
        env->SetObjectField(jExternalAlbumId, sourceId, jSource);
        env->SetObjectField(jExternalAlbumId, valueId, jValue);

        env->CallVoidMethod(jTrackInfo, addAlbumMethodId, jExternalAlbumId);
        flag = env->ExceptionCheck();
        if ( flag )
        {
            env->ExceptionClear();
            ALOG_ERR("could not add ExternalAlbumId to TrackInfo");
        }
        env->DeleteLocalRef(jSource);
        env->DeleteLocalRef(jValue);
        env->DeleteLocalRef(jExternalAlbumId);
    }

    jmethodID callbackId = env->GetMethodID(pctx->mainActivityCls,
                                            "onNewRecognitionDataReceived",
                                            "(Ltechnology/cariad/entertainment/metadataservice/data/provider/gracenote/TrackInfo;[B)V");
    jbyteArray array = env->NewByteArray(length);
    env->SetByteArrayRegion(array, 0, length, reinterpret_cast<const jbyte*>(data));

    env->CallVoidMethod(pctx->mainActivityObj, callbackId, jTrackInfo, array);
    env->DeleteLocalRef(jTrackInfo);
    env->DeleteLocalRef(array);

    ALOG_DEBUG("EXIT");
}

void GnService::sendCoverArtCallback(const GnServiceAlbumInfo &albumInfo,
                                     const std::vector<unsigned char> &imageData,
                                     const jobject callback)
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    const GnServiceJavaContext *pctx = m_gnJavaContext.get();
    JavaVM* javaVM = pctx->javaVM;
    JNIEnv* env;

    jint res = javaVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if ( res != JNI_OK )
    {
        res = javaVM->AttachCurrentThread(&env, nullptr);
        if ( res != JNI_OK )
        {
            ALOG_ERR_CV("failed to AttachCurrentThread, error=%d", res);
            return;
        }
    }

    jclass implCls = env->GetObjectClass(callback);
    jboolean flag = env->ExceptionCheck();
    if ( flag )
    {
        env->ExceptionClear();
        ALOG_ERR("could not find java class for callback");
        return;
    }

    jmethodID callbackId = env->GetMethodID(implCls,
                                            "callback",
                                            "(Ltechnology/cariad/entertainment/metadataservice/data/provider/gracenote/AlbumInfo;[B)V");
    flag = env->ExceptionCheck();
    if ( flag )
    {
        env->ExceptionClear();
        ALOG_ERR("could not find java method for callback");
        return;
    }

    jclass jAlbumInfoCls = getJavaClass("technology/cariad/entertainment/metadataservice/data/provider/gracenote/AlbumInfo");
    jobject jAlbumInfo = env->AllocObject(jAlbumInfoCls);

    jfieldID nameId = env->GetFieldID(jAlbumInfoCls, "name", "Ljava/lang/String;");
    jstring javaName = env->NewStringUTF(albumInfo.Name.c_str());
    env->SetObjectField(jAlbumInfo, nameId, javaName);

    jfieldID artistId = env->GetFieldID(jAlbumInfoCls, "artist", "Ljava/lang/String;");
    jstring javaArtist = env->NewStringUTF(albumInfo.Artist.c_str());
    env->SetObjectField(jAlbumInfo, artistId, javaArtist);

    jfieldID genreId = env->GetFieldID(jAlbumInfoCls, "genre", "Ljava/lang/String;");
    jstring javaGenre = env->NewStringUTF(albumInfo.Genre.c_str());
    env->SetObjectField(jAlbumInfo, genreId, javaGenre);

    jfieldID yearId = env->GetFieldID(jAlbumInfoCls, "year", "Ljava/lang/String;");
    jstring javaYear = env->NewStringUTF(albumInfo.Year.c_str());
    env->SetObjectField(jAlbumInfo, yearId, javaYear);

    jbyteArray array = env->NewByteArray(imageData.size());
    env->SetByteArrayRegion(array, 0, imageData.size(), reinterpret_cast<const jbyte*>(imageData.data()));

    env->CallVoidMethod(callback, callbackId, jAlbumInfo, array);
    env->DeleteLocalRef(jAlbumInfo);
    env->DeleteLocalRef(array);

    ALOG_DEBUG("EXIT");
}

JNIEnv* GnService::getJniEnv() const
{
    USE_CHANNEL();
    const GnServiceJavaContext *pctx = m_gnJavaContext.get();
    JavaVM* javaVM = pctx->javaVM;
    JNIEnv* env;

    jint res = javaVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if ( res != JNI_OK )
    {
        res = javaVM->AttachCurrentThread(&env, nullptr);
        if ( res != JNI_OK )
        {
            ALOG_ERR_CV("failed to AttachCurrentThread, error=%d", res);
            return nullptr;
        }
    }

    return env;
}

void GnService::setCountryOfSale(const string& country)
{
    USE_CHANNEL();
    ALOG_INFO_CV("setting country of sale to '%s'", country.c_str());
    m_countryOfSale = country;

    /* get country of sale and covert to lower case in order to retrieve iso-alpha-2 country code
     * from our mapping table */
    const char* iso3CountryCode;
    string iso2CountryCode = m_countryOfSale;
    std::transform( iso2CountryCode.begin(),
                    iso2CountryCode.end(),
                    iso2CountryCode.begin(),
                    [](unsigned char c){return std::tolower(c);});
    if ( CountryCodeTable::getIsoAlpha3CountryCodeFromIsoAlpha2(iso2CountryCode.c_str(), &iso3CountryCode) != 0)
    {
        ALOG_WARN_CV("no valid country code found for country of sale '%s'", m_countryOfSale.c_str());
    }
    m_iso3CountryCode = iso3CountryCode;
}

void GnService::activateCNProxy()
{
    USE_CHANNEL();
    ALOG_INFO_CV("proxy needs to be set, using '%s'", kChinaProxyUrl);

    if ( m_gnUser != nullptr )
    {
        try {
            m_gnUser->Options().Custom("gnsdk_useroption_proxy_host_with_domain_override",
                                       kChinaProxyUrl);
        }
        catch (GnError &e)
        {
            ALOG_WARN_CV("could not set proxy: %s 0x%x : %s", e.ErrorAPI(), e.ErrorCode(),
                         e.ErrorDescription());
        }
    }
}


void GnService::setupGnUser()
{
    USE_CHANNEL();
    GnLookupMode  lookupMode         = gracenote::kLookupModeOnlineNoCache;
    gnsdk_cstr_t  applicationVersion = "1.0.0.0";
    auto* userStore = new GnUserStore(m_filePath);

    ALOG_DEBUG("trying to create gn user");
    try {
        /* Get GnUser instance to allow us to perform queries */
        m_gnUser = make_unique<GnUser>(clientId, clientIdTag, applicationVersion, userStore);
        ALOG_DEBUG("user created sucessfully");

        /* set user to match our desired lookup mode (all queries done with this user will inherit
         * the lookup mode) */
        auto isUserLocalOnly = m_gnUser->IsLocalOnly();

#ifdef GN_INTEGRATE_FRAMEWORK
        TBIRC::mmx::MetadataService::GnOnlineUserStatus(isUserLocalOnly ?
                                                        TBIRC::mmx::MetadataService::EGnOnlineUserStatus::GnOnlineUserStatusFailed
                                                                        :
                                                        TBIRC::mmx::MetadataService::EGnOnlineUserStatus::GnOnlineUserStatusRegistered);
#endif

        ALOG_INFO_CV("user is %sallowed for online requests, setting lookup mode accordingly",
                     isUserLocalOnly ? "not " : "");
        m_gnUser->Options().LookupMode(lookupMode);
        m_gnUser->Options().Custom("gnsdk_useropt_tls_session_no_ticket", GNSDK_VALUE_TRUE);

        if ( isCNProxyNeeded() )
        {
            activateCNProxy();
        }
    }
    catch(GnError& e)
    {
        ALOG_ERR_CV("could not create gn user properly: %s 0x%x : %s", e.ErrorAPI(), e.ErrorCode(),
                    e.ErrorDescription() );
    }

    ALOG_DEBUG("trying to store serialized user\n");
    userStore->StoreSerializedUser(clientId, m_gnUser->Serialize());

}

bool GnService::preconditionsFulfilled()
{
    USE_CHANNEL();
    if ( strcmp(gnLicenseString, "") == 0 )
    {
        ALOG_WARN("no license given for this variant, cannot proceed");
        return false;
    }
    if ( m_countryOfSale.empty() )
    {
        ALOG_WARN("no country of sale given, cannot proceed");
        return false;
    }
    return true;
}

void GnService::useFallbackCertFileRetryLocale()
{
    USE_CHANNEL();
    // delete the ota cert file
    string certPath = m_filePath + kCertificateOtaFilePath;
    ALOG_INFO_CV("deleting OTA certificate file '%s'", certPath.c_str());
    if ( remove(certPath.c_str()) )
    {
        ALOG_WARN("cannot remove OTA certificate file");
    }
    // set path to fallback certificate file
    certPath = m_filePath + kCertificateFilePath;

    try
    {
        GnUserOptions().OnlineEncryption().ClearTLSCertificates();
        ALOG_INFO_CV("using fallback certificate file in '%s'", certPath.c_str());
        GnUserOptions().OnlineEncryption().AddTLSCertificates(certPath.c_str());
    }
    catch (GnError& e)
    {
        ALOG_ERR_CV("could not set up encryption for gnsdk: %s 0x%x : %s",
                    e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
    }

    setupGnUser();

    retryLocale();
}

void GnService::retryLocale()
{
    USE_CHANNEL();
    gnServiceLocale gnServiceLocaleVal;
    if ( GnServiceLocaleTable::getGnServiceLocaleFromSystemLanguage(m_systemLang, &gnServiceLocaleVal) )
    {
        ALOG_WARN_CV("could not find language region mapping for gnsdk from system language (%s), using default '%s'",
                     m_systemLang.c_str(),
                     gnServiceLocaleTable[0].systemLocale.c_str());
        gnServiceLocaleVal = gnServiceLocaleTable[0];
    }
    try
    {
        auto *pStatusEventHandler = new GnServiceStatusEventHandler( const_cast<gnsdk_str_t>("Retry Locale"));
        // set locale with desired Group, Language, Region and Descriptor
        GnLocale locale(gracenote::kLocaleGroupMusic,
                        gnServiceLocaleVal.gnLang,
                        gnServiceLocaleVal.gnRegion,
                        gracenote::kDescriptorSimplified,
                        *m_gnUser,
                        pStatusEventHandler);
        // set this locale as default for the duration of gnsdk
        locale.SetGroupDefault();

        ALOG_INFO("Locale set successfully");
    }
    catch (GnError &e)
    {
        ALOG_ERR_CV("error retrying locale, %s 0x%x: %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription() );
    }
}
