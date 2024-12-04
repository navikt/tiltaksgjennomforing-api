DROP INDEX IF EXISTS arena_events_arena_op_time_idx;
DROP INDEX IF EXISTS arena_events_arena_retry_count_idx;

CREATE INDEX IF NOT EXISTS arena_event_status_idx ON arena_event(status);
CREATE INDEX IF NOT EXISTS arena_event_status_arena_table_idx ON arena_event(status, arena_table);
