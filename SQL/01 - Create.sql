-- ALTER DATABASE "CinemaHub-DEV"
--     SET lc_monetary TO 'en_US';

-- tables
-- Table: movie
CREATE TABLE movie
(
    movie_id          varchar(10)  NOT NULL,
    title             varchar(256) NOT NULL,
    release_date      date         NOT NULL,
    duration          interval     NOT NULL CHECK (duration > '0 seconds'),
    vote_average      real         NOT NULL CHECK (vote_average >= 0 AND vote_average <= 10) DEFAULT 0,
    vote_count        int          NOT NULL CHECK (vote_count >= 0)                          DEFAULT 0,
    plot              varchar(512) NOT NULL,
    is_adult          boolean      NOT NULL,
    popularity        int          NOT NULL CHECK (popularity >= 0)                          DEFAULT 0,
    price             money        NOT NULL CHECK (price >= '0.00')                          DEFAULT 0,
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
    user_id        int         NULL,
    movie_id       varchar(10) NULL,
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
            ON DELETE CASCADE;

-- Reference: Movie_Genre_Movie (table: movie_genre)
ALTER TABLE movie_genre
    ADD CONSTRAINT Movie_Genre_Genre
        FOREIGN KEY (genre_id)
            REFERENCES genre (genre_id)
            ON DELETE CASCADE;

-- Reference: Favorite_Movie (table: favorite)
ALTER TABLE favorite
    ADD CONSTRAINT Favorite_Movie
        FOREIGN KEY (movie_id)
            REFERENCES movie (movie_id)
            ON DELETE SET NULL;

-- Reference: Favorite_User (table: favorite)
ALTER TABLE favorite
    ADD CONSTRAINT Favorite_User
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE CASCADE;

-- Reference: History_Movie (table: history)
ALTER TABLE history
    ADD CONSTRAINT History_Movie
        FOREIGN KEY (movie_id)
            REFERENCES movie (movie_id)
            ON DELETE SET NULL;

-- Reference: History_User (table: history)
ALTER TABLE history
    ADD CONSTRAINT History_User
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE CASCADE;

-- Reference: Review_Movie (table: review)
ALTER TABLE review
    ADD CONSTRAINT Review_Movie
        FOREIGN KEY (movie_id)
            REFERENCES movie (movie_id)
            ON DELETE CASCADE;

-- Reference: Review_User (table: review)
ALTER TABLE review
    ADD CONSTRAINT Review_User
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE SET NULL;

-- Reference: Transaction_Movie (table: transaction)
ALTER TABLE "transaction"
    ADD CONSTRAINT Transaction_Movie
        FOREIGN KEY (movie_id)
            REFERENCES movie (movie_id)
            ON DELETE SET NULL;

-- Reference: Transaction_PaymentMethod (table: transaction)
ALTER TABLE "transaction"
    ADD CONSTRAINT Transaction_PaymentMethod
        FOREIGN KEY (payment_method)
            REFERENCES payment_method (payment_method_id)
            ON DELETE SET NULL;

-- Reference: Transaction_User (table: transaction)
ALTER TABLE "transaction"
    ADD CONSTRAINT Transaction_User
        FOREIGN KEY (user_id)
            REFERENCES "user" (user_id)
            ON DELETE SET NULL;

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

CREATE OR REPLACE FUNCTION check_age_limit()
    RETURNS TRIGGER AS
$$
DECLARE
    user_age       INT;
    is_movie_adult boolean;
BEGIN
    SELECT EXTRACT(YEAR FROM AGE(u.birth_date))
    INTO user_age
    FROM "user" u
    WHERE u.user_id = NEW.user_id;

    SELECT m.is_adult
    INTO is_movie_adult
    FROM movie m
    WHERE m.movie_id = NEW.movie_id;

    RAISE NOTICE 'User age: %', user_age;
    RAISE NOTICE 'is_movie_adult: %', is_movie_adult;

    IF is_movie_adult AND user_age < 18 THEN
        RAISE EXCEPTION 'Users under the age of 18 are prohibited from viewing 18+ movies.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_age_limit_trigger
    BEFORE INSERT
    ON history
    FOR EACH ROW
EXECUTE FUNCTION check_age_limit();

CREATE OR REPLACE FUNCTION update_movie_popularity()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE movie SET popularity = popularity + 1 WHERE movie_id = new.movie_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_movie_popularity
    AFTER INSERT
    ON history
    FOR EACH ROW
EXECUTE FUNCTION update_movie_popularity();

CREATE OR REPLACE FUNCTION update_movie_vote_average()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE movie
    SET vote_average = (vote_count * vote_average + new.vote) / (vote_count + 1),
        vote_count   = vote_count + 1
    WHERE movie_id = new.movie_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER update_movie_vote_average
    AFTER INSERT
    ON review
    FOR EACH ROW
EXECUTE FUNCTION update_movie_vote_average();
