alter table avtale add column base_avtale_id uuid;
update avtale set base_avtale_id = id;
