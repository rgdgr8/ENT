// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 11.05.2021.
//

#ifndef GNSERVICE_HELPER_GNSERVICETRACKINFO_HXX_
#define GNSERVICE_HELPER_GNSERVICETRACKINFO_HXX_

#include <string>
#include <vector>
#include "GnServiceExternalId.hxx"

class GnServiceTrackInfo
{
public:
    GnServiceTrackInfo();
    ~GnServiceTrackInfo();

    void init();

    void setTrackName(const std::string &name);
    const std::string& getTrackName() const;

    void setAlbumName(const std::string &name);
    const std::string& getAlbumName() const;

    void setArtistName(const std::string &name);
    const std::string& getArtistName() const;

    void setAlbumGenre(const std::string &name);
    const std::string& getAlbumGenre() const;

    void setAlbumYear(const std::string &year);
    const std::string& getAlbumYear() const;

    void setMood(const std::string &mood);
    const std::string& getMood() const;

    void setTempo(const std::string &tempo);
    const std::string& getTempo() const;

    void setGnId(const std::string &gnId);
    const std::string& getGnId() const;

    void setGnUId(const std::string &gnUId);
    const std::string& getGnUId() const;

    void setTrackLength(const int32_t length);
    int32_t getTrackLength() const;

    void setPlayPosition(const int32_t playPos);
    int32_t getPlayPosition() const;

    void addExternalId(const GnServiceExternalId& externalId);
    const std::vector<GnServiceExternalId> &getExternalIdList() const;

    void addExternalAlbumId(const GnServiceExternalId& externalId);
    const std::vector<GnServiceExternalId>& getExternalAlbumIdList() const;

private:
    std::string m_trackName;
    std::string m_albumName;
    std::string m_artistName;
    std::string m_albumGenre;
    std::string m_albumYear;
    std::string m_mood;
    std::string m_tempo;
    std::string m_gnId;
    std::string m_gnUId;
    int32_t m_trackLength{};
    int32_t m_playPosition{};
    std::vector<GnServiceExternalId> m_externalIdList;
    std::vector<GnServiceExternalId> m_externalAlbumIdList;
};


#endif //GNSERVICE_HELPER_GNSERVICETRACKINFO_HXX_
