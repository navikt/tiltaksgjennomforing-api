package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import org.springframework.util.Assert;

import java.util.UUID;

@UtilityClass
public class CorrelationIdSupplier {
    private static final String MDC_TOKEN_KEY = "correlationId";

    public static void generateToken() {
        MDC.put(MDC_TOKEN_KEY, UUID.randomUUID().toString());
    }

    public static void set(String token) {
        Assert.hasLength(token, "Token kan ikke være blank");
        MDC.put(MDC_TOKEN_KEY, token);
    }

    public static String get() {
        return MDC.get(MDC_TOKEN_KEY);
    }

    public static void remove() {
        MDC.remove(MDC_TOKEN_KEY);
    }
}
