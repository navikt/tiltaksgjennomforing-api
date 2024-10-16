package no.nav.tag.tiltaksgjennomforing.satser;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static java.lang.String.format;

@Entity(name = "satser")
class SatserEntitet {
    @Id
    private UUID id = UUID.randomUUID();
    @NotNull
    private String satsType;
    @NotNull
    private Double satsVerdi;
    @NotNull
    private LocalDate gyldigFraOgMed;
    private LocalDate gyldigTilOgMed;
    @NotNull
    private Instant opprettetTidspunkt = Instant.now();

    public SatserEntitet() {
    }

    public SatserEntitet(@NotNull String satsType, @NotNull Double satsVerdi, @NotNull LocalDate gyldigFraOgMed, LocalDate gyldigTilOgMed) {
        this.satsType = satsType;
        this.satsVerdi = satsVerdi;
        this.gyldigFraOgMed = gyldigFraOgMed;
        this.gyldigTilOgMed = gyldigTilOgMed;
    }

    public static SatserEntitet of(String satsType, SatsPeriodeData satsPeriodeData) {
        return new SatserEntitet(satsType, satsPeriodeData.satsVerdi(), satsPeriodeData.gyldigFra(), satsPeriodeData.gyldigTil());
    }

    public @NotNull String getSatsType() {
        return satsType;
    }

    public @NotNull Double getSatsVerdi() {
        return satsVerdi;
    }

    public @NotNull LocalDate getGyldigFraOgMed() {
        return gyldigFraOgMed;
    }

    public LocalDate getGyldigTilOgMed() {
        return gyldigTilOgMed;
    }

    public boolean overlapper(SatserEntitet that) {
        var thisGyldigTilOgMed = this.gyldigTilOgMed == null ? LocalDate.MAX : this.gyldigTilOgMed;
        var thatGyldigTilOgMed = that.gyldigTilOgMed == null ? LocalDate.MAX : that.gyldigTilOgMed;
        return this.gyldigFraOgMed.isBefore(thatGyldigTilOgMed) && that.gyldigFraOgMed.isBefore(thisGyldigTilOgMed);
    }

    @Override
    public String toString() {
        return format("Satser(id=%s, type=%s, verdi=%s, fra=%s, til=%s)",
                id, satsType, satsVerdi, gyldigFraOgMed, gyldigTilOgMed);
    }
}
