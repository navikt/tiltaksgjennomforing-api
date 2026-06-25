package no.nav.tag.tiltaksgjennomforing.brev;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.brev.digitalkontaktinformasjon.DigitalKontaktinformasjonClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.brev.postadresse.PostadresseClient;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostutsendelseService {
    private final PostadresseClient postadresseClient;
    private final DigitalKontaktinformasjonClient digitalKontaktinformasjonClient;
    private final FeatureToggleService featureToggleService;

    public void sjekkOmPersonKanMottaPost(Fnr fnr) {
        if (!featureToggleService.isEnabled(FeatureToggle.SJEKK_OM_DELTAKER_KAN_MOTTA_POST)){
            return;
        }
        boolean harAdresse = postadresseClient.sjekkOmPersonErRegistrertMedAdresse(fnr);
        boolean erReservertMotDigitalKommunikasjon = digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(fnr);

        if (!harAdresse && erReservertMotDigitalKommunikasjon) {
            log.info("Person kan ikke få post: mangler adresse og er reservert mot digital kommunikasjon");
            throw new FeilkodeException(Feilkode.KAN_IKKE_SENDE_POST_MANGLER_ADRESSE_OG_RESERVERT);
        }
    }
}










