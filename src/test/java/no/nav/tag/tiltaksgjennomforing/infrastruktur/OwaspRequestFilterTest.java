package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class OwaspRequestFilterTest {

    private OwaspRequestFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new OwaspRequestFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void hopper_over_GET_foresporsler_uten_body() throws ServletException, IOException {
        request.setMethod("GET");
        request.setRequestURI("/avtaler");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void hopper_over_DELETE_foresporsler_uten_body() throws ServletException, IOException {
        request.setMethod("DELETE");
        request.setRequestURI("/avtaler/123");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void validerer_POST_med_body() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"test\":\"valid content\"}".getBytes());

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void validerer_PUT_med_body() throws ServletException, IOException {
        request.setMethod("PUT");
        request.setRequestURI("/avtaler/123");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"test\":\"valid content\"}".getBytes());

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void blokkerer_foresporsler_med_SQL() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"query\":\"SELECT * FROM users WHERE id='1' OR '1'='1'\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_foresporsler_med_XSS() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"description\":\"<script>alert('XSS')</script>\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_foresporsler_med_iframe_XSS() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"description\":\"<iframe src='evil.com'></iframe>\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_foresporsler_med_javascript() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"url\":\"javascript:alert('XSS')\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_foresporsler_med_path_traversal() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"file\":\"../../etc/passwd\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_foresporsler_med_command_injection() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"cmd\":\"test; cat /etc/passwd\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_foresporsler_med_for_stor_payload_i_body() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Lag en body som er større enn 10MB
        byte[] largeContent = new byte[11 * 1024 * 1024];
        request.setContent(largeContent);

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_foresporsler_med_for_lange_headers() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Lag en header som er større enn 4096 tegn
        String longHeader = "a".repeat(5000);
        request.addHeader("X-Custom-Header", longHeader);

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_foresporsler_med_nyll_bytes() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"test\":\"value\u0000\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void tillatter_normal_tekst_med_norske_bokstaver() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"navn\":\"Test Testesen\",\"bedrift\":\"Testbedrift AS\",\"beskrivelse\":\"Dette er en normal beskrivelse med æøå\"}".getBytes());

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void tillatter_ny_linje_og_tabs() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"beskrivelse\":\"Linje 1\\nLinje 2\\tMed tab\\r\\nLinje 3\"}".getBytes());

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void validerer_ikke_actuator_endepunkt() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/actuator/health");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void validerer_ikke_swagger_endepunkt() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/swagger-ui/index.html");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void validerer_ikke_internal_status_endepunkt() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/internal/status");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void validerer_tom_body() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("".getBytes());

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void skal_blokkere_onclick_inline_javascript() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"html\":\"<div onclick='alert(1)'>\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void skal_blokkere_img_med_inline_javascript() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"html\":\"<img src=x onerror='alert(1)'>\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void tillatter_datoer_og_kommatall() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"id\":123,\"dato\":\"2024-01-15\",\"beløp\":5000.50,\"aktiv\":true}".getBytes());

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void tillater_store_og_sma_norske_bokstaver() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"beskrivelse\":\"Ærlighet, Øvelse og Å være god\"}".getBytes());

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void blokkerer_unicode_kontrolltegn() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // Unicode control character U+0001 (Start of Heading)
        request.setContent("{\"test\":\"value\u0001\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_tegn_som_er_unprintable() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // ASCII control character (Bell character - 0x07)
        request.setContent("{\"test\":\"value\u0007\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void blokkerer_vertical_tab_og_form_feed() {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // Vertical tab (0x0B) and form feed (0x0C) are control characters
        request.setContent("{\"test\":\"value\u000B\u000C\"}".getBytes());

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(FeilkodeException.class)
            .hasFieldOrPropertyWithValue("feilkode", Feilkode.UGYLDIG_FORESPØRSEL);
    }

    @Test
    void tillater_symboler_anforselstegn_valutategn_og_parenteser() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"tekst\":\"100% av €1,000.50 (euro) - test: 'success' + [ok]!\"}".getBytes());

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void tillatter_emojis_og_andre_typer_anforselstegn() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/avtaler");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"melding\":\"God dag! ✓ Dette er en test «med» norske anførselstegn\"}".getBytes());

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(), any());
    }
}
