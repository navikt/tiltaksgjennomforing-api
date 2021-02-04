package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.function.Predicate;

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
    private String navEnhet;

    private static boolean erLiktHvisOppgitt(Object kriterie, Object avtaleVerdi) {
        return kriterie == null || kriterie.equals(avtaleVerdi);
    }

    @Override
    public boolean test(Avtale avtale) {
        return erLiktHvisOppgitt(veilederNavIdent, avtale.getVeilederNavIdent())
                && erLiktHvisOppgitt(bedriftNr, avtale.getBedriftNr())
                && erLiktHvisOppgitt(deltakerFnr, avtale.getDeltakerFnr())
                && erLiktHvisOppgitt(tiltakstype, avtale.getTiltakstype())
                && erLiktHvisOppgitt(status, avtale.statusSomEnum())
                && erLiktHvisOppgitt(tilskuddPeriodeStatus, avtale.getGjeldendeTilskuddsperiodestatus())
                && (erLiktHvisOppgitt(navEnhet, avtale.getEnhetGeografisk()) || erLiktHvisOppgitt(navEnhet, avtale.getEnhetOppfolging()));
    }
}
