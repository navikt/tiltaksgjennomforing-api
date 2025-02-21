package no.nav.tag.tiltaksgjennomforing.persondata;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AktsomhetService {
    private final InnloggingService innloggingService;
    private final PersondataService persondataService;
    private final AvtaleRepository avtaleRepository;

    public AktsomhetService(InnloggingService innloggingService, PersondataService persondataService, AvtaleRepository avtaleRepository) {
        this.innloggingService = innloggingService;
        this.persondataService = persondataService;
        this.avtaleRepository = avtaleRepository;
    }

    public boolean kreverAktsomhet(Avtalerolle avtalerolle, UUID avtaleId) {
        try {
            switch (avtalerolle) {
                case VEILEDER -> {
                    Avtalepart avtalepart = innloggingService.hentAvtalepart(avtalerolle);
                    return Optional.ofNullable(avtalepart.hentAvtale(avtaleRepository, avtaleId))
                            .map(avtale -> persondataService.erKode6(avtale.getDeltakerFnr()))
                            .orElse(false);
                }
                case ARBEIDSGIVER, MENTOR -> {
                    Avtalepart avtalepart = innloggingService.hentAvtalepart(avtalerolle);
                    return Optional.ofNullable(avtalepart.hentAvtale(avtaleRepository, avtaleId))
                        .filter(avtale -> !avtale.erUfordelt())
                        .map(avtale -> persondataService.erKode6(avtale.getDeltakerFnr()))
                        .orElse(false);
                }
                default -> {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }
}
