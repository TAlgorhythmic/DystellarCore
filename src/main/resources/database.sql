CREATE TABLE IF NOT EXISTS players
(
    uuid CHAR(36) NOT NULL,
    chat BOOLEAN NOT NULL,
    messages BOOLEAN NOT NULL,
    suffix VARCHAR(15) NOT NULL,
    punishments VARCHAR(10000),
    notes VARCHAR(10000),
    lang VARCHAR(4) NOT NULL,
    inbox VARCHAR(3000),
    PRIMARY KEY (uuid)
);
CREATE TABLE IF NOT EXISTS mappings
(
    something0 VARCHAR(40) NOT NULL,
    something1 VARCHAR(40) NOT NULL,
    something2 VARCHAR(40) NOT NULL,
    punishments VARCHAR(10000),
    PRIMARY KEY (something0)
);