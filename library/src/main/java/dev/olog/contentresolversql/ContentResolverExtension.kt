@file:Suppress("NOTHING_TO_INLINE")

package dev.olog.contentresolversql

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

/**
 * Extension function that allow to write SQL queries instead of calling [ContentResolver.query]
 *
 * **JOIN is not supported**.
 *
 * **Example 1**: *(get all songs)*
 *
 * SELECT *
 * FROM [MediaStore.Audio.Media.EXTERNAL_CONTENT_URI]
 *
 * **Example 2**: *(get first 10 artists offsetted by 2, that have at least 5 tracks and 2 albums,
 * excluding the podcast,
 * ordered by artist desc*
 *
 * SELECT
 *  distinct [MediaStore.Audio.Media.ARTIST_ID],
 *  [MediaStore.Audio.Media.ARTIST],
 *  count(*) as songs,
 *  count(distinct [MediaStore.Audio.Media.ALBUM_ID]) as albums
 * FROM [MediaStore.Audio.Media.EXTERNAL_CONTENT_URI]
 * WHERE [MediaStore.Audio.Media.IS_PODCAST] = 0
 * GROUP BY [MediaStore.Audio.Media.ARTIST_ID]
 * HAVING songs >= 5 AND albums >= 2
 * ORDER BY [MediaStore.Audio.Media.ARTIST_KEY] DESC
 * LIMIT 10
 * OFFSET 2
 *
 * **Don't forget to close the cursor**
 */
@SuppressLint("Recycle")
fun ContentResolver.queryParser(query: String, selectionArgs: Array<String>? = null): Cursor {
    try {
        // select
        val projection = makeProjection(query.extract("SELECT", "FROM")!!)
        // from
        val uri = makeUri(query.extract("FROM", "WHERE")!!)
        // where, group by, having
        val selection = makeSelection(query.extract("WHERE", "ORDER BY"))
        // sort by, limit, offset
        val sortOrder = query.extract("ORDER BY", null)

        return query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )!!
    } catch (ex: Throwable) {
        Log.e("QueryParser", "check query:\n$query \nargs=$selectionArgs", ex)
        throw ex
    }
}

private inline fun makeProjection(projection: String): Array<String>? {
    return if (projection == "*") null
    else projection.split(",").toTypedArray()
}

private inline fun makeUri(query: String): Uri {
    return Uri.parse(query.sanitize())
}

private fun makeSelection(selection: String?): String? {
    if (selection == null){
        return null
    }

    val indexOfHaving = selection.indexOf("HAVING", ignoreCase = true)
    val indexOfGroupBy = selection.indexOf("GROUP BY", ignoreCase = true)
    // no group by, return itself
    if (indexOfGroupBy == -1){
        return selection
    }
    if (indexOfHaving > -1){
        // put '(' after having by
        return buildString {
            append(selection.substring(0, indexOfGroupBy))
            append(")")
            append(selection.substring(indexOfGroupBy, indexOfHaving))
            append(" HAVING (")
            append(selection.substring(indexOfHaving + 6))
        }
    }
    return buildString {
        append(selection.substring(0, indexOfGroupBy))
        append(") GROUP BY (")
        append(selection.substring(indexOfGroupBy + 8))
    }
}

private fun String.extract(from: String, to: String?): String? {
    val indexOfFrom = this.indexOf(from, ignoreCase = true)
    if (indexOfFrom == -1){
        // substring not found
        return null
    }

    return if (to != null && this.indexOf(to, ignoreCase = true) > -1) {
        // from - to
        this.substring(
            indexOfFrom + from.length,
            this.indexOf(to, ignoreCase = true)
        )
    } else {
        // from - end of string
        this.substring(
            indexOfFrom + from.length
        )
    }.sanitize()
}

private inline fun String.sanitize(): String {
    return this.replace("\n", " ")
        .replace("\t", " ")
        .trim()
}