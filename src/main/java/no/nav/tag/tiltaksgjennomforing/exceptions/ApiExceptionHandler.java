package no.nav.tag.tiltaksgjennomforing.exceptions;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String FEILKODE = "feilkode";

    @ExceptionHandler({ FeilkodeException.class })
    public ResponseEntity<Object> feilkodeException(FeilkodeException e) {
        log.info("Feilkode inntruffet: {}", e.getFeilkode());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(FEILKODE, e.getFeilkode().name())
                .build();
    }
    
    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<Object> constraintException(ConstraintViolationException e) {
        log.info("Validering failet: {}", e.getMessage());
        
        Map<String, String> errors = e.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        ConstraintViolation::getMessageTemplate,
                        (violation) -> violation.getInvalidValue().toString()
                ));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errors);
    }
}
