```sql
USE test;

drop table if exists first;
CREATE TABLE `first` (
id bigint AUTO_INCREMENT PRIMARY KEY,
status varchar(100) 
)

drop table if exists second;
CREATE TABLE `second` (
id bigint AUTO_INCREMENT PRIMARY KEY,
first_id bigint 
)
```