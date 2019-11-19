insert into avtale (id, opprettet_tidspunkt, versjon, deltaker_fornavn, deltaker_etternavn, deltaker_fnr, bedrift_navn,
                    bedrift_nr, arbeidsgiver_fnr, arbeidsgiver_fornavn, arbeidsgiver_etternavn, arbeidsgiver_tlf,
                    veileder_nav_ident, veileder_fornavn, veileder_etternavn, veileder_tlf, oppfolging, tilrettelegging,
                    godkjent_av_deltaker, godkjent_av_arbeidsgiver, godkjent_av_veileder, tiltakstype)
values ('6ae3be81-abcd-477e-a8f3-4a5eb5fe91e3', current_timestamp, 7, 'Didrik', 'Deltaker', '01093434109',
        'Fiskebåten', '999999999', '29118923330', 'Filip', 'Fisker', '22334455', 'X123456', 'Vera', 'Veileder',
        '33445566', 'Ingen', 'Ingen', null, null, null, 'ARBEIDSTRENING');


insert into avtale (id, opprettet_tidspunkt, versjon, deltaker_fornavn, deltaker_etternavn, deltaker_fnr, deltaker_tlf,
                    bedrift_navn,
                    bedrift_nr, arbeidsgiver_fnr, arbeidsgiver_fornavn, arbeidsgiver_etternavn, arbeidsgiver_tlf,
                    veileder_nav_ident, veileder_fornavn, veileder_etternavn, veileder_tlf, oppfolging, tilrettelegging,
                    godkjent_av_deltaker, godkjent_av_arbeidsgiver, godkjent_av_veileder, start_dato, slutt_dato,
                    stillingprosent, tiltakstype)
values ('5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3', current_timestamp, 7, 'Dagny', 'Deltaker', '01093434109', '00000000',
        'Pers butikk', '975959171', '29118923330', 'Per', 'Kremmer', '22334455', 'X123456', 'Vera', 'Veileder',
        '33445566', 'Telefon hver uke', 'Ingen', null, current_timestamp, null, '2019-03-25', '2019-05-25', 100,
        'ARBEIDSTRENING');


insert into maal(id, kategori, beskrivelse, avtale)
values ('e16350f6-27ea-49b3-9fbc-25fcee0dd080', 'Arbeidserfaring', 'Lære butikkarbeid',
        '5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');

insert into oppgave (id, tittel, beskrivelse, opplaering, avtale)
values ('86a83e6d-e668-4073-a1ac-88885ae4df90', 'Lager', 'Rydde på lageret', 'Ryddekurs',
        '5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');

insert into avtale (id, opprettet_tidspunkt, versjon, deltaker_fornavn, deltaker_etternavn, deltaker_fnr, deltaker_tlf,
                    bedrift_navn, bedrift_nr, arbeidsgiver_fnr, arbeidsgiver_fornavn, arbeidsgiver_etternavn,
                    arbeidsgiver_tlf, veileder_nav_ident, veileder_fornavn, veileder_etternavn, veileder_tlf,
                    oppfolging, tilrettelegging, godkjent_av_deltaker, godkjent_av_arbeidsgiver, godkjent_av_veileder,
                    journalpost_id, start_dato, slutt_dato, stillingprosent, tiltakstype)
values ('ca3d7189-0852-4693-a3dd-d518b4ec42e4', current_timestamp, 7, 'Ronny', 'Deltaker', '01093434109', '00000000',
        'Ronnys butikk', '975959171', '29118923330', 'Ronnys', 'Kremmer', '22334455', 'X123456', 'Ronny', 'Veileder',
        '33445566', 'Telefon hver uke', 'Ingen', current_timestamp, current_timestamp, current_timestamp, null,
        '2019-03-25', '2019-05-25', 100, 'ARBEIDSTRENING');

insert into maal(id, kategori, beskrivelse, avtale)
values ('ca3d7189-0852-4693-a3dd-d518b4ec42e4', 'Arbeidserfaring', 'Lære butikkarbeid',
        '5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');

insert into oppgave (id, tittel, beskrivelse, opplaering, avtale)
values ('ca3d7189-0852-4693-a3dd-d518b4ec42e4', 'Lager', 'Rydde på lageret', 'Ryddekurs',
        '5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');

insert into avtale (id, opprettet_tidspunkt, versjon, deltaker_fornavn, deltaker_etternavn, deltaker_fnr, deltaker_tlf,
                    bedrift_navn, bedrift_nr, arbeidsgiver_fnr, arbeidsgiver_fornavn, arbeidsgiver_etternavn,
                    arbeidsgiver_tlf, veileder_nav_ident, veileder_fornavn, veileder_etternavn, veileder_tlf,
                    oppfolging, tilrettelegging, godkjent_av_deltaker, godkjent_av_arbeidsgiver, godkjent_av_veileder,
                    godkjent_pa_vegne_av,
                    journalpost_id, start_dato, slutt_dato, stillingprosent, tiltakstype, digital_kompetanse, ikke_bank_id, reservert)
values ('8238bedf-d6d9-4145-bcdc-cf857f4bc63f', current_timestamp, 7, 'Kenneth', 'Deltaker', '01093434109', '00000000',
        'Kenneths butikk', '975959171', '29118923330', 'Kenneths', 'Kremmer', '22334455', 'X123456', 'Kenneth',
        'Veileder', '33445566', 'Telefon hver uke', 'Ingen', current_timestamp, current_timestamp, current_timestamp,
        true, null, '2019-03-25', '2019-05-25', 100, 'MIDLERTIDIG_LONNSTILSKUDD', true, true, true);

insert into maal(id, kategori, beskrivelse, avtale)
values ('2cd6fd24-9369-44cd-b8e3-10c4f53762f2', 'Arbeidserfaring', 'Lære butikkarbeid',
        '8238bedf-d6d9-4145-bcdc-cf857f4bc63f');

insert into oppgave (id, tittel, beskrivelse, opplaering, avtale)
values ('2cd6fd24-9369-44cd-b8e3-10c4f53762f2', 'Lager', 'Rydde på lageret', 'Ryddekurs',
        '8238bedf-d6d9-4145-bcdc-cf857f4bc63f');