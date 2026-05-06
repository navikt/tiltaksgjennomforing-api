package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.variant.Variant;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FeatureToggleService {

    private final Unleash unleash;
    private final TokenUtils tokenUtils;

    @Autowired
    public FeatureToggleService(Unleash unleash, TokenUtils tokenUtils) {
        this.unleash = unleash;
        this.tokenUtils = tokenUtils;
    }

    public Map<String, Boolean> hentFeatureToggles(List<String> features) {
        return features.stream().collect(Collectors.toMap(feature -> feature, this::isEnabled));
    }

    public Map<String, Variant> hentVarianter(List<String> features) {
        return features.stream().collect(Collectors.toMap(
                feature -> feature,
                feature -> unleash.getVariant(feature, contextMedInnloggetBruker())
        ));
    }

    @Deprecated
    public Boolean isEnabled(String feature) {
        return unleash.isEnabled(feature, contextMedInnloggetBruker());
    }

    public Boolean isEnabled(FeatureToggle feature) {
        return isEnabled(feature, contextMedInnloggetBruker());
    }

    public Boolean isEnabled(FeatureToggle feature, UnleashContext context) {
        return unleash.isEnabled(feature.getToggleNavn(), context);
    }

    public boolean isTiltakSkrivebeskyttet(Tiltakstype tiltakstype) {
        boolean isSkrivebeskyttet = isEnabled(FeatureToggle.MIGRERING_SKRIVEBESKYTTET);
        return tiltakstype == Tiltakstype.MENTOR && isSkrivebeskyttet;
    }

    public boolean kanOppretteTiltak(Avtalerolle rolle, Tiltakstype tiltakstype) {
        if (!tiltakstype.isFirearigLonnstilskudd()) {
            return true;
        }
        return rolle.erInternBruker() ? isEnabled(FeatureToggle.FIREARIG_LONNSTILSKUDD) : false;
    }

    public boolean kanOppretteAvtale(Avtale avtale) {
        return harEnhetTilgangPaTiltak(avtale.getTiltakstype(), avtale.getEnhetOppfolging());
    }

    public boolean harEnhetTilgangPaTiltak(Tiltakstype tiltakstype, String enhet) {
        if (!tiltakstype.isFirearigLonnstilskudd()) {
            return true;
        }
        return Optional.ofNullable(enhet)
            .map(e -> UnleashContext.builder().addProperty("enhet", e).build())
            .map(context -> isEnabled(FeatureToggle.FIREARIG_LONNSTILSKUDD, context))
            .orElse(false);
    }

    private UnleashContext contextMedInnloggetBruker() {
        UnleashContext.Builder builder = UnleashContext.builder();
        tokenUtils.hentBrukerOgIssuer().map(a -> builder.userId(a.getBrukerIdent()));
        return builder.build();
    }

}
