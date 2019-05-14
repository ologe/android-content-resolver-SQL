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
[![](https://jitpack.io/v/ologe/android-content-resolver-SQL.svg)](https://jitpack.io/#ologe/android-content-resolver-SQL)


Allows to write SQL statements instead of using `contentResolver.query(...)`. 
The library add an extension function to ContentResolver named 
`querySql(query: String, selectionArgs: Array<String>? = null)`

### Limitations
- When using `LIMIT` keyword, you need to specify also `ORDER BY`.<p>
- When using `GROUP BY` keyword, you need to specify also  `WHERE`.<p>
- `JOIN` are not supported by android content provider itself.

## Getting started
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```
implementation 'com.github.ologe:android-content-resolver-SQL:1.0'
```

## Example 1
**Get all songs**
```kotlin
val query = """ 
    SELECT *
    FROM ${Media.EXTERNAL_CONTENT_URI}
""" 
contentResolver.querySql(query)
```
**instead of**
```kotlin
contentResolver.query(Media.EXTERNAL_CONTENT_URI, null, null, null, null)
```
## Example 2

**Get first 10 artists offsetted by 2, that have at least 5 tracks and 2 albums, excluding the podcast, ordered by artist desc**
```kotlin
val query = """
    SELECT distinct $ARTIST_ID, $ARTIST, count(*) as songs, count(distinct $ALBUM_ID) as albums
    FROM ${Media.EXTERNAL_CONTENT_URI}
    WHERE $IS_PODCAST = 0
    GROUP BY $ARTIST_ID
    HAVING songs >= 5 AND albums >= 2
    ORDER BY $ARTIST_KEY DESC
    LIMIT 10
    OFFSET 2
"""
contentResolver.querySql(query)
```
**instead of**
```kotlin
contentResolver.query(
    uri = Media.EXTERNAL_CONTENT_URI,
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
