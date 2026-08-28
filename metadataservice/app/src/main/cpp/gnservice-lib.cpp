// Copyright 2020 <e.solutions GmbH>
#include <jni.h>
#include <memory>
#include <string>
#include <thread> // NOLINT [build/c++11], google style guide sees <thread> as an unapproved c++11 header
#include <map>
#include "gnservice/helper/GnServiceLogging.hxx"
#include "gnservice/resultdb/GnLocalLookupDb.hxx"
#include "gnservice/helper/GnsdkLogEvents.hxx"
#include "./gnservice/GnService.hxx"

#include "./gnservice-lib.h"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ JNI
DECLARE_CHANNEL();
#define LOG_TAG "JNI_Gnservice"

GnJavaContext gnJavaContext;

std::unique_ptr<GnService> gnService = nullptr;

extern "C"
{
JNIEXPORT
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
#ifndef GN_INTEGRATE_FRAMEWORK
    // Eso tracing can not be initialized yet at this point
    ALOG_INFO("JNI_OnLoad was called");
#endif
    JNIEnv *env;

    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // initialize gnJavaContext struct
    memset(&gnJavaContext, 0, sizeof(gnJavaContext));
    gnJavaContext.javaVM = vm;

    // in order to have access to java classes from native world we need to store the
    // java class loader in the java context object
    auto TrackInfoClass = env->FindClass("technology/cariad/entertainment/metadataservice/data/provider/gracenote/TrackInfo");
    auto trackInfo = env->GetObjectClass(TrackInfoClass);
    auto classLoaderClass = env->FindClass("java/lang/ClassLoader");
    auto getClassLoaderMethod = env->GetMethodID(trackInfo,
                                                 "getClassLoader",
                                                 "()Ljava/lang/ClassLoader;");

    gnJavaContext.javaClassLoader = env->NewGlobalRef(env->CallObjectMethod(TrackInfoClass, getClassLoaderMethod));
    gnJavaContext.javaFindClassMethod = env->GetMethodID(classLoaderClass,
                                                         "findClass",
                                                         "(Ljava/lang/String;)Ljava/lang/Class;");
    gnJavaContext.initialized = 0;

    gnService = std::make_unique<GnService>();

    // silence clang-tidy complaint
    (void) reserved;
    return JNI_VERSION_1_6;
}

JNIEXPORT jstring JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_getGnsdkVersionInfo(
        JNIEnv *env,
        jobject /*this*/,
        jstring dir
        )
{
    const char *temp = env->GetStringUTFChars(dir, nullptr);
    std::string logPath(temp);

    std::string version = gnService->getGnsdkVersionInfo();
    return env->NewStringUTF(version.c_str());
}

JNIEXPORT void JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_initializeGnService(
        JNIEnv* env,
        jobject instance,
        jstring dir,
        jstring lang
        )
{
    std::string logPath(env->GetStringUTFChars(dir, nullptr));
    std::string language(env->GetStringUTFChars(lang, nullptr));

    JavaVM* vm = gnJavaContext.javaVM;
    jobject classLoader = gnJavaContext.javaClassLoader;
    jmethodID methodId = gnJavaContext.javaFindClassMethod;

#ifdef GN_INTEGRATE_FRAMEWORK
    gnService->initEsoTracing(logPath);
#endif // GN_INTEGRATE_FRAMEWORK
    gnService->initializeJniContext(vm, env, instance, classLoader, methodId);
    gnService->init(logPath, language);
}

JNIEXPORT void JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_doTextLookupForAlbum(
        JNIEnv *env,
        jobject instance/*this*/,
        jstring jtitle,
        jstring jartist,
        jstring jalbum,
        jstring dir,
        jobject callback
        )
{
    USE_CHANNEL();
    const char *temp = env->GetStringUTFChars(dir, nullptr);
    std::string logPath(temp);
    std::string Title;
    std::string Artist;
    std::string Album;

    ALOG_DEBUG_CV("env= %p, instance=%p", env, instance);
    JavaVM *vm = gnJavaContext.javaVM;
    gnService->initializeJniContext(vm, env, instance, gnJavaContext.javaClassLoader,
                                    gnJavaContext.javaFindClassMethod);
    gnService->init(logPath);

    Title = env->GetStringUTFChars(jtitle, nullptr);
    Artist = env->GetStringUTFChars(jartist, nullptr);
    Album = env->GetStringUTFChars(jalbum, nullptr);

    gnService->requestCoverArtFromMetadata(callback, Title, Artist, Album);
}

JNIEXPORT void JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_startSongRecognition(
        JNIEnv *env,
        jobject instance,
        jstring dir,
        int32_t fileDescriptor,
        jint sampleRate,
        jint bitRate,
        jint channels
        )
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    const char *temp = env->GetStringUTFChars(dir, nullptr);
    std::string path(temp);

    JavaVM *vm = gnJavaContext.javaVM;
    gnService->initializeJniContext(vm, env, instance, gnJavaContext.javaClassLoader,
                                    gnJavaContext.javaFindClassMethod);
    gnService->init(path);

    gnService->startSongRecognition(fileDescriptor, sampleRate, bitRate, channels);

    ALOG_DEBUG("EXIT");
}

JNIEXPORT void JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_stopSongRecognition(
        JNIEnv *env,
        jobject instance,
        jstring dir
        )
{
    const char *temp = env->GetStringUTFChars(dir, nullptr);
    std::string path(temp);

    JavaVM *vm = gnJavaContext.javaVM;
    gnService->initializeJniContext(vm, env, instance, gnJavaContext.javaClassLoader,
                                    gnJavaContext.javaFindClassMethod);
    gnService->init(path);
    gnService->stopSongRecognition();
}

JNIEXPORT void JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_requestLyrics(
        JNIEnv *env,
        jobject instance,
        jint jType,
        jboolean jFamilyFriendly,
        jobject callback
)
{
    if ( gnService != nullptr )
    {
        auto type = static_cast<GnServiceLyricsType>(jType);
        gnService->requestLyrics(callback, type, jFamilyFriendly);
    }
}

JNIEXPORT void JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_setMcc(
        JNIEnv* env,
        jobject instance,
        jint mcc
        )
{
    JavaVM *vm = gnJavaContext.javaVM;
    jobject classLoader = gnJavaContext.javaClassLoader;
    jmethodID methodId = gnJavaContext.javaFindClassMethod;

    if ( gnService != nullptr )
    {
        gnService->initializeJniContext(vm, env, instance, classLoader, methodId);
        gnService->setMcc(mcc);
    }

}

extern "C" JNIEXPORT void JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_setApnConfig(
        JNIEnv *env,
        jobject instance,
        jobject config
)
{
    USE_CHANNEL();
    JavaVM *vm = gnJavaContext.javaVM;
    gnService->initializeJniContext(vm, env, instance, gnJavaContext.javaClassLoader,
                                    gnJavaContext.javaFindClassMethod);

    jclass clsConfig = env->GetObjectClass(config);
    jmethodID idEntrySet = env->GetMethodID(clsConfig, "entrySet", "()Ljava/util/Set;");

    jclass clsEntrySet = env->FindClass("java/util/Set");
    jmethodID idIterator = env->GetMethodID(clsEntrySet, "iterator", "()Ljava/util/Iterator;");

    jclass clsIterator = env->FindClass("java/util/Iterator");
    jmethodID idHasNext = env->GetMethodID(clsIterator, "hasNext", "()Z");
    jmethodID idNext = env->GetMethodID(clsIterator, "next", "()Ljava/lang/Object;");

    jclass clsEntry = env->FindClass("java/util/Map$Entry");
    jmethodID idGetKey = env->GetMethodID(clsEntry, "getKey", "()Ljava/lang/Object;");
    jmethodID idGetValue = env->GetMethodID(clsEntry, "getValue", "()Ljava/lang/Object;");

    jclass clsString = env->FindClass("java/lang/String");
    jmethodID idToString = env->GetMethodID(clsString, "toString", "()Ljava/lang/String;");

    jobject objEntrySet = env->CallObjectMethod(config, idEntrySet);
    jobject objIterator = env->CallObjectMethod(objEntrySet, idIterator);

    bool hasNext = env->CallBooleanMethod(objIterator, idHasNext);

    std::map<GnServiceRequestNr, std::string> apnConfig;

    while (hasNext)
    {
        jobject entry = env->CallObjectMethod(objIterator, idNext);

        jobject key = env->CallObjectMethod(entry, idGetKey);
        jobject value = env->CallObjectMethod(entry, idGetValue);

        jclass clsKey = env->GetObjectClass(key);
        jmethodID idKeyGetValue = env->GetMethodID(clsKey, "getValue", "()I");

        auto jintKey = static_cast<GnServiceRequestNr>(env->CallIntMethod(key, idKeyGetValue));
        auto jstrValue = reinterpret_cast<jstring>(env->CallObjectMethod(value, idToString));
        const char *strValue = env->GetStringUTFChars(jstrValue, 0);

        ALOG_DEBUG_CV("request %i requires network device: %s", jintKey, strValue);
        apnConfig.insert(std::make_pair(jintKey, strValue));

        hasNext = env->CallBooleanMethod(objIterator, idHasNext);
    }

    gnService->setApnConfig(apnConfig);
}

extern "C" JNIEXPORT void JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_requestRadioStationData(
        JNIEnv *env,
        jobject instance,
        jstring dir,
        jobject positionData,
        jobject stationInfo,
        jint radioCountry
)
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    const char *temp = env->GetStringUTFChars(dir, nullptr);
    std::string path(temp);

    JavaVM *vm = gnJavaContext.javaVM;
    gnService->initializeJniContext(vm, env, instance, gnJavaContext.javaClassLoader,
                                    gnJavaContext.javaFindClassMethod);
    gnService->init(path);

    // get position data from java object PositionData
    jclass posDataCls = env->GetObjectClass(positionData);
    jfieldID latId = env->GetFieldID(posDataCls, "latitude", "Ljava/lang/String;");
    jfieldID longId = env->GetFieldID(posDataCls, "longitude", "Ljava/lang/String;");
    auto jLat = reinterpret_cast<jstring>(env->GetObjectField(positionData, latId));
    auto jLong = reinterpret_cast<jstring>(env->GetObjectField(positionData, longId));
    GnServicePositionData posData;
    posData.Latitude = env->GetStringUTFChars(jLat, nullptr);
    posData.Longitude = env->GetStringUTFChars(jLong, nullptr);

    jclass stationInfoCls = env->GetObjectClass(stationInfo);
    jfieldID freqId = env->GetFieldID(stationInfoCls, "Frequency", "Ljava/lang/String;");
    jfieldID bandId = env->GetFieldID(stationInfoCls, "Band", "Ljava/lang/String;");
    auto jFreq = reinterpret_cast<jstring>(env->GetObjectField(stationInfo, freqId));
    auto jBand = reinterpret_cast<jstring>(env->GetObjectField(stationInfo, bandId));
    std::string strFrequency = env->GetStringUTFChars(jFreq, nullptr);
    std::string band = env->GetStringUTFChars(jBand, nullptr);

    uint32_t frequency = std::stoi(strFrequency);
    uint32_t country = radioCountry;

    gnService->requestRadioStationMetadata(frequency, band, country, posData);
}

JNIEXPORT void JNICALL
Java_de_esolutions_gracenoteservice_gntest_MainActivity_setCountryOfSale(
        JNIEnv *env,
        jobject instance,
        jstring jcountry
)
{
    JavaVM *vm = gnJavaContext.javaVM;
    jobject classLoader = gnJavaContext.javaClassLoader;
    jmethodID methodId = gnJavaContext.javaFindClassMethod;
    std::string Country = env->GetStringUTFChars(jcountry, nullptr);

    if (gnService != nullptr) {
        gnService->initializeJniContext(vm, env, instance, classLoader, methodId);
        gnService->setCountryOfSale(Country);
    }
}
}
