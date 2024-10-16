package no.nav.tag.tiltaksgjennomforing.satser;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;

@Service
public class SatsService {
    private final SatserRepository satserRepository;

    SatsService(SatserRepository satserRepository) {
        this.satserRepository = satserRepository;
    }

    public Set<String> hentSatsetyper() {
        return this.satserRepository.finnSatsetyper();
    }

    public Sats hentSats(String satsType) {
        List<SatserEntitet> satserEntitet = satserRepository.findAllBySatsType(satsType);
        return new Sats(satsType, satserEntitet);
    }

    public void opprettNySats(String satsType, SatsPeriodeData satsPeriodeData) {
        var eksisterendeSatser = satserRepository.findAllBySatsType(satsType);
        var nySats = SatserEntitet.of(satsType, satsPeriodeData);
        eksisterendeSatser.forEach(eksisterendeSats -> {
            if (eksisterendeSats.overlapper(nySats)) {
                throw new IllegalArgumentException(
                        format("Ny sats (%s) overlapper eksisterende sats (%s)", satsPeriodeData, eksisterendeSats)
                );
            }
        });
        satserRepository.save(nySats);
    }
}
