package technology.cariad.entertainment.metadataservice.data.database.trigger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.AlbumDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ArtistDao
import technology.cariad.entertainment.metadataservice.data.database.dao.CoverArtLookupDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.dao.LifeCycleDataDao
import technology.cariad.entertainment.metadataservice.data.database.entity.AlbumEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ArtistEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.CoverArtLookupEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoverArtLookupTriggerAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var coverArtLookupDao: CoverArtLookupDao
    private lateinit var artistDao: ArtistDao
    private lateinit var albumDao: AlbumDao
    private lateinit var imageDao: ImageDao
    private lateinit var lifeCycleDataDao: LifeCycleDataDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        coverArtLookupDao = database.getCoverArtLookupDao()
        artistDao = database.getArtistDao()
        albumDao = database.getAlbumDao()
        imageDao = database.getImageDao()
        lifeCycleDataDao = database.getLifeCycleDataDao()
    }

    @Test
    fun deleteCoverArtLookup_shouldDeleteArtistIfLastReferenced() = runBlocking {
        val artistEntry = ArtistEntry(0, "artist")
        val artistId = artistDao.insert(artistEntry)

        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(artistId = artistId, lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val artistIdAfterDelete = artistDao.findArtistIdByName("artist")

        Assert.assertNull(artistIdAfterDelete)
    }

    @Test
    fun deleteCoverArtLookup_shouldNotDeleteArtistIfReferencedByOtherEntries() = runBlocking {
        val artistEntry = ArtistEntry(0, "artist")
        val artistId = artistDao.insert(artistEntry)

        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(artistId = artistId, lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        val otherLifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val otherLifeCycleDataId = lifeCycleDataDao.insert(otherLifeCycleDataEntry)

        val otherCoverArtLookupEntry =
            CoverArtLookupEntry(artistId = artistId, lifeCycleDataId = otherLifeCycleDataId)

        coverArtLookupDao.insert(otherCoverArtLookupEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val artistIdAfterDelete = artistDao.findArtistIdByName("artist")

        Assert.assertNotNull(artistIdAfterDelete)
    }

    @Test
    fun deleteCoverArtLookup_shouldNotDeleteOtherArtists() = runBlocking {
        val artistEntry = ArtistEntry(0, "artist")
        val artistId = artistDao.insert(artistEntry)

        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(artistId = artistId, lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        val otherArtistEntry = ArtistEntry(0, "other_artist")
        artistDao.insert(otherArtistEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val otherArtistIdAfterDelete = artistDao.findArtistIdByName("other_artist")

        Assert.assertNotNull(otherArtistIdAfterDelete)
    }

    @Test
    fun deleteCoverArtLookup_shouldDeleteAlbumIfLastReferenced() = runBlocking {
        val albumEntry = AlbumEntry(0, "album")
        val albumId = albumDao.insert(albumEntry)

        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(albumId = albumId, lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val albumIdAfterDelete = albumDao.findAlbumIdByName("album")

        Assert.assertNull(albumIdAfterDelete)
    }

    @Test
    fun deleteCoverArtLookup_shouldNotDeleteAlbumIfReferencedByOtherEntries() = runBlocking {
        val albumEntry = AlbumEntry(0, "album")
        val albumId = albumDao.insert(albumEntry)

        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(albumId = albumId, lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        val otherLifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val otherLifeCycleDataId = lifeCycleDataDao.insert(otherLifeCycleDataEntry)

        val otherCoverArtLookupEntry =
            CoverArtLookupEntry(albumId = albumId, lifeCycleDataId = otherLifeCycleDataId)

        coverArtLookupDao.insert(otherCoverArtLookupEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val albumIdAfterDelete = albumDao.findAlbumIdByName("album")

        Assert.assertNotNull(albumIdAfterDelete)
    }

    @Test
    fun deleteCoverArtLookup_shouldNotDeleteOtherAlbums() = runBlocking {
        val albumEntry = AlbumEntry(0, "album")
        val albumId = albumDao.insert(albumEntry)

        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(albumId = albumId, lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        val otherAlbumEntry = AlbumEntry(0, "other_album")
        albumDao.insert(otherAlbumEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val otherAlbumIdAfterDelete = albumDao.findAlbumIdByName("other_album")

        Assert.assertNotNull(otherAlbumIdAfterDelete)
    }

    @Test
    fun deleteCoverArtLookup_shouldDeleteReferencedImage() = runBlocking {
        val imageEntry = ImageEntry(0, ImageEntity.Companion.ImageType.COVER_ART.value, "image")
        val imageId = imageDao.insert(imageEntry)

        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(imageId = imageId, lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val imageAfterDelete = imageDao.findImageById(imageId)

        Assert.assertNull(imageAfterDelete)
    }

    @Test
    fun deleteCoverArtLookup_shouldNotDeleteOtherImages() = runBlocking {
        val imageEntry = ImageEntry(0, ImageEntity.Companion.ImageType.COVER_ART.value, "image")
        val imageId = imageDao.insert(imageEntry)

        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(imageId = imageId, lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        val otherImageEntry =
            ImageEntry(0, ImageEntity.Companion.ImageType.COVER_ART.value, "other_image")
        val otherImageId = imageDao.insert(otherImageEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val otherImageAfterDelete = imageDao.findImageById(otherImageId)

        Assert.assertNotNull(otherImageAfterDelete)
    }

    @Test
    fun deleteCoverArtLookup_shouldDeleteReferencedLifeCycleData() = runBlocking {
        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val lifeCycleDataAfterDelete = lifeCycleDataDao.findById(lifeCycleDataId)

        Assert.assertNull(lifeCycleDataAfterDelete)
    }

    @Test
    fun deleteCoverArtLookup_shouldNotDeleteOtherLifeCycleData() = runBlocking {
        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val coverArtLookupEntry =
            CoverArtLookupEntry(lifeCycleDataId = lifeCycleDataId)
        val coverArtLookupId = coverArtLookupDao.insert(coverArtLookupEntry)

        val otherLifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val otherLifeCycleDataId = lifeCycleDataDao.insert(otherLifeCycleDataEntry)

        coverArtLookupDao.deleteById(coverArtLookupId)

        val otherLifeCycleDataAfterDelete = lifeCycleDataDao.findById(otherLifeCycleDataId)

        Assert.assertNotNull(otherLifeCycleDataAfterDelete)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

