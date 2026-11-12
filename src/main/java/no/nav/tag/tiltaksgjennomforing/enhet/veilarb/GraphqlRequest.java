package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import java.util.Map;

record GraphqlRequest(String query, Map<String, Object> variables) {}
