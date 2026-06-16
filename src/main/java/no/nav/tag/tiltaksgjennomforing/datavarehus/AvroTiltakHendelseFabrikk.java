package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.ForkortetGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.TilskuddstrinnDTO;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class AvroTiltakHendelseFabrikk {
    public static AvroTiltakHendelse konstruer(Avtale avtale, DvhHendelseType hendelseType, String utførtAv) {
        return konstruer(avtale, hendelseType, utførtAv, null);
    }

    public static AvroTiltakHendelse konstruer(Avtale avtale, DvhHendelseType hendelseType, String utførtAv, ForkortetGrunn forkortetGrunn) {
        AvroTiltakHendelse hendelse = new AvroTiltakHendelse();

        hendelse.setAnnullertGrunn(avtale.getAnnullertGrunn());
        hendelse.setAnnullertTidspunkt(avtale.getAnnullertTidspunkt());
        hendelse.setArbeidsgiveravgiftBelop(avtale.getGjeldendeInnhold().getArbeidsgiveravgiftBelop());
        hendelse.setArbeidstreningsMaal(mapArbeidstreningsMaal(avtale));
        hendelse.setAvtaleId(avtale.getId().toString());
        hendelse.setAvtaleInnholdId(avtale.getGjeldendeInnhold().getId().toString());
        hendelse.setAvtaleNr(avtale.getAvtaleNr());
        hendelse.setBedriftNr(avtale.getBedriftNr().asString());
        hendelse.setDatoForRedusertProsent(null);
        hendelse.setDeltakerFnr(avtale.getDeltakerFnr().asString());
        hendelse.setEnhetGeografisk(avtale.getEnhetGeografisk());
        hendelse.setEnhetOppfolging(avtale.getEnhetOppfolging());
        hendelse.setFeriepengerBelop(avtale.getGjeldendeInnhold().getFeriepengerBelop());
        hendelse.setForkortetGrunn(Optional.ofNullable(forkortetGrunn).flatMap(ForkortetGrunn::utled).orElse(null));
        hendelse.setGodkjentPaVegneAv(avtale.getGjeldendeInnhold().isGodkjentPaVegneAv());
        hendelse.setHarFamilietilknytning(avtale.getGjeldendeInnhold().getHarFamilietilknytning());
        hendelse.setHendelseType(hendelseType.name());
        hendelse.setInkluderingstilskuddsutgift(mapInkluderingstilskuddsutgift(avtale));
        hendelse.setLonnstilskuddFormaal(mapLonnstilskuddFormaal(avtale));
        hendelse.setLonnstilskuddProsent(avtale.getGjeldendeInnhold().getLonnstilskuddProsent());
        hendelse.setManedslonn(avtale.getGjeldendeInnhold().getManedslonn());
        hendelse.setMaster(erMaster(avtale));
        hendelse.setMentorAntallTimer(avtale.getGjeldendeInnhold().getMentorAntallTimer());
        hendelse.setMentorTimelonn(avtale.getGjeldendeInnhold().getMentorTimelonn());
        hendelse.setOpprettetAvArbeidsgiver(Avtaleopphav.ARBEIDSGIVER.equals(avtale.getOpphav()));
        hendelse.setOtpBelop(avtale.getGjeldendeInnhold().getOtpBelop());
        hendelse.setSluttDato(avtale.getGjeldendeInnhold().getSluttDato());
        hendelse.setStartDato(avtale.getGjeldendeInnhold().getStartDato());
        hendelse.setStillingKonseptId(avtale.getGjeldendeInnhold().getStillingKonseptId());
        hendelse.setStillingStyrk08(avtale.getGjeldendeInnhold().getStillingStyrk08());
        hendelse.setStillingstittel(avtale.getGjeldendeInnhold().getStillingstittel());
        hendelse.setSumLonnstilskudd(avtale.getGjeldendeInnhold().getSumLonnstilskudd());
        hendelse.setSumLonnstilskuddRedusert(null);
        hendelse.setSumLonnsutgifter(avtale.getGjeldendeInnhold().getSumLonnsutgifter());
        hendelse.setTilskuddstrinn(mapTilskuddstrinn(avtale));
        hendelse.setTiltakStatus(avtale.getStatus().name());
        hendelse.setTiltakstype(TiltakType.valueOf(avtale.getTiltakstype().name()));
        hendelse.setUtfortAv(utførtAv);
        hendelse.setVeilederNavIdent(avtale.getVeilederNavIdent().asString());

        Optional.ofNullable(avtale.getVeilederNavIdent()).ifPresent(navIdent -> hendelse.setVeilederNavIdent(navIdent.asString()));
        Optional.ofNullable(avtale.getGjeldendeInnhold().getAntallDagerPerUke()).ifPresent(adpu -> hendelse.setAntallDagerPerUke(adpu.floatValue()));
        Optional.ofNullable(avtale.getGjeldendeInnhold().getArbeidsgiveravgift()).ifPresent(aga -> hendelse.setArbeidsgiveravgift(aga.floatValue()));
        Optional.ofNullable(avtale.getGjeldendeInnhold().getAvtaleInngått()).ifPresent(hendelse::setAvtaleInngaatt);
        Optional.ofNullable(avtale.getGjeldendeInnhold().getFeriepengesats()).ifPresent(fps -> hendelse.setFeriepengesats(fps.floatValue()));
        Optional.ofNullable(avtale.getGjeldendeInnhold().getGodkjentAvArbeidsgiver()).ifPresent(hendelse::setGodkjentAvArbeidsgiver);
        Optional.ofNullable(avtale.getGjeldendeInnhold().getGodkjentAvBeslutter()).ifPresent(hendelse::setGodkjentAvBeslutter);
        Optional.ofNullable(avtale.getGjeldendeInnhold().getGodkjentAvDeltaker()).ifPresent(hendelse::setGodkjentAvDeltaker);
        Optional.ofNullable(avtale.getGjeldendeInnhold().getGodkjentAvVeileder()).ifPresent(hendelse::setGodkjentAvVeileder);
        Optional.ofNullable(avtale.getGjeldendeInnhold().getOtpSats()).ifPresent(otp -> hendelse.setOtpSats(otp.floatValue()));
        Optional.ofNullable(avtale.getGjeldendeInnhold().getStillingprosent()).ifPresent(sp -> hendelse.setStillingprosent(sp.floatValue()));
        Optional.ofNullable(avtale.getGjeldendeInnhold().getStillingstype()).ifPresent(st -> hendelse.setStillingstype(StillingType.valueOf(st.name())));
        Optional.ofNullable(avtale.getTiltakstype().getTiltakskodeArena()).ifPresent(ttka -> hendelse.setTiltakskodeArena(TiltakKodeArena.valueOf(ttka)));
        Optional.ofNullable(avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn()).ifPresent(godkjentPaVegneGrunn -> {
            hendelse.setIkkeBankId(godkjentPaVegneGrunn.isIkkeBankId());
            hendelse.setReservert(godkjentPaVegneGrunn.isReservert());
            hendelse.setDigitalKompetanse(godkjentPaVegneGrunn.isDigitalKompetanse());
            hendelse.setArenaMigreringDeltaker(godkjentPaVegneGrunn.isArenaMigreringDeltaker());
        });

        hendelse.setMeldingId(beregnNokkel(hendelse));
        hendelse.setTidspunkt(Now.instant());

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

    static String beregnNokkel(AvroTiltakHendelse hendelse) {
        AvroTiltakHendelse kopi = AvroTiltakHendelse.newBuilder(hendelse)
            .setMeldingId("")
            .setTidspunkt(Instant.EPOCH)
            .build();
        try {
            ByteBuffer buffer = kopi.toByteBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke serialisere Avro-melding for nøkkelberegning", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 ikke tilgjengelig", e);
        }
    }
}
