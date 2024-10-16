package no.nav.tag.tiltaksgjennomforing.satser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleApiTestUtil.lagTokenForNavIdent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(Miljø.TEST)
@SpringBootTest
@AutoConfigureMockMvc
public class SatserAPITest {
    @Autowired
    SatserController satserController;
    @Autowired
    SatserRepository satserRepository;
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @AfterEach
    void tearDown() {
        satserRepository.deleteAll();
        Now.resetClock();
    }

    @Test
    public void settInnVTAOSatsTest() throws Exception {
        settInnSats(new SatsPeriodeData(
                6000.0,
                LocalDate.parse("2021-01-01"),
                LocalDate.parse("2021-12-31")
        ));

        settInnSats(new SatsPeriodeData(
                6500.0,
                LocalDate.parse("2022-01-01"),
                LocalDate.parse("2022-12-31")
        ));

        var resultat = satserController.hentSatser("VTAO");
        assertEquals(
                new Sats("vtao", List.of(
                        new SatserEntitet(
                                "VTAO",
                                6000.0,
                                LocalDate.of(2021, 1, 1),
                                LocalDate.of(2021, 12, 31)),
                        new SatserEntitet(
                                "VTAO",
                                6500.0,
                                LocalDate.of(2022, 1, 1),
                                LocalDate.of(2022, 12, 31))
                )),
                resultat);
    }

    private void settInnSats(SatsPeriodeData vtao) throws Exception {
        postForNavIdent(
                "/satser/sats/vtao",
                objectMapper.writeValueAsString(vtao),
                "X123456"
        ).andExpect(status().is(201));
    }

    @Test
    public void settInnOverlappendeVTAOSatsFeilerTest() {
        var opprinneligSats = new SatsPeriodeData(
                6000.0,
                LocalDate.parse("2021-01-01"),
                LocalDate.parse("2021-12-31")
        );

        satserController.settInnSats("VTAO", opprinneligSats);

        assertThrows(
                IllegalArgumentException.class,
                () -> satserController.settInnSats("VTAO", new SatsPeriodeData(
                        6500.0,
                        LocalDate.parse("2020-12-31"),
                        LocalDate.parse("2022-01-01")
                )), "Sett inn samme periode på ny");

        assertThrows(
                IllegalArgumentException.class,
                () -> satserController.settInnSats("VTAO", new SatsPeriodeData(
                        6500.0,
                        LocalDate.parse("2021-03-31"),
                        LocalDate.parse("2021-11-30")
                )), "Sett inn periode som 'innerlapper' perioden");

        assertThrows(
                IllegalArgumentException.class,
                () -> satserController.settInnSats("VTAO", new SatsPeriodeData(
                        6500.0,
                        LocalDate.parse("2021-10-01"),
                        LocalDate.parse("2022-12-31")
                )), "Sett inn periode som overlapper mot enden av perioden");

        assertThrows(
                IllegalArgumentException.class,
                () -> satserController.settInnSats("VTAO", new SatsPeriodeData(
                        6500.0,
                        LocalDate.parse("2021-10-01"),
                        null
                )), "Sett inn periode som overlapper mot med åpen sluttdato");

        assertThrows(
                IllegalArgumentException.class,
                () -> satserController.settInnSats("VTAO", new SatsPeriodeData(
                        6500.0,
                        LocalDate.parse("2020-11-01"),
                        LocalDate.parse("2021-03-31")
                )), "Sett inn periode som overlapper mot starten av perioden");

        assertThrows(
                IllegalArgumentException.class,
                () -> satserController.settInnSats("VTAO", new SatsPeriodeData(
                        6500.0,
                        LocalDate.parse("2020-12-31"),
                        LocalDate.parse("2022-01-01")
                )), "Sett inn en periode som 'OVERlapper' en annen (dekker perioden)");
    }

    @Test
    void hentGjeldendeSatserTest() throws Exception {
        satserController.settInnSats("VTAO", new SatsPeriodeData(
                6500.0,
                LocalDate.parse("2020-12-31"),
                LocalDate.parse("2022-01-01")));
        satserController.settInnSats("5G", new SatsPeriodeData(
                6500.0,
                LocalDate.parse("2020-12-31"),
                LocalDate.parse("2022-01-01")));

        getResultat("/satser/typer").andExpectAll(
                status().is(200),
                content().json(objectMapper.writeValueAsString(Set.of("vtao", "5g")))
        );
    }

    private ResultActions getResultat(String url) throws Exception {
        return mockMvc.perform(get(url));
    }

    private ResultActions postForNavIdent(String url, String body, String navIdent) throws Exception {
        var token = lagTokenForNavIdent(new NavIdent(navIdent));

        return mockMvc.perform(
                post(url)
                        .content(body)
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
        );
    }
}
