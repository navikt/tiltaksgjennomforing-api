package no.nav.tag.tiltaksgjennomforing;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SyntetiskFnrExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }
}
