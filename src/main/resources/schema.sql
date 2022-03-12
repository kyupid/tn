drop table if exists `member`;

create table `member`
(
    id  int(10)     not null auto_increment,
    name varchar(50) not null,
    age  int(10)     not null,
    ci varchar(300),
    primary key (id)
)
