package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class AvtaleQueryParameter {

    private NavIdent veilederNavIdent;
    private BedriftNr bedriftNr;
    private Fnr deltakerFnr;
    private Tiltakstype tiltakstype;
    private Status status;
    private Boolean erUfordelt;
    private TilskuddPeriodeStatus tilskuddPeriodeStatus;
    private String navEnhet;
    private Integer avtaleNr;

    public boolean harFilterPaEnEntitet() {
        return erUfordelt != null ||
            veilederNavIdent != null ||
            bedriftNr != null ||
            deltakerFnr != null ||
            navEnhet != null ||
            avtaleNr != null;
    }

    public boolean erSokPaEnkeltperson() {
        return deltakerFnr != null || avtaleNr != null;
    }

    public boolean erUfordelt() {
        return Optional.ofNullable(this.erUfordelt).orElse(false);
    }

    public String generateHash() {
        List<String> liste = List.of(Objects.toString(veilederNavIdent, "")
                , Objects.toString(bedriftNr, "")
                , Objects.toString(deltakerFnr, "")
                , Objects.toString(tiltakstype, "")
                , Objects.toString(status, "")
                , Objects.toString(erUfordelt, "")
                , Objects.toString(tilskuddPeriodeStatus, "")
                , Objects.toString(navEnhet, "")
                , Objects.toString(avtaleNr, ""));

        String predicateString = String.join(";", liste);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = digest.digest(predicateString.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


}
