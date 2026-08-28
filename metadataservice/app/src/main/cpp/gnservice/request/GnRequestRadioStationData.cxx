// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 21.06.2021.
//
#include <algorithm>
#include <vector>
#include <sstream>
#include "../GnServiceConstants.hxx"
#include "../helper/GnServiceLogging.hxx"

#include "GnRequestRadioStationData.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ Request
DECLARE_CHANNEL_EXTERN();
// In case of android logging tags can only have upt to max. 23 characters
#define LOG_TAG "GnRequestRadioStation"

using std::string;
using std::stringstream;
using std::vector;
using std::recursive_mutex;
using std::lock_guard;

using gracenote::GnError;
using gracenote::GnAssetFetch;
using gracenote::kImageSize300;
using gracenote::metadata::GnResponseRadioStations;
using gracenote::metadata::GnRadioStation;
using gracenote::metadata::GnAsset;
using gracenote::metadata::kBandAm;
using gracenote::metadata::kBandFm;
using gracenote::GnLookupMode::kLookupModeLocal;
using gracenote::GnLookupMode::kLookupModeOnlineNoCache;

static recursive_mutex mutexResultDbUpdate;

GnRequestRadioStationData::GnRequestRadioStationData(IGnRadioStationEventListener *listener,
                                                     gracenote::GnUser *user,
                                                     GnRadioResultDb* resultDb,
                                                     GnServiceRadioStation& radioStation,
                                                     GnServicePositionData positionData)
    : GnRequest(user, nullptr),
    m_listener(listener),
    m_radioStation(radioStation),
    m_positionData(positionData),
    m_radioDb(resultDb),
    m_radioId(*m_gnUser, GNSDK_NULL)
{
    setRequestName(("REQUEST_RADIOSTATION"));
}

GnRequestRadioStationData::~GnRequestRadioStationData() noexcept = default;

void GnRequestRadioStationData::executeTask()
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("job-%lu: ENTER", getJobId());

    setupRadioId();

    checkAndUpdateStations();

    findStationsLocal();

    finished();
    ALOG_DEBUG_CV("job-%lu: EXIT", getJobId());
}

void GnRequestRadioStationData::setupRadioIdOptions()
{
    try
    {
        m_radioId.Options().LookupData(gracenote::kLookupDataContent, true);
        m_radioId.Options().LookupMode(gracenote::kLookupModeOnlineNoCache);
    }
    catch (GnError &e)
    {
        USE_CHANNEL();
        ALOG_ERR_CV("job-%lu: could not set option for radio id: %s, error: 0x%x %s",
                    getJobId(), e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
    }

}

void GnRequestRadioStationData::setupRadioId()
{
    setupRadioIdOptions();
    setBand(m_radioStation.getBand());
    setFrequency(m_radioStation.getFrequency());
    setCountry(m_radioStation.getCountry());
    setPosition();
}

void GnRequestRadioStationData::setFrequency(const uint32_t frequency)
{
    USE_CHANNEL();
    stringstream sstring;

    if ( m_radioStation.getBand() == "FM" || m_radioStation.getBand() == "fm" )
    {
        double const freqMHz = static_cast<double>(frequency) / 1000.0;
        sstring << freqMHz;
    }
    else
    {
        sstring << frequency;
    }
    try
    {
        m_radioId.SetFrequency(sstring.str().c_str());
    }
    catch (GnError &e)
    {
        ALOG_ERR_CV("job-%lu: could not set frequency: %s 0x%x : %s",
                    getJobId(), e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
        return;
    }

    ALOG_INFO_CV("job-%lu: setting frequency to '%s'", getJobId(), sstring.str().c_str());
}

void GnRequestRadioStationData::setBand(const std::string& band)
{
    USE_CHANNEL();
    if ( band == "AM" || band == "am" )
    {
        m_radioId.SetBand(kBandAm);
        ALOG_INFO_CV("job-%lu: setting radio frequency band to 'AM'", getJobId());
    }

    if ( band == "FM" || band == "fm" )
    {
        m_radioId.SetBand(kBandFm);
        ALOG_INFO_CV("job-%lu: setting radio frequency band to 'FM'", getJobId());
    }
}

void GnRequestRadioStationData::setCountry(const std::string& country)
{
    USE_CHANNEL();
    ALOG_INFO_CV("job-%lu: setting country to '%s'", getJobId(), country.c_str());
    try
    {
        m_radioId.SetCountryCodeIsoAlpha2(country.c_str());
    }
    catch (GnError &e)
    {
        ALOG_ERR_CV("job-%lu: could not set country code: %s 0x%x : %s",
                    getJobId(), e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
    }
}

void GnRequestRadioStationData::setPosition()
{
    USE_CHANNEL();
    try
    {
        m_radioId.SetLatitude(m_positionData.Latitude.c_str());
        m_radioId.SetLongitude(m_positionData.Longitude.c_str());
    }
    catch (GnError &e)
    {
        ALOG_ERR_CV("job-%lu: could not set position: %s 0x%x : %s",
                    getJobId(), e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
        return;
    }
    ALOG_INFO_CV("job-%lu: setting position data", getJobId());
}

void GnRequestRadioStationData::checkAndUpdateStations()
{
    if ( m_radioDb == nullptr )
    {
        USE_CHANNEL();
        ALOG_WARN_CV("job-%lu: no result db for radio stations given", getJobId());
        return;
    }

    lock_guard<recursive_mutex> lock(mutexResultDbUpdate);
    m_radioDb->checkAndUpdateStations(m_radioId);
}

uint32_t GnRequestRadioStationData::findStationsLocal()
{
    uint32_t ret = 0;

    m_radioId.Options().LookupMode(kLookupModeLocal);

    try
    {
        GnResponseRadioStations result = m_radioId.FindStations();
        ret = displayResults(result);
    }
    catch(GnError& e)
    {
        USE_CHANNEL();
        ALOG_ERR_CV("job-%lu: %s 0x%x : %s",
                    getJobId(), e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
    }

    return ret;
}

uint32_t GnRequestRadioStationData::displayResults(GnResponseRadioStations &response)
{
    USE_CHANNEL();
    gnsdk_uint32_t stationsCount = response.RadioStations().count();

    if ( stationsCount == 0 )
    {
        ALOG_WARN_CV("job-%lu: no radio station found, count=0", getJobId());
    }
    for ( gnsdk_uint32_t i=0; i < stationsCount; ++i)
    {
        GnRadioStation station = *(response.RadioStations()[i]);
        if ( strcmp(station.Name().Display(), "") != 0 )
        {
            ALOG_INFO_CV("job-%lu: station found, name: %s", getJobId(), station.Name().Display());
            m_radioStation.setName(string(station.Name().Display()));
            m_radioStation.setGracenoteId(station.GracenoteId());
            fetchAndStoreStationLogo(station);
        }
    }

#ifdef GN_INTEGRATE_FRAMEWORK
    TBIRC::mmx::MetadataService::PositioningData(TBIRC::mmx::MetadataService::EPositioningData::PositioningDataNotInUse);
#endif
    return stationsCount;
}

void GnRequestRadioStationData::fetchAndStoreStationLogo(GnRadioStation &station)
{
    USE_CHANNEL();
    GnAsset asset = station.Logo().Asset(kImageSize300);
    vector<unsigned char> imgData(0);

    if ( !asset.IsNull() )
    {
        gnsdk_cstr_t url = asset.UrlGnsdk();

        if ( (url != GNSDK_NULL) )
        {
            if ( strcmp("", url) != 0 )
            {
                ALOG_INFO_CV("job-%lu: trying local query to fetch logo, url='%s'", getJobId(), url);
                GnAssetFetch assetFetch(*m_gnUser, url, kLookupModeLocal);

                if (assetFetch.Data() && assetFetch.Size())
                {
                    ALOG_INFO_CV("job-%lu: found image for station local, type=%s, size=%ld",
                                 getJobId(),
                                 assetFetch.Type(),
                                 assetFetch.Size());
                    imgData.resize(assetFetch.Size());
                    std::copy(assetFetch.Data(),assetFetch.Data()+assetFetch.Size(),
                              imgData.begin());
                } else {
                    ALOG_INFO_CV("job-%lu: no local image found, trying online now ", getJobId());
                    GnAssetFetch assetFetchOnline(*m_gnUser, url, kLookupModeOnlineNoCache);

                    if (assetFetchOnline.Data() && assetFetchOnline.Size()) {
                        ALOG_INFO_CV("job-%lu: found image for station online, type=%s, size=%ld",
                                     getJobId(),
                                     assetFetchOnline.Type(),
                                     assetFetchOnline.Size());
                        imgData.resize(assetFetchOnline.Size(), 0);
                        std::copy(assetFetchOnline.Data(), assetFetchOnline.Data() + assetFetchOnline.Size(),
                                  imgData.begin());
                        if ( m_radioDb != nullptr )
                        {
                            m_radioDb->addImage(assetFetchOnline.Data(),
                                                assetFetchOnline.Size(),
                                                asset);
                        }

                    } else {
                        ALOG_WARN_CV("job-%lu: no image found for station, giving up", getJobId());
                    }
                }
            }
        }
        else
        {
            ALOG_WARN_CV("job-%lu: no or empty asset url given, cannot fetch image", getJobId());
        }
    }

    m_listener->onRadioStationEvent_dataReceived(m_radioStation, imgData);
}
