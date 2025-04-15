package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum ArenaTiltakskode {
    ARBEIDSTRENING("ARBTREN", Tiltakstype.ARBEIDSTRENING, null),
    MENTOR("MENTOR", Tiltakstype.MENTOR, null),
    VTAO("VATIAROR", Tiltakstype.VTAO, LocalDate.of(2025, 7, 1)),
    INKLUDERINGSTILSKUDD("INKLUTILS", Tiltakstype.INKLUDERINGSTILSKUDD, null),
    UKJENT("", null, null);

    public static final ArenaTiltakskode GJELDENDE_MIGRERING = VTAO;

    private static final Set<ArenaTiltakskode> FERDIG_MIGRERT = Set.of(
        ARBEIDSTRENING
    );

    private final String kode;
    private final Tiltakstype tiltakstype;
    private final LocalDate migreringsdatoForTilskudd;

    public boolean skalBehandles() {
        return this != UKJENT;
    }

    public boolean isFerdigMigrert() {
        return FERDIG_MIGRERT.contains(this);
    }

    public static ArenaTiltakskode parse(String kode) {
        for (ArenaTiltakskode arenaTiltakskode : values()) {
            if (arenaTiltakskode.kode.equalsIgnoreCase(kode)) {
                return arenaTiltakskode;
            }
        }
        return ArenaTiltakskode.UKJENT;
    }

    public static class Deserializer extends JsonDeserializer<ArenaTiltakskode> {
        @Override
        public ArenaTiltakskode deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            return parse(p.getValueAsString());
        }
    }

    @Converter
    public static class Convert implements AttributeConverter<ArenaTiltakskode, String> {
        @Override
        public String convertToDatabaseColumn(ArenaTiltakskode arenaTiltakskode) {
            return arenaTiltakskode.getKode();
        }

        @Override
        public ArenaTiltakskode convertToEntityAttribute(String string) {
            return ArenaTiltakskode.parse(string);
        }
    }
}
