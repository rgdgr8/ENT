// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 31.05.2021.
//

#include "GnServiceExternalId.hxx"

using std::string;

GnServiceExternalId::GnServiceExternalId(const string &source, const string &value)
{
    init();

    setSource(source);
    setValue(value);
}

GnServiceExternalId::~GnServiceExternalId()
= default;

void GnServiceExternalId::init()
{
    m_source.clear();
    m_value.clear();
}