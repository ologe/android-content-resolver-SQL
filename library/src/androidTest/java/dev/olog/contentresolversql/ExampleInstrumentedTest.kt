package dev.olog.contentresolversql

import android.provider.MediaStore.Audio.Media.ARTIST_ID
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test(expected = IllegalStateException::class)
    fun missing_select_throws(){
        val query = "SEL * FROM $EXTERNAL_CONTENT_URI"
        val appContext = InstrumentationRegistry.getTargetContext()
        appContext.contentResolver.querySql(query, null)
    }

    @Test(expected = IllegalStateException::class)
    fun missing_from_throws(){
        val query = "SELECT * FRO $EXTERNAL_CONTENT_URI"
        val appContext = InstrumentationRegistry.getTargetContext()
        appContext.contentResolver.querySql(query, null)
    }

    @Test(expected = IllegalStateException::class)
    fun groupby_without_where_throws(){
        val query = """
            SELECT $ARTIST_ID, count(*)
            FROM $EXTERNAL_CONTENT_URI
            GROUP BY $ARTIST_ID
        """
        val appContext = InstrumentationRegistry.getTargetContext()
        appContext.contentResolver.querySql(query, null)
    }

    @Test(expected = IllegalStateException::class)
    fun having_without_groupby_throws(){
        val query = """
            SELECT $ARTIST_ID, count(*) as songs
            FROM $EXTERNAL_CONTENT_URI
            HAVING songs > 10
        """
        val appContext = InstrumentationRegistry.getTargetContext()
        appContext.contentResolver.querySql(query, null)
    }

    @Test(expected = IllegalStateException::class)
    fun limit_without_orderby_throws(){
        val query = """
            SELECT $ARTIST_ID, count(*) as songs
            FROM $EXTERNAL_CONTENT_URI
            LIMIT 1
        """
        val appContext = InstrumentationRegistry.getTargetContext()
        appContext.contentResolver.querySql(query, null)
    }

    @Test(expected = IllegalStateException::class)
    fun offset_without_limit_throws(){
        val query = """
            SELECT $ARTIST_ID, count(*) as songs
            FROM $EXTERNAL_CONTENT_URI
            OFFSET 10
        """
        val appContext = InstrumentationRegistry.getTargetContext()
        appContext.contentResolver.querySql(query, null)
    }

}
