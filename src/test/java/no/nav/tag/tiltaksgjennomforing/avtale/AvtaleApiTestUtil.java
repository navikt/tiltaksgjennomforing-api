package no.nav.tag.tiltaksgjennomforing.avtale;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;

public class AvtaleApiTestUtil {

    private static String tokenRequest(String url) {
        try {
            return HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create(url))
                            .build(), HttpResponse.BodyHandlers.ofString()
            ).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String lagTokenForFnr(Fnr fnr) {
        return tokenRequest(format("https://tiltak-fakelogin.ekstern.dev.nav.no/token?pid=%s&aud=fake-tokenx&iss=tokenx&acr=Level4", fnr.asString()));
    }

    public static String lagTokenForNavIdent(NavIdent navIdent) {
        return tokenRequest(format("https://tiltak-fakelogin.ekstern.dev.nav.no/token?NAVident=%s&aud=fake-aad&iss=aad&acr=Level4", navIdent.asString()));
    }

    static MockHttpServletResponse getForPart(MockMvc mockMvc, Avtalepart<?> part, String url) throws Exception {
        var headers = new HttpHeaders();

        String token = "";
        if (part.getIdentifikator() instanceof Fnr fnr) {
            token = lagTokenForFnr(fnr);
        } else if (part.getIdentifikator() instanceof NavIdent ident) {
            token = lagTokenForNavIdent(ident);
        }

        headers.put("Authorization", List.of("Bearer " + token));
        return mockMvc.perform(MockMvcRequestBuilders.get(URI.create(url))
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .cookie(new Cookie("innlogget-part", part.rolle().name()))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    public static boolean jsonHarVerdi(String content, Object verdi) {
        return verdier(JsonPath.read(content, "$")).stream().anyMatch(x -> Objects.equals(x, verdi));
    }

    public static boolean jsonHarNøkkel(String content, Object key) {
        return keys(JsonPath.read(content, "$")).stream().anyMatch(x -> {
            if (x instanceof Map.Entry<?, ?> entry) {
                return Objects.equals(entry.getKey(), key);
            }
            return false;
        });
    }

    private static List<Object> verdier(Object obj) {
        if (obj instanceof LinkedHashMap<?, ?> map) {
            return map.values().stream().flatMap(x -> verdier(x).stream()).toList();
        } else if (obj instanceof List<?> list) {
            return list.stream().flatMap(x -> verdier(x).stream()).toList();
        } else {
            var list = new ArrayList<>();
            list.add(obj);
            return list;
        }
    }

    private static List<Object> keys(Object obj) {
        if (obj instanceof LinkedHashMap<?, ?> map) {
            return Stream.concat(map.entrySet().stream(), map.values().stream().flatMap(x -> keys(x).stream())).toList();
        } else if (obj instanceof List<?> list) {
            return list.stream().flatMap(x -> keys(x).stream()).toList();
        } else {
            return List.of();
        }
    }
}
