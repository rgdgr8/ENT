package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.domain.ICoverArtRepository
import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity

/**
 * Use case for retrieving cover art data.
 */
class TextBasedCoverArtLookup(private val coverArtRepository: ICoverArtRepository) {

    /**
     * Provides cover art data.
     * @param coverArt Information based on which cover art data should be retrieved
     * @return text based uri of the cover art if found, otherwise null
     */
    suspend fun coverArtImageByInfo(coverArt: CoverArtRequestDataEntity): String? {
        val haveArtist = !coverArt.artist.isNullOrEmpty()
        val haveAlbum = !coverArt.album.isNullOrEmpty()
        val haveTrack = !coverArt.title.isNullOrEmpty()

        if((haveArtist && haveAlbum) || (haveArtist && haveTrack) || haveAlbum) {
            return coverArtRepository.retrieveCoverArtImage(coverArt)
        }
        return null
    }
}
