package no.nav.tag.tiltaksgjennomforing.avtale.regelmotor

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold
import no.nav.tag.tiltaksgjennomforing.avtale.regelmotor.mentor.MentorRegel
import org.springframework.stereotype.Component

@Component
class Regelmotor() {
    //TODO: Legg til flere tiltakstyper her etterhvert som de blir implementert
    fun hent(avtale: Avtale): Tiltaksmotor = MentorRegel(avtale).motor
}

// --- Regelmotor ---
class Tiltaksregel(
    val beskrivelse: String,
    val betingelse: () -> Boolean,
    val innhold: () -> AvtaleInnhold
)

class Tiltaksmotor(val avtale: Avtale) {
    private val regler = mutableListOf<Tiltaksregel>()

    fun regel(
        beskrivelse: String,
        betingelse: () -> Boolean,
        innhold: () -> AvtaleInnhold
    ) {
        regler += Tiltaksregel(beskrivelse, betingelse, innhold)
    }

    fun evaluer() {
        regler.filter { it.betingelse() }
    }
}

// --- DSL inngangspunkt ---
fun tiltak(avtale: Avtale, init: Tiltaksmotor.(Avtale) -> Unit): Tiltaksmotor =
    Tiltaksmotor(avtale).apply { init(avtale) }
