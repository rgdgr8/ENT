// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 01.12.19.
//

#ifndef GNSERVICE_HELPER_GNSERVICELOGGING_HXX_
#define GNSERVICE_HELPER_GNSERVICELOGGING_HXX_

#ifdef GN_INTEGRATE_FRAMEWORK

// All macros for eso tracog are defined within included file
#include "GnServiceEsoTracing.hxx"

#else // GN_INTEGRATE_FRAMEWORK

#include <android/log.h>

#define LOG_MD "\a{\"pf\":\"n\"}"

// These empty macros needed to avoid ifdefs in source files when alog is used
#define DECLARE_CHANNEL()
#define DECLARE_CHANNEL_EXTERN()
#define USE_CHANNEL()

#define ANDROIDLOG_DEBUG(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, LOG_MD " " __VA_ARGS__)
#define ANDROIDLOG_INFO(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, LOG_MD " "  __VA_ARGS__)
#define ANDROIDLOG_WARN(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, LOG_MD " "  __VA_ARGS__)
#define ANDROIDLOG_ERR(...)   __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, LOG_MD " "  __VA_ARGS__)

#define ALOG_DEBUG(...)\
    ANDROIDLOG_DEBUG("%s : %s", __func__, __VA_ARGS__)

#define ALOG_DEBUG_CV(param, ...)\
    ANDROIDLOG_DEBUG("%s : " param, __func__, __VA_ARGS__)

#define ALOG_INFO(...)\
    ANDROIDLOG_INFO("%s : %s", __func__, __VA_ARGS__)

#define ALOG_INFO_CV(param, ...)\
    ANDROIDLOG_INFO("%s : " param, __func__, __VA_ARGS__)

#define ALOG_WARN(...)\
    ANDROIDLOG_WARN("%s : %s", __func__, __VA_ARGS__)

#define ALOG_WARN_CV(param, ...)\
    ANDROIDLOG_WARN("%s : " param, __func__, __VA_ARGS__)

#define ALOG_ERR(...)\
    ANDROIDLOG_ERR("%s : %s", __func__, __VA_ARGS__)

#define ALOG_ERR_CV(param, ...)\
    ANDROIDLOG_ERR("%s : " param, __func__, __VA_ARGS__)

#endif // GN_INTEGRATE_FRAMEWORK

#endif //GNSERVICE_HELPER_GNSERVICELOGGING_HXX_
