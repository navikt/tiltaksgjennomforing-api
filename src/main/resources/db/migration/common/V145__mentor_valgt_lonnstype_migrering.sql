UPDATE avtale_innhold
SET mentor_valgt_lonnstype = CASE mentor_valgt_lonnstype
    WHEN '0' THEN 'ÅRSLØNN'
    WHEN '1' THEN 'MÅNEDSLØNN'
    WHEN '2' THEN 'UKELØNN'
    WHEN '3' THEN 'DAGSLØNN'
    WHEN '4' THEN 'TIMELØNN'
END
WHERE mentor_valgt_lonnstype IN ('0', '1', '2', '3', '4');
