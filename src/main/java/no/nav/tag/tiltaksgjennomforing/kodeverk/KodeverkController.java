package no.nav.tag.tiltaksgjennomforing.kodeverk;

import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Unprotected
@RequestMapping("/kodeverk")
public class KodeverkController {
    @GetMapping
    public Map<String, List<Object>> get() {
        Map<String, List<Object>> map = new HashMap<>();
        map.put("tiltakstype", Arrays.stream(Tiltakstype.values()).map(t -> {
            HashMap<String, String> tiltakMap = new HashMap<>();
            tiltakMap.put("navn", t.name());
            tiltakMap.put("beskrivelse", t.getBeskrivelse());
            tiltakMap.put("behandlingstema", t.getBehandlingstema());
            if (t.getTiltakskodeArena() != null) {
                tiltakMap.put("tiltakskodeArena", t.getTiltakskodeArena());
            }
            return tiltakMap;
        }).collect(Collectors.toList()));

        map.put("status", Arrays.stream(Status.values()).map(s -> {
            HashMap<String, String> statusMap = new HashMap<>();
            statusMap.put("navn", s.name());
            statusMap.put("beskrivelse", s.getBeskrivelse());
            return statusMap;
        }).collect(Collectors.toList()));

        return map;
    }
}
