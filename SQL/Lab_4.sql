-- Отримати (в одному запиті) список фільмів зі вказанням рейтингу по:
-- - кількості доданих в "улюблене"
-- - значенню vote_average
-- - середньому часу перегляду
WITH favorite_counts AS (SELECT f.movie_id,
                                COUNT(*) AS favorites_count
                         FROM favorite f
                         GROUP BY f.movie_id),
     average_duration AS (SELECT h.movie_id,
                                 AVG(EXTRACT(EPOCH FROM h.watched_duration)) AS avg_duration
                          FROM history h
                          GROUP BY h.movie_id)

SELECT m.movie_id,
       m.title,
       COALESCE(fc.favorites_count, 0) AS favorites_count,
       m.vote_average,
       COALESCE(ad.avg_duration, 0)    AS avg_duration_seconds
FROM movie m
         LEFT JOIN favorite_counts fc ON m.movie_id = fc.movie_id
         LEFT JOIN average_duration ad ON m.movie_id = ad.movie_id
ORDER BY favorites_count DESC, m.vote_average DESC, avg_duration_seconds DESC;


-- Зробити представлення про користувачів.
-- - Додати інформацію про 3 найулюбленіших фільми (за кількістю переглядів)
-- - (Додайте колонку genre_id до фільма і винесіть ці жанри в окрему таблицю) Додати інформацію про 2 жанри фільмів, які користувач дивився найменше (або взагалі не дивився)
-- - Додайте інформацію про період, коли виходили фільми, які він дивився більше 5 разів
CREATE VIEW user_info AS
WITH favorite_movies AS (SELECT u.user_id,
                                ARRAY(SELECT m.title
                                      FROM history h
                                               JOIN movie m ON h.movie_id = m.movie_id
                                      WHERE u.user_id = h.user_id
                                      GROUP BY m.title
                                      ORDER BY COUNT(*) DESC
                                      LIMIT 3) AS favorite_movies
                         FROM "user" u
                         GROUP BY u.user_id),
     least_watched_genres AS (SELECT u.user_id,
                                     ARRAY(SELECT g.name
                                           FROM genre g
                                                    LEFT JOIN movie m ON g.genre_id = m.genre_id
                                           WHERE NOT EXISTS (SELECT 1
                                                             FROM history h
                                                             WHERE u.user_id = h.user_id
                                                               AND h.movie_id = m.movie_id)
                                           LIMIT 2) AS least_watched_genres
                              FROM "user" u
                              GROUP BY u.user_id),
     period_more_than_5_views AS (SELECT u.user_id,
                                         ARRAY(SELECT EXTRACT(YEAR FROM m.release_date)
                                               FROM history h
                                                        JOIN movie m ON h.movie_id = m.movie_id
                                               WHERE u.user_id = h.user_id
                                               GROUP BY m.title, EXTRACT(YEAR FROM m.release_date)
                                               HAVING COUNT(*) > 5
                                               ORDER BY COUNT(*) DESC) AS period_more_than_5_views
                                  FROM "user" u
                                  GROUP BY u.user_id)
SELECT u.user_id,
       u.username,
       fm.favorite_movies,
       lwg.least_watched_genres,
       pmv.period_more_than_5_views
FROM "user" u
         LEFT JOIN favorite_movies fm ON u.user_id = fm.user_id
         LEFT JOIN least_watched_genres lwg ON u.user_id = lwg.user_id
         LEFT JOIN period_more_than_5_views pmv ON u.user_id = pmv.user_id
ORDER BY u.user_id;


-- Отримати фільми, які не були збережені до "улюблене", але їх середній час перегляду більший за 70%
WITH non_favorite_movies AS (SELECT DISTINCT h.movie_id
                             FROM history h
                                      LEFT JOIN favorite f ON h.user_id = f.user_id AND h.movie_id = f.movie_id
                             WHERE f.movie_id IS NULL),
     average_duration AS (SELECT h.movie_id,
                                 AVG(EXTRACT(EPOCH FROM h.watched_duration)) AS avg_duration_seconds
                          FROM history h
                          GROUP BY h.movie_id)

SELECT m.movie_id,
       m.title,
       COALESCE(ad.avg_duration_seconds, 0) AS avg_duration_seconds
FROM movie m
         JOIN non_favorite_movies nfm ON m.movie_id = nfm.movie_id
         LEFT JOIN average_duration ad ON m.movie_id = ad.movie_id
WHERE COALESCE(ad.avg_duration_seconds, 0) > (EXTRACT(EPOCH FROM m.duration) * 0.7);

