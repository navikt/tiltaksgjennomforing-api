ALTER TABLE inkluderingstilskuddsutgift
    ADD COLUMN tilskuddperiode_id uuid
    CONSTRAINT fk_inkluderingstilskuddsutgift_tilskuddperiode
    REFERENCES tilskudd_periode (id)
    ON DELETE SET NULL;

CREATE INDEX inkluderingstilskuddsutgift_tilskuddperiode_id
    ON inkluderingstilskuddsutgift (tilskuddperiode_id);
