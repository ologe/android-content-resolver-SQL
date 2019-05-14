# Release notes

## 1.0.0
- Fixed crashed
- Now is **mandatory** to specify `ORDER BY` when using `LIMIT`
- Now is **mandatory** to specify `WHERE` when using `GROUP BY`
- Now throws an exception when using `HAVING` without `GROUP BY`
- Now throws an exception when using `OFFSET` without `LIMIT`
- Added tests 

## 1.0.0-beta02
- Renamed `contentResolver.queryParser(..)` to `contentResolver.querySql(..)` for better discoverability 

## 1.0.0-beta01
- First release