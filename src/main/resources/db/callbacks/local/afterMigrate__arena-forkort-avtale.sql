-- INAKTIV I ARENA, MEN AKTIV HOS OSS

INSERT INTO avtale_innhold (id, avtale, deltaker_fornavn, deltaker_etternavn, deltaker_tlf, bedrift_navn, arbeidsgiver_fornavn, arbeidsgiver_etternavn, arbeidsgiver_tlf, veileder_fornavn, veileder_etternavn, veileder_tlf, oppfolging, tilrettelegging, start_dato, slutt_dato, stillingprosent, journalpost_id, godkjent_av_deltaker, godkjent_av_arbeidsgiver, godkjent_av_veileder, godkjent_pa_vegne_av, ikke_bank_id, reservert, digital_kompetanse, arbeidsgiver_kontonummer, stillingstittel, arbeidsoppgaver, lonnstilskudd_prosent, manedslonn, feriepengesats, arbeidsgiveravgift, versjon, mentor_fornavn, mentor_etternavn, mentor_oppgaver, mentor_antall_timer, mentor_timelonn, har_familietilknytning, familietilknytning_forklaring, feriepenger_belop, otp_belop, arbeidsgiveravgift_belop, sum_lonnsutgifter, sum_lonnstilskudd, stillingstype, stilling_styrk08, stilling_konsept_id, manedslonn100pst, otp_sats, godkjent_av_nav_ident, sum_lønnstilskudd_redusert, dato_for_redusert_prosent, antall_dager_per_uke, ikrafttredelsestidspunkt, avtale_inngått, godkjent_av_beslutter, godkjent_av_beslutter_nav_ident, innhold_type, godkjent_pa_vegne_av_arbeidsgiver, klarer_ikke_gi_fa_tilgang, vet_ikke_hvem_som_kan_gi_tilgang, far_ikke_tilgang_personvern, enhet_kostnadssted, enhetsnavn_kostnadssted, refusjon_kontaktperson_fornavn, refusjon_kontaktperson_etternavn, refusjon_kontaktperson_tlf, ønsker_varsling_om_refusjon, inkluderingstilskudd_begrunnelse, godkjent_taushetserklæring_av_mentor, mentor_tlf, arena_migrering_deltaker, arena_migrering_arbeidsgiver)
VALUES ('cc096571-ccbc-4445-8c96-2769d835e9a0', null, 'Dennis', 'Deltaker', '40000000', 'Olavs butikk', 'Olav', 'Kremmer', '99999999', 'Vera', 'Veileder', '44444444', 'Telefon hver uke', 'Ingen', CURRENT_DATE, DATEADD('DAY', 30, CURRENT_DATE), 50, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, null, null, null, null, 'Butikkbetjent', 'Butikkarbeid', null, null, null, null, 1, null, null, null, null, null, null, null, null, null, null, null, null, null, 5223, 112968, null, null, 'Q987654', null, null, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null, null, 'INNGÅ', false, null, null, null, null, null, 'Donald', 'Duck', '55555123', true, null, null, null, null, null)
ON CONFLICT DO NOTHING;

INSERT INTO avtale (id, opprettet_tidspunkt, deltaker_fnr, bedrift_nr, arbeidsgiver_fnr, veileder_nav_ident, arbeidstrening_lengde, gammel_godkjent_av_deltaker, gammel_godkjent_av_arbeidsgiver, gammel_godkjent_av_veileder, avbrutt, tiltakstype, sist_endret, avbrutt_dato, avbrutt_grunn, enhet_oppfolging, enhet_geografisk, slettemerket, annullert_tidspunkt, annullert_grunn, feilregistrert, enhetsnavn_geografisk, enhetsnavn_oppfolging, kvalifiseringsgruppe, formidlingsgruppe, godkjent_for_etterregistrering, gjeldende_innhold_id, mentor_fnr, opphav)
VALUES ('206cb3fa-f1f2-4da5-ab6b-0caf847fbf1c', CURRENT_TIMESTAMP, '27090438663', '922407048', null, 'Z123456', null, null, null, null, false, 'ARBEIDSTRENING', CURRENT_TIMESTAMP, null, null, null, null, false, null, null, false, null, null, null, null, false, 'cc096571-ccbc-4445-8c96-2769d835e9a0', null, 'VEILEDER')
ON CONFLICT DO NOTHING;

UPDATE avtale_innhold SET avtale='206cb3fa-f1f2-4da5-ab6b-0caf847fbf1c' WHERE id='cc096571-ccbc-4445-8c96-2769d835e9a0';

INSERT INTO arena_ords_fnr (person_id, fnr)
VALUES (1008, '27090438663')
ON CONFLICT DO NOTHING;

INSERT INTO arena_ords_arbeidsgiver (arbgiv_id_arrangor, virksomhetsnummer, organisasjonsnummer_morselskap)
VALUES (1008, '922407048', '123456789')
ON CONFLICT DO NOTHING;

INSERT INTO arena_tiltakssak (sak_id, sakskode, reg_dato, reg_user, mod_dato, mod_user, tabellnavnalias, objekt_id, aar, lopenrsak, dato_avsluttet, sakstatuskode, arkivnokkel, aetatenhet_arkiv, arkivhenvisning, brukerid_ansvarlig, aetatenhet_ansvarlig, objekt_kode, status_endret, partisjon, er_utland)
VALUES (1008, 'TILT', CURRENT_DATE, 'ARBLINJE', CURRENT_DATE, 'BRUKER', 'SAK', 13193683, 2024, 2244, null, 'AKTIV', null, null, null, 'BRUKER', '1219', null, CURRENT_TIMESTAMP, null, false)
ON CONFLICT DO NOTHING;

INSERT INTO arena_tiltakgjennomforing(tiltakgjennomforing_id, sak_id, tiltakskode, antall_deltakere, antall_varighet, dato_fra, dato_til, fagplankode, maaleenhet_varighet, tekst_fagbeskrivelse, tekst_kurssted, tekst_maalgruppe, status_treverdikode_innsokning, reg_dato, reg_user, mod_dato, mod_user, lokaltnavn, tiltakstatuskode, prosent_deltid, kommentar, arbgiv_id_arrangor, profilelement_id_geografi, klokketid_fremmote, dato_fremmote, begrunnelse_status, avtale_id, aktivitet_id, dato_innsokningstart, gml_fra_dato, gml_til_dato, aetat_fremmotereg, aetat_konteringssted, opplaeringnivaakode, tiltakgjennomforing_id_rel, vurdering_gjennomforing, profilelement_id_oppl_tiltak, dato_oppfolging_ok, partisjon, maalform_kravbrev, ekstern_id)
VALUES (1008, 1008, 'ARBTREN', 1, null, CURRENT_DATE, DATEADD('DAY', 10, CURRENT_DATE), null, null, null, null, null, null, CURRENT_DATE, 'ARBLINJE', CURRENT_DATE, 'BRUKER', null, 'GJENNOMFOR', 100, null, 1008, null, null, null, null, null, '133292332', null, null, null, '1219', '1219', null, null, null, null, null, null, 'NO', '206cb3fa-f1f2-4da5-ab6b-0caf847fbf1c')
ON CONFLICT DO NOTHING;

INSERT INTO arena_tiltakdeltaker(tiltakdeltaker_id, person_id, tiltakgjennomforing_id, deltakerstatuskode, deltakertypekode, aarsakverdikode_status, oppmotetypekode, prioritet, begrunnelse_innsokt, begrunnelse_prioritering, reg_dato, reg_user, mod_dato, mod_user, dato_svarfrist, dato_fra, dato_til, begrunnelse_status, prosent_deltid, brukerid_statusendring, dato_statusendring, aktivitet_id, brukerid_endring_prioritering, dato_endring_prioritering, dokumentkode_siste_brev, status_innsok_pakke, status_opptak_pakke, opplysninger_innsok, partisjon, begrunnelse_bestilling, antall_dager_pr_uke, ekstern_id)
VALUES (1008, 1008, 1008, 'GJENN', 'INNSOKT', null, null, null, 'Syntetisert rettighet', null, CURRENT_DATE, 'BRUKER', CURRENT_DATE, 'BRUKER', null, CURRENT_DATE, DATEADD('DAY', 10, CURRENT_DATE), null, 100, 'BRUKER', CURRENT_DATE, 1, null, null, null, null, null, null, null, null, null, '206cb3fa-f1f2-4da5-ab6b-0caf847fbf1c')
ON CONFLICT DO NOTHING;