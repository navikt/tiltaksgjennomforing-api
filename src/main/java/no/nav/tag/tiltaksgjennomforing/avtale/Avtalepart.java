package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeEndreException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@Data
public abstract class Avtalepart<T extends Identifikator> {
    private final T identifikator;
    final Avtale avtale;
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

    abstract void godkjennForAvtalepart();

    abstract boolean kanEndreAvtale();

    public abstract AvtaleStatusDetaljer statusDetaljerForAvtale();

    public abstract boolean erGodkjentAvInnloggetBruker();

    void sjekkOmAvtaleKanGodkjennes() {
    }

    abstract boolean kanOppheveGodkjenninger();

    public abstract Avtalerolle rolle();

    abstract void godkjennForVeilederOgDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn);

    abstract void opphevGodkjenningerSomAvtalepart();

    public void godkjennAvtale(Instant sistEndret) {
        avtale.sjekkSistEndret(sistEndret);
        sjekkOmAvtaleKanGodkjennes();
        godkjennForAvtalepart();
    }

    public void godkjennPaVegneAvDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn) {
        godkjennForVeilederOgDeltaker(paVegneAvGrunn);
    }

    public void endreAvtale(Instant sistEndret, EndreAvtale endreAvtale) {
        if (!kanEndreAvtale()) {
            throw new KanIkkeEndreException();
        }
        avtale.endreAvtale(sistEndret, endreAvtale, rolle());
    }

    public void opphevGodkjenninger() {
        if (!kanOppheveGodkjenninger()) {
            throw new KanIkkeOppheveException();
        }
        opphevGodkjenningerSomAvtalepart();
    }

    public abstract void låsOppAvtale();
}
