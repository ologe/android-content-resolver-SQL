package dev.olog.contentresolversql

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {

    @Test
    fun assertSqlCorrectKeywords(){
        val correctKeywords = setOf(
            "select",
            "from",
            "where",
            "group by",
            "having",
            "order by",
            "limit",
            "offset"
        )

        assertEquals(keywords, correctKeywords)
    }

    @Test
    fun assertStarProjectionReturnNull(){
        assertTrue(makeProjection("*") == null)
    }

    @Test
    fun assertCustomProjectionArray(){
        val projectionString = "distinct id, album_id, count(*) as songs"
        val expected = arrayOf("distinct id", " album_id", " count(*) as songs")
        assertArrayEquals(makeProjection(projectionString), expected)
    }

    @Test
    fun assertExtractNotFound(){
        val query = "SELECT * FROM test"
        assertNull(query.extract("where", null))
        assertNull(query.extract("where", "group by"))
    }

    @Test
    fun assertExtractFoundInMiddleString(){
        val query = "SELECT * FROM test WHERE condition = 0"
        assertEquals(query.extract("from", "where"), "test")
    }

    @Test
    fun assertExtractFoundAtEndString(){
        val query = "SELECT * FROM test WHERE condition = 0"
        assertEquals(query.extract("where", "group by"), "condition = 0")
    }

}