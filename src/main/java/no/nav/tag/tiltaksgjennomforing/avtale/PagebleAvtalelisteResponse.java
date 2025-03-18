package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagebleAvtalelisteResponse(
    List<BegrensetAvtale> avtaler,
    int size,
    int currentPage,
    long totalItems,
    int totalPages,
    AvtaleQueryParameter sokeParametere,
    String sorteringskolonne,
    String sorteringOrder,
    String sokId
) {
    public static PagebleAvtalelisteResponse fra(
        Page<BegrensetAvtale> avtaler,
        AvtaleQueryParameter sokeParametere,
        String sorteringskolonne,
        String sorteringOrder,
        String sokId
    ) {
        return new PagebleAvtalelisteResponse(
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

    public static PagebleAvtalelisteResponse fra(Page<BegrensetAvtale> avtaler) {
        return fra(avtaler, null, null, null, null);
    }

    public static PagebleAvtalelisteResponse tom() {
        return new PagebleAvtalelisteResponse(List.of(), 0, 0, 0, 0, new AvtaleQueryParameter(), "sistEndret", "DESC", "");
    }
}
