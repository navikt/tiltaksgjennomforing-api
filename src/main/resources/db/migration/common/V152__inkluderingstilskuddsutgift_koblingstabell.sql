CREATE TABLE avtale_innhold_inkluderingstilskuddsutgift
(
    avtale_innhold_id uuid NOT NULL REFERENCES avtale_innhold (id) ON DELETE CASCADE,
    inkluderingstilskuddsutgift_id uuid NOT NULL REFERENCES inkluderingstilskuddsutgift (id) ON DELETE CASCADE,
    PRIMARY KEY (avtale_innhold_id, inkluderingstilskuddsutgift_id)
);

CREATE INDEX idx_innhold_utgift_utgift_id
    ON avtale_innhold_inkluderingstilskuddsutgift (inkluderingstilskuddsutgift_id);

INSERT INTO avtale_innhold_inkluderingstilskuddsutgift (avtale_innhold_id, inkluderingstilskuddsutgift_id)
SELECT avtale_innhold_id, id
FROM inkluderingstilskuddsutgift
WHERE avtale_innhold_id IS NOT NULL;
