package dev.olog.contentresolversql

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class QueryTest {

    @Test
    fun star_projection() {
        val query = """
           SELECT *
           FROM table
        """
        val keywords = createKeywords(query)
        assertArrayEquals(null, makeProjection(query, keywords))
    }

    @Test
    fun one_column_projection() {
        val query = """
           SELECT column1
           FROM table
        """
        val keywords = createKeywords(query)
        assertArrayEquals(arrayOf("column1"), makeProjection(query, keywords))
    }

    @Test
    fun multiple_column_projection() {
        val query = """
           SELECT column1, column2
           FROM table
        """
        val keywords = createKeywords(query)
        assertArrayEquals(arrayOf("column1", "column2"), makeProjection(query, keywords))
    }

    @Test
    fun where_only(){
        // where as last keyword
        val query = """
           SELECT *
           FROM table
           WHERE is_podcast = 0
        """
        var keywords = createKeywords(query)
        assertEquals("is_podcast = 0", makeSelection(query, keywords))

        // where as last keyword, alt
        val query2 = """
           SELECT * FROM table WHERE is_podcast = 0
        """
        keywords = createKeywords(query2)
        assertEquals("is_podcast = 0", makeSelection(query2, keywords))

        // where as last selection keyword
        val query3 = """
           SELECT * FROM table
           WHERE is_podcast = 0
           ORDER BY artist
        """
        keywords = createKeywords(query3)
        assertEquals("is_podcast = 0", makeSelection(query3, keywords))

        // where as last selection keyword, multiple conditions
        val query4 = """
           SELECT * FROM table
           WHERE is_podcast = 0 AND is_music = 1
           ORDER BY artist
        """
        keywords = createKeywords(query4)
        assertEquals("is_podcast = 0 AND is_music = 1", makeSelection(query4, keywords))
    }

    @Test
    fun where_group_by(){
        // single group by condition and last condition
        val query = """
           SELECT *
           FROM table
           WHERE is_podcast = 0
           GROUP BY artist_id
        """
        var keywords = createKeywords(query)
        assertEquals("is_podcast = 0) GROUP BY (artist_id", makeSelection(query, keywords))

        // multiple group by condition and last condition
        val query2 = """
           SELECT *
           FROM table
           WHERE is_podcast = 0
           GROUP BY artist_id, album_id
        """
        keywords = createKeywords(query2)
        assertEquals("is_podcast = 0) GROUP BY artist_id, (album_id", makeSelection(query2, keywords))

        // single group by condition, not last condition
        val query3 = """
           SELECT *
           FROM table
           WHERE is_podcast = 0
           GROUP BY artist_id
           ORDER BY artist
        """
        keywords = createKeywords(query3)
        assertEquals("is_podcast = 0) GROUP BY (artist_id", makeSelection(query3, keywords))

        // multiple group by condition, not last condition
        val query4 = """
           SELECT *
           FROM table
           WHERE is_podcast = 0
           GROUP BY artist_id, album_id
           ORDER BY artist
        """
        keywords = createKeywords(query4)
        assertEquals("is_podcast = 0) GROUP BY artist_id, (album_id", makeSelection(query4, keywords))
    }

    @Test
    fun where_groupby_having(){
        // single having condition, last condition
        val query = """
           SELECT *
           FROM table
           WHERE is_podcast = 0
           GROUP BY artist_id
           HAVING songs > 0
        """
        var keywords = createKeywords(query)
        assertEquals("is_podcast = 0) GROUP BY artist_id HAVING (songs > 0", makeSelection(query, keywords))

        // multiple having condition, last condition
        val query2 = """
           SELECT *
           FROM table
           WHERE is_podcast = 0
           GROUP BY artist_id
           HAVING songs > 0 AND albums > 0
        """
        keywords = createKeywords(query2)
        assertEquals(
            "is_podcast = 0) GROUP BY artist_id HAVING (songs > 0 AND albums > 0",
            makeSelection(query2, keywords)
        )

        // multiple group by and having condition, last condition
        val query3 = """
           SELECT *
           FROM table
           WHERE is_podcast = 0
           GROUP BY artist_id, album_id
           HAVING songs > 0 AND albums > 0
        """
        keywords = createKeywords(query3)
        assertEquals(
            "is_podcast = 0) GROUP BY artist_id, album_id HAVING (songs > 0 AND albums > 0",
            makeSelection(query3, keywords)
        )

        // multiple group by and having condition, not last condition
        val query4 = """
           SELECT *
           FROM table
           WHERE is_podcast = 0
           GROUP BY artist_id, album_id
           HAVING songs > 0 AND albums > 0
           ORDER BY album
        """
        keywords = createKeywords(query4)
        assertEquals(
            "is_podcast = 0) GROUP BY artist_id, album_id HAVING (songs > 0 AND albums > 0",
            makeSelection(query4, keywords)
        )
    }

    @Test
    fun orderby(){
        val query = """
            SELECT *
            FROM table
            WHERE contition
            ORDER BY artist
        """.trimIndent()
        var keywords = createKeywords(query)
        assertEquals("artist", makeSort(query, keywords))

        val query2 = """
            SELECT *
            FROM table
            WHERE contition
            ORDER BY artist, album
        """.trimIndent()
        keywords = createKeywords(query2)
        assertEquals("artist, album", makeSort(query2, keywords))
    }

    @Test
    fun limit_offset(){
        val query = """
            SELECT *
            FROM table
            WHERE contition
            ORDER BY artist
            LIMIT 10
            OFFSET 10
        """.trimIndent()
        val keywords = createKeywords(query)
        assertEquals("artist\nLIMIT 10\nOFFSET 10", makeSort(query, keywords))
    }

}