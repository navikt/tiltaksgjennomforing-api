package no.nav.tag.tiltaksgjennomforing.enhet;

public class HentEnhetResponseMapper {
    
    public static HentEnhetResponse map(Norg2OppfølgingResponse response) {
        return HentEnhetResponse.builder()
                .enhetNr(response.getEnhetNr())
                .navn(response.getNavn())
                .build();
    }
}
