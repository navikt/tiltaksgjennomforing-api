create table filter_sok
(
    sok_id          varchar primary key,
    tidspunkt_sokt  timestamp without time zone not null,
    query_parametre text not null
);