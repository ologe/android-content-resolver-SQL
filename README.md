[github]:            https://github.com/ologe/android-content-resolver-SQL
[paypal-url]:        https://paypal.me/nextmusicplayer

[platform-badge]:   https://img.shields.io/badge/Platform-Android-F3745F.svg
[paypal-badge]:     https://img.shields.io/badge/Donate-Paypal-F3745F.svg
[minsdk-badge]:     https://img.shields.io/badge/minSdkVersion-16-F3745F.svg

<!------------------------------------------------------------------------------------------------------->

Content Resolver SQL
=

[![platform-badge]][github]
[![minsdk-badge]][github]
[![paypal-badge]][paypal-url]

Allows to write SQL statements instead of using `ContentResolver.query(...)`. 
The library add an extension function to Context.ContentResolver named 
`queryParser(query: String, selectionArgs: Array<String>? = null)`

## Example 1: 
**Get all songs**
```kotlin
val query = """ 
    SELECT *
    FROM ${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}
""" 
contentResolver.queryParser(query)
```
**instead of**
```kotlin
contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null)
```
## Example 2

**Get first 10 artists offsetted by 2, that have at least 5 tracks and 2 albums, excluding the podcast, ordered by artist desc**
```kotlin
val query = """
    SELECT distinct ${MediaStore.Audio.Media.ARTIST_ID}, ${MediaStore.Audio.Media.ARTIST}, 
        count(*) as songs, 
        count(distinct ${MediaStore.Audio.Media.ALBUM_ID}) as albums
    FROM ${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}
    WHERE ${MediaStore.Audio.Media.IS_PODCAST} = 0
    GROUP BY ${MediaStore.Audio.Media.ARTIST_ID}
    HAVING songs >= 5 AND albums >= 2
    ORDER BY ${MediaStore.Audio.Media.ARTIST_KEY} DESC
    LIMIT 10
    OFFSET 2
"""
contentResolver.queryParser(query)
```
**instead of**
```kotlin
contentResolver.query(
    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    projection = arrayOf(
        "distinct $ARTIST_ID", 
        ARTIST, 
        "count(*) as songs", 
        "count(distinct $ALBUM_ID) as albums"
    ),
    selection = "$IS_PODCAST = 0 ) GROUP BY $ARTIST_ID HAVING (songs >= 5 AND albums >= 2",
    selectionArgs = null,
    sortOrder = "$ARTIST_KEY DESC LIMIT 20 OFFSET 2"
)
```