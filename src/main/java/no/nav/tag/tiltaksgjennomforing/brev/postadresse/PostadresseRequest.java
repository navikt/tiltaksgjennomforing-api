package no.nav.tag.tiltaksgjennomforing.brev.postadresse;

import lombok.Builder;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Builder(toBuilder = true)
public record PostadresseRequest(
	String ident,
	@Nullable Set<String> filtrerAdressebeskyttelse
) {}
