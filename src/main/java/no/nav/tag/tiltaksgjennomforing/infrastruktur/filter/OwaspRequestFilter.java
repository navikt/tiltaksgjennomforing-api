package no.nav.tag.tiltaksgjennomforing.infrastruktur.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class OwaspRequestFilter extends OncePerRequestFilter {

    // Maksimal lengde på request body (10 MB)
    private static final int MAX_REQUEST_BODY_SIZE = 10 * 1024 * 1024;

    // Maksimal lengde på header verdier
    private static final int MAX_HEADER_VALUE_LENGTH = 4096;

    // Regex patterns for farlige mønstre
    private static final List<Pattern> DANGEROUS_PATTERNS = Arrays.asList(
        // SQL Injection patterns - sjekk for vanlige SQL injection mønstre
        // Limit the wildcard to prevent ReDoS - max 200 chars between keywords
        Pattern.compile("(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|EXECUTE)\\b.{0,200}?\\b(FROM|INTO|WHERE|TABLE)\\b)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("('\\s*OR\\s+'?\\d+'?\\s*=\\s*'?\\d+'?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("('\\s*AND\\s+'?\\d+'?\\s*=\\s*'?\\d+'?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(--|;--)"),

        // XSS patterns - sjekk for farlige HTML tags og JavaScript
        // Limit script tag content to prevent ReDoS - max 10000 chars
        Pattern.compile("<script[^>]{0,500}?>.{0,10000}?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<iframe[\\s>]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on(load|error|click|mouse|focus|blur|change|submit)\\s*=", Pattern.CASE_INSENSITIVE),
        // Limit img tag attributes to prevent ReDoS - max 500 chars
        Pattern.compile("<img[^>]{0,500}?src\\s*=", Pattern.CASE_INSENSITIVE),

        // Path traversal - sjekk for directory traversal forsøk
        Pattern.compile("\\.\\./"),
        Pattern.compile("\\.\\.\\\\"),

        // Command injection - sjekk for shell kommandoer
        Pattern.compile("[;&|]\\s*(cat|ls|wget|curl|bash|sh|cmd|powershell|rm|chmod)", Pattern.CASE_INSENSITIVE),

        // Null bytes - sjekk for null byte injection
        Pattern.compile("\\x00")
    );

    // Paths som skal hoppes over (f.eks. actuator, swagger, etc.)
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/actuator/",
        "/swagger-ui/",
        "/v3/api-docs/",
        "/internal/",
        "/isalive",
        "/isready",
        "/metrics"
    );

    @Override
    protected void doFilterInternal(
            @NotNull final HttpServletRequest request,
            @NotNull final HttpServletResponse response,
            @NotNull final FilterChain chain
    ) {
        try {
            validateHeaders(request);

            // Wrap request for å kunne lese body flere ganger
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
            String contentType = request.getContentType();
            if (contentType != null && (
                    contentType.contains(MediaType.APPLICATION_JSON_VALUE) ||
                    contentType.contains(MediaType.TEXT_PLAIN_VALUE) ||
                    contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE))) {

                validateRequestBody(wrappedRequest);
            }

            chain.doFilter(wrappedRequest, response);
        } catch (FeilkodeException e) {
            log.warn("Validering feilet for {} {}", request.getMethod(), request.getRequestURI());
            throw e;
        } catch (Exception e) {
            log.error("Uventet feil i tekstvalidering for {} {}", request.getMethod(), request.getRequestURI(), e);
            throw new FeilkodeException(Feilkode.UGYLDIG_FORESPØRSEL);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().replace("/tiltaksgjennomforing-api", "");
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private void validateHeaders(HttpServletRequest request) {
        // Valider at headers ikke er for lange eller inneholder farlige mønstre
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                if (headerValue.length() > MAX_HEADER_VALUE_LENGTH) {
                    log.warn("Header {} er for lang: {} tegn", headerName, headerValue.length());
                    throw new FeilkodeException(Feilkode.UGYLDIG_FORESPØRSEL);
                }
                validateTextContent(headerValue, "HTTP Header: " + headerName);
            }
        });
    }

    private void validateRequestBody(CachedBodyHttpServletRequest request) {
        byte[] content = request.getCachedBody();

        if (content.length > MAX_REQUEST_BODY_SIZE) {
            log.warn("Request body er for stor: {} bytes", content.length);
            throw new FeilkodeException(Feilkode.UGYLDIG_FORESPØRSEL);
        }

        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            validateTextContent(body, "Request body");
        }
    }

    private void validateTextContent(String text, String context) {
        if (text == null || text.isEmpty()) {
            return;
        }

        // Sjekk mot farlige mønstre
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(text).find()) {
                log.warn("Farlig mønster funnet i {}: {}", context, pattern.pattern());
                throw new FeilkodeException(Feilkode.UGYLDIG_FORESPØRSEL);
            }
        }

        // Sjekk for excessive control characters (unntatt vanlige som \n, \r, \t)
        long controlCharCount = text.chars()
            .filter(c -> Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t')
            .count();

        if (controlCharCount > 0) {
            log.warn("Ugyldige kontrolltegn funnet i {}", context);
            throw new FeilkodeException(Feilkode.UGYLDIG_FORESPØRSEL);
        }

        // Sjekk for unprintable characters (ikke-printbare tegn)
        // Tillatte tegn: printable ASCII (32-126), extended ASCII/Unicode printable, og whitespace (\n, \r, \t)
        long unprintableCount = text.chars()
            .filter(c -> !isPrintableOrAllowedWhitespace(c))
            .count();

        if (unprintableCount > 0) {
            log.warn("Ikke-printbare tegn funnet i {}: {} tegn", context, unprintableCount);
            throw new FeilkodeException(Feilkode.UGYLDIG_FORESPØRSEL);
        }
    }

    /**
     * Sjekker om et tegn er printbart eller tillatt whitespace.
     * Tillater: printable ASCII, Unicode letters/digits, og standard whitespace.
     */
    private boolean isPrintableOrAllowedWhitespace(int codePoint) {
        // Tillat standard whitespace
        if (codePoint == '\n' || codePoint == '\r' || codePoint == '\t' || codePoint == ' ') {
            return true;
        }

        // Tillat printable ASCII (33-126, eksluderer space som allerede er håndtert)
        if (codePoint >= 33 && codePoint <= 126) {
            return true;
        }

        // Tillat emojis og andre symboler
        if (Character.isEmoji(codePoint) || Character.isEmojiPresentation(codePoint)) {
            return true;
        }

        // Tillat Unicode letters, digits, og andre printbare tegn
        // Dette dekker norske tegn (æøå), europeiske tegn, etc.
        if (Character.isLetterOrDigit(codePoint) ||
            Character.isSpaceChar(codePoint) ||
            isPrintableSymbol(codePoint)) {
            return true;
        }

        return false;
    }

    /**
     * Sjekker om et tegn er et tillatt printbart symbol.
     */
    private boolean isPrintableSymbol(int codePoint) {
        int type = Character.getType(codePoint);
        return type == Character.DASH_PUNCTUATION ||
               type == Character.START_PUNCTUATION ||
               type == Character.END_PUNCTUATION ||
               type == Character.CONNECTOR_PUNCTUATION ||
               type == Character.OTHER_PUNCTUATION ||
               type == Character.MATH_SYMBOL ||
               type == Character.CURRENCY_SYMBOL ||
               type == Character.MODIFIER_SYMBOL ||
               type == Character.OTHER_SYMBOL ||
               type == Character.INITIAL_QUOTE_PUNCTUATION ||
               type == Character.FINAL_QUOTE_PUNCTUATION;
    }
}
