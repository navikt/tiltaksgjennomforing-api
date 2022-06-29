alter table avtale add column mentor_fnr varchar(11) default null;
alter table avtale add column mentor_signert_taushetserklæring boolean default false;
alter table avtale_innhold add column mentor_tlf varchar(255) default null;
