-- Створити процедуру, яка змінить popularity у фільма відповідно до формули
-- [movie.popularity += (- якщо watched_duration < 50%, + навпаки) history.watched_duration / movie.duration]
CREATE OR REPLACE PROCEDURE update_movie_popularity(p_user_id INT, p_movie_id INT)
AS
$$
DECLARE
    movie_record   RECORD;
    new_popularity REAL;
    watched_ratio  REAL;
BEGIN
    FOR movie_record IN (SELECT m.popularity,
                                EXTRACT(EPOCH FROM h.watched_duration) AS watched_duration,
                                EXTRACT(EPOCH FROM m.duration)         AS duration,
                                h.watched_date
                         FROM history h
                                  JOIN movie m ON h.movie_id = m.movie_id
                         WHERE h.user_id = p_user_id
                           AND m.movie_id = p_movie_id
                         ORDER BY h.watched_date DESC
                         LIMIT 1)
        LOOP
            IF movie_record.duration > 0 THEN
                watched_ratio := movie_record.watched_duration / movie_record.duration;
                CASE
                    WHEN (watched_ratio < 0.5)
                        THEN new_popularity := movie_record.popularity - watched_ratio;
                    ELSE new_popularity := movie_record.popularity + watched_ratio;
                    END CASE;
            ELSE
                new_popularity := movie_record.popularity;
            END IF;

            UPDATE movie
            SET popularity = new_popularity
            WHERE movie_id = p_movie_id;
        END LOOP;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE update_popularity_for_all_records()
AS
$$
DECLARE
    viewing RECORD;
BEGIN
    FOR viewing IN (SELECT movie_id, user_id FROM history ORDER BY movie_id)
        LOOP
            CALL update_movie_popularity(viewing.user_id, viewing.movie_id);
        END LOOP;
END
$$ LANGUAGE plpgsql;

CALL update_popularity_for_all_records();



-- Створити тригер, який додасть фільм до "улюблене" користувачу, який переглянув фільм більше 7 разів із середньою тривалістю перегляду > 90%
-- Створення тригер-функції
CREATE OR REPLACE FUNCTION add_to_favorite_trigger() RETURNS TRIGGER
AS
$$
DECLARE
    total_views      INTEGER;
    average_duration REAL;
BEGIN
    SELECT COUNT(*),
           AVG(EXTRACT(EPOCH FROM h.watched_duration) / EXTRACT(EPOCH FROM m.duration))
    INTO total_views,
        average_duration
    FROM history h
             JOIN public.movie m on m.movie_id = h.movie_id
    WHERE h.user_id = NEW.user_id
      AND h.movie_id = NEW.movie_id;

    IF total_views > 7 AND average_duration > 0.9 THEN
        INSERT INTO favorite (user_id, movie_id, added_date)
        VALUES (NEW.user_id, NEW.movie_id, NEW.watched_date)
        ON CONFLICT (user_id, movie_id) DO UPDATE
            SET added_date = NEW.watched_date;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER add_to_favorite_trigger
    AFTER INSERT
    ON history
    FOR EACH ROW
EXECUTE FUNCTION add_to_favorite_trigger();


-- Створити тригер, який заборонить переглянути фільм 18+ користувачу, вік якого менше за 18 років.
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

    SELECT m.adult
    INTO is_movie_adult
    FROM movie m
    WHERE m.movie_id = NEW.movie_id;

    RAISE NOTICE 'User age: %', user_age;
    RAISE NOTICE 'is_movie_adult: %', is_movie_adult;

    IF is_movie_adult AND user_age < 18 THEN
        RAISE EXCEPTION 'Users under the age of 18 are prohibited from viewing 18+ movies.';
    ELSE
--         INSERT INTO history (user_id, movie_id, watched_date, watched_duration)
--         VALUES (NEW.user_id, NEW.movie_id, NEW.watched_date, NEW.watched_duration);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_age_limit_trigger
    BEFORE INSERT
    ON history
    FOR EACH ROW
EXECUTE FUNCTION check_age_limit();
