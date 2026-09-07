package no.nav.tag.tiltaksgjennomforing.brev.postadresse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostadresseTypeTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void fraNavn__skal_godta_regoppslag_sitt_visningsnavn() {
		assertThat(PostadresseType.fraNavn("NorskPostadresse")).isEqualTo(PostadresseType.NORSKPOSTADRESSE);
		assertThat(PostadresseType.fraNavn("UtenlandskPostadresse")).isEqualTo(PostadresseType.UTENLANDSKPOSTADRESSE);
	}

	@Test
	void fraNavn__skal_godta_enum_navn_fra_regoppslag() {
		assertThat(PostadresseType.fraNavn("NORSKPOSTADRESSE")).isEqualTo(PostadresseType.NORSKPOSTADRESSE);
		assertThat(PostadresseType.fraNavn("UTENLANDSKPOSTADRESSE")).isEqualTo(PostadresseType.UTENLANDSKPOSTADRESSE);
	}

	@Test
	void json_deserialisering__skal_godta_enum_navn_fra_regoppslag() throws Exception {
		PostadresseType postadresseType = objectMapper.readValue("\"NORSKPOSTADRESSE\"", PostadresseType.class);

		assertThat(postadresseType).isEqualTo(PostadresseType.NORSKPOSTADRESSE);
	}
}
