package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageableAvtalelisteResponse<T>(
    List<T> avtaler,
    int size,
    int currentPage,
    long totalItems,
    int totalPages,
    AvtaleQueryParameter sokeParametere,
    String sorteringskolonne,
    String sorteringOrder,
    String sokId
) {
    public static <T> PageableAvtalelisteResponse<T> fra(
        Page<T> avtaler,
        AvtaleQueryParameter sokeParametere,
        String sorteringskolonne,
        String sorteringOrder,
        String sokId
    ) {
        return new PageableAvtalelisteResponse<>(
            avtaler.getContent(),
            avtaler.getSize(),
            avtaler.getNumber(),
            avtaler.getTotalElements(),
            avtaler.getTotalPages(),
            sokeParametere,
            sorteringskolonne,
            sorteringOrder,
            sokId
        );
    }

    public static <T> PageableAvtalelisteResponse<T> fra(Page<T> avtaler) {
        return fra(avtaler, null, null, null, null);
    }

    public static <T> PageableAvtalelisteResponse<T> tom() {
        return new PageableAvtalelisteResponse<T>(List.of(), 0, 0, 0, 0, new AvtaleQueryParameter(), "sistEndret", "DESC", "");
    }
}
