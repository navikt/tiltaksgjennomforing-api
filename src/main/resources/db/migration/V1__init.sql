CREATE TABLE person (
	id serial primary key,
	fornavn varchar(255) not null,
	etternavn varchar(255) not null
);

insert into PERSON (fornavn, etternavn) values ('Donald', 'Duck');