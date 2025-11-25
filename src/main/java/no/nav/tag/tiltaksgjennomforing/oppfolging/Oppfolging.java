package no.nav.tag.tiltaksgjennomforing.oppfolging;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.AvtaleDTO;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDate;
import java.time.YearMonth;

import static no.nav.tag.tiltaksgjennomforing.utils.DatoUtils.maksDato;

public class Oppfolging {

    private static final int OPPFOLGINGSVINDU_1_MND = 1;
    private static final int OPPFOLGINGSINTERVALL_6_MND = 6;

    private final LocalDate avtaleSlutt;
    private final LocalDate avtaleStart;
    private final LocalDate varselstidspunkt;
    private final LocalDate oppfolgingsfrist;
    private final LocalDate sisteMuligeVarselstidspunkt;

    Oppfolging(LocalDate varselstidspunkt, LocalDate avtaleStart, LocalDate avtaleSlutt) {
        this.varselstidspunkt = varselstidspunkt;
        this.avtaleStart = avtaleStart;
        this.avtaleSlutt = avtaleSlutt;

        if (avtaleStart == null || avtaleSlutt == null) {
            oppfolgingsfrist = null;
            sisteMuligeVarselstidspunkt = null;
        } else {
            oppfolgingsfrist = varselstidspunkt == null ? null : YearMonth.from(varselstidspunkt)
                .plusMonths(OPPFOLGINGSVINDU_1_MND)
                .atEndOfMonth();
            sisteMuligeVarselstidspunkt = YearMonth.from(avtaleSlutt).minusMonths(1).atDay(1);
        }
    }

    public Oppfolging nullstill() {
        return new Oppfolging(null, avtaleStart, avtaleSlutt);
    }

    /**
     * Fristen for oppfølging tilsvarer siste dag i måneden, måneden etter varselstidspunktet.
     * Feks: varselstidspunkt = '2024-02-1' => frist '2024-03-31'
     */
    public LocalDate getOppfolgingsfrist() {
        return oppfolgingsfrist;
    }

    /**
     * Finn varselstidspunktet for denne oppfølgingen. Vil returnere null dersom det ikke fins
     * en oppfølging enda (kall .neste()), eller dersom neste oppfølging ville forekommet etter
     * avtalen er avsluttet.
     */
    public LocalDate getVarselstidspunkt() {
        if (varselstidspunkt == null || varselstidspunkt.isAfter(sisteMuligeVarselstidspunkt.minusDays(1))) {
            return null;
        }
        return varselstidspunkt;
    }

    public static Oppfolging fra(Avtale avtale) {
        return new Oppfolging(
            avtale.getKreverOppfolgingFom(),
            avtale.getGjeldendeInnhold().getStartDato(),
            avtale.getGjeldendeInnhold().getSluttDato()
        );
    }

    public static Oppfolging fra(AvtaleDTO avtale) {
        return new Oppfolging(
            avtale.getKreverOppfolgingFom(),
            avtale.getGjeldendeInnhold().getStartDato(),
            avtale.getGjeldendeInnhold().getSluttDato()
        );
    }

    /**
     * Gitt en oppfølging (varsel og frist), finn neste gang oppfølging skal utføres. Neste frist
     * er tidligst 6 mnd frem i tid.
     * <p>
     * <b>NB:</b> Neste oppfølging vil være avhengig av dagens dato i visse tilfeller, som feks
     * når en avtale har blitt etterregistrert, eller forrige oppfølging skjedde lenge over frist.
     */
    public Oppfolging neste() {
        if (avtaleStart == null || avtaleSlutt == null) {
            // Ingen "neste" oppfølging dersom avtale ikke har start/sluttdato
            return this;
        }
        LocalDate utgangspunktForBeregningAvNyFrist = maksDato(Now.localDate(), varselstidspunkt == null ? avtaleStart : oppfolgingsfrist);
        YearMonth nyFristMnd = YearMonth.from(utgangspunktForBeregningAvNyFrist).plusMonths(OPPFOLGINGSINTERVALL_6_MND);
        LocalDate nyFrist = nyFristMnd.atEndOfMonth();

        LocalDate varselTidspunkt = nyFrist.minusMonths(OPPFOLGINGSVINDU_1_MND);
        return new Oppfolging(varselTidspunkt.withDayOfMonth(1), avtaleStart, avtaleSlutt);
    }
}
