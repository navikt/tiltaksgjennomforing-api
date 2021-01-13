package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.function.Predicate;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AvtalePredicate implements Predicate<Avtale> {

    private NavIdent veilederNavIdent;
    private BedriftNr bedriftNr;
    private Fnr deltakerFnr;
    private Tiltakstype tiltakstype;
    private Status status;
    private Boolean erUfordelt;
    private TilskuddPeriodeStatus tilskuddPeriodeStatus;

    private static boolean erLiktHvisOppgitt(Object kriterie, Object avtaleVerdi) {
        return kriterie == null || kriterie.equals(avtaleVerdi);
    }

    public TilskuddPeriodeStatus hentTilskuddPeriodeStatus() {
        return tilskuddPeriodeStatus == null ? TilskuddPeriodeStatus.UBEHANDLET : tilskuddPeriodeStatus;
    }

    @Override
    public boolean test(Avtale avtale) {
        return erLiktHvisOppgitt(veilederNavIdent, avtale.getVeilederNavIdent())
            && erLiktHvisOppgitt(bedriftNr, avtale.getBedriftNr())
            && erLiktHvisOppgitt(deltakerFnr, avtale.getDeltakerFnr())
            && erLiktHvisOppgitt(tiltakstype, avtale.getTiltakstype())
            && erLiktHvisOppgitt(status, avtale.statusSomEnum());
    }
}
