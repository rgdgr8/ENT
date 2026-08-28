package technology.cariad.entertainment.metadataservice.data.mapper

import android.os.Build
import de.esolutions.connectedradioclient.data.StationLogo
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class StationLogoMapperTest {

    private val stationLogoMapper = StationLogoMapper()

    private val stationLogo = StationLogo(
        byteArrayOf(12, 34, 56),
        "logo",
        "PNG"
    )

    private val expectedStationLogoEntity = ImageEntity(
        null,
        "logo.PNG",
        null,
        listOf(12, 34, 56)
    )

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun mapValue_shoudConvertStationLogoToEntity() {

        val stationLogoEntity = stationLogoMapper.mapValue(stationLogo)

        Assert.assertEquals(expectedStationLogoEntity.imageType, stationLogoEntity.imageType)
        Assert.assertEquals(expectedStationLogoEntity.uri, stationLogoEntity.uri)
        Assert.assertEquals(expectedStationLogoEntity.fileName, stationLogoEntity.fileName)
        Assert.assertEquals(expectedStationLogoEntity.imageData, stationLogoEntity.imageData)
    }

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun mapList_shoudConvertStationLogosToEntities() {

        val stationLogoEntity = stationLogoMapper.mapList(listOf(stationLogo))

        Assert.assertEquals(expectedStationLogoEntity.imageType, stationLogoEntity[0].imageType)
        Assert.assertEquals(expectedStationLogoEntity.uri, stationLogoEntity[0].uri)
        Assert.assertEquals(expectedStationLogoEntity.fileName, stationLogoEntity[0].fileName)
        Assert.assertEquals(
            expectedStationLogoEntity.imageData,
            stationLogoEntity[0].imageData
        )
    }

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun mapList_shoudConvertEmptyListToEmptyList() {

        val stationLogoEntity = stationLogoMapper.mapList(listOf())

        Assert.assertEquals(listOf<ImageEntity>(), stationLogoEntity)
    }
}

