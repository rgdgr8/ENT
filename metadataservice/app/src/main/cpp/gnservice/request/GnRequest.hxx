// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 31.05.20.
//

#ifndef GNSERVICE_REQUEST_GNREQUEST_HXX_
#define GNSERVICE_REQUEST_GNREQUEST_HXX_

#include <string>
#include <memory>
#include <gnsdk.hpp>

#include "GnRequestExecutor.hxx"
#include "../resultdb/GnLocalLookupDb.hxx"

class GnRequest
{
public:
    GnRequest(gracenote::GnUser* user, gnservice::GnLocalLookupDB* resultDb);
    virtual ~GnRequest();

    // start worker thread of request
    virtual void execute();
    virtual void finished();

    // stop the request
    virtual void cancel();

    static void abort(uint64_t jobID);

    // set the network interface used for this request
    virtual void setNetworkInterface(const std::string& interface);
    void setRequestName(const std::string& name);
    std::string getRequestName();
    uint64_t getJobId() const;

    virtual void executeTask() = 0;

    void setRunning(const bool isRunning);
    bool isRunning() const;
    bool isStarted() const;

private:
    static std::unique_ptr<GnRequestExecutor> s_executorPtr;
    static uint64_t s_requestJobIdCtr;

protected:
    gracenote::GnUser* m_gnUser;
    gnservice::GnLocalLookupDB* m_resultDb;
    std::string m_requestName;
    uint64_t m_jobId;

    bool m_cancelFlag;
    bool m_runningFlag;
};


#endif //GNSERVICE_REQUEST_GNREQUEST_HXX_
