package no.nav.tag.tiltaksgjennomforing.varsel.altinnvarsel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType;
import no.altinn.schemas.services.serviceengine.notification._2009._10.*;
import no.altinn.schemas.services.serviceengine.standalonenotificationbe._2009._10.StandaloneNotificationBEList;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasic;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselService;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

@Service
@Slf4j
@RequiredArgsConstructor
public class AltinnVarselService implements VarselService {
    private static final String NAMESPACE = "http://schemas.altinn.no/services/ServiceEngine/Notification/2009/10";

    private final INotificationAgencyExternalBasic iNotificationAgencyExternalBasic;
    private final AltinnVarselProperties varselProperties;

    private static JAXBElement<String> ns(String localpart, String value) {
        return new JAXBElement<>(new QName(NAMESPACE, localpart), String.class, value);
    }

    private static <T> JAXBElement<T> ns(String localpart, Class<T> clazz, T value) {
        return new JAXBElement<>(new QName(NAMESPACE, localpart), clazz, value);
    }

    private static JAXBElement<Boolean> ns(String localpart, Boolean value) {
        return new JAXBElement<>(new QName(NAMESPACE, localpart), Boolean.class, value);
    }

    public void sendVarsel(Identifikator avgiver, String telefonnummer, String varseltekst) {
        StandaloneNotificationBEList standaloneNotification = new StandaloneNotificationBEList().withStandaloneNotification(new StandaloneNotification()
                .withIsReservable(ns("IsReservable", false))
                .withLanguageID(1044)
                .withNotificationType(ns("NotificationType", "VarselDPVUtenRevarsel"))
                .withReceiverEndPoints(ns("ReceiverEndPoints", ReceiverEndPointBEList.class, new ReceiverEndPointBEList()
                        .withReceiverEndPoint(new ReceiverEndPoint()
                                .withTransportType(ns("TransportType", TransportType.class, TransportType.SMS))
                                .withReceiverAddress(ns("ReceiverAddress", telefonnummer)))))
                .withReporteeNumber(ns("ReporteeNumber", avgiver.asString()))
                .withTextTokens(ns("TextTokens", TextTokenSubstitutionBEList.class, new TextTokenSubstitutionBEList()
                        .withTextToken(new TextToken()
                                .withTokenValue(ns("TokenValue", varseltekst)))))
                .withUseServiceOwnerShortNameAsSenderOfSms(ns("UseServiceOwnerShortNameAsSenderOfSms", true)));

        try {
            iNotificationAgencyExternalBasic.sendStandaloneNotificationBasicV3(
                    varselProperties.getSystemBruker(),
                    varselProperties.getSystemPassord(),
                    standaloneNotification);
        } catch (INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage | RuntimeException e) {
            log.error("Feil ved varsling gjennom Altinn", e);
            throw new TiltaksgjennomforingException("Feil ved varsling gjennom Altinn");
        }
    }
}
