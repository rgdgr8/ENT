package technology.cariad.entertainment.metadataservice.contentprovider

import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import technology.cariad.entertainment.metadataservice.common.util.loadLibrary
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGracenoteServiceCallback
import org.junit.Test
import org.junit.Assert.*
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.xmlpull.v1.XmlPullParser

class PathConfigParserTest {
    private val context = mockk<Context>()
    private val resources = mockk<Resources>()
    private val xmlParser = mockk<XmlResourceParser>(relaxed = true)

    private lateinit var pathConfigParser : PathConfigParser.Companion

    @Before
    fun setup() {
        pathConfigParser = PathConfigParser
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `companion constructor`() {
        assertNotNull(pathConfigParser)
    }

    @Test
    fun `constructor`() {
        val pathConfigParserConstructor = PathConfigParser()
        assertNotNull(pathConfigParserConstructor)
    }

    @Test
    fun `getPathConfig parses xml correctly`() {
        every { context.resources } returns resources

        every { xmlParser.eventType } returnsMany listOf(
            XmlPullParser.START_DOCUMENT,
            XmlPullParser.START_TAG,
            XmlPullParser.END_DOCUMENT
        )

        every { xmlParser.name } returns "files-path"

        every { xmlParser.getAttributeValue(null, "name") } returns "img"
        every { xmlParser.getAttributeValue(null, "path") } returns "images/"

        var callCount = 0
        every { xmlParser.next() } answers {
            callCount++
            when (callCount) {
                1 -> XmlPullParser.START_TAG
                2 -> XmlPullParser.END_DOCUMENT
                else -> XmlPullParser.END_DOCUMENT
            }
        }
        every { resources.getXml(any()) } returns xmlParser

        val map = pathConfigParser.getPathConfig(context)

        assertEquals(1, map.size)
        assertEquals("images/", map["img"])
    }

    @Test
    fun `getPathConfig parses xml answering path null`() {
        every { context.resources } returns resources

        every { xmlParser.eventType } returnsMany listOf(
            XmlPullParser.START_DOCUMENT,
            XmlPullParser.START_TAG,
            XmlPullParser.END_DOCUMENT
        )

        every { xmlParser.name } returns "files-path"

        every { xmlParser.getAttributeValue(null, "name") } returns "img"
        every { xmlParser.getAttributeValue(null, "path") } returns null

        var callCount = 0
        every { xmlParser.next() } answers {
            callCount++
            when (callCount) {
                1 -> XmlPullParser.START_TAG
                2 -> XmlPullParser.END_DOCUMENT
                else -> XmlPullParser.END_DOCUMENT
            }
        }
        every { resources.getXml(any()) } returns xmlParser

        val map = pathConfigParser.getPathConfig(context)

        assertEquals(0, map.size)
        assertEquals(null, map["img"])
    }

    @Test
    fun `getPathConfig parses xml answering name null`() {
        every { context.resources } returns resources

        every { xmlParser.eventType } returnsMany listOf(
            XmlPullParser.START_DOCUMENT,
            XmlPullParser.START_TAG,
            XmlPullParser.END_DOCUMENT
        )

        every { xmlParser.name } returns "files-path"

        every { xmlParser.getAttributeValue(null, "name") } returns null
        every { xmlParser.getAttributeValue(null, "path") } returns null

        var callCount = 0
        every { xmlParser.next() } answers {
            callCount++
            when (callCount) {
                1 -> XmlPullParser.START_TAG
                2 -> XmlPullParser.END_DOCUMENT
                else -> XmlPullParser.END_DOCUMENT
            }
        }
        every { resources.getXml(any()) } returns xmlParser

        val map = pathConfigParser.getPathConfig(context)

        assertEquals(0, map.size)
        assertEquals(null, map["img"])
    }

    @Test
    fun `getPathConfig parses xml`() {
        every { context.resources } returns resources

        every { xmlParser.eventType } returnsMany listOf(
            XmlPullParser.START_DOCUMENT,
            XmlPullParser.START_TAG,
            XmlPullParser.END_DOCUMENT
        )

        every { xmlParser.name } returns "files-path-other"

        every { xmlParser.getAttributeValue(null, "name") } returns "img"
        every { xmlParser.getAttributeValue(null, "path") } returns "images/"

        var callCount = 0
        every { xmlParser.next() } answers {
            callCount++
            when (callCount) {
                1 -> XmlPullParser.START_TAG
                2 -> XmlPullParser.END_DOCUMENT
                else -> XmlPullParser.END_DOCUMENT
            }
        }
        every { resources.getXml(any()) } returns xmlParser

        val map = pathConfigParser.getPathConfig(context)

        assertEquals(0, map.size)
        assertEquals(null, map["img"])
    }
}

