create table if not exists `users`
(
    `id`         bigint primary key auto_increment    not null unique,
    `name`       varchar(50) character set 'UTF8MB4'  not null,
    `family`     varchar(50) character set 'UTF8MB4'  not null,
    `phone`      varchar(20)                          not null,
    `username`   varchar(100) character set 'UTF8MB4' not null unique,
    `password`   text character set 'UTF8MB4'         not null,
    `created_at` datetime                             not null DEFAULT current_timestamp
);

# NEXT-QUERY

insert into `users` (`id`, `name`, `family`, `phone`, `username`, `password`, `created_at`)
    value (null, 'bardia', 'demon',
           '989170221393',
           'bardiademon',
           '844df1e561f853999cd39606f56e3084aff12214f90d8588b5f5d83ae7bfd8dd0c5c6531b31ab1352e729ca8f101c72cfc81c483038be452293c5424879ff57a',
           current_timestamp);
