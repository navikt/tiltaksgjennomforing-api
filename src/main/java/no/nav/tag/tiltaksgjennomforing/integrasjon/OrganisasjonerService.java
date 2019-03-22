package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.tiltaksgjennomforing.controller.TokenUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OrganisasjonerService {









    public String getOrganisasjoner() throws IOException {
        TokenUtils tokenUtils = null;
        String dnaGetOrganisasjoner = "https://arbeidsgiver-q.nav.no/ditt-nav-arbeidsgiver/api/organisasjoner";
        String tokenString = TokenUtils.hentToken();

        URL url = new URL(dnaGetOrganisasjoner);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("selvbetjening", TokenUtils);

        StringBuilder result = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }




}


