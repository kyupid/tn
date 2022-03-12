drop table if exists `member`;

create table `member`
(
    id  int(10)     not null auto_increment,
    email varchar(200) not null,
    age  int(10)     not null,
    ci varchar(300),
    primary key (id)
)
