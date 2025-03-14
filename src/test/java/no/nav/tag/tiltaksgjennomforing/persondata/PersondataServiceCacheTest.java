package no.nav.tag.tiltaksgjennomforing.persondata;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.Adressebeskyttelse;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.Folkeregisteridentifikator;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.HentPerson;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.HentPersonBolk;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.PdlResponsBolk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersondataServiceCacheTest {
    private static Fnr STRENG_FORTROLIG_FNR = new Fnr("00000000000");
    private static HentPerson STRENG_FORTROLIG_PERSON = new HentPerson(
        List.of(new Adressebeskyttelse(Diskresjonskode.STRENGT_FORTROLIG)),
        List.of(new Folkeregisteridentifikator(STRENG_FORTROLIG_FNR.asString(), null, null)),
        null
    );
    private static Fnr FORTROLIG_FNR = new Fnr("00000000001");
    private static HentPerson FORTROLIG_PERSON = new HentPerson(
        List.of(new Adressebeskyttelse(Diskresjonskode.FORTROLIG)),
        List.of(new Folkeregisteridentifikator(FORTROLIG_FNR.asString(), null, null)),
        null
    );
    private static Fnr UGRADERT_FNR = new Fnr("00000000002");
    private static HentPerson UGRADERT_PERSON = new HentPerson(
        List.of(new Adressebeskyttelse(Diskresjonskode.UGRADERT)),
        List.of(new Folkeregisteridentifikator(UGRADERT_FNR.asString(), null, null)),
        null
    );
    private static Fnr TOM_RESPONS_FNR = new Fnr("00000000003");
    private static HentPerson TOM_RESPONS_PERSON = new HentPerson(
        Collections.emptyList(),
        List.of(new Folkeregisteridentifikator(TOM_RESPONS_FNR.asString(), null, null)),
        null
    );

    private PersondataService persondataService;
    private PersondataClient persondataClient;

    @BeforeEach
    void setUp() {
        this.persondataClient = mock(PersondataClient.class);
        this.persondataService = new PersondataService(persondataClient);

        when(persondataClient.hentPersondata(STRENG_FORTROLIG_FNR)).thenReturn(
            new PdlRespons(new PdlRespons.Data(STRENG_FORTROLIG_PERSON, null, null))
        );

        when(persondataClient.hentPersondata(TOM_RESPONS_FNR)).thenReturn(
            new PdlRespons(new PdlRespons.Data(TOM_RESPONS_PERSON, null, null))
        );

        when(persondataClient.hentPersonBolk(any())).thenReturn(
            new PdlResponsBolk(
                new PdlResponsBolk.Data(
                    List.of(
                        new HentPersonBolk(null, STRENG_FORTROLIG_PERSON, HentPersonBolk.OK),
                        new HentPersonBolk(null, FORTROLIG_PERSON, HentPersonBolk.OK),
                        new HentPersonBolk(null, UGRADERT_PERSON, HentPersonBolk.OK),
                        new HentPersonBolk(null, TOM_RESPONS_PERSON, HentPersonBolk.OK)
                    )
                )
            )
        );
    }

    @Test
    void hentDiskresjonskode__skal_kun_hente_fra_klient_1_gang() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(STRENG_FORTROLIG_FNR);
        assertEquals(Diskresjonskode.STRENGT_FORTROLIG, diskresjonskode);

        Diskresjonskode diskresjonskode2 = persondataService.hentDiskresjonskode(STRENG_FORTROLIG_FNR);
        assertEquals(Diskresjonskode.STRENGT_FORTROLIG, diskresjonskode2);

        verify(persondataClient, times(1)).hentPersondata(any());
    }

    @Test
    void hentDiskresjonskoder_henter_bare_de_som_mangler_i_cache() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(STRENG_FORTROLIG_FNR);
        assertEquals(Diskresjonskode.STRENGT_FORTROLIG, diskresjonskode);

        Map<Fnr, Diskresjonskode> diskresjonskodeMap = persondataService.hentDiskresjonskoder(
            Set.of(STRENG_FORTROLIG_FNR, FORTROLIG_FNR, UGRADERT_FNR)
        );

        assertEquals(Diskresjonskode.STRENGT_FORTROLIG, diskresjonskodeMap.get(STRENG_FORTROLIG_FNR));
        assertEquals(Diskresjonskode.FORTROLIG, diskresjonskodeMap.get(FORTROLIG_FNR));
        assertEquals(Diskresjonskode.UGRADERT, diskresjonskodeMap.get(UGRADERT_FNR));

        verify(persondataClient, times(1)).hentPersondata(STRENG_FORTROLIG_FNR);
        verify(persondataClient, times(1)).hentPersonBolk(Set.of(FORTROLIG_FNR, UGRADERT_FNR));
    }

    @Test
    void hentDiskresjonskode_henter_bare_dersom_det_mangler_i_cache() {
        Map<Fnr, Diskresjonskode> diskresjonskodeMap = persondataService.hentDiskresjonskoder(
            Set.of(STRENG_FORTROLIG_FNR, FORTROLIG_FNR, UGRADERT_FNR)
        );

        assertEquals(Diskresjonskode.STRENGT_FORTROLIG, diskresjonskodeMap.get(STRENG_FORTROLIG_FNR));
        assertEquals(Diskresjonskode.FORTROLIG, diskresjonskodeMap.get(FORTROLIG_FNR));
        assertEquals(Diskresjonskode.UGRADERT, diskresjonskodeMap.get(UGRADERT_FNR));

        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(FORTROLIG_FNR);
        assertEquals(Diskresjonskode.FORTROLIG, diskresjonskode);

        verify(persondataClient, never()).hentPersondata(any());
        verify(persondataClient, times(1)).hentPersonBolk(Set.of(STRENG_FORTROLIG_FNR, FORTROLIG_FNR, UGRADERT_FNR));
    }

    @Test
    void hentDiskresjonskode_lagrer_ikke_i_cache_og_defaulter_til_UGRADERT_dersom_respons_fra_pdl_er_tom() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(TOM_RESPONS_FNR);
        assertEquals(Diskresjonskode.UGRADERT, diskresjonskode);

        Diskresjonskode diskresjonskode2 = persondataService.hentDiskresjonskode(TOM_RESPONS_FNR);
        assertEquals(Diskresjonskode.UGRADERT, diskresjonskode2);

        verify(persondataClient, times(2)).hentPersondata(any());
    }

    @Test
    void hentDiskresjonskoder_lagrer_ikke_i_cache_og_defaulter_til_UGRADERT_dersom_respons_fra_pdl_er_tom() {
        Map<Fnr, Diskresjonskode> diskresjonskodeMap = persondataService.hentDiskresjonskoder(
            Set.of(STRENG_FORTROLIG_FNR, FORTROLIG_FNR, UGRADERT_FNR, TOM_RESPONS_FNR)
        );

        assertEquals(Diskresjonskode.STRENGT_FORTROLIG, diskresjonskodeMap.get(STRENG_FORTROLIG_FNR));
        assertEquals(Diskresjonskode.FORTROLIG, diskresjonskodeMap.get(FORTROLIG_FNR));
        assertEquals(Diskresjonskode.UGRADERT, diskresjonskodeMap.get(UGRADERT_FNR));
        assertEquals(Diskresjonskode.UGRADERT, diskresjonskodeMap.get(TOM_RESPONS_FNR));

        verify(persondataClient, times(1))
            .hentPersonBolk(Set.of(STRENG_FORTROLIG_FNR, FORTROLIG_FNR, UGRADERT_FNR, TOM_RESPONS_FNR));

        Map<Fnr, Diskresjonskode> diskresjonskodeMap2 = persondataService.hentDiskresjonskoder(
            Set.of(STRENG_FORTROLIG_FNR, FORTROLIG_FNR, UGRADERT_FNR, TOM_RESPONS_FNR)
        );

        assertEquals(Diskresjonskode.STRENGT_FORTROLIG, diskresjonskodeMap2.get(STRENG_FORTROLIG_FNR));
        assertEquals(Diskresjonskode.FORTROLIG, diskresjonskodeMap2.get(FORTROLIG_FNR));
        assertEquals(Diskresjonskode.UGRADERT, diskresjonskodeMap2.get(UGRADERT_FNR));
        assertEquals(Diskresjonskode.UGRADERT, diskresjonskodeMap2.get(TOM_RESPONS_FNR));

        verify(persondataClient, times(1)).hentPersonBolk(Set.of(TOM_RESPONS_FNR));
    }

}
