// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 11.08.2021.
//

#ifndef GNSERVICE_REQUEST_GNREQUESTEXECUTOR_HXX_
#define GNSERVICE_REQUEST_GNREQUESTEXECUTOR_HXX_

#include <map>

class GnRequest;

class GnRequestExecutor
{
public :
    GnRequestExecutor();

    bool isBusy() const;

    void addRequest(GnRequest* requestPtr);

    void abortRequest(const uint64_t jobId);

    void removeRequestFromQueue(const uint64_t jobId);

    void executeNextRequest();

    void abortAll();

private:
    std::map<uint64_t,GnRequest*> m_requestMap;
    uint32_t m_numberOfActiveThreads;
};

inline bool GnRequestExecutor::isBusy() const
{
    return 0 != m_requestMap.size();
}


#endif //GNSERVICE_REQUEST_GNREQUESTEXECUTOR_HXX_
