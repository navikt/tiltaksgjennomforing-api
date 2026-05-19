package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.ForkortetGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.TilskuddstrinnDTO;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public class AvroTiltakHendelseFabrikk {
    public static AvroTiltakHendelse konstruer(Avtale avtale, DvhHendelseType hendelseType, String utførtAv) {
        return konstruer(avtale, hendelseType, utførtAv, null);
    }

    public static AvroTiltakHendelse konstruer(Avtale avtale, DvhHendelseType hendelseType, String utførtAv, ForkortetGrunn forkortetGrunn) {
        AvroTiltakHendelse hendelse = new AvroTiltakHendelse();
        hendelse.setMeldingId(UUID.randomUUID().toString());
        hendelse.setTidspunkt(Now.instant());
        hendelse.setAvtaleId(avtale.getId().toString());
        hendelse.setAvtaleInnholdId(avtale.getGjeldendeInnhold().getId().toString());
        hendelse.setTiltakstype(TiltakType.valueOf(avtale.getTiltakstype().name()));
        hendelse.setTiltakskodeArena(avtale.getTiltakstype().getTiltakskodeArena() != null ? TiltakKodeArena.valueOf(avtale.getTiltakstype().getTiltakskodeArena()) : null);
        hendelse.setHendelseType(hendelseType.name());
        hendelse.setTiltakStatus(avtale.getStatus().name());
        hendelse.setDeltakerFnr(avtale.getDeltakerFnr().asString());
        hendelse.setBedriftNr(avtale.getBedriftNr().asString());
        hendelse.setVeilederNavIdent(avtale.getVeilederNavIdent().asString());
        hendelse.setHarFamilietilknytning(avtale.getGjeldendeInnhold().getHarFamilietilknytning());
        hendelse.setStartDato(avtale.getGjeldendeInnhold().getStartDato());
        hendelse.setSluttDato(avtale.getGjeldendeInnhold().getSluttDato());
        hendelse.setStillingprosent(avtale.getGjeldendeInnhold().getStillingprosent() != null ?  avtale.getGjeldendeInnhold().getStillingprosent().floatValue() : null);
        hendelse.setAntallDagerPerUke(avtale.getGjeldendeInnhold().getAntallDagerPerUke() != null ? avtale.getGjeldendeInnhold().getAntallDagerPerUke().floatValue() : null);
        hendelse.setStillingstittel(avtale.getGjeldendeInnhold().getStillingstittel());
        hendelse.setStillingstype(avtale.getGjeldendeInnhold().getStillingstype() != null ? StillingType.valueOf(avtale.getGjeldendeInnhold().getStillingstype().name()) : null);
        hendelse.setStillingStyrk08(avtale.getGjeldendeInnhold().getStillingStyrk08());
        hendelse.setStillingKonseptId(avtale.getGjeldendeInnhold().getStillingKonseptId());
        hendelse.setLonnstilskuddProsent(avtale.getGjeldendeInnhold().getLonnstilskuddProsent());
        hendelse.setManedslonn(avtale.getGjeldendeInnhold().getManedslonn());
        hendelse.setFeriepengesats(avtale.getGjeldendeInnhold().getFeriepengesats() != null ? avtale.getGjeldendeInnhold().getFeriepengesats().floatValue() : null);
        hendelse.setFeriepengerBelop(avtale.getGjeldendeInnhold().getFeriepengerBelop());
        hendelse.setArbeidsgiveravgift(avtale.getGjeldendeInnhold().getArbeidsgiveravgift() != null ? avtale.getGjeldendeInnhold().getArbeidsgiveravgift().floatValue() : null);
        hendelse.setArbeidsgiveravgiftBelop(avtale.getGjeldendeInnhold().getArbeidsgiveravgiftBelop());
        hendelse.setOtpSats(avtale.getGjeldendeInnhold().getOtpSats() != null ? avtale.getGjeldendeInnhold().getOtpSats().floatValue() : null);
        hendelse.setOtpBelop(avtale.getGjeldendeInnhold().getOtpBelop());
        hendelse.setSumLonnsutgifter(avtale.getGjeldendeInnhold().getSumLonnsutgifter());
        hendelse.setSumLonnstilskudd(avtale.getGjeldendeInnhold().getSumLonnstilskudd());
        hendelse.setSumLonnstilskuddRedusert(null);
        hendelse.setDatoForRedusertProsent(null);
        hendelse.setGodkjentPaVegneAv(avtale.getGjeldendeInnhold().isGodkjentPaVegneAv());
        hendelse.setIkkeBankId(avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn() != null && avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn().isIkkeBankId());
        hendelse.setReservert(avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn() != null && avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn().isReservert());
        hendelse.setDigitalKompetanse(avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn() != null && avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn().isDigitalKompetanse());
        hendelse.setArenaMigreringDeltaker(avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn() != null && avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn().isArenaMigreringDeltaker());
        hendelse.setGodkjentAvDeltaker(avtale.getGjeldendeInnhold().getGodkjentAvDeltaker());
        hendelse.setGodkjentAvArbeidsgiver(avtale.getGjeldendeInnhold().getGodkjentAvArbeidsgiver());
        hendelse.setGodkjentAvVeileder(avtale.getGjeldendeInnhold().getGodkjentAvVeileder());
        hendelse.setGodkjentAvBeslutter(avtale.getGjeldendeInnhold().getGodkjentAvBeslutter());
        hendelse.setAvtaleInngaatt(avtale.getGjeldendeInnhold().getAvtaleInngått());
        hendelse.setUtfortAv(utførtAv);
        hendelse.setEnhetOppfolging(avtale.getEnhetOppfolging());
        hendelse.setEnhetGeografisk(avtale.getEnhetGeografisk());
        hendelse.setOpprettetAvArbeidsgiver(Avtaleopphav.ARBEIDSGIVER.equals(avtale.getOpphav()));
        hendelse.setAnnullertTidspunkt(avtale.getAnnullertTidspunkt());
        hendelse.setAnnullertGrunn(avtale.getAnnullertGrunn());
        hendelse.setMaster(erMaster(avtale));
        hendelse.setForkortetGrunn(Optional.ofNullable(forkortetGrunn).flatMap(ForkortetGrunn::utled).orElse(null));
        hendelse.setAvtaleNr(avtale.getAvtaleNr());
        hendelse.setArbeidstreningsMaal(mapArbeidstreningsMaal(avtale));
        hendelse.setMentorTimelonn(avtale.getGjeldendeInnhold().getMentorTimelonn());
        hendelse.setMentorAntallTimer(avtale.getGjeldendeInnhold().getMentorAntallTimer());
        hendelse.setLonnstilskuddFormaal(mapLonnstilskuddFormaal(avtale));
        hendelse.setInkluderingstilskuddsutgift(mapInkluderingstilskuddsutgift(avtale));
        hendelse.setTilskuddstrinn(mapTilskuddstrinn(avtale));
        return hendelse;
    }

    private LonnstilskuddFormaal mapLonnstilskuddFormaal(Avtale avtale) {
        return avtale.getGjeldendeInnhold().getLonnstilskuddFormaal() != null
            ? LonnstilskuddFormaal.valueOf(avtale.getGjeldendeInnhold().getLonnstilskuddFormaal().name())
            : null;
    }

    private List<MaalKategori> mapArbeidstreningsMaal(Avtale avtale) {
        return avtale.getGjeldendeInnhold().getMaal().stream()
                .filter(maal -> maal.getKategori() != null)
                .map(maal -> switch (maal.getKategori()) {
                    case FÅ_JOBB_I_BEDRIFTEN -> MaalKategori.FAA_JOBB_I_BEDRIFTEN;
                    case UTPRØVING -> MaalKategori.UTPROVING;
                    case SPRÅKOPPLÆRING -> MaalKategori.SPRAAKOPPLAERING;
                    case OPPNÅ_FAGBREV_KOMPETANSEBEVIS -> MaalKategori.OPPNAA_FAGBREV_KOMPETANSEBEVIS;
                    default -> MaalKategori.valueOf(maal.getKategori().name());
                })
                .collect(Collectors.toList());
    }

    private List<InkluderingstilskuddsutgiftRecord> mapInkluderingstilskuddsutgift(Avtale avtale) {
        return avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift().stream()
            .map(utgift -> InkluderingstilskuddsutgiftRecord.newBuilder()
                .setBelop(utgift.getBeløp())
                .setType(mapInkluderingstilskuddsutgiftType(utgift))
                .build()
            )
            .collect(Collectors.toList());
    }

    private InkluderingstilskuddsutgiftType mapInkluderingstilskuddsutgiftType(Inkluderingstilskuddsutgift utgift) {
        return switch (utgift.getType()) {
            case OPPLÆRING -> InkluderingstilskuddsutgiftType.OPPLAERING;
            case null -> null;
            default -> InkluderingstilskuddsutgiftType.valueOf(utgift.getType().name());
        };
    }

    private List<TilskuddstrinnRecord> mapTilskuddstrinn(Avtale avtale) {
        return TilskuddstrinnDTO.map(avtale.getGjeldendeInnhold()).stream()
                .map(t -> TilskuddstrinnRecord.newBuilder()
                    .setStart(t.start())
                    .setSlutt(t.slutt())
                    .setProsent(t.prosent())
                    .setBelopPerMnd(t.belopPerMnd())
                    .build()
                )
                .collect(Collectors.toList());
    }

    private Boolean erMaster(Avtale avtale) {
        if (avtale.getTiltakstype() == Tiltakstype.SOMMERJOBB || avtale.getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD || avtale.getTiltakstype() == Tiltakstype.VARIG_LONNSTILSKUDD) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
