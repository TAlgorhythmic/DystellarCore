CREATE TABLE IF NOT EXISTS players_core
(
    uuid CHAR(36) NOT NULL,
    chat BOOLEAN NOT NULL,
    messages INTEGER NOT NULL DEFAULT 1,
    suffix VARCHAR(15) NOT NULL,
    punishments VARCHAR(1000),
    notes VARCHAR(1000),
    lang VARCHAR(4) NOT NULL,
    inbox VARCHAR(3000),
    version INTEGER NOT NULL DEFAULT 0,
    tabcompletion BOOLEAN NOT NULL DEFAULT FALSE,
    scoreboard BOOLEAN NOT NULL DEFAULT TRUE,
    ignoreList VARCHAR(4000) NOT NULL,
    PRIMARY KEY (uuid)
);
CREATE TABLE IF NOT EXISTS mappings
(
    something0 VARCHAR(40) NOT NULL,
    something1 VARCHAR(40) NOT NULL,
    something2 VARCHAR(40) NOT NULL,
    punishments VARCHAR(1000),
    PRIMARY KEY (something0)
);
CREATE TABLE IF NOT EXISTS senders
(
    id INTEGER NOT NULL,
    serialized VARCHAR(4000) NOT NULL,
    PRIMARY KEY (id)
);