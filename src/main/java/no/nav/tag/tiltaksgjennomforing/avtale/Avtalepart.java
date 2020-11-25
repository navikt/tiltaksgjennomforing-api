package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeEndreException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@Data
public abstract class Avtalepart<T extends Identifikator> {
    private final T identifikator;
    static String tekstHeaderAvtalePaabegynt = "Du må fylle ut avtalen";
    static String tekstHeaderVentAndreGodkjenning = "Vent til de andre har godkjent";
    static String tekstHeaderAvtaleErGodkjentAvAllePartner = "Avtalen er ferdig utfylt og godkjent";
    static String tekstAvtaleErGodkjentAvAllePartner = "Tiltaket starter ";
    static String tekstHeaderAvtaleVenterPaaDinGodkjenning = "Du må godkjenne ";
    static String tekstAvtaleVenterPaaAndrepartnerGodkjenning = "Andre partner må godkjenne avtalen";
    static String ekstraTekstAvtaleVenterPaaAndrePartnerGodkjenning = "Avtalen kan ikke tas i bruk før de andre har godkjent avtalen.";
    static String tekstHeaderAvtaleGjennomfores = "Tiltaket gjennomføres";
    static String tekstHeaderAvtaleErAvsluttet = "Tiltaket er avsluttet";
    static String tekstHeaderAvtaleAvbrutt = "Tiltaket er avbrutt";
    static String tekstAvtaleAvbrutt = "Veilederen har bestemt at tiltaket og avtalen skal avbrytes.";
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd. MMMM yyyy");

    abstract void godkjennForAvtalepart(Avtale avtale);

    abstract boolean kanEndreAvtale();

    public abstract AvtaleStatusDetaljer statusDetaljerForAvtale(Avtale avtale);

    public abstract boolean erGodkjentAvInnloggetBruker(Avtale avtale);

    void sjekkOmAvtaleKanGodkjennes(Avtale avtale) {
    }

    abstract boolean kanOppheveGodkjenninger(Avtale avtale);

    abstract void godkjennForVeilederOgDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn, Avtale avtale);

    abstract void opphevGodkjenningerSomAvtalepart(Avtale avtale);

    public void godkjennAvtale(Instant sistEndret, Avtale avtale) {
        avtale.sjekkSistEndret(sistEndret);
        sjekkOmAvtaleKanGodkjennes(avtale);
        godkjennForAvtalepart(avtale);
    }

    public void godkjennPaVegneAvDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn, Avtale avtale) {
        godkjennForVeilederOgDeltaker(paVegneAvGrunn, avtale);
    }

    public void endreAvtale(Instant sistEndret, EndreAvtale endreAvtale, Avtale avtale) {
        if (!kanEndreAvtale()) {
            throw new KanIkkeEndreException();
        }
        avtale.endreAvtale(sistEndret, endreAvtale, rolle());
    }

    protected abstract Avtalerolle rolle();

    public void opphevGodkjenninger(Avtale avtale) {
        if (!kanOppheveGodkjenninger(avtale)) {
            throw new KanIkkeOppheveException();
        }
        opphevGodkjenningerSomAvtalepart(avtale);
    }

    public abstract void låsOppAvtale(Avtale avtale);
}
