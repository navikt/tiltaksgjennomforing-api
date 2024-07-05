ALTER TABLE arena_event DROP COLUMN status;

DROP TYPE IF EXISTS arena_status;

ALTER TABLE arena_event ADD COLUMN status varchar not null;
