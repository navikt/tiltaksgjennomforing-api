package no.nav.tag.tiltaksgjennomforing.autorisasjon;

public record AdGruppeTilganger(
    boolean beslutter,
    boolean fortroligAdresse,
    boolean strengtFortroligAdresse
) {
    public static AdGruppeTilganger av(AdGruppeProperties adGrupperProperties, TokenUtils tokenUtils) {
        return new AdGruppeTilganger(
            tokenUtils.harAdGruppe(adGrupperProperties.getBeslutter()),
            tokenUtils.harAdGruppe(adGrupperProperties.getFortroligAdresse()),
            tokenUtils.harAdGruppe(adGrupperProperties.getStrengtFortroligAdresse())
        );
    }
}
