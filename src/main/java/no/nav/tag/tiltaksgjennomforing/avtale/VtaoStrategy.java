package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.KidnummerValidator;

import java.util.HashMap;
import java.util.Map;

public class VtaoStrategy extends BaseAvtaleInnholdStrategy {
    public VtaoStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        if (nyAvtale.getArbeidsgiverKid() != null && !KidnummerValidator.isValid(nyAvtale.getArbeidsgiverKid())) {
            throw new FeilkodeException(Feilkode.FEIL_KID_NUMMER);
        }

        avtaleInnhold.setStillingstittel(nyAvtale.getStillingstittel());
        avtaleInnhold.setStillingStyrk08(nyAvtale.getStillingStyrk08());
        avtaleInnhold.setStillingKonseptId(nyAvtale.getStillingKonseptId());
        avtaleInnhold.setHarFamilietilknytning(nyAvtale.getHarFamilietilknytning());
        avtaleInnhold.setFamilietilknytningForklaring(nyAvtale.getFamilietilknytningForklaring());
        avtaleInnhold.setManedslonn(nyAvtale.getManedslonn());
        avtaleInnhold.setArbeidsgiverKontonummer(nyAvtale.getArbeidsgiverKontonummer());
        avtaleInnhold.setArbeidsgiverKid(nyAvtale.getArbeidsgiverKid());
        avtaleInnhold.setStillingstype(nyAvtale.getStillingstype());

        super.endre(nyAvtale);
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        HashMap<String, Object> alleFelter = new HashMap<>();
        alleFelter.putAll(super.alleFelterSomMåFyllesUt());
        alleFelter.put(AvtaleInnhold.Fields.harFamilietilknytning, avtaleInnhold.getHarFamilietilknytning());
        alleFelter.put(AvtaleInnhold.Fields.arbeidsgiverKontonummer, avtaleInnhold.getArbeidsgiverKontonummer());
        alleFelter.put(AvtaleInnhold.Fields.stillingstittel, avtaleInnhold.getStillingstittel());
        alleFelter.put(AvtaleInnhold.Fields.arbeidsoppgaver, avtaleInnhold.getArbeidsoppgaver());
        alleFelter.put(AvtaleInnhold.Fields.stillingprosent, avtaleInnhold.getStillingprosent());
        alleFelter.put(AvtaleInnhold.Fields.stillingstype, avtaleInnhold.getStillingstype());
        if (avtaleInnhold.getHarFamilietilknytning() != null && avtaleInnhold.getHarFamilietilknytning()) {
            alleFelter.put(AvtaleInnhold.Fields.familietilknytningForklaring, avtaleInnhold.getFamilietilknytningForklaring());
        }

        return alleFelter;
    }
}
