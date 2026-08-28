// Copyright 2019 <e.solutions GmbH>//
// Created by armo7765 on 20.02.23.
//

#ifndef METADATASERVICE_GNSERVICEESOTRACING_HXX
#define METADATASERVICE_GNSERVICEESOTRACING_HXX

#include <common/tracing/tracing.hxx>

#include "GnServiceIrcTracing.hxx"

/*================================================================================*\
 * DEFINES
 *================================================================================*/
#define __CHANNEL_BASE__ metadataservice_gnservice_

/*================================================================================*\
 * MACROS
 *================================================================================*/
// Declare Channel macro
#define DECLARE_CHANNEL_CONCAT(X, Y) IPL_TRACE_DECLARE_CHANNEL(X##Y)
#define DECLARE_CHANNEL_PASTER(X, Y) DECLARE_CHANNEL_CONCAT(X, Y)
#define DECLARE_CHANNEL() DECLARE_CHANNEL_PASTER(__CHANNEL_BASE__, __CHANNEL__EXT__)

// Declare Channel extern macro
#define DECLARE_CHANNEL_CONCAT_EXTERN(X, Y) IPL_TRACE_DECLARE_CHANNEL_EXTERN(X##Y)
#define DECLARE_CHANNEL_PASTER_EXTERN(X, Y) DECLARE_CHANNEL_CONCAT_EXTERN(X, Y)
#define DECLARE_CHANNEL_EXTERN() DECLARE_CHANNEL_PASTER_EXTERN(__CHANNEL_BASE__, __CHANNEL__EXT__)

// Use Channel macro
#define USE_CHANNEL_CONCAT(X, Y) IPL_TRACE_USE_CHANNEL(X##Y)
#define USE_CHANNEL_PASTER(X, Y) USE_CHANNEL_CONCAT(X, Y)
#define USE_CHANNEL() USE_CHANNEL_PASTER(__CHANNEL_BASE__, __CHANNEL__EXT__)

#define ALOG_DEBUG(param)\
    IPL_TRACE_DEBUG_CV("[" LOG_TAG "] %s : " param, __func__)

#define ALOG_DEBUG_CV(param, ...)\
    IPL_TRACE_DEBUG_CV("[" LOG_TAG "] %s : " param, __func__, __VA_ARGS__)

#define ALOG_INFO(param)\
    IPL_TRACE_INFO_CV("[" LOG_TAG "] %s : " param, __func__)

#define ALOG_INFO_CV(param, ...)\
    IPL_TRACE_INFO_CV("[" LOG_TAG "] %s : " param, __func__, __VA_ARGS__)

#define ALOG_WARN(param)\
    IPL_TRACE_WARN_CV("[" LOG_TAG "] %s : " param, __func__)

#define ALOG_WARN_CV(param, ...)\
    IPL_TRACE_WARN_CV("[" LOG_TAG "] %s : " param, __func__, __VA_ARGS__)

#define ALOG_ERR(param)\
    IPL_TRACE_ERR_CV("[" LOG_TAG "] %s : " param, __func__)

#define ALOG_ERR_CV(param, ...)\
    IPL_TRACE_ERR_CV("[" LOG_TAG "] %s : " param, __func__, __VA_ARGS__)

#endif //METADATASERVICE_GNSERVICEESOTRACING_HXX
