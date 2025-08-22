package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.EqualsAndHashCode;
import no.bekk.bekkopen.person.Fodselsnummer;
import no.bekk.bekkopen.person.FodselsnummerCalculator;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.apache.commons.lang3.NotImplementedException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class Fnr extends Identifikator {
    @EqualsAndHashCode.Exclude
    private LocalDate fodselsdato;

    public static Fnr fraDb(String verdi) {
        return new Fnr(verdi, Fnr::erNaivtGyldigFnr);
    }

    public static Fnr generer(int aar, int maned, int dag) {
        if (!FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS) {
            throw new NotImplementedException("Generering av syntetiske fødselsnumre er ikke tillatt i produksjon.");
        }
        Date date = Date.from(LocalDate.of(aar, maned, dag).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Fodselsnummer fnr = FodselsnummerCalculator.getFodselsnummerForDate(date);
        return new Fnr(fnr.getValue());
    }

    public static boolean erGyldigFnr(String verdi) {
        if (FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS) {
            return switch (verdi) {
                case "12345678910", "00000000000", "11111111111", "99999999999" -> true;
                case null, default -> FodselsnummerValidator.isValid(verdi);
            };
        }
        return FodselsnummerValidator.isValid(verdi);
    }

    private static boolean erNaivtGyldigFnr(String verdi) {
        return verdi != null && verdi.matches("^[0-9]{11}$");
    }

    public Fnr(String verdi) {
        this(verdi, Fnr::erGyldigFnr);
    }

    private Fnr(String verdi, Predicate<String> sjekkGyldighet) {
        super(verdi);

        if (!sjekkGyldighet.test(verdi)) {
            throw new FeilkodeException(Feilkode.FØDSELSNUMMER_IKKE_GYLDIG);
        }

        try {
            Fodselsnummer fnr = FodselsnummerValidator.getFodselsnummer(this.asString());
            int dag = Integer.parseInt(fnr.getDayInMonth());
            int maned = Integer.parseInt(fnr.getMonth());
            int aar = Integer.parseInt(fnr.getBirthYear());

            this.fodselsdato = LocalDate.of(aar, maned, dag);
        } catch (IllegalArgumentException ignored) {
            // Blir allerede håndtert i gyldighetssjekk
        }
    }

    public boolean erUnder16år() {
        if (this.fodselsdato == null) {
            return false;
        }
        return this.fodselsdato.isAfter(Now.localDate().minusYears(16));
    }

    public boolean erOver30år() {
        if (this.fodselsdato == null) {
            return false;
        }
        return this.fodselsdato.isBefore(Now.localDate().minusYears(30));
    }

    public boolean erOver30årFørsteJanuar() {
        if (this.fodselsdato == null) {
            return false;
        }
        return this.fodselsdato.isBefore(LocalDate.of(Now.localDate().getYear(), 1, 1).minusYears(30));
    }

    public boolean erOver30årFraOppstartDato(LocalDate opprettetTidspunkt) {
        if (this.fodselsdato == null) {
            return false;
        }
        return this.fodselsdato.isBefore(opprettetTidspunkt.minusYears(30));
    }

    public boolean erOver72ÅrFraSluttDato(LocalDate sluttDato) {
        if (this.fodselsdato == null) {
            return false;
        }
        return this.fodselsdato.isBefore(sluttDato.minusYears(72).plusDays(1));
    }

    public boolean erOver67ÅrFraSluttDato(LocalDate sluttDato) {
        if (this.fodselsdato == null) {
            return false;
        }
        return this.fodselsdato.isBefore(sluttDato.minusYears(67).plusDays(1));
    }

}
