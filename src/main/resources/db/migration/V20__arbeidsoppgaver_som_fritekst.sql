alter table avtale_innhold add column arbeidsoppgaver varchar;

update avtale_innhold ai set arbeidsoppgaver = SELECT string_agg(tekst, stringdecode('\n\n')) FROM (select avtale_innhold, tittel || stringdecode('\nBeskrivelse: ') || beskrivelse || stringdecode('\nOpplæring: ') || opplaering as tekst from oppgave) where avtale_innhold = ai.id GROUP BY avtale_innhold;
