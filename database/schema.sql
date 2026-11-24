CREATE SEQUENCE seq_player_id
START WITH 1
INCREMENT BY 1
NOCACHE;

CREATE TABLE Players (
    player_id NUMBER(10) DEFAULT seq_player_id.NEXTVAL PRIMARY KEY,
    username VARCHAR2(8 CHAR) UNIQUE NOT NULL,
    wins NUMBER(38) DEFAULT 0,
    losses NUMBER(38) DEFAULT 0,
    highest_score NUMBER(38) DEFAULT 0
);

CREATE SEQUENCE seq_match_id
START WITH 1
INCREMENT BY 1
NOCACHE;

CREATE TABLE Matches (
    match_id NUMBER(10) DEFAULT seq_match_id.NEXTVAL PRIMARY KEY,
    player_1_id NUMBER(10) NOT NULL,
    player_2_id NUMBER(10) NOT NULL,
    winner_id NUMBER(10) DEFAULT NULL,
    CONSTRAINT fk_matches_player1 FOREIGN KEY (player_1_id)
        REFERENCES players(player_id) ON DELETE CASCADE,
    CONSTRAINT fk_matches_player2 FOREIGN KEY (player_2_id)
        REFERENCES players(player_id) ON DELETE CASCADE,
    CONSTRAINT fk_matches_winner FOREIGN KEY (winner_id)
        REFERENCES players(player_id) ON DELETE SET NULL,
    CONSTRAINT chk_different_players CHECK (player_1_id <> player_2_id)
);

CREATE SEQUENCE seq_round_id
START WITH 1
INCREMENT BY 1
NOCACHE;

CREATE TABLE Rounds (
    round_id NUMBER(10) DEFAULT seq_round_id.NEXTVAL PRIMARY KEY,
    match_id NUMBER(10) NOT NULL,
    round_number NUMBER(10) NOT NULL,
    player_1_score NUMBER(10) DEFAULT 0,
    player_2_score NUMBER(10) DEFAULT 0,
    CONSTRAINT fk_rounds_match FOREIGN KEY (match_id)
        REFERENCES matches(match_id) ON DELETE CASCADE
);
