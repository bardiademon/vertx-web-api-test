create table if not exists `users`
(
    `id`         bigint primary key auto_increment    not null unique,
    `name`       varchar(50) character set 'UTF8MB4'  not null,
    `family`     varchar(50) character set 'UTF8MB4'  not null,
    `username`   varchar(100) character set 'UTF8MB4' not null unique,
    `password`   varchar(100) character set 'UTF8MB4' not null,
    `created_at` datetime                             not null DEFAULT current_timestamp
);