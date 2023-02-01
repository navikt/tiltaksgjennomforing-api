package no.nav.tag.tiltaksgjennomforing.avtale;

public interface AvtaleMinimal {
    String getId();
    String getVeilederNavIdent();
    String getDeltakerFornavn();
    String getDeltakerEtternavn();
    String startDatoPeriode();
    String antallUbehandlet();
}
