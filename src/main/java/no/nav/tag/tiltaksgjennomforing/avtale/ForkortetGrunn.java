package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor(staticName = "av")
public class ForkortetGrunn {
    public static final String BEGYNT_I_ARBEID = "Begynt i arbeid";
    public static final String FÅTT_TILBUD_OM_ANNET_TILTAK = "Fått tilbud om annet tiltak";
    public static final String SYK = "Syk";
    public static final String IKKE_MØTT = "Ikke møtt";
    public static final String FULLFØRT = "Fullført";
    public static final String AVSLUTTET_I_ARENA = "Avtalen er avsluttet i Arena";
    public static final String ANNET = "Annet";

    private String grunn;
    private String annetGrunn;

    public Optional<String> utled() {
        if (StringUtils.isBlank(grunn)) {
            return Optional.empty();
        }
        if (ANNET.equals(grunn)) {
            return StringUtils.isBlank(annetGrunn) ? Optional.empty() : Optional.of(annetGrunn);
        }
        return Optional.of(grunn);
    }

    public boolean mangler() {
        return utled().isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ForkortetGrunn forkortetGrunn)) {
            return false;
        }
        return Objects.equals(grunn, forkortetGrunn.grunn) && Objects.equals(annetGrunn, forkortetGrunn.annetGrunn);
    }
}
