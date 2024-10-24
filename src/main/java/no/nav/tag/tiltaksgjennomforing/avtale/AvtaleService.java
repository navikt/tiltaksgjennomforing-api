package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AvtaleService {
    private final AvtaleRepository avtaleRepository;
    private final InnloggingService innloggingService;
    private final FeatureToggleService featureToggleService;

    public AvtaleService(AvtaleRepository avtaleRepository, FeatureToggleService featureToggleService, InnloggingService innloggingService) {
        this.avtaleRepository = avtaleRepository;
        this.featureToggleService = featureToggleService;
        this.innloggingService = innloggingService;
    }

    @Transactional
    public Avtale endreTilskuddsberegning(UUID avtaleId, EndreTilskuddsberegning endreTilskuddsberegning, boolean dryRun) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .map(this::sjekkArbeidstreningToggle)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.sjekkTilgang(avtale);
        avtale.endreTilskuddsberegning(endreTilskuddsberegning, veileder.getIdentifikator());
        if (!dryRun) {
            return avtaleRepository.save(avtale);
        }
        return avtale;
    }

    Avtale sjekkArbeidstreningToggle(Avtale avtale) {
        if (featureToggleService.isEnabled(FeatureToggle.ARBEIDSTRENING_READONLY) &&
                avtale.getTiltakstype() == Tiltakstype.ARBEIDSTRENING) {
            throw new FeilkodeException(Feilkode.IKKE_ADMIN_TILGANG);
        }
        return avtale;
    }
}
