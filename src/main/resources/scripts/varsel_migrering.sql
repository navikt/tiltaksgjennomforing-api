-- OPPRETTET ARBEIDSGIVER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale er opprettet', avtale_id, hendelse, tidspunkt, true, 'ARBEIDSGIVER', 'DELTAKER'
FROM hendelselogg WHERE hendelse = 'OPPRETTET_AV_ARBEIDSGIVER';

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale er opprettet', avtale_id, hendelse, tidspunkt, false, 'ARBEIDSGIVER', 'ARBEIDSGIVER'
FROM hendelselogg WHERE hendelse = 'OPPRETTET_AV_ARBEIDSGIVER';

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale er opprettet', avtale_id, hendelse, tidspunkt, true, 'ARBEIDSGIVER', 'VEILEDER'
FROM hendelselogg WHERE hendelse = 'OPPRETTET_AV_ARBEIDSGIVER';
-----------------------------------------------------------------------

-- OPPRETTET VEILEDER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er opprettet', avtale_id, varslbar_hendelse_type, tidspunkt, true, 'VEILEDER', 'DELTAKER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'OPPRETTET' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er opprettet', avtale_id, varslbar_hendelse_type, tidspunkt, true, 'VEILEDER', 'ARBEIDSGIVER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'OPPRETTET' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er opprettet', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'OPPRETTET' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);
------------------------------------------

-- GODKJENT_AV_ARBEIDSGIVER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av arbeidsgiver', avtale_id, varslbar_hendelse_type, tidspunkt, true, 'ARBEIDSGIVER', 'VEILEDER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_AV_ARBEIDSGIVER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av arbeidsgiver', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'ARBEIDSGIVER', 'DELTAKER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_AV_ARBEIDSGIVER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av arbeidsgiver', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'ARBEIDSGIVER', 'ARBEIDSGIVER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_AV_ARBEIDSGIVER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);
-----------------------------------------

-- GODKJENT_AV_VEILEDER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av veileder', avtale_id, varslbar_hendelse_type, tidspunkt, true, 'VEILEDER', 'ARBEIDSGIVER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_AV_VEILEDER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av veileder', avtale_id, varslbar_hendelse_type, tidspunkt, true, 'VEILEDER', 'DELTAKER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_AV_VEILEDER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av veileder', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_AV_VEILEDER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);
-------------------------------------------

-- GODKJENT_AV_DELTAKER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av deltaker', avtale_id, varslbar_hendelse_type, tidspunkt, true, 'DELTAKER', 'ARBEIDSGIVER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_AV_DELTAKER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av deltaker', avtale_id, varslbar_hendelse_type, tidspunkt, true, 'DELTAKER', 'VEILEDER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_AV_DELTAKER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av deltaker', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'DELTAKER', 'DELTAKER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_AV_DELTAKER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);
---------------------------------------------

-- GODKJENT_PAA_VEGNE_AV
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av NAV-veileder', avtale_id, varslbar_hendelse_type, tidspunkt, true, 'VEILEDER', 'ARBEIDSGIVER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_PAA_VEGNE_AV' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av NAV-veileder', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_PAA_VEGNE_AV' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale er godkjent av NAV-veileder', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'DELTAKER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENT_PAA_VEGNE_AV' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);
-------------------------------------------

-- GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtalens godkjenninger er opphevet av arbeidsgiver', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'DELTAKER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtalens godkjenninger er opphevet av arbeidsgiver', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtalens godkjenninger er opphevet av arbeidsgiver', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'ARBEIDSGIVER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);
---------------------------------------------

-- GODKJENNINGER_OPPHEVET_AV_VEILEDER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtalens godkjenninger er opphevet av NAV-veileder', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'ARBEIDSGIVER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENNINGER_OPPHEVET_AV_VEILEDER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtalens godkjenninger er opphevet av NAV-veileder', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENNINGER_OPPHEVET_AV_VEILEDER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtalens godkjenninger er opphevet av NAV-veileder', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'DELTAKER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'GODKJENNINGER_OPPHEVET_AV_VEILEDER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);
---------------------------------------------

-- DELT_MED_DELTAKER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale delt med deltaker', avtale_id, varslbar_hendelse_type, tidspunkt, true, 'VEILEDER', 'DELTAKER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'DELT_MED_DELTAKER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale delt med deltaker', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'DELT_MED_DELTAKER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);
--------------------------------------------

-- DELT_MED_ARBEIDSGIVER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale delt med arbeidsgiver', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'ARBEIDSGIVER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'DELT_MED_ARBEIDSGIVER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = varslbar_hendelse.avtale_id), 'Avtale delt med arbeidsgiver', avtale_id, varslbar_hendelse_type, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM varslbar_hendelse WHERE varslbar_hendelse_type = 'DELT_MED_ARBEIDSGIVER' and exists (select 1 from avtale where avtale.id = varslbar_hendelse.avtale_id);
-------------------------------------------

-- AVBRUTT
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, veileder_nav_ident, 'Avtale avbrutt av veileder', ID, 'AVBRUTT', SIST_ENDRET, false, 'VEILEDER', 'VEILEDER'
FROM AVTALE WHERE AVBRUTT = true;

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, deltaker_fnr, 'Avtale avbrutt av veileder', ID, 'AVBRUTT', SIST_ENDRET, true, 'VEILEDER', 'DELTAKER'
FROM AVTALE WHERE AVBRUTT = true;

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, bedrift_nr, 'Avtale avbrutt av veileder', ID, 'AVBRUTT', SIST_ENDRET, true, 'VEILEDER', 'ARBEIDSGIVER'
FROM AVTALE WHERE AVBRUTT = true;
------------------------------------------

-- LÅST_OPP
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = ai1.avtale), 'Avtale låst opp av veileder', ai1.AVTALE, 'LÅST_OPP', (select (ai2.godkjent_av_veileder + interval '00:00:01') from avtale_innhold ai2 where ai2.avtale = ai1.avtale and ai2.versjon = ai1.versjon - 1), true, 'VEILEDER', 'DELTAKER'
FROM AVTALE_INNHOLD ai1 WHERE ai1.VERSJON > 1;

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = ai1.avtale), 'Avtale låst opp av veileder', ai1.AVTALE, 'LÅST_OPP', (select (ai2.godkjent_av_veileder + interval '00:00:01') from avtale_innhold ai2 where ai2.avtale = ai1.avtale and ai2.versjon = ai1.versjon - 1), true, 'VEILEDER', 'ARBEIDSGIVER'
FROM AVTALE_INNHOLD ai1 WHERE ai1.VERSJON > 1;

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = ai1.avtale), 'Avtale låst opp av veileder', ai1.AVTALE, 'LÅST_OPP', (select (ai2.godkjent_av_veileder + interval '00:00:01') from avtale_innhold ai2 where ai2.avtale = ai1.avtale and ai2.versjon = ai1.versjon - 1), false, 'VEILEDER', 'VEILEDER'
FROM AVTALE_INNHOLD ai1 WHERE ai1.VERSJON > 1;
------------------------------------------

-- GJENOPPRETTET
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale gjenopprettet', avtale_id, hendelse, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM hendelselogg WHERE hendelse = 'GJENOPPRETTET';

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale gjenopprettet', avtale_id, hendelse, tidspunkt, true, 'VEILEDER', 'ARBEIDSGIVER'
FROM hendelselogg WHERE hendelse = 'GJENOPPRETTET';

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale gjenopprettet', avtale_id, hendelse, tidspunkt, true, 'VEILEDER', 'DELTAKER'
FROM hendelselogg WHERE hendelse = 'GJENOPPRETTET';
--------------------------------------------

-- NY_VEILEDER
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale tildelt ny veileder', avtale_id, hendelse, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM hendelselogg WHERE hendelse = 'NY_VEILEDER';

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale tildelt ny veileder', avtale_id, hendelse, tidspunkt, true, 'VEILEDER', 'ARBEIDSGIVER'
FROM hendelselogg WHERE hendelse = 'NY_VEILEDER';

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale tildelt ny veileder', avtale_id, hendelse, tidspunkt, true, 'VEILEDER', 'DELTAKER'
FROM hendelselogg WHERE hendelse = 'NY_VEILEDER';
--------------------------------------------

-- AVTALE_FORDELT
INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select veileder_nav_ident from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale tildelt veileder', avtale_id, hendelse, tidspunkt, false, 'VEILEDER', 'VEILEDER'
FROM hendelselogg WHERE hendelse = 'AVTALE_FORDELT';

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select bedrift_nr from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale tildelt veileder', avtale_id, hendelse, tidspunkt, true, 'VEILEDER', 'ARBEIDSGIVER'
FROM hendelselogg WHERE hendelse = 'AVTALE_FORDELT';

INSERT INTO varsel (id, lest, identifikator, tekst, avtale_id, hendelse_type, tidspunkt, bjelle, utført_av, mottaker)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), true, (select deltaker_fnr from avtale where avtale.id = hendelselogg.avtale_id), 'Avtale tildelt veileder', avtale_id, hendelse, tidspunkt, true, 'VEILEDER', 'DELTAKER'
FROM hendelselogg WHERE hendelse = 'AVTALE_FORDELT';