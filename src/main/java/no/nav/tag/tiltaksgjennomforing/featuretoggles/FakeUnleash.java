package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import io.getunleash.UnleashContext;
import io.getunleash.variant.Variant;
import jakarta.servlet.http.HttpServletRequest;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

public final class FakeUnleash extends io.getunleash.FakeUnleash {
    private final Set<String> miljo;

    public FakeUnleash(String... miljo) {
        super();
        this.miljo = Set.of(miljo);
    }

    @Override
    public boolean isEnabled(String toggleName, UnleashContext context) {
        updateTogglesFromRequest();
        return super.isEnabled(toggleName, false);
    }

    @Override
    public boolean isEnabled(String toggleName, boolean defaultSetting) {
        updateTogglesFromRequest();
        return super.isEnabled(toggleName, defaultSetting);
    }

    @Override
    public boolean isEnabled(String toggleName, BiPredicate<String, UnleashContext> fallbackAction) {
        updateTogglesFromRequest();
        return super.isEnabled(toggleName, fallbackAction);
    }

    @Override
    public Variant getVariant(String toggleName, Variant defaultValue) {
        updateTogglesFromRequest();
        return super.getVariant(toggleName, defaultValue);
    }
/*
    @Override
    public List<String> getFeatureToggleNames() {
        updateTogglesFromRequest();
        return super.getFeatureToggleNames();
    }
*/
    private void updateTogglesFromRequest() {
        if (miljo.contains(Miljø.TEST)) {
            return;
        }

        Optional<HttpServletRequest> requestOpt = resolveRequest();
        if (requestOpt.isEmpty()) {
            this.enableAll();
            return;
        }

        List<String> headers = Optional.ofNullable(requestOpt.get().getHeader("features"))
            .map(feature -> List.of(feature.split(",")))
            .orElse(Collections.emptyList());

        Optional<String> first = headers.stream().findFirst();
        if (first.map("enabled"::equals).orElse(false)) {
            this.enableAllExcept(getToggleExclusions(headers));
        } else if (first.map("disabled"::equals).orElse(false)) {
            this.disableAllExcept(getToggleExclusions(headers));
        } else {
            this.disableAll();
        }
    }

    private static String[] getToggleExclusions(List<String> headers) {
        return headers.stream()
            .map(header -> header.startsWith("!") ? header.substring(1) : null)
            .filter(Objects::nonNull)
            .toList()
            .toArray(new String[0]);
    }

    private static Optional<HttpServletRequest> resolveRequest() {
        try {
            return Optional.of(
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
            );
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }
}
