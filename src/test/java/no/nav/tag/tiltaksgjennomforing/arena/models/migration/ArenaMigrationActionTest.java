package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArenaMigrationActionTest {

    private Avtale avtale;
    private ArenaAgreementAggregate agreementAggregate;

    @BeforeEach
    void setUp() {
        avtale = mock(Avtale.class);
        agreementAggregate = mock(ArenaAgreementAggregate.class);
        Now.resetClock();
    }

    @AfterEach
    void tearDown() {
        Now.resetClock();
    }

    @Nested
    class WhenDublett {
        @Test
        void skal_returnere_IGNORER_naar_agreementAggregate_er_dublett() {
            when(agreementAggregate.isDublett()).thenReturn(true);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.IGNORER);
        }
    }

    @Nested
    class WhenDeltakerstatuskodeGjennOrTilbud {
        @BeforeEach
        void setUp() {
            when(agreementAggregate.isDublett()).thenReturn(false);
        }

        @Test
        void skal_returnere_OPPRETT_naar_avtalestatus_er_ANNULLERT_og_feilregistrert() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN);
            when(avtale.getStatus()).thenReturn(Status.ANNULLERT);
            when(avtale.isFeilregistrert()).thenReturn(true);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.OPPRETT);
        }

        @Test
        void skal_returnere_OPPDATER_naar_avtalestatus_er_ANNULLERT_og_ikke_feilregistrert() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN);
            when(avtale.getStatus()).thenReturn(Status.ANNULLERT);
            when(avtale.isFeilregistrert()).thenReturn(false);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
        }

        @Test
        void skal_returnere_OPPDATER_naar_avtalestatus_er_GJENNOMFOERES_med_GJENN() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
        }

        @Test
        void skal_returnere_OPPDATER_naar_avtalestatus_er_GJENNOMFOERES_med_TILBUD() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.TILBUD);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
        }

        @Test
        void skal_returnere_OPPDATER_naar_avtalestatus_er_PAABEGYNT() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN);
            when(avtale.getStatus()).thenReturn(Status.PÅBEGYNT);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
        }

        @Test
        void skal_returnere_OPPDATER_naar_avtalestatus_er_MANGLER_GODKJENNING() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN);
            when(avtale.getStatus()).thenReturn(Status.MANGLER_GODKJENNING);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
        }

        @Test
        void skal_returnere_OPPDATER_naar_avtalestatus_er_KLAR_FOR_OPPSTART() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN);
            when(avtale.getStatus()).thenReturn(Status.KLAR_FOR_OPPSTART);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
        }

        @Test
        void skal_returnere_OPPDATER_naar_avtalestatus_er_AVSLUTTET() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN);
            when(avtale.getStatus()).thenReturn(Status.AVSLUTTET);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
        }

        @Test
        void skal_kaste_exception_naar_avtalestatus_er_null() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN);
            when(agreementAggregate.getTiltakstatuskode()).thenReturn(Tiltakstatuskode.GJENNOMFOR);
            when(avtale.getStatus()).thenReturn(null);

            assertThatThrownBy(() -> ArenaMigrationAction.map(avtale, agreementAggregate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fikk ugyldig kombinasjon av tiltakstatuskode");
        }
    }

    @Nested
    class WhenDeltakerstatuskodeFullfOrGjennAvb {
        @BeforeEach
        void setUp() {
            when(agreementAggregate.isDublett()).thenReturn(false);
        }

        @Nested
        class MedSluttdatoIAar {
            @BeforeEach
            void setUp() {
                when(agreementAggregate.isSluttdatoIAar()).thenReturn(true);
            }

            @Test
            void skal_returnere_OPPRETT_naar_ANNULLERT_og_feilregistrert_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(avtale.getStatus()).thenReturn(Status.ANNULLERT);
                when(avtale.isFeilregistrert()).thenReturn(true);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.OPPRETT);
            }

            @Test
            void skal_returnere_OPPDATER_naar_ANNULLERT_og_ikke_feilregistrert_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(avtale.getStatus()).thenReturn(Status.ANNULLERT);
                when(avtale.isFeilregistrert()).thenReturn(false);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
            }

            @Test
            void skal_returnere_OPPDATER_naar_AVSLUTTET_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(avtale.getStatus()).thenReturn(Status.AVSLUTTET);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
            }

            @Test
            void skal_returnere_OPPDATER_naar_GJENNOMFOERES_med_GJENN_AVB() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN_AVB);
                when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
            }

            @Test
            void skal_returnere_OPPDATER_naar_PAABEGYNT_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(avtale.getStatus()).thenReturn(Status.PÅBEGYNT);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
            }

            @Test
            void skal_returnere_OPPDATER_naar_KLAR_FOR_OPPSTART_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(avtale.getStatus()).thenReturn(Status.KLAR_FOR_OPPSTART);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.OPPDATER);
            }

            @Test
            void skal_kaste_exception_naar_avtalestatus_er_null_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(agreementAggregate.getTiltakstatuskode()).thenReturn(Tiltakstatuskode.AVSLUTT);
                when(avtale.getStatus()).thenReturn(null);

                assertThatThrownBy(() -> ArenaMigrationAction.map(avtale, agreementAggregate))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Fikk ugyldig kombinasjon av tiltakstatuskode");
            }
        }

        @Nested
        class UtenSluttdatoIAar {
            @BeforeEach
            void setUp() {
                when(agreementAggregate.isSluttdatoIAar()).thenReturn(false);
            }

            @Test
            void skal_returnere_IGNORER_naar_ANNULLERT_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(avtale.getStatus()).thenReturn(Status.ANNULLERT);
                when(avtale.isFeilregistrert()).thenReturn(false);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.IGNORER);
            }

            @Test
            void skal_returnere_IGNORER_naar_ANNULLERT_og_feilregistrert_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(avtale.getStatus()).thenReturn(Status.ANNULLERT);
                when(avtale.isFeilregistrert()).thenReturn(true);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.IGNORER);
            }

            @Test
            void skal_returnere_IGNORER_naar_AVSLUTTET_med_GJENN_AVB() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN_AVB);
                when(avtale.getStatus()).thenReturn(Status.AVSLUTTET);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.IGNORER);
            }

            @Test
            void skal_returnere_AVSLUTT_naar_GJENNOMFOERES_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.AVSLUTT);
            }

            @Test
            void skal_returnere_AVSLUTT_naar_PAABEGYNT_med_GJENN_AVB() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN_AVB);
                when(avtale.getStatus()).thenReturn(Status.PÅBEGYNT);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.AVSLUTT);
            }

            @Test
            void skal_returnere_AVSLUTT_naar_KLAR_FOR_OPPSTART_med_FULLF() {
                when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FULLF);
                when(avtale.getStatus()).thenReturn(Status.KLAR_FOR_OPPSTART);

                ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

                assertThat(result).isEqualTo(ArenaMigrationAction.AVSLUTT);
            }
        }
    }

    @Nested
    class WhenDeltakerstatuskodeNull {
        @BeforeEach
        void setUp() {
            when(agreementAggregate.isDublett()).thenReturn(false);
        }

        @Test
        void skal_kaste_exception_naar_deltakerstatuskode_er_null() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(null);
            when(agreementAggregate.getTiltakstatuskode()).thenReturn(Tiltakstatuskode.GJENNOMFOR);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            assertThatThrownBy(() -> ArenaMigrationAction.map(avtale, agreementAggregate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fikk ugyldig kombinasjon av tiltakstatuskode");
        }
    }

    @Nested
    class WhenDeltakerstatuskodeDefault {
        @BeforeEach
        void setUp() {
            when(agreementAggregate.isDublett()).thenReturn(false);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_AKTUELL() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.AKTUELL);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_AVSLAG() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.AVSLAG);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_DELAVB() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.DELAVB);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_FEILREG() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.FEILREG);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_GJENN_AVL() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.GJENN_AVL);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_IKKAKTUELL() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.IKKAKTUELL);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_IKKEM() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.IKKEM);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_INFOMOETE() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.INFOMOETE);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_JATAKK() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.JATAKK);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_NEITAKK() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.NEITAKK);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }

        @Test
        void skal_returnere_ANNULLER_naar_deltakerstatuskode_er_VENTELISTE() {
            when(agreementAggregate.getDeltakerstatuskode()).thenReturn(Deltakerstatuskode.VENTELISTE);
            when(avtale.getStatus()).thenReturn(Status.GJENNOMFØRES);

            ArenaMigrationAction result = ArenaMigrationAction.map(avtale, agreementAggregate);

            assertThat(result).isEqualTo(ArenaMigrationAction.ANNULLER);
        }
    }
}
