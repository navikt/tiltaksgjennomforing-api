insert into avtale (ID, OPPRETTET_TIDSPUNKT, VERSJON, DELTAKER_FORNAVN, DELTAKER_ETTERNAVN, DELTAKER_FNR, BEDRIFT_NAVN,
                    BEDRIFT_NR, ARBEIDSGIVER_FNR, ARBEIDSGIVER_FORNAVN, ARBEIDSGIVER_ETTERNAVN, ARBEIDSGIVER_TLF,
                    VEILEDER_NAV_IDENT, VEILEDER_FORNAVN, VEILEDER_ETTERNAVN, VEILEDER_TLF, OPPFOLGING, TILRETTELEGGING,
                    START_DATO, ARBEIDSTRENING_LENGDE, ARBEIDSTRENING_STILLINGPROSENT, GODKJENT_AV_DELTAKER,
                    GODKJENT_AV_ARBEIDSGIVER, GODKJENT_AV_VEILEDER)
values ('6ae3be81-abcd-477e-a8f3-4a5eb5fe91e3', current_timestamp, 7, 'Didrik', 'Deltaker', '01093434109',
        'Fiskebåten', '999999999', '29118923330', 'Filip', 'Fisker', '22334455', 'Z123456', 'Vera', 'Veileder',
        '33445566', 'Ingen', 'Ingen', '2019-03-25', 2, 100, null, null, null);

insert into maal(ID, KATEGORI, BESKRIVELSE, AVTALE)
values ('d16350f6-27ea-49b3-9fbc-25fcee0dd080', 'Arbeidserfaring', 'Sette sjøbein.',
        '6ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');

insert into oppgave (ID, TITTEL, BESKRIVELSE, OPPLAERING, AVTALE)
values ('76a83e6d-e668-4073-a1ac-88885ae4df90', 'Fiskemann', 'Trekke opp garn. Pilking.', 'Opplæring underveis.',
        '6ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');


insert into avtale (ID, OPPRETTET_TIDSPUNKT, VERSJON, DELTAKER_FORNAVN, DELTAKER_ETTERNAVN, DELTAKER_FNR, DELTAKER_TLF, BEDRIFT_NAVN,
                    BEDRIFT_NR, ARBEIDSGIVER_FNR, ARBEIDSGIVER_FORNAVN, ARBEIDSGIVER_ETTERNAVN, ARBEIDSGIVER_TLF,
                    VEILEDER_NAV_IDENT, VEILEDER_FORNAVN, VEILEDER_ETTERNAVN, VEILEDER_TLF, OPPFOLGING, TILRETTELEGGING,
                    START_DATO, ARBEIDSTRENING_LENGDE, ARBEIDSTRENING_STILLINGPROSENT, GODKJENT_AV_DELTAKER,
                    GODKJENT_AV_ARBEIDSGIVER, GODKJENT_AV_VEILEDER)
values ('5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3', current_timestamp, 7, 'Dagny', 'Deltaker', '01093434109', '00000000',
        'Pers butikk', '975959171', '29118923330', 'Per', 'Kremmer', '22334455', 'X123456', 'Vera', 'Veileder',
        '33445566', 'Telefon hver uke', 'Ingen', '2019-03-25', 2, 100, current_timestamp, current_timestamp, null);

insert into maal(ID, KATEGORI, BESKRIVELSE, AVTALE)
values ('e16350f6-27ea-49b3-9fbc-25fcee0dd080', 'Arbeidserfaring', 'Lære butikkarbeid',
        '5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');

insert into oppgave (ID, TITTEL, BESKRIVELSE, OPPLAERING, AVTALE)
values ('86a83e6d-e668-4073-a1ac-88885ae4df90', 'Lager', 'Rydde på lageret', 'Ryddekurs',
        '5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');

insert into avtale (ID, OPPRETTET_TIDSPUNKT, VERSJON, DELTAKER_FORNAVN, DELTAKER_ETTERNAVN, DELTAKER_FNR, DELTAKER_TLF, BEDRIFT_NAVN,
                    BEDRIFT_NR, ARBEIDSGIVER_FNR, ARBEIDSGIVER_FORNAVN, ARBEIDSGIVER_ETTERNAVN, ARBEIDSGIVER_TLF,
                    VEILEDER_NAV_IDENT, VEILEDER_FORNAVN, VEILEDER_ETTERNAVN, VEILEDER_TLF, OPPFOLGING, TILRETTELEGGING,
                    START_DATO, ARBEIDSTRENING_LENGDE, ARBEIDSTRENING_STILLINGPROSENT, GODKJENT_AV_DELTAKER,
                    GODKJENT_AV_ARBEIDSGIVER, GODKJENT_AV_VEILEDER, JOURNALPOST_ID)
values ('ca3d7189-0852-4693-a3dd-d518b4ec42e4', current_timestamp, 7, 'Ronny', 'Deltaker', '01093434109', '00000000',
        'Ronnys butikk', '975959171', '29118923330', 'Ronnys', 'Kremmer', '22334455', 'X123456', 'Ronny', 'Veileder',
        '33445566', 'Telefon hver uke', 'Ingen', '2019-03-25', 2, 100, current_timestamp, current_timestamp, current_timestamp, null);

insert into maal(ID, KATEGORI, BESKRIVELSE, AVTALE)
values ('ca3d7189-0852-4693-a3dd-d518b4ec42e4', 'Arbeidserfaring', 'Lære butikkarbeid',
        '5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');

insert into oppgave (ID, TITTEL, BESKRIVELSE, OPPLAERING, AVTALE)
values ('ca3d7189-0852-4693-a3dd-d518b4ec42e4', 'Lager', 'Rydde på lageret', 'Ryddekurs',
        '5ae3be81-abcd-477e-a8f3-4a5eb5fe91e3');

insert into avtale (ID, OPPRETTET_TIDSPUNKT, VERSJON, DELTAKER_FORNAVN, DELTAKER_ETTERNAVN, DELTAKER_FNR, DELTAKER_TLF, BEDRIFT_NAVN,
                    BEDRIFT_NR, ARBEIDSGIVER_FNR, ARBEIDSGIVER_FORNAVN, ARBEIDSGIVER_ETTERNAVN, ARBEIDSGIVER_TLF,
                    VEILEDER_NAV_IDENT, VEILEDER_FORNAVN, VEILEDER_ETTERNAVN, VEILEDER_TLF, OPPFOLGING, TILRETTELEGGING,
                    START_DATO, ARBEIDSTRENING_LENGDE, ARBEIDSTRENING_STILLINGPROSENT, GODKJENT_AV_DELTAKER,
                    GODKJENT_AV_ARBEIDSGIVER, GODKJENT_AV_VEILEDER, JOURNALPOST_ID)
values ('8238bedf-d6d9-4145-bcdc-cf857f4bc63f', current_timestamp, 7, 'Kenneth', 'Deltaker', '01093434109', '00000000',
        'Kenneths butikk', '975959171', '29118923330', 'Kenneths', 'Kremmer', '22334455', 'X123456', 'Kenneth', 'Veileder',
        '33445566', 'Telefon hver uke', 'Ingen', '2019-03-25', 2, 100, current_timestamp, current_timestamp, current_timestamp, null);

insert into maal(ID, KATEGORI, BESKRIVELSE, AVTALE)
values ('2cd6fd24-9369-44cd-b8e3-10c4f53762f2', 'Arbeidserfaring', 'Lære butikkarbeid',
        '8238bedf-d6d9-4145-bcdc-cf857f4bc63f');

insert into oppgave (ID, TITTEL, BESKRIVELSE, OPPLAERING, AVTALE)
values ('2cd6fd24-9369-44cd-b8e3-10c4f53762f2', 'Lager', 'Rydde på lageret', 'Ryddekurs',
        '8238bedf-d6d9-4145-bcdc-cf857f4bc63f');

insert into godkjent_pa_vegne_grunn (AVTALE, DIGITAL_KOMPETANSE, IKKE_BANK_ID, RESERVERT)
values ('8238bedf-d6d9-4145-bcdc-cf857f4bc63f', 'true', 'true', 'false');