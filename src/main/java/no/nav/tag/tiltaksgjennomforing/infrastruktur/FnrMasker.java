package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import com.fasterxml.jackson.core.JsonStreamContext;
import net.logstash.logback.mask.ValueMasker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FnrMasker implements ValueMasker {
    private final Pattern fnrPattern = Pattern.compile("\\b\\d{11}\\b");

    private final Pattern partialDeltakerFnr = Pattern.compile("\"deltakerFnr\" *?: *?\"(\\d+)");

    @Override
    public Object mask(JsonStreamContext jsonStreamContext, Object o) {
        if (o instanceof CharSequence) {
            return maskPartialFnr(maskFnr((CharSequence) o));
        }
        return null;
    }

    private String maskFnr(CharSequence sequence) {
        Matcher matcher = fnrPattern.matcher(sequence);
        return matcher.replaceAll(mr -> mr.group().substring(0, 4) + "*******");
    }

    private String maskPartialFnr(CharSequence sequence) {
        Matcher matcher = partialDeltakerFnr.matcher(sequence);
        return matcher.replaceAll(mr -> "\"deltakerFnr\":\""
                + mr.group(1).substring(0, Math.min(4, mr.group(1).length()))
                + "*".repeat(Math.max(0, mr.group(1).length() - 4)));
    }

    public static void main(String[] args) {

    }
}