package dev.olog.contentresolversql

import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns.*
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun simple() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = null
        val selection: String? = null
        val sortOrder: String? = null

        val query = """
            SELECT *
            FROM $uri
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun only_projection() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = arrayOf(ALBUM_ID, ALBUM, "count(*)")
        val selection: String? = null
        val sortOrder: String? = null

        val query = """
            SELECT $ALBUM_ID, $ALBUM, count(*)
            FROM $uri
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun where() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = null
        val selection: String? = "$IS_PODCAST = 0 AND $IS_MUSIC = 0"
        val sortOrder: String? = null

        val query = """
            SELECT *
            FROM $uri
            WHERE $IS_PODCAST = 0 AND $IS_MUSIC = 0
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun order_by() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = null
        val selection: String? = null
        val sortOrder: String? = ALBUM_ID

        val query = """
            SELECT *
            FROM $uri
            ORDER BY $ALBUM_ID
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun where_orderby() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = null
        val selection: String? = "$IS_PODCAST = 0"
        val sortOrder: String? = ALBUM_ID

        val query = """
            SELECT *
            FROM $uri
            WHERE $IS_PODCAST = 0
            ORDER BY $ALBUM_ID
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun where_groupby() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = arrayOf("distinct $ARTIST_ID", "count(*)")
        val selection: String? = "$IS_PODCAST = 0 ) GROUP BY ( $ARTIST_ID"
        val sortOrder: String? = null

        val query = """
            SELECT distinct $ARTIST_ID, count(*)
            FROM $uri
            WHERE $IS_PODCAST = 0
            GROUP BY $ARTIST_ID
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun where_groupby_having() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = arrayOf("distinct $ARTIST_ID", "count(*) as songs")
        val selection: String? = "$IS_PODCAST = 0 )GROUP BY $ARTIST_ID  HAVING ( songs > 5"
        val sortOrder: String? = null

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            WHERE $IS_PODCAST = 0
            GROUP BY $ARTIST_ID
            HAVING songs > 5
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun sortby() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = arrayOf("distinct $ARTIST_ID", "count(*) as songs")
        val selection: String? = null
        val sortOrder: String? = ARTIST_ID

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            ORDER BY $ARTIST_ID
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun sortby_limit() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = arrayOf("distinct $ARTIST_ID", "count(*) as songs")
        val selection: String? = null
        val sortOrder: String? = "$ARTIST_ID LIMIT 10"

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            ORDER BY $ARTIST_ID
            LIMIT 10
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun sortby_limit_offset() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = arrayOf("distinct $ARTIST_ID", "count(*) as songs")
        val selection: String? = null
        val sortOrder: String? = "$ARTIST_ID LIMIT 10 OFFSET 2"

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            ORDER BY $ARTIST_ID
            LIMIT 10
            OFFSET 2
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun where_sortby() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = arrayOf("distinct $ARTIST_ID", "count(*) as songs")
        val selection: String? = "$IS_PODCAST = 0"
        val sortOrder: String? = ARTIST_ID

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            WHERE $IS_PODCAST = 0
            ORDER BY $ARTIST_ID
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    @Test
    fun where_group_by_sortby() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String>? = arrayOf("distinct $ARTIST_ID", "count(*) as songs")
        val selection: String? = "$IS_PODCAST = 0 ) GROUP BY ( $ARTIST_ID"
        val sortOrder: String? = ARTIST_ID

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            WHERE $IS_PODCAST = 0
            GROUP BY $ARTIST_ID
            ORDER BY $ARTIST_ID
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
        assertEquals(makeUri(query, keywords), uri)
        assertArrayEquals(makeProjection(query), projection)
        assertEquals(makeSelection(query), selection)
        assertEquals(makeSort(query), sortOrder)
    }

    ////////////////////////////////////////////////////////////

    @Test(expected = IllegalStateException::class)
    fun where_having() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            WHERE $IS_PODCAST = 0
            HAVING songs > 5
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
    }

    @Test(expected = IllegalStateException::class)
    fun groupby() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            group by $ARTIST_ID
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
    }

    @Test(expected = IllegalStateException::class)
    fun having() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            HAVING songs > 5
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
    }

    @Test(expected = IllegalStateException::class)
    fun limit() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            LIMIT 10
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
    }

    @Test(expected = IllegalStateException::class)
    fun offset() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            OFFSET 10
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
    }

    @Test(expected = IllegalStateException::class)
    fun limit_offset() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val query = """
            SELECT distinct $ARTIST_ID, count(*) as songs
            FROM $uri
            LIMIT 10
            OFFSET 10
        """.trimIndent()

        val keywords = createKeywords(query)
        sanitize(keywords)
    }

}
