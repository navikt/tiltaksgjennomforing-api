package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;

import java.util.List;

@Value
public class GodkjennFlereTilskuddsperioderRequest {
    String enhet;
    List<String> tilskuddsperioderIder;
}
