package no.nav.tag.tiltaksgjennomforing;

import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class TiltaksgjennomforingApplication {
    @Data
    public static class HelloWorld {
        private String hello = "World";
    }

    @RequestMapping("/hello")
    public HelloWorld hello() {
        return new HelloWorld();
    }

    public static void main(String[] args) {
        SpringApplication.run(TiltaksgjennomforingApplication.class, args);
    }
}
