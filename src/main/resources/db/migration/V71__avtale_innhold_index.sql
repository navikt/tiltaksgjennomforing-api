alter table avtale_innhold add constraint fk_avtale_avtale_innhold foreign key (avtale) references avtale (id);
create index concurrently on avtale_innhold(avtale);