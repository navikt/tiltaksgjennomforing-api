/* INAKTIV I ARENA SOM IKKE EKSISTERER HOS OSS */

INSERT INTO arena_ords_fnr (person_id, fnr)
VALUES (1002, '20087334985')
ON CONFLICT DO NOTHING;

INSERT INTO arena_ords_arbeidsgiver (arbgiv_id_arrangor, virksomhetsnummer, organisasjonsnummer_morselskap)
VALUES (1002, '977273939', '123456789')
ON CONFLICT DO NOTHING;

INSERT INTO arena_tiltakssak (sak_id, sakskode, reg_dato, reg_user, mod_dato, mod_user, tabellnavnalias, objekt_id, aar, lopenrsak, dato_avsluttet, sakstatuskode, arkivnokkel, aetatenhet_arkiv, arkivhenvisning, brukerid_ansvarlig, aetatenhet_ansvarlig, objekt_kode, status_endret, partisjon, er_utland)
VALUES (1002, 'TILT', CURRENT_DATE, 'ARBLINJE', CURRENT_DATE, 'BRUKER', 'SAK', 13193683, 2024, 2244, null, 'AKTIV', null, null, null, 'BRUKER', '1219', null, CURRENT_TIMESTAMP(), null, false)
ON CONFLICT DO NOTHING;

INSERT INTO arena_tiltakgjennomforing(tiltakgjennomforing_id, sak_id, tiltakskode, antall_deltakere, antall_varighet, dato_fra, dato_til, fagplankode, maaleenhet_varighet, tekst_fagbeskrivelse, tekst_kurssted, tekst_maalgruppe, status_treverdikode_innsokning, reg_dato, reg_user, mod_dato, mod_user, lokaltnavn, tiltakstatuskode, prosent_deltid, kommentar, arbgiv_id_arrangor, profilelement_id_geografi, klokketid_fremmote, dato_fremmote, begrunnelse_status, avtale_id, aktivitet_id, dato_innsokningstart, gml_fra_dato, gml_til_dato, aetat_fremmotereg, aetat_konteringssted, opplaeringnivaakode, tiltakgjennomforing_id_rel, vurdering_gjennomforing, profilelement_id_oppl_tiltak, dato_oppfolging_ok, partisjon, maalform_kravbrev, ekstern_id)
VALUES (1002, 1002, 'ARBTREN', 1, null, CURRENT_DATE, DATEADD('DAY', 10, CURRENT_DATE), null, null, null, null, null, null, CURRENT_DATE, 'ARBLINJE', CURRENT_DATE, 'BRUKER', null, 'PLANLAGT', 100, null, 1002, null, null, null, null, null, '133292332', null, null, null, '1219', '1219', null, null, null, null, null, null, 'NO', null)
ON CONFLICT DO NOTHING;

INSERT INTO arena_tiltakdeltaker(tiltakdeltaker_id, person_id, tiltakgjennomforing_id, deltakerstatuskode, deltakertypekode, aarsakverdikode_status, oppmotetypekode, prioritet, begrunnelse_innsokt, begrunnelse_prioritering, reg_dato, reg_user, mod_dato, mod_user, dato_svarfrist, dato_fra, dato_til, begrunnelse_status, prosent_deltid, brukerid_statusendring, dato_statusendring, aktivitet_id, brukerid_endring_prioritering, dato_endring_prioritering, dokumentkode_siste_brev, status_innsok_pakke, status_opptak_pakke, opplysninger_innsok, partisjon, begrunnelse_bestilling, antall_dager_pr_uke, ekstern_id)
VALUES (1002, 1002, 1002, 'IKKAKTUELL', 'INNSOKT', null, null, null, 'Syntetisert rettighet', null, CURRENT_DATE, 'BRUKER', CURRENT_DATE, 'BRUKER', null, CURRENT_DATE, DATEADD('DAY', 10, CURRENT_DATE), null, 100, 'GRENSESN', CURRENT_DATE, 1, null, null, null, null, null, null, null, null, null, null)
ON CONFLICT DO NOTHING;
