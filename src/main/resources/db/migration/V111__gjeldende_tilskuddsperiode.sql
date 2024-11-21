ALTER TABLE avtale
    ADD COLUMN gjeldende_tilskuddsperiode uuid
    CONSTRAINT fk_gjeldende_tilskuddsperiode REFERENCES tilskudd_periode (id);
