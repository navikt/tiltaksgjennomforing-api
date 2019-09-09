alter table avtale add column base_avtale_id uuid;
update avtale set base_avtale_id = id;
alter table avtale add column godkjent_Versjon integer default 0;
