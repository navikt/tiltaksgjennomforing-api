create table arbeidstrening as select id from avtale;

create table varig_lonnstilskudd
(
    id                            uuid,
    arbeidsgiver_kontonummer      varchar(11),
    stillingtype                  varchar,
    stillingbeskrivelse           varchar,
    lonnstilskudd_prosent         integer,
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
    lonnstilskudd_prosent         integer,
    manedslonn                    integer,
    feriepengesats                decimal,
    arbeidsgiveravgift            decimal
);