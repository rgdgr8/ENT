// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 31.05.20.
//

#include <thread> // NOLINT [build/c++11], google style guide sees <thread> as an unapproved c++11 header
#include "../helper/GnServiceLogging.hxx"
#include "../resultdb/GnLocalLookupDb.hxx"

#include "GnRequest.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ Request
DECLARE_CHANNEL();
#define LOG_TAG "GnRequest"

using std::string;
using std::make_unique;
using gnservice::GnLocalLookupDB;
using gracenote::GnUser;
using gracenote::GnError;

std::unique_ptr<GnRequestExecutor> GnRequest::s_executorPtr = nullptr;
uint64_t GnRequest::s_requestJobIdCtr = 0;

GnRequest::GnRequest(GnUser* pUser, GnLocalLookupDB* pResultDb) :
    m_gnUser(nullptr),
    m_resultDb(nullptr),
    m_cancelFlag(false),
    m_runningFlag(false)
{
    if ( pUser != nullptr )
    {
        m_gnUser = pUser;
    }

    if ( pResultDb != nullptr )
    {
        m_resultDb = pResultDb;
    }

    if ( s_executorPtr == nullptr)
    {
        s_executorPtr = make_unique<GnRequestExecutor>();
    }

    m_jobId = ++s_requestJobIdCtr;
}

GnRequest::~GnRequest()
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("%s - jobID=%lu ", getRequestName().c_str(), getJobId());
}

uint64_t GnRequest::getJobId() const
{
    return m_jobId;
}

void GnRequest::finished()
{
    USE_CHANNEL();
    ALOG_INFO_CV("request %s finished thread execution, jobId=%lu",
                 getRequestName().c_str(), getJobId());
    s_executorPtr->removeRequestFromQueue(getJobId());
}

void GnRequest::execute()
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");

    if ( m_gnUser == nullptr )
    {
        // request was not setup with a gn user, we cannot continue
        // no requests possible
        ALOG_WARN("no gracenote user is available for this request");
        return;
    }

    if ( m_resultDb == nullptr )
    {
        // request was not setup with a result db, no caching
        // will be possible
        ALOG_WARN("no gracenote result db was setup, no caching");
    }

    if ( s_executorPtr != nullptr )
    {
        s_executorPtr->addRequest(this);
    }
}

void GnRequest::cancel()
{
    USE_CHANNEL();
    ALOG_INFO("cancelling flag is set");
    if ( isRunning() )
    {
        m_cancelFlag = true;
        m_runningFlag = false;
    }
}

void GnRequest::abort(uint64_t jobId)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("aborting request with jobID=%lu", jobId);

    if ( s_executorPtr != nullptr )
    {
        s_executorPtr->abortRequest(jobId);
    }
}

void GnRequest::setRunning(const bool isRunning)
{
    if (isRunning == true && m_runningFlag != true && m_cancelFlag != true)
    {
        m_runningFlag = true;
    }
    else if (isRunning == false)
    {
        m_runningFlag = isRunning;
    }
}

bool GnRequest::isRunning() const
{
    return m_runningFlag;
}

bool GnRequest::isStarted() const
{
    return m_runningFlag || m_cancelFlag;
}

void GnRequest::setNetworkInterface(const std::string& interface)
{
    USE_CHANNEL();
    if (m_gnUser == nullptr)
    {
        ALOG_ERR("no gracenote user is available, cannot set network interface for this request");
        return;
    }

    if (!interface.empty())
    {
        try
        {
            m_gnUser->Options().NetworkInterface(interface.c_str());
            ALOG_INFO_CV("setting network interface as user option for this request to '%s'",
                         interface.c_str());
        }
        catch (GnError &e)
        {
            ALOG_WARN_CV("could not set network interface for request: %s 0x%x : %s",
                         e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
        }
    }
}

void GnRequest::setRequestName(const string& name)
{
    m_requestName = name;
}

string GnRequest::getRequestName()
{
    return m_requestName;
}
