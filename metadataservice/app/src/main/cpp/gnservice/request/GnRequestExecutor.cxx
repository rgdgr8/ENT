// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 19.08.2021.
//

#include <mutex>
#include <thread> // NOLINT [build/c++11], google style guide sees <thread> as an unapproved c++11 header
#include <utility>

#include "GnRequest.hxx"
#include "GnRequestExecutor.hxx"

#include "../helper/GnServiceLogging.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ Request
DECLARE_CHANNEL_EXTERN();
#define LOG_TAG "GnRequestExecutor"

using std::make_pair;
using std::thread;
using std::recursive_mutex;
using std::lock_guard;

static uint32_t kMaxNumberOfActiveThreads = 10;
static recursive_mutex requestMutex;

GnRequestExecutor::GnRequestExecutor() :
    m_requestMap(),
    m_numberOfActiveThreads(0)
{

}

void GnRequestExecutor::addRequest(GnRequest *requestPtr)
{
    if ( requestPtr != nullptr )
    {
        USE_CHANNEL();
        lock_guard<recursive_mutex> lock(requestMutex);
        m_requestMap.insert(make_pair(requestPtr->getJobId(), requestPtr));
        ALOG_DEBUG_CV("adding request to queue: %s, jobID=%lu, number of running requests:%u, "
                      "number of current requests in queue: %lu",
                      requestPtr->getRequestName().c_str(),
                      requestPtr->getJobId(),
                      m_numberOfActiveThreads,
                      m_requestMap.size());
    }
    executeNextRequest();
}

void GnRequestExecutor::abortRequest(const uint64_t jobId)
{
    lock_guard<recursive_mutex> lock(requestMutex);
    auto it = m_requestMap.find(jobId);

    if ( it != m_requestMap.end() )
    {
        auto reqPtr = it->second;
        if ( reqPtr != nullptr)
        {
            USE_CHANNEL();
            reqPtr->cancel();
            ALOG_DEBUG_CV("aborting request %s, jobID=%lu",
                          reqPtr->getRequestName().c_str(), reqPtr->getJobId());
        }
    }
}

void GnRequestExecutor::removeRequestFromQueue(const uint64_t jobId)
{
    lock_guard<recursive_mutex> lock(requestMutex);
    auto it = m_requestMap.find(jobId);

    if ( it != m_requestMap.end() )
    {
        USE_CHANNEL();
        auto reqPtr = it->second;
        reqPtr->setRunning(false);
        m_requestMap.erase(it);
        if (m_numberOfActiveThreads > 0)
        {
            m_numberOfActiveThreads--;
        }
        ALOG_DEBUG_CV("removing request %s, jobID=%lu from request queue, "
                      "number of running requests: %u "
                      "number of remaining requests in queue: %lu",
                      reqPtr->getRequestName().c_str(),
                      reqPtr->getJobId(),
                      m_numberOfActiveThreads,
                      m_requestMap.size());
        delete reqPtr;
    }

    executeNextRequest();
}

void GnRequestExecutor::abortAll()
{
    lock_guard<recursive_mutex> lock(requestMutex);
    auto it = m_requestMap.begin();

    while ( it != m_requestMap.end() )
    {
        auto reqPtr = it->second;
        reqPtr->setRunning(false);
        it = m_requestMap.erase(it);
        delete reqPtr;
    }
    m_numberOfActiveThreads = 0;
}

void GnRequestExecutor::executeNextRequest()
{
    USE_CHANNEL();
    lock_guard<recursive_mutex> lock(requestMutex);
    auto it = m_requestMap.begin();

    while ( it != m_requestMap.end() )
    {
        auto reqPtr = it->second;
        if ( reqPtr != nullptr && !reqPtr->isStarted() )
        {
            if ( m_numberOfActiveThreads < kMaxNumberOfActiveThreads )
            {
                m_numberOfActiveThreads++;
                reqPtr->setRunning(true);
                thread th(&GnRequest::executeTask, reqPtr);
                th.detach();
            }
            else
            {
                ALOG_WARN_CV("number of running request threads (%u) has reached maximum value (%u), "
                              "number of remaining requests in queue: %lu",
                              m_numberOfActiveThreads,
                              kMaxNumberOfActiveThreads,
                              m_requestMap.size());
                break;
            }
        }
        it++;
    }
}
