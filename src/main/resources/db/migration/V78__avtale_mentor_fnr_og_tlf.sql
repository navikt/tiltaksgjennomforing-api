alter table avtale add column mentor_fnr varchar(11) default null;
alter table avtale_innhold add column godkjent_av_mentor timestamp without time zone default null;
alter table avtale_innhold add column mentor_tlf varchar(255) default null;
