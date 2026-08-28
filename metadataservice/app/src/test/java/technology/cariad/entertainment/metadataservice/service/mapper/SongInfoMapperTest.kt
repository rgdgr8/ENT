package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.sampledata.SampleSongInfo
import technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfo
import org.junit.Assert
import org.junit.Test

class SongInfoMapperTest {

    @Test
    fun mapValue_shouldConvertSongInfoEntityToRecognizedSongInfo() {
        val recognizedSongInfo = SongInfoMapper().mapValue(SampleSongInfo.SONG_INFO_ENTITY_1)

        Assert.assertEquals(SampleSongInfo.RECOGNIZED_SONG_INFO_1, recognizedSongInfo)
    }

    @Test
    fun mapList_shouldConvertSongInfoEntitiesToRecognizedSongInfos() {
        val songInfoEntites =
            listOf(SampleSongInfo.SONG_INFO_ENTITY_1, SampleSongInfo.SONG_INFO_ENTITY_1)

        val recognizedSongInfos = SongInfoMapper().mapList(songInfoEntites)

        val expectedRecognizedSongInfos =
            listOf(SampleSongInfo.RECOGNIZED_SONG_INFO_1, SampleSongInfo.RECOGNIZED_SONG_INFO_1)

        Assert.assertEquals(expectedRecognizedSongInfos, recognizedSongInfos)
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        val emptyList = SongInfoMapper().mapList(listOf())

        Assert.assertEquals(listOf<RecognizedSongInfo>(), emptyList)
    }
}

