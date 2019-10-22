package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.ArrayList;
import java.util.Collections;

public class AvtaleSorter {
    ArrayList<Avtale> avtaleSorter= new ArrayList<>();
    public AvtaleSorter(ArrayList<Avtale> avtaleSorter){
        this.avtaleSorter=avtaleSorter;
    }
    public ArrayList<Avtale> getSortedAvtalerByOpprettetTidspunkt(){
        Collections.sort(avtaleSorter);
        return avtaleSorter;
    }
}
