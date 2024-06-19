CREATE TYPE arena_status AS ENUM ('PENDING', 'PROCESSING', 'DONE', 'IGNORED', 'FAILED', 'RETRY');

CREATE TABLE arena_event
(
    id             uuid         primary key,
    arena_id       varchar      not null,
    arena_table    varchar      not null,
    created        timestamp    not null,
    status         arena_status not null,
    retry_count    int          not null,
    operation      varchar      not null,
    operation_time timestamp    not null,
    payload        jsonb        not null
);

CREATE INDEX idx_arena_id_table ON arena_event (arena_id, arena_table);
CREATE INDEX idx_arena_status ON arena_event (status);

CREATE TABLE arena_ords_fnr
(
    person_id int primary key,
    fnr       varchar
);

CREATE TABLE arena_ords_arbeidsgiver
(
    arbgiv_id_arrangor            int primary key,
    virksomhetsnummer             varchar,
    organisasjonsnummerMorselskap varchar
);

CREATE TABLE arena_tiltakssak
(
    sak_id               int primary key,
    sakskode             varchar,
    reg_dato             date,
    reg_user             varchar,
    mod_dato             date,
    mod_user             varchar,
    tabellnavnalias      varchar,
    objekt_id            int,
    aar                  int,
    lopenrsak            int,
    dato_avsluttet       date,
    sakstatuskode        varchar,
    arkivnokkel          varchar,
    aetatenhet_arkiv     varchar,
    arkivhenvisning      varchar,
    brukerid_ansvarlig   varchar,
    aetatenhet_ansvarlig varchar,
    objekt_kode          varchar,
    status_endret        varchar,
    partisjon            varchar,
    er_utland            boolean
);

CREATE TABLE arena_tiltakgjennomforing
(
    tiltakgjennomforing_id         int primary key,
    sak_id                         int not null,
    tiltakskode                    varchar,
    antall_deltakere               int,
    antall_varighet                int,
    dato_fra                       date,
    dato_til                       date,
    fagplankode                    varchar,
    maaleenhet_varighet            varchar,
    tekst_fagbeskrivelse           varchar,
    tekst_kurssted                 varchar,
    tekst_maalgruppe               varchar,
    status_treverdikode_innsokning boolean,
    reg_dato                       date,
    reg_user                       varchar,
    mod_dato                       date,
    mod_user                       varchar,
    lokaltnavn                     varchar,
    tiltakstatuskode               varchar,
    prosent_deltid                 int,
    kommentar                      varchar,
    arbgiv_id_arrangor             int,
    profilelement_id_geografi      varchar,
    klokketid_fremmote             varchar,
    dato_fremmote                  varchar,
    begrunnelse_status             varchar,
    avtale_id                      varchar,
    aktivitet_id                   varchar,
    dato_innsokningstart           date,
    gml_fra_dato                   varchar,
    gml_til_dato                   varchar,
    aetat_fremmotereg              varchar,
    aetat_konteringssted           varchar,
    opplaeringnivaakode            varchar,
    tiltakgjennomforing_id_rel     varchar,
    vurdering_gjennomforing        varchar,
    profilelement_id_oppl_tiltak   varchar,
    dato_oppfolging_ok             varchar,
    partisjon                      varchar,
    maalform_kravbrev              varchar,
    ekstern_id                     varchar,
    FOREIGN KEY (sak_id) REFERENCES arena_tiltakssak (sak_id) ON DELETE CASCADE,
    FOREIGN KEY (arbgiv_id_arrangor) REFERENCES arena_ords_arbeidsgiver (arbgiv_id_arrangor) ON DELETE CASCADE
);

CREATE TABLE arena_tiltakdeltaker
(
    tiltakdeltaker_id             int primary key,
    person_id                     int not null,
    tiltakgjennomforing_id        int not null,
    deltakerstatuskode            varchar,
    deltakertypekode              varchar,
    aarsakverdikode_status        varchar,
    oppmotetypekode               varchar,
    prioritet                     varchar,
    begrunnelse_innsokt           varchar,
    begrunnelse_prioritering      varchar,
    reg_dato                      date,
    reg_user                      varchar,
    mod_dato                      date,
    mod_user                      varchar,
    dato_svarfrist                date,
    dato_fra                      date,
    dato_til                      date,
    begrunnelse_status            varchar,
    prosent_deltid                int,
    brukerid_statusendring        varchar,
    dato_statusendring            date,
    aktivitet_id                  int,
    brukerid_endring_prioritering varchar,
    dato_endring_prioritering     date,
    dokumentkode_siste_brev       varchar,
    status_innsok_pakke           varchar,
    status_opptak_pakke           varchar,
    opplysninger_innsok           varchar,
    partisjon                     varchar,
    begrunnelse_bestilling        varchar,
    antall_dager_pr_uke           varchar,
    FOREIGN KEY (tiltakgjennomforing_id) REFERENCES arena_tiltakgjennomforing (tiltakgjennomforing_id) ON DELETE CASCADE,
    FOREIGN KEY (person_id) REFERENCES arena_ords_fnr (person_id) ON DELETE CASCADE
);
