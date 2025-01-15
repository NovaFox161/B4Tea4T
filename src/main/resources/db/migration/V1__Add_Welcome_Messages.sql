CREATE TABLE IF NOT EXISTS welcome_messages
(
    guild_id BIGINT NOT NULL,
    enabled BOOL NOT NULL DEFAULT false,
    channel_id BIGINT NOT NULL,
    message_content TEXT NULL DEFAULT NULL,
    embed_description TEXT NULL DEFAULT NULL
);
