-- tables
-- Table: movie
CREATE TABLE movie
(
    movie_id          varchar(10)  NOT NULL,
    title             varchar(256) NOT NULL,
    release_date      date         NOT NULL,
    duration          interval     NOT NULL,
    vote_average      real         NOT NULL CHECK (vote_average >= 0 AND vote_average <= 10),
    vote_count        int          NOT NULL,
    plot              varchar(512) NOT NULL,
    is_adult          boolean      NOT NULL,
    popularity        int          NOT NULL,
    price             money        NOT NULL,
    primary_image_url varchar(512) NULL,

    CONSTRAINT Movie_pk PRIMARY KEY (movie_id)
);

-- Table: genre
CREATE TABLE genre
(
    genre_id serial,
    name     varchar(50) NOT NULL UNIQUE,
    CONSTRAINT Genre_pk PRIMARY KEY (genre_id)
);

-- Table: movie_genre
CREATE TABLE movie_genre
(
    movie_id varchar(10),
    genre_id int,
    PRIMARY KEY (movie_id, genre_id)
);

-- Table: favorite
CREATE TABLE favorite
(
    user_id    int,
    movie_id   varchar(10) NULL,
    added_date date        NOT NULL,
    CONSTRAINT Favorite_pk PRIMARY KEY (user_id, movie_id)
);

-- Table: history
CREATE TABLE history
(
    user_id          int,
    movie_id         varchar(10) NULL,
    watched_date     timestamp,
    watched_duration interval    NOT NULL,
    CONSTRAINT History_pk PRIMARY KEY (user_id, movie_id, watched_date)
);

-- Table: payment_method
CREATE TABLE payment_method
(
    payment_method_id int,
    name              varchar(50)  NOT NULL,
    description       varchar(256) NOT NULL,
    is_active         boolean      NOT NULL,
    CONSTRAINT payment_method_pk PRIMARY KEY (payment_method_id)
);

-- Table: review
CREATE TABLE review
(
    user_id  int          NULL,
    movie_id varchar(10),
    vote     int          NOT NULL,
    comment  varchar(256) NOT NULL,
    likes    int          NOT NULL,
    dislikes int          NOT NULL,
    CONSTRAINT review_pk PRIMARY KEY (user_id, movie_id)
);

-- Table: transaction
CREATE TABLE "transaction"
(
    transaction_id serial,
    user_id        int         NOT NULL,
    movie_id       varchar(10) NOT NULL,
    purchase_date  timestamp   NOT NULL,
    payment_method int         NULL,
    CONSTRAINT Transaction_pk PRIMARY KEY (transaction_id)
);

-- Table: user
CREATE TABLE "user"
(
    user_id      serial,
    username     varchar(30)  NOT NULL UNIQUE CHECK (username <> ''),
    first_name   varchar(50)  NOT NULL CHECK (first_name <> ''),
    last_name    varchar(50)  NOT NULL CHECK (last_name <> ''),
    email        varchar(256) NOT NULL UNIQUE CHECK (email ~* '^[A-Za-z0-9._+%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'),
    phone_number varchar(20)  NOT NULL UNIQUE CHECK (phone_number ~ '^[0-9]{10,20}$'),
    birth_date   date         NOT NULL,
    CONSTRAINT User_pk PRIMARY KEY (user_id)
);

-- foreign keys

-- Reference: Movie_Genre_Movie (table: movie_genre)
ALTER TABLE movie_genre
    ADD CONSTRAINT Movie_Genre_Movie
        FOREIGN KEY (movie_id)
            REFERENCES movie (movie_id)
            ON DELETE CASCADE
;

-- Reference: Movie_Genre_Movie (table: movie_genre)
ALTER TABLE movie_genre
    ADD CONSTRAINT Movie_Genre_Genre
        FOREIGN KEY (genre_id)
            REFERENCES genre (genre_id)
            ON DELETE CASCADE
;

-- Reference: Favorite_Movie (table: favorite)
ALTER TABLE favorite
    ADD CONSTRAINT Favorite_Movie
        FOREIGN KEY (movie_id)
            REFERENCES movie (movie_id)
            ON DELETE SET NULL;
;

-- Reference: Favorite_User (table: favorite)
ALTER TABLE favorite
    ADD CONSTRAINT Favorite_User
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE CASCADE;
;

-- Reference: History_Movie (table: history)
ALTER TABLE history
    ADD CONSTRAINT History_Movie
        FOREIGN KEY (movie_id)
            REFERENCES movie (movie_id)
            ON DELETE SET NULL
;

-- Reference: History_User (table: history)
ALTER TABLE history
    ADD CONSTRAINT History_User
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE CASCADE;
;

-- Reference: Review_Movie (table: review)
ALTER TABLE review
    ADD CONSTRAINT Review_Movie
        FOREIGN KEY (movie_id)
            REFERENCES movie (movie_id)
            ON DELETE CASCADE;
;

-- Reference: Review_User (table: review)
ALTER TABLE review
    ADD CONSTRAINT Review_User
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE SET NULL;
;

-- Reference: Transaction_Movie (table: transaction)
ALTER TABLE "transaction"
    ADD CONSTRAINT Transaction_Movie
        FOREIGN KEY (movie_id)
            REFERENCES movie (movie_id)
            ON DELETE SET NULL;
;

-- Reference: Transaction_PaymentMethod (table: transaction)
ALTER TABLE "transaction"
    ADD CONSTRAINT Transaction_PaymentMethod
        FOREIGN KEY (payment_method)
            REFERENCES payment_method (payment_method_id)
            ON DELETE SET NULL;
;

-- Reference: Transaction_User (table: transaction)
ALTER TABLE "transaction"
    ADD CONSTRAINT Transaction_User
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE SET NULL;
;

-- Triggers:
CREATE OR REPLACE FUNCTION check_watched_duration()
    RETURNS TRIGGER AS
$$
BEGIN
    IF new.watched_duration > (SELECT duration FROM movie WHERE movie_id = new.movie_id) THEN
        RAISE EXCEPTION 'Watched duration cannot be greater than the movie duration';

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_watched_duration_trigger
    BEFORE INSERT OR UPDATE
    ON history
    FOR EACH ROW
EXECUTE FUNCTION check_watched_duration();

-- -- Views:
-- CREATE OR REPLACE VIEW movie_search AS
-- WITH favorite_movies AS (SELECT u.user_id,
--                                 ARRAY(SELECT m.title
--                                       FROM history h
--                                                JOIN movie m ON h.movie_id = m.movie_id
--                                       WHERE u.user_id = h.user_id
--                                       GROUP BY m.title
--                                       ORDER BY COUNT(*) DESC
--                                       LIMIT 3) AS favorite_movies
--                          FROM "user" u
--                          GROUP BY u.user_id),
--      least_watched_genres AS (SELECT u.user_id,
--                                      ARRAY(SELECT g.name
--                                            FROM genre g
--                                                     LEFT JOIN movie m ON g.genre_id = m.genre_id
--                                            WHERE NOT EXISTS (SELECT 1
--                                                              FROM history h
--                                                              WHERE u.user_id = h.user_id
--                                                                AND h.movie_id = m.movie_id)
--                                            LIMIT 2) AS least_watched_genres
--                               FROM "user" u
--                               GROUP BY u.user_id);
