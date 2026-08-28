package technology.cariad.entertainment.metadataservice.data.provider.gracenote

import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.Test

class SongRecognitionResultTest {

    @Test
    fun check_equals_hashCode() {
        EqualsVerifier.forClass(SongRecognitionResult::class.java).verify()
    }
}

