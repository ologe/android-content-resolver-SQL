# Release notes

## 1.2.2
- removed unused code
- supressed warnings
- internal cleaning

# 1.2.1
- hotfix: made Query params public

## 1.2.0
- Added a second method `contentResolver.querySql2(..)` that returns the `contentResolver.query(params)` calculated params
instead of cursor  

## 1.1.0
- Fixed multiple `GROUP BY` not working
- Improved tests

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