package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DvhAvtalePatcher {

    private final DvhMeldingEntitetRepository dvhRepository;

    @Transactional
    public void lagDvhPatchMelding(Avtale avtale) {
        var melding = AvroTiltakHendelseFabrikk.konstruer(avtale, DvhHendelseType.PATCHING, "system");
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(avtale, melding);
        dvhRepository.save(entitet);
    }
}
