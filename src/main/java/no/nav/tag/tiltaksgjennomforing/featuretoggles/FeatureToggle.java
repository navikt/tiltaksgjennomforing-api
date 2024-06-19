package no.nav.tag.tiltaksgjennomforing.featuretoggles;

public enum FeatureToggle {
    VTAO_TILTAK_TOGGLE("vtaoTiltakToggle"),
    SMS_MIN_SIDE_DELTAKER("sms-min-side-deltaker"),
    ARBEIDSGIVERNOTIFIKASJON_MED_SAK_OG_SMS("arbeidsgivernotifikasjon-med-sak-og-sms");

    private String toggleNavn;

    FeatureToggle(String toggleNavn) {
        this.toggleNavn = toggleNavn;
    }

    public String getToggleNavn() {
        return toggleNavn;
    }
}
