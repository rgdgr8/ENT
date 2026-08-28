// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 11.05.2021.
//

#ifndef GNSERVICE_HELPER_GNSERVICEEXTERNALID_HXX_
#define GNSERVICE_HELPER_GNSERVICEEXTERNALID_HXX_

#include <string>

class GnServiceExternalId
{
public:
    GnServiceExternalId(const std::string& source, const std::string& value);
    ~GnServiceExternalId();

    void init();

    void setSource(const std::string& source)
    {
        m_source = source;
    }

    const std::string& getSource() const
    {
        return m_source;
    }

    void setValue(const std::string& value)
    {
        m_value = value;
    }

    const std::string& getValue() const
    {
        return m_value;
    }

private:
    std::string m_source;
    std::string m_value;
};


#endif //GNSERVICE_HELPER_GNSERVICEEXTERNALID_HXX_
