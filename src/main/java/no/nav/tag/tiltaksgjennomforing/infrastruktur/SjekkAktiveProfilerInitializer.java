package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;

public class SjekkAktiveProfilerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final List<String> MILJOER = Arrays.asList("preprod", "prod");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (ugyldigKjoremiljo(applicationContext.getEnvironment().getActiveProfiles())) {
            String feilmelding = "For å starte applikasjonen lokalt må du kjøre LokalTiltaksgjennomforingApplication. På NAIS må én av profilene være aktivert: " + MILJOER.toString();
            System.out.println("--------------------------------------------------");
            System.out.println(feilmelding);
            System.out.println("--------------------------------------------------");
            throw new IllegalStateException(feilmelding);
        }
    }

    private static boolean ugyldigKjoremiljo(String[] profiler) {
        int antall = 0;
        for (String profil : profiler) {
            if (MILJOER.contains(profil)) {
                antall++;
            }
        }
        return antall != 1;
    }
}
