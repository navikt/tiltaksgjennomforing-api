package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.tilskuddsperioder")
public class TilskuddsperiodeConfig {
    private EnumSet<Tiltakstype> tiltakstyper = EnumSet.allOf(Tiltakstype.class);
    private List<BedriftNr> pilotvirksomheter = new ArrayList<>();
    private List<String> pilotenheter = new ArrayList<>();
}
