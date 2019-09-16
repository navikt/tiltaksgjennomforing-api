alter table avtale add column base_avtale_id varchar(128) ;
update avtale set base_avtale_id = id;
alter table avtale add column revisjon integer default 0;
