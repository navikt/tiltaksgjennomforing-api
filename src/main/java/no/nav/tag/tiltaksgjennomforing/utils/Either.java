package no.nav.tag.tiltaksgjennomforing.utils;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Either<T> {
    private final T a;
    private final T b;

    private Either(T a, T b) {
        this.a = a;
        this.b = b;
    }

    public Optional<T> whereFirst(Predicate<T> predicate) {
        return Stream.of(a, b).filter(predicate).findFirst();
    }

    public static <T> Either<T> of(T a, T last) {
        return new Either<>(a, last);
    }

}
