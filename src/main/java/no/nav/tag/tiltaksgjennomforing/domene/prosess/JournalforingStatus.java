package no.nav.tag.tiltaksgjennomforing.domene.prosess;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JournalforingStatus {


    TIL_PROSESS("Venter på prosessering"), //På kafka kø, ikke hentet av Tiltaksgjennomføring-prosess
    TIL_JOURNALFORING("Midlertidig journalført"), //Midlertidig journalført, behandles i dokumentfordeling
    FERDIG("Fullført"),
    FEILET_IKKE_SENDT("Feilet ved sending til prosess"), //Feilet i denne applikasjonen
    PROSESS_FEIL("Feilet ved sending til Joark"), //Feilet i Tiltaksgjennomføring-prosess
    JOURNALFORING_FEIL("Feilet v/sending til Arena"); //Feilet i dokumentfordeling

    private final String tekst;
}
