package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.ErAlleredeVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VeilederSkalGodkjenneSistException;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class VeilederTest {
    @Test(expected = VeilederSkalGodkjenneSistException.class)
    public void godkjennAvtale__kan_ikke_godkjenne_foerst() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.godkjennAvtale(avtale.getSistEndret(), avtale);
    }

    @Test
    public void godkjennAvtale__kan_godkjenne_sist() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.godkjennAvtale(avtale.getSistEndret(), avtale);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
    }

    @Test
    public void opphevGodkjenninger__kan_alltid_oppheve_godkjenninger() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.opphevGodkjenninger(avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
    }

    @Test
    public void avbrytAvtale__kan_avbryt_avtale_etter_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.avbrytAvtale(avtale.getSistEndret(), new AvbruttInfo(LocalDate.now(), "enGrunn"), avtale);
        assertThat(avtale.isAvbrutt()).isTrue();
        assertThat(avtale.getAvbruttDato()).isNotNull();
        assertThat(avtale.getAvbruttGrunn()).isEqualTo("enGrunn");
    }

    @Test
    public void avbrytAvtale__kan_avbryte_avtale_foer_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.avbrytAvtale(avtale.getSistEndret(), new AvbruttInfo(LocalDate.now(), "enGrunn"), avtale);
        assertThat(avtale.isAvbrutt()).isTrue();
        assertThat(avtale.getAvbruttDato()).isNotNull();
        assertThat(avtale.getAvbruttGrunn()).isEqualTo("enGrunn");
    }

    @Test
    public void gjenopprettAvtale__kan_gjenopprette_avtale_etter_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setAvbrutt(true);
        avtale.setAvbruttGrunn("enGrunn");
        avtale.setAvbruttDato(LocalDate.now());

        Veileder veileder = TestData.enVeileder(avtale);
        veileder.gjenopprettAvtale(avtale);
        assertThat(avtale.isAvbrutt()).isFalse();
        assertThat(avtale.getAvbruttDato()).isNull();
        assertThat(avtale.getAvbruttGrunn()).isNull();
    }

    @Test
    public void gjenopprettAvtale__kan_gjenopprette_avtale_foer_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setAvbrutt(true);
        avtale.setAvbruttGrunn("enGrunn");
        avtale.setAvbruttDato(LocalDate.now());
        Veileder veileder = TestData.enVeileder(avtale);

        veileder.gjenopprettAvtale(avtale);
        assertThat(avtale.isAvbrutt()).isFalse();
        assertThat(avtale.getAvbruttDato()).isNull();
        assertThat(avtale.getAvbruttGrunn()).isNull();
    }

    @Test
    public void overtarAvtale() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        NavIdent gammelVeileder = avtale.getVeilederNavIdent();
        Veileder nyVeileder = TestData.enVeileder(new NavIdent("J987654"));

        nyVeileder.overtaAvtale(avtale);
        assertThat(gammelVeileder).isNotEqualTo(nyVeileder.getIdentifikator());
        assertThat(avtale.getVeilederNavIdent()).isEqualTo(nyVeileder.getIdentifikator());
    }

    @Test(expected = ErAlleredeVeilederException.class)
    public void overtarAvtale__feil_hvis_samme_ident() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.overtaAvtale(avtale);
    }
}
