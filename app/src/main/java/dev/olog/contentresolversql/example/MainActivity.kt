package dev.olog.contentresolversql.example

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AlbumColumns.ARTIST
import android.provider.MediaStore.Audio.AudioColumns.*
import androidx.appcompat.app.AppCompatActivity
import dev.olog.contentresolversql.queryParser

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var query = """
            SELECT *
            FROM ${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}
        """.trimIndent()

        var cursor = contentResolver.queryParser(query)
        cursor.close()

        query = """
            SELECT distinct $ARTIST_ID, $ARTIST, count(*) as songs, count(distinct $ALBUM_ID) as albums
            FROM ${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}
            WHERE $IS_PODCAST = 0
            GROUP BY $ARTIST_ID
            HAVING songs >= 2 AND albums >= 2
            ORDER BY $ARTIST_KEY ASC
            LIMIT 20
            OFFSET 2
        """.trimIndent()

        contentResolver.query(Uri.parse(""), null, null, null, null)
        cursor = contentResolver.queryParser(query)
        val result = mutableListOf<Artist>()
        while (cursor.moveToNext()){
            val item = Artist(
                cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                cursor.getString(cursor.getColumnIndex(ARTIST)),
                cursor.getInt(cursor.getColumnIndex("songs")),
                cursor.getInt(cursor.getColumnIndex("albums"))
            )
            result.add(item)
        }

        cursor.close()
    }
}

data class Artist(
    val id: Long,
    val name: String,
    val songs: Int,
    val albums: Int
)
