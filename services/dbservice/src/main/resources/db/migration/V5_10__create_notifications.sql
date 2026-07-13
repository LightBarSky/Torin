CREATE TABLE IF NOT EXISTS systems.notifications (
    id bigint PRIMARY KEY,
    "timestamp" timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    type character varying(20),
    message text,
    read boolean DEFAULT false
);