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

internal val keywords = setOf(
    "select",
    "from",
    "where",
    "group by",
    "having",
    "order by",
    "limit",
    "offset"
)

@SuppressLint("Recycle")
fun ContentResolver.querySql(
    query: String,
    selectionArgs: Array<String>? = null
): Cursor {
    try {
        val indexOfKeywords = createKeywords(query)
        sanitize(indexOfKeywords)

        // select
        val projection = makeProjection(query)
        // from
        val uri = makeUri(query, indexOfKeywords)

        // where, group by, having

        val selection = makeSelection(query)

        // sort by, limit, offset
        val sortOrder = makeSort(query)

        return query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )!!
    } catch (ex: Throwable) {
        Log.e("ContentResolverSQL", "Executed query:\n$query \nargs=$selectionArgs")
        throw ex
    }
}

internal fun createKeywords(query: String): List<Pair<String, Int>> {
    return keywords.map { it to query.indexOf(it, ignoreCase = true) }
        .filter { it.second > -1 }
}

internal inline fun sanitize(keywords: List<Pair<String, Int>>) {
    if (keywords.find { it.first == "select" } == null) {
        throw IllegalStateException("Missing SELECT clause")
    }
    if (keywords.find { it.first == "from" } == null) {
        throw IllegalStateException("Missing FROM clause")
    }

    if (keywords.find { it.first == "group by" } != null && keywords.find { it.first == "where" } == null) {
        throw IllegalStateException("WHERE clause is mandatory when using GROUP BY")
    }
    if (keywords.find { it.first == "limit" } != null && keywords.find { it.first == "order by" } == null) {
        throw IllegalStateException("ORDER BY clause is mandatory when using LIMIT")
    }
    if (keywords.find { it.first == "offset" } != null && keywords.find { it.first == "limit" } == null) {
        throw IllegalStateException("LIMIT clause is mandatory when using OFFSET")
    }
    if (keywords.find { it.first == "having" } != null && keywords.find { it.first == "group by" } == null) {
        throw IllegalStateException("GROUP BY clause is mandatory when using HAVING")
    }
}

internal inline fun makeProjection(query: String): Array<String>? {
    val projection = query.extract("SELECT", "FROM")!!
    return if (projection == "*") null
    else projection
        .split(",")
        .map { it.trim() }
        .toTypedArray()
}

internal inline fun makeUri(query: String, indexOfKeywords: List<Pair<String, Int>>): Uri {
    val indexOfFrom = indexOfKeywords.first { it.first == "from" }.second
    val keywordAfterFrom = indexOfKeywords.find { it.second > indexOfFrom }?.first
    val tableName = query.extract("FROM", keywordAfterFrom)!!
    return Uri.parse(tableName.sanitize())
}

internal fun makeSelection(query: String): String? {
    val selection = query.extract("WHERE", "ORDER BY") ?: return null

    val indexOfHaving = selection.indexOf("HAVING", ignoreCase = true)
    val indexOfGroupBy = selection.indexOf("GROUP BY", ignoreCase = true)

    // no group by, return only where clause
    if (indexOfGroupBy == -1) {
        return selection
    }
    if (indexOfHaving > -1) {
        // WHERE .. ) GROUP BY .. HAVING ( ..
        return buildString {
            append(selection.substring(0, indexOfGroupBy))
            append(")")
            append(selection.substring(indexOfGroupBy, indexOfHaving))
            append(" HAVING (")
            append(selection.substring(indexOfHaving + 6))
        }
    }
    // WHERE .. ) GROUP BY ( ..
    return buildString {
        append(selection.substring(0, indexOfGroupBy))
        append(") GROUP BY (")
        append(selection.substring(indexOfGroupBy + 8))
    }
}

internal fun makeSort(query: String): String? {
    return query.extract("ORDER BY", null)
}

internal fun String.extract(from: String, to: String?): String? {
    val indexOfFrom = this.indexOf(from, ignoreCase = true)
    if (indexOfFrom == -1) {
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

internal inline fun String.sanitize(): String {
    return this.replace("\n", " ")
        .replace("\t", " ")
        .trim()
}