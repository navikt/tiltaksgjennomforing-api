package no.nav.tag.tiltaksgjennomforing.persondata.aktsomhet;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
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

    public Aktsomhet kreverAktsomhet(Avtalerolle avtalerolle, UUID avtaleId) {
        try {
            switch (avtalerolle) {
                case BESLUTTER, VEILEDER -> {
                    Avtalepart avtalepart = innloggingService.hentAvtalepart(avtalerolle);
                    return Optional.ofNullable(avtalepart.hentAvtale(avtaleRepository, avtaleId))
                        .map(avtale -> Aktsomhet.intern(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())))
                        .orElse(Aktsomhet.tom());
                }
                case ARBEIDSGIVER, MENTOR -> {
                    Avtalepart avtalepart = innloggingService.hentAvtalepart(avtalerolle);
                    return Optional.ofNullable(avtalepart.hentAvtale(avtaleRepository, avtaleId))
                        .filter(avtale -> !avtale.erUfordelt())
                        .map(avtale -> Aktsomhet.ekstern(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())))
                        .orElse(Aktsomhet.tom());
                }
                default -> {
                    return Aktsomhet.tom();
                }
            }
        } catch (Exception e) {
            return Aktsomhet.tom();
        }
    }
}
