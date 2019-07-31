@file:Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")

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

@Suppress("unused")
class Query(
    val uri: Uri,
    val projection: Array<String>?,
    val selection: String?,
    val selectionArgs: Array<String>?,
    val sortOrder: String?

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
        val projection = makeProjection(query, indexOfKeywords)
        // from
        val uri = makeUri(query, indexOfKeywords)

        // where, group by, having

        val selection = makeSelection(query, indexOfKeywords)

        // sort by, limit, offset
        val sortOrder = makeSort(query, indexOfKeywords)

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

@Suppress("unused")
fun ContentResolver.querySql2(
    query: String,
    selectionArgs: Array<String>? = null
): Query {
    try {
        val indexOfKeywords = createKeywords(query)
        sanitize(indexOfKeywords)

        // select
        val projection = makeProjection(query, indexOfKeywords)
        // from
        val uri = makeUri(query, indexOfKeywords)

        // where, group by, having
        val selection = makeSelection(query, indexOfKeywords)

        // sort by, limit, offset
        val sortOrder = makeSort(query, indexOfKeywords)

        return Query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    } catch (ex: Throwable) {
        Log.e("ContentResolverSQL", "Executed query:\n$query \nargs=$selectionArgs")
        throw ex
    }
}

internal inline fun createKeywords(query: String): List<Pair<String, Int>> {
    return keywords.asSequence()
        .map { it to query.indexOf(it, ignoreCase = true) }
        .filter { it.second > -1 }
        .toList()
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

internal inline fun makeProjection(query: String, indexOfKeywords: List<Pair<String, Int>>): Array<String>? {
    val indexOfSelect = indexOfKeywords.first { it.first == "select" }.second + 6
    val indexOfFrom = indexOfKeywords.first { it.first == "from" }.second
    val projection = query.substring(indexOfSelect, indexOfFrom).trim()
    return if (projection == "*") null
    else projection
        .split(",")
        .map { it.trim() }
        .toTypedArray()
}

internal inline fun makeUri(query: String, indexOfKeywords: List<Pair<String, Int>>): Uri {
    val indexOfFrom = indexOfKeywords.first { it.first == "from" }.second
    val keywordAfterFrom = indexOfKeywords.find { it.second > indexOfFrom }
    return if (keywordAfterFrom == null) {
        Uri.parse(query.substring(indexOfFrom + 4).trim())
    } else {
        Uri.parse(query.substring(indexOfFrom + 4, keywordAfterFrom.second).trim())
    }
}

internal fun makeSelection(query: String, keywordsIndex: List<Pair<String, Int>>): String? {
    val whereClauseKeyword = keywordsIndex.find { it.first == "where" }
        ?: return null // no where clause

    var nextKeyword = keywordsIndex.find { it.second > whereClauseKeyword.second }
        ?: return query.substring(whereClauseKeyword.second + 5).trim() // where is the last clause

    if (nextKeyword.first == "order by") {
        // where clause only
        return query.substring(
            whereClauseKeyword.second + 5, // after where
            nextKeyword.second      // before order by
        ).trim()
    }
    // next keyword is group by
    val groupByKeyword = nextKeyword
    val whereCondition = query.substring(whereClauseKeyword.second + 5, groupByKeyword.second).trim()
    nextKeyword = keywordsIndex.find { it.second > groupByKeyword.second } ?: Pair("placeholder", -1)
    if (nextKeyword.first == "order by" || nextKeyword.second == -1) {
        // group by only, no having clause
        val groupByString = if (nextKeyword.second == -1) query.substring(groupByKeyword.second + 8)
        else query.substring(groupByKeyword.second + 8, nextKeyword.second)

        val groupBy = groupByString.split(",").map { it.trim() }.toMutableList()
        return buildString {
            append(whereCondition)
            append(") GROUP BY ")
            // add a closing parenthesis after last condition of groyp by
            groupBy[groupBy.lastIndex] = "(${groupBy[groupBy.lastIndex]}"
            append(groupBy.joinToString())
        }.trim()
    }
    // next keyword is having
    val havingKeyword = nextKeyword
    nextKeyword = keywordsIndex.find { it.second > havingKeyword.second } ?: Pair("placeholder", -1)

    val groupBy = query.substring(groupByKeyword.second + 8, havingKeyword.second)
        .split(",")
        .map { it.trim() }

    val havingConditions = if (nextKeyword.second == -1) query.substring(havingKeyword.second + 6)
    else query.substring(havingKeyword.second + 6, nextKeyword.second)


    return buildString {
        append(whereCondition)
        append(") GROUP BY ")
        append(groupBy.joinToString())
        append(" HAVING (")
        append(havingConditions.split("AND", ignoreCase = true).joinToString(separator = " AND ") { it.trim() })
    }.trim()
}

internal fun makeSort(query: String, indexOfKeywords: List<Pair<String, Int>>): String? {
    val indexOfSort = indexOfKeywords.find { it.first == "order by" } ?: return null
    return query.substring(indexOfSort.second + 8).trim()
}