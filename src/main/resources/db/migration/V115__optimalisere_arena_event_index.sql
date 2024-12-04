DROP INDEX IF EXISTS arena_events_arena_op_time_idx;
DROP INDEX IF EXISTS arena_events_arena_retry_count_idx;

CREATE INDEX arena_event_status_idx ON arena_event(status);
CREATE INDEX arena_event_arena_table_status_idx ON arena_event(arena_table, status);
