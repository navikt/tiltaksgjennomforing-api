UPDATE avtale_innhold SET mentor_valgt_lonnstype = 'ÅRSLØNN' WHERE mentor_valgt_lonnstype = 0;
UPDATE avtale_innhold SET mentor_valgt_lonnstype = 'MÅNEDSLØNN' WHERE mentor_valgt_lonnstype = 1;
UPDATE avtale_innhold SET mentor_valgt_lonnstype = 'UKELØNN' WHERE mentor_valgt_lonnstype = 2;
UPDATE avtale_innhold SET mentor_valgt_lonnstype = 'DAGSLØNN' WHERE mentor_valgt_lonnstype = 3;
UPDATE avtale_innhold SET mentor_valgt_lonnstype = 'TIMELØNN' WHERE mentor_valgt_lonnstype = 4;
