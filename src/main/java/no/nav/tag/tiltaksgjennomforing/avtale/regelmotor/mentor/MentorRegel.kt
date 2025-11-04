package no.nav.tag.tiltaksgjennomforing.avtale.regelmotor.mentor

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold
import no.nav.tag.tiltaksgjennomforing.avtale.Status
import no.nav.tag.tiltaksgjennomforing.avtale.regelmotor.Tiltaksmotor
import no.nav.tag.tiltaksgjennomforing.avtale.regelmotor.tiltak
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException

class MentorRegel(avtale: Avtale) {
    val motor: Tiltaksmotor = tiltak(avtale) {
        regel(
            "Er en Mentor Avtale",
            betingelse = { if(!avtale.getTiltakstype().isMentor){
                throw FeilkodeException(Feilkode.KAN_IKKE_ENDRE_FEIL_TILTAKSTYPE)
            }
                false },
            innhold = {AvtaleInnhold.nyttTomtInnhold(avtale.getTiltakstype())}
        )
        regel(
            "Ikke lov med en annullert avtale",
            betingelse = {
                if(avtale.getStatus().equals(Status.ANNULLERT)){
                    throw FeilkodeException(Feilkode.KAN_IKKE_ENDRE_ANNULLERT_AVTALE)
            }
                         false
                         },
            innhold = {AvtaleInnhold.nyttTomtInnhold(avtale.getTiltakstype())}
        )
        regel(
            "Avtalen skal være godkjent av veileder",
            betingelse = {
                if (avtale.godkjentAvVeileder() == null) {
                    throw FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OM_MENTOR_IKKE_INNGAATT_AVTALE);
                }
                false
            },
            innhold = {AvtaleInnhold.nyttTomtInnhold(avtale.getTiltakstype())}
        )
    }
}
