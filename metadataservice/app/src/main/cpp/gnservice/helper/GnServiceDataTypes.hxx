// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 31.05.20.
//

#ifndef GNSERVICE_HELPER_GNSERVICEDATATYPES_HXX_
#define GNSERVICE_HELPER_GNSERVICEDATATYPES_HXX_

#include <string>
#include <vector>

struct GnServiceAlbumInfo
{
    std::string Track;
    std::string Name;
    std::string Artist;
    std::string Genre;
    std::string Year;
};

struct GnServicePositionData
{
    std::string Latitude;
    std::string Longitude;
};

namespace gnservice
{
class GnServiceTypes
{
    public:
    class GnExternalId
    {
    public:
        GnExternalId();
        ~GnExternalId();

        void init();

        void setSource(const std::string& source)
        {
            m_sourceIdentifier = source;
        }
        const std::string& getSource() const
        {
            return m_sourceIdentifier;
        }
        void setExternalId(const std::string& id)
        {
            m_externalId = id;
        }
        const std::string& getExternalId() const
        {
            return m_externalId;
        }
    private:
        std::string m_sourceIdentifier;
        std::string m_externalId;
    };

    //---------------------------------------------------
    class GnServiceTrackInfo
    {
    public:
        GnServiceTrackInfo();
        ~GnServiceTrackInfo();

        void init();

        void setTrackName(const std::string& name)
        {
            m_trackName = name;
        }
        const std::string &getTrackName() const
        {
            return m_trackName;
        }
        void setAlbumName(const std::string& name)
        {
            m_albumName = name;
        }
        const std::string &getAlbumName() const
        {
            return m_albumName;
        }
        void setArtistName(const std::string& name)
        {
            m_artistName = name;
        }
        const std::string &getArtistName() const
        {
            return m_artistName;
        }
        void setAlbumGenre(const std::string& name)
        {
            m_albumGenre = name;
        }
        const std::string &getAlbumGenre() const
        {
            return m_albumGenre;
        }
        void setAlbumYear(const std::string& year)
        {
            m_albumYear = year;
        }
        const std::string &getAlbumYear() const
        {
            return m_albumYear;
        }
        void setTrackLength(const int32_t length)
        {
            m_trackLength = length;
        }
        int32_t getTrackLength() const
        {
            return m_trackLength;
        }
        void setTrackPlayPosition(const int32_t playPos)
        {
            m_playPosition = playPos;
        }
        int32_t getTrackPlayPosition() const
        {
            return m_playPosition;
        }
        void setExternalIdList(const std::vector<GnExternalId>& extIdList)
        {
            m_extIdList = extIdList;
        }
        const std::vector<GnExternalId>& getExternalIdList() const
        {
            return m_extIdList;
        }
    private:
        std::string m_trackName;
        std::string m_albumName;
        std::string m_artistName;
        std::string m_albumGenre;
        std::string m_albumYear;
        int32_t m_trackLength;
        int32_t m_playPosition;
        std::vector<GnExternalId> m_extIdList;
    };
};
}//namespace gnservice

enum class GnServiceStatus
{
    STATUS_UNKNOWN,
    STATUS_STARTED,
    STATUS_STOPPED,
    STATUS_ERROR
};

enum class GnServiceSongRecognitionStatus
{
    STATUS_UNKNOWN,
    STATUS_STOPPED,
    STATUS_RUNNING,
    STATUS_ERROR
};

enum class GnServiceRadioStationStatus
{
    STATUS_UNKNOWN,
    STATUS_ERROR,
    STATUS_OK
};

enum class GnServiceRequestNr
{
    REQUEST_COVERART,
    REQUEST_SONGRECOGNITION,
    REQUEST_RADIOSTATION,
    REQUEST_LYRICS,
};

enum class GnServiceLyricsType
{
    LYRICS_STATIC = 0,
    LYRICS_LINEBYLINE,
    LYRICS_WORDBYWORD
};

#endif //GNSERVICE_HELPER_GNSERVICEDATATYPES_HXX_
