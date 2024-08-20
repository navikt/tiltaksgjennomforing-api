package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OpprettMentorAvtale extends OpprettAvtale {
    private Fnr mentorFnr;
    private Avtalerolle avtalerolle;

    public OpprettMentorAvtale(Fnr deltakerFnr, Fnr mentorFnr, BedriftNr bedriftNr, Tiltakstype tiltakstype, Avtalerolle avtalerolle) {
        super(deltakerFnr, bedriftNr, tiltakstype);
        this.mentorFnr = mentorFnr;
        this.avtalerolle = avtalerolle;
    }
}
