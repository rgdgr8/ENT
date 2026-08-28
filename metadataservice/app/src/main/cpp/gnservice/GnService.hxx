// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 29.11.19.
//

#ifndef GNSERVICE_GNSERVICE_HXX_
#define GNSERVICE_GNSERVICE_HXX_

#include <map>
#include <string>
#include <vector>
#include <memory>

#ifdef GN_INTEGRATE_FRAMEWORK
#include <common/error/Error.hxx>
#include <osal/osal.hxx>
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Winconsistent-missing-override"
// Supress compiler warnings for eso framework
#include <util/util.hxx>
#pragma GCC diagnostic pop
#endif // GN_INTEGRATE_FRAMEWORK

#include <gnsdk.hpp>
#include "helper/GnServiceDataTypes.hxx"
#include "helper/GnServiceTrackInfo.hxx"
#include "helper/GnServiceRadioStation.hxx"
#include "event/IGnCoverArtEventListener.hxx"
#include "event/IGnSongRecognitionEventListener.hxx"
#include "event/IGnRadioStationEventListener.hxx"
#include "event/IGnLyricsEventListener.hxx"

#include "request/GnRequestSongRecognition.hxx"
#include "request/GnRequestLyrics.hxx"
#include "radioresultdb/GnRadioResultDb.hxx"

struct GnServiceJavaContext
{
    JavaVM* javaVM;
    jobject classLoader;
    jmethodID findClassMethod;
    jclass  mainActivityCls;
    jobject mainActivityObj;
    int32_t initialized;
};


class GnService : public IGnCoverArtEventListener,
        public IGnSongRecognitionEventListener,
        public IGnRadioStationEventListener,
        public IGnLyricsEventListener,
        public gracenote::IGnSystemEvents,
        public gracenote::IGnLogEvents
{
    public:
        GnService();
        virtual ~GnService();

    /**
     * Initializing esoFw tracing should be done as soon as possible to enable propper logging
     * in eso traceViewer.
     *
     * @param path path to where tracing.json file of gracenoteservice is located.
     */
#ifdef GN_INTEGRATE_FRAMEWORK
    void initEsoTracing(const std::string& path);
#endif // GN_INTEGRATE_FRAMEWORK

    /**
     * initializing the GnService is mandatory before starting any of the requests.
     *
     * @param path file path accessible for GnService to write internal files
     */
    void init(std::string path, std::string lang);
    /* keep old initialization for compatibility reasons */
    void init(std::string path);

    /**
     * get the version of the used gnsdk for auto
     *
     * @return version of gnsdk for auto
     */
    std::string getGnsdkVersionInfo();

    /**
     * request cover art for album. An album name or an artist must be given in
     * order to succeed. Providing a track name is optional.
     *
     * @param callback callback to call with retrieved coverart result
     * @param albumName album name
     * @param artistName album artist
     * @param titleName (optional) name of a track on the requested album
     */
    void requestCoverArtFromMetadata(const jobject& callback,
                                     const std::string &albumName,
                                     const std::string &artistName,
                                     const std::string &titleName = ""
                                     );

    /**
     * request lyrics for currently playing and via song recognition identified track
     * @param callback callback into java/kotlin world to deliver the lyrics result
     * @param requestId id to identify what client need to be presented with result
     * @param type the type of the lyrics (static, line-by-line or word-by-word)
     * @param familyFriendly boolean if family friendly lyrics are requested or not.
     */
    void requestLyrics(const jobject& callback,
                       const GnServiceLyricsType type,
                       bool familyFriendly);

    /**
     * request radio station metadata and image. Input of frequency, frequency band and
     * a country name in ISO ALPHA-2 coding is necessary.
     *
     * @param frequency frequency of the requested radio station
     * @param band      radio frequency band of the requested radio station
     * @param country   country of the requested radio station
     */
    void requestRadioStationMetadata(const uint32_t frequency,
                                     const std::string& band,
                                     const uint32_t country,
                                     const GnServicePositionData& posData);

    /**
     * start song recognition
     *
     * @param audioFd file descriptor where the audio data is read from
     * @param sampleRate sample rate of audio data
     * @param bitRate    bit rate of audio data
     * @params channels  number of audio channels (mono/stereo)
     */
    void startSongRecognition(const int32_t audioFd,
                              const uint32_t sampleRate,
                              const uint32_t bitRate,
                              const uint32_t channels);

    /**
     * stop song recognition
     */
    void stopSongRecognition();

    /**
     * set mobile country code and covert it to iso-alpha-3 country code, which is necessary for
     * lyrics feature to work. Iso country code is necessary in RequestSongRecognition
     * @param countryCode mcc to be used
     */
    void setMcc(uint32_t countryCode);

    /**
     * set country of Sale. For China (CN) a proxy server must be set up
     * for all gracenote requests
     * @param country
     */
    void setCountryOfSale(const std::string& country);

    void setApnConfig(std::map<GnServiceRequestNr, std::string> config);

    void initializeJniContext(JavaVM* vm, JNIEnv* env, jobject instance, jobject classLoader, jmethodID methodID);

    /**
     * event handler
     */
    void onCoverArtEvent_dataReceived(const GnServiceAlbumInfo& albumInfo,
                                      const std::vector<unsigned char>& imageData,
                                      const uint64_t jobId) override;

    /**
     * event handler from IGnSongRecognitionEventListener
     */
    void onRecognitionEvent_generalStatus(const uint64_t jobId,
                                          const GnServiceSongRecognitionStatus& status) override;
    void onRecognitionEvent_identifierStatus(const uint64_t jobId,
                                             const std::string& status) const override;
    void onRecognitionEvent_matchStatus(const uint64_t jobId,
                                        const std::string& status) const override;
    void onRecognitionEvent_clearResult(const uint64_t jobId) override;
    void onRecognitionEvent_dataReceived(const uint64_t jobId,
                                         const GnServiceTrackInfo& trackInfo,
                                         const unsigned char* imageData,
                                         const uint32_t length) const override;
    void onRecognitionEvent_currentlyPlayingTrack(const gracenote::metadata::GnTrack& track) override;

    /**
     * event handler from IGnRadioStationEventHandler
     */
     void onRadioStationEvent_status(const GnServiceRadioStationStatus status) const override;
     void onRadioStationEvent_dataReceived(const GnServiceRadioStation& radioStation,
                                           const std::vector<unsigned char>& imageData)
                                           const override;

     /**
      * event handler from IGnLyricsEventListener
      */
      void onLyricsEvent_dataReceived(const std::vector<unsigned char>& lyricsData,
                                      const uint64_t jobId) override;
      void onLyricsEvent_finished(const uint64_t jobId) override;

    /**
      * event handler from gnsdk system events (IGnSystemEvents)
      */
      void SystemMemoryWarning(gnsdk_size_t curMemSize, gnsdk_size_t memoryWarnSize) override;
      void LocaleUpdateNeeded(gracenote::GnLocale& locale) override;
      void ListUpdateNeeded(gracenote::GnList& list) override;

     /**
      * callback from IGnLogEvents
      */
     bool LogMessage(gnsdk_uint16_t packageId, gracenote::GnLogMessageType messageType,
                     gnsdk_uint32_t errorCode, gnsdk_cstr_t message) override;

    private:
    void loadLocale();

    void enableLogging();

    void initGnsdk();

    void initGnManager();

    void setupGnUser();

    void initResultDb();

    uint32_t initRadioResultDb();

    void enableEncryption();

    static std::string getRequestName(GnServiceRequestNr request);

    void addCallbackForJob(uint64_t jobId, jobject callback);
    void deleteCallbackForJob(uint64_t jobId);
    void activateCNProxy();

    bool preconditionsFulfilled();
    void useFallbackCertFileRetryLocale();
    void retryLocale();


    /**
     * send events to Java app
     */
    void sendClearResultEvent() const;
    void sendRecognitionDataEvent(const GnServiceTrackInfo& trackInfo,
                                  const unsigned char* data,
                                  const uint32_t length) const;
    void sendCoverArtCallback(const GnServiceAlbumInfo& albumInfo,
                              const std::vector<unsigned char>& imageData,
                              const jobject callback);
    void sendSongRecognitionStatusEvent(const GnServiceSongRecognitionStatus status) const;

    void sendRadioStationEvent(const GnServiceRadioStation& station,
                               const std::vector<unsigned char>& imageData) const;

    void sendLyricsDataEvent(const uint32_t playPosition,
                             const std::vector<unsigned char>& lyricsData,
                             const jobject callback) const;

    void sendJavaEvent(const std::string& funcName) const;
    void sendJavaEvent(const std::string& funcName, const GnServiceSongRecognitionStatus status) const;

    jclass getJavaClass(const char* name) const;
    JNIEnv* getJniEnv() const;

    /**
     * internal helper methods
     */
    bool cleanupRecognitionRequest(const uint64_t jobId);
    void cleanupLyricsRequest(const uint64_t jobId);
    bool isInitialized() const;
    bool isCNProxyNeeded() const;

    bool isJniContextInitialized() const;

    bool configureTracingEnvVariables(const std::string& path);

    int setEnvVaribale(const std::string& key, const std::string& value, const bool overwrite = true);

    bool isEsoTracingInitialized() const;

    void logGnsdkErrorToIRC(const gnsdk_uint16_t packageId, const gnsdk_uint32_t errorCode, const gnsdk_cstr_t message);

    private:
    std::unique_ptr<GnServiceJavaContext>       m_gnJavaContext;
    std::string                                 m_filePath;
    std::string                                 m_systemLang;
    std::string                                 m_iso3CountryCode;
    std::string                                 m_countryOfSale;
    std::unique_ptr<gracenote::GnManager>       m_gnManager;
    std::unique_ptr<gracenote::GnUser>          m_gnUser;
    gracenote::metadata::GnTrack                m_curPlayingTrack;
    std::unique_ptr<gnservice::GnLocalLookupDB> m_resultDb;
    std::unique_ptr<GnRadioResultDb>            m_radioResultDb;
    std::unique_ptr<gnservice::GnsdkLogEvents>  m_gnsdkLogEvents;
    std::map<GnServiceRequestNr, std::string>   m_apnConfigTable;
    std::map<uint64_t, jobject>                 m_callbackTable;
    std::recursive_mutex                        m_callbackTableMtx;
    std::mutex                                  m_initMtx;
    std::mutex                                  m_rdbMtx;
    std::mutex                                  m_apnTableMtx;
    GnRequestLyrics*                            m_reqLyrics;
    uint64_t                                    m_songRecogJobId;
    uint64_t                                    m_lyricsJobId;
    bool                                        m_initialized;
    bool                                        m_jniContextInitialized;
    bool                                        m_lyricsActivated;
    bool                                        m_lyricsPending;
    bool                                        m_esoTracingInitiazed;

#ifdef GN_INTEGRATE_FRAMEWORK
    // Framewrok variables that are required to initialize eso tracing
    ipl::ErrorStorage m_errstor;
    osal::Osal m_osal;
    std::unique_ptr<util::Util> m_util;
#endif // GN_INTEGRATE_FRAMEWORK
};

#endif //GNSERVICE_GNSERVICE_HXX_
