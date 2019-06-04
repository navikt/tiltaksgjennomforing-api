alter table avtale add dato_godkjent_deltaker timestamp without time zone;
alter table avtale add dato_godkjent_arbeidsgiver timestamp without time zone;
alter table avtale add dato_godkjent_veileder timestamp without time zone;

update avtale set dato_godkjent_deltaker = '2019-01-01T00:00:00.000' where godkjent_av_deltaker is true;
update avtale set dato_godkjent_arbeidsgiver = '2019-01-01T00:00:00.000' where godkjent_av_arbeidsgiver is true;
update avtale set dato_godkjent_veileder = '2019-01-01T00:00:00.000' where godkjent_av_veileder is true;


update avtale set godkjent_av_deltaker = null;
update avtale set godkjent_av_arbeidsgiver = null;
update avtale set godkjent_av_veileder = null;

alter table avtale add godkjent_pa_vegne_av boolean default false;

alter table avtale alter column godkjent_av_deltaker TYPE timestamp without time zone;
alter table avtale alter column godkjent_av_arbeidsgiver TYPE timestamp without time zone;
alter table avtale alter column godkjent_av_veileder TYPE timestamp without time zone;

update avtale set godkjent_av_deltaker = dato_godkjent_deltaker;
update avtale set godkjent_av_arbeidsgiver = dato_godkjent_arbeidsgiver;
update avtale set godkjent_av_veileder = dato_godkjent_veileder;

alter table avtale drop column dato_godkjent_deltaker;
alter table avtale drop column dato_godkjent_arbeidsgiver;
alter table avtale drop column dato_godkjent_veileder;


create table godkjent_pa_vegne_grunn (
    avtale uuid primary key references avtale(id),
    ikke_min_id boolean default false,
    reservert boolean default false,
    digital_kompetanse boolean default false
);
