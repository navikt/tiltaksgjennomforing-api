package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;

public class SjekkAktiveProfilerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final List<String> MILJOER = Arrays.asList("dev", "heroku", "preprod", "prod");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (ugyldigKjoremiljo(applicationContext.getEnvironment().getActiveProfiles())) {
            throw new IllegalStateException("Applikasjonen må startes med én av profilene aktivert: " + MILJOER.toString());
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
