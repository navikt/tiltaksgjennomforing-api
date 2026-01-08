package no.nav.tag.tiltaksgjennomforing.enhet.entra;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EntraproxyService {
    private final EntraproxyClient client;

    public EntraproxyService(EntraproxyClient client) {
        this.client = client;
    }

    @Cacheable(CacheConfig.ENTRAPROXY_CACHE)
    public List<NavEnhet> hentEnheterNavAnsattHarTilgangTil(NavIdent ident) {
        return client.hentEnheterNavAnsattHarTilgangTil(ident).stream()
                .map(enhet -> new NavEnhet(enhet.enhetnummer(), enhet.navn()))
                .toList();
    }
}
