package no.nav.tag.tiltaksgjennomforing.avtale;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
public class MentorTilskuddsperioderToggle {

    private static MentorTilskuddsperioderToggle INSTANCE;

    @Value("${tiltaksgjennomforing.mentor-tilskuddsperioder.enabled}")
    private boolean enabled;

    @PostConstruct
    void init() {
        INSTANCE = this;
    }

    public static boolean isEnabled() {
        return INSTANCE != null && INSTANCE.enabled;
    }

    @Profile({"test", "local", "dockercompose"})
    public static void setValue(boolean value) {
        if (INSTANCE != null) {
            INSTANCE.enabled = value;
        } else {
            MentorTilskuddsperioderToggle temp = new MentorTilskuddsperioderToggle();
            temp.enabled = value;
            INSTANCE = temp;
        }
    }
}
