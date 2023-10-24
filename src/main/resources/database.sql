CREATE TABLE IF NOT EXISTS players
(
    uuid CHAR(36) NOT NULL,
    chat BOOLEAN NOT NULL,
    messages BOOLEAN NOT NULL,
    suffix VARCHAR(15) NOT NULL,
    punishments VARCHAR(200),
    lang VARCHAR(4) NOT NULL,
    PRIMARY KEY (uuid)
);