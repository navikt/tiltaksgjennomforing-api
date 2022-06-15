package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDateTime;
import java.util.UUID;

public class Inkluderingstilskudd {

    private UUID id;
    private Integer avtale_id;
    private Integer beløp;
    private Inkluderingstilskuddtyper type;
    private String forklaring;
    private LocalDateTime tidspunkt_lagt_til;
}
