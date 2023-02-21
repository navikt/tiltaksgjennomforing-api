package no.nav.tag.tiltaksgjennomforing.enhet;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class VeilarbArenaCache {

    private final VeilarbArenaClient veilarbArenaClient;

    @Autowired
    public VeilarbArenaCache(VeilarbArenaClient veilarbArenaClient) {
        this.veilarbArenaClient = veilarbArenaClient;
    }

    @Cacheable(value = "arena")
    public String hentOppfølgingsenhet(Avtale avtale) {
        return veilarbArenaClient.hentOppfølgingsEnhet(avtale.getDeltakerFnr().asString());
    }
}
