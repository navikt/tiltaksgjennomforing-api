alter table avtale_innhold alter column godkjent_av_deltaker type timestamp with time zone
    using godkjent_av_deltaker at time zone 'Europe/Oslo';

alter table avtale_innhold alter column godkjent_av_arbeidsgiver type timestamp with time zone
    using godkjent_av_arbeidsgiver at time zone 'Europe/Oslo';

alter table avtale_innhold alter column godkjent_av_veileder type timestamp with time zone
    using godkjent_av_veileder at time zone 'Europe/Oslo';

alter table avtale_innhold alter column godkjent_av_beslutter type timestamp with time zone
    using godkjent_av_beslutter at time zone 'Europe/Oslo';

alter table avtale_innhold alter column godkjent_taushetserklæring_av_mentor type timestamp with time zone
    using godkjent_taushetserklæring_av_mentor at time zone 'Europe/Oslo';

alter table avtale_innhold alter column avtale_inngått type timestamp with time zone
    using avtale_inngått at time zone 'Europe/Oslo';

alter table avtale_innhold alter column ikrafttredelsestidspunkt type timestamp with time zone
    using ikrafttredelsestidspunkt at time zone 'Europe/Oslo';
