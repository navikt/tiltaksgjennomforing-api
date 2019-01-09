CREATE TABLE avtale (
  id serial primary key,
  opprettet_tidspunkt timestamp without time zone not null default now(),

  deltaker_fornavn varchar(255),
  deltaker_etternavn varchar(255),
  deltaker_adresse varchar(255),
  deltaker_postnummer varchar(255),
  deltaker_poststed varchar(255),

  bedrift_navn varchar(255),
  bedrift_adresse varchar(255),
  bedrift_postnummer varchar(255),
  bedrift_poststed varchar(255),

  arbeidsgiver_fornavn varchar(255),
  arbeidsgiver_etternavn varchar(255),
  arbeidsgiver_epost varchar(255),
  arbeidsgiver_tlf varchar(255),

  veileder_fornavn varchar(255),
  veileder_etternavn varchar(255),
  veileder_epost varchar(255),
  veileder_tlf varchar(255),

  oppfolging varchar(255),
  tilrettelegging varchar(255),

  start_dato_tidspunkt timestamp without time zone,
  arbeidstrening_lengde integer,
  arbeidstrening_stillingprosent integer,

  bekreftet_av_bruker boolean,
  bekreftet_av_arbeidsgiver boolean,
  bekreftet_av_veileder boolean
);

CREATE TABLE maal (
  id serial primary key,
  opprettet_tidspunkt timestamp without time zone not null default now(),
  kategori varchar(255),
  beskrivelse varchar(255),
  avtale integer references avtale(id)
);


CREATE TABLE oppgave (
  id serial primary key,
  opprettet_tidspunkt timestamp without time zone not null default now(),
  tittel varchar(255),
  beskrivelse varchar(255),
  opplaering varchar(255),
  avtale integer references avtale(id)
);
