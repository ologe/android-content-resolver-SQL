package dev.olog.contentresolversql.example

import android.Manifest
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import dev.olog.contentresolversql.querySql

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            0
        )

        example()
    }


    private fun example() {
        val query = """
            SELECT *
            FROM ${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}
            WHERE id = ?
        """.trimIndent()

        contentResolver.querySql(query, arrayOf("1")).close()
    }

}