ALTER TABLE avtale
    ADD COLUMN gjeldende_tilskuddsperiode_id uuid
    CONSTRAINT fk_gjeldende_tilskuddsperiode REFERENCES tilskudd_periode (id);
