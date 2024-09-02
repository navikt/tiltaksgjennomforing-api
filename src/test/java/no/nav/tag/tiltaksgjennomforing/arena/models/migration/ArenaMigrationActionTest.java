package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;
import no.nav.tag.tiltaksgjennomforing.avtale.AnnullertGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArenaMigrationActionTest {

    @Test
    void tiltaksstatuskode_AVBRUTT_for_ny_avtale() {
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.AKTUELL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.FULLF));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVBRUTT_for_eksisterende_avtale() {
        Avtale avtale = new Avtale();

        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.AKTUELL));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.FULLF));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVBRUTT_for_feilregistrert_avtale() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.FEILREGISTRERING);

        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.AKTUELL));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.FULLF));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVBRUTT_for_annullert_avtale_med_gyldig_grunn() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.IKKE_MØTT);

        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.AKTUELL));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.FULLF));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVBRUTT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVLYST_for_ny_avtale() {
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.FULLF));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN_AVB));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.NEITAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVLYST, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVLYST_for_eksisterende_avtale() {
        Avtale avtale = new Avtale();

        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.FULLF));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN_AVB));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.NEITAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVLYST_for_feilregistrert_avtale() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.FEILREGISTRERING);

        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.FULLF));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN_AVB));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.NEITAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVLYST_for_annullert_avtale_med_gyldig_grunn() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.IKKE_MØTT);

        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.FULLF));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN_AVB));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.NEITAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVLYST, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVSLUTT_for_ny_avtale() {
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVSLUTT_for_eksisterende_avtale() {
        Avtale avtale = new Avtale();

        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVSLUTT_for_feilregistrert_avtale() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.FEILREGISTRERING);

        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_AVSLUTT_for_annullert_avtale_med_gyldig_grunn() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.IKKE_MØTT);

        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.AVSLUTT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_GJENNOMFOR_for_ny_avtale() {
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_GJENNOMFOR_for_eksisterende_avtale() {
        Avtale avtale = new Avtale();

        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_GJENNOMFOR_for_feilregistrert_avtale() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.FEILREGISTRERING);

        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_GJENNOMFOR_for_annullert_avtale_med_gyldig_grunn() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.IKKE_MØTT);

        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.GJENNOMFOR, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_PLANLAGT_for_ny_avtale() {
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.IGNORE, ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_PLANLAGT_for_eksisterende_avtale() {
        Avtale avtale = new Avtale();

        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_PLANLAGT_for_feilregistrert_avtale() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.FEILREGISTRERING);

        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.CREATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.VENTELISTE));
    }

    @Test
    void tiltaksstatuskode_PLANLAGT_for_annullert_avtale_med_gyldig_grunn() {
        Avtale avtale = new Avtale();
        avtale.setAnnullertGrunn(AnnullertGrunn.IKKE_MØTT);

        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.AKTUELL));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.DELAVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.FEILREG));
        assertEquals(ArenaMigrationAction.END, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.FULLF));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN_AVB));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.GJENN_AVL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.IKKAKTUELL));
        assertEquals(ArenaMigrationAction.TERMINATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.IKKEM));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.INFOMOETE));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.JATAKK));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.NEITAKK));
        assertEquals(ArenaMigrationAction.UPDATE, ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.TILBUD));
        assertThrows(IllegalStateException.class, () -> ArenaMigrationAction.map(avtale, Tiltakstatuskode.PLANLAGT, Deltakerstatuskode.VENTELISTE));
    }

}
