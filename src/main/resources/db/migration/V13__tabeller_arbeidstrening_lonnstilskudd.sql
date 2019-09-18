create table arbeidstrening as
select id, start_dato, arbeidstrening_lengde, arbeidstrening_stillingprosent
from avtale;

alter table avtale
    drop column start_dato, arbeidstrening_lengde, arbeidstrening_stillingprosent;

create table varig_lonnstilskudd
(
    id                            uuid,
    arbeidsgiver_kontonummer      varchar(11),
    stillingtype                  varchar,
    stillingbeskrivelse           varchar,
    stillingprosent               varchar,
    lonnstilskudd_prosent         integer,
    lonnstilskudd_startdato       date,
    lonnstilskudd_evalueringsdato date,
    manedslonn                    integer,
    feriepengesats                decimal,
    arbeidsgiveravgift            decimal
);

create table midlertidig_lonnstilskudd
(
    id                            uuid,
    arbeidsgiver_kontonummer      varchar(11),
    stillingtype                  varchar,
    stillingbeskrivelse           varchar,
    stillingprosent               varchar,
    lonnstilskudd_prosent         integer,
    lonnstilskudd_startdato       date,
    lonnstilskudd_evalueringsdato date,
    manedslonn                    integer,
    feriepengesats                decimal,
    arbeidsgiveravgift            decimal
);