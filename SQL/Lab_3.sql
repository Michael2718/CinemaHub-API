-- 1. Показати 5 фільмів, які не додивилися до середини (у порядку спадання часу перегляду)
SELECT m.title,
       m.overview,
       CONCAT(EXTRACT(HOUR FROM h.watched_duration), 'h ',
              EXTRACT(MINUTE FROM h.watched_duration), 'm') AS watched_duration,
       CONCAT(EXTRACT(HOUR FROM m.duration), 'h ',
              EXTRACT(MINUTE FROM m.duration), 'm')         AS movie_duration
FROM history h
         JOIN movie m ON h.movie_id = m.movie_id
WHERE h.watched_duration < m.duration / 2
ORDER BY h.watched_duration DESC
LIMIT 5;


-- 2. Показати користувачів, які залишили більше 10 ревью на різні фільми і середня кількість лайків на 10 найпопулярніших фільмах перевищує 15

WITH UserReviewCount AS (SELECT user_id,
                                COUNT(DISTINCT movie_id) AS review_count
                         FROM review
                         GROUP BY user_id
                         HAVING COUNT(DISTINCT movie_id) > 10)

SELECT urc.user_id,
       AVG(r.likes) AS average_likes
FROM UserReviewCount urc
         JOIN
     review r ON urc.user_id = r.user_id
WHERE r.movie_id IN (SELECT movie_id
                     FROM movie
                     ORDER BY popularity DESC
                     LIMIT 10)
GROUP BY urc.user_id
HAVING AVG(r.likes) > 15;


-- 3. Показати 3 найпопулярніші фільми (за кількістю доданих в "улюблене") категорії 18+, які вийшли більше 10 років тому
SELECT COUNT(f.user_id) AS favorites_count,
       m.movie_id,
       m.title,
       m.overview
FROM movie m
         LEFT JOIN favorite f ON m.movie_id = f.movie_id
WHERE m.adult = true
  AND m.release_date < CURRENT_DATE - INTERVAL '10 years'
GROUP BY m.movie_id
ORDER BY favorites_count DESC
LIMIT 3;

-- 4. Показати 5 найдорожчих фільмів, які купили більше 10 разів і які були переглянуті більше 90%
WITH PopularMovies AS (SELECT t.movie_id
                       FROM transaction t
                                JOIN history h ON t.movie_id = h.movie_id
                       GROUP BY t.movie_id
                       HAVING AVG((EXTRACT(epoch FROM h.watched_duration) / 60) /
                                  (EXTRACT(epoch FROM h.watched_duration) / 60)) > 0.9
                          AND COUNT(*) > 10)

SELECT m.price,
       m.movie_id,
       m.title,
       m.overview
FROM movie m
         JOIN PopularMovies pm ON m.movie_id = pm.movie_id
ORDER BY m.price DESC;

-- 5. Показати співвідношення між фактом потрапляння фільму в "улюблене" і тим, який середній відсоток часу його було переглянуто
SELECT m.movie_id,
       m.title,
       m.overview,
       COUNT(f.user_id) AS favorites_count,
       ROUND(AVG(EXTRACT(EPOCH FROM h.watched_duration) / EXTRACT(EPOCH FROM m.duration) * 100),
             2)         AS avg_watched_percentage
FROM favorite f
         JOIN history h ON f.user_id = h.user_id AND f.movie_id = h.movie_id
         JOIN movie m ON f.movie_id = m.movie_id
GROUP BY m.movie_id
ORDER BY favorites_count DESC;

-- 6. показати в одному запиті для найактивніших користувачів (по загальному часу переглядання фільмів):
--     - кількість переглядів фільмів за останній місяць
--     - кількіть переглядів фільмів за попередній від останнього місяця місяць

WITH user_duration AS (SELECT h.user_id,
                              SUM(h.watched_duration) AS total_watched
                       FROM history h
                       GROUP BY h.user_id
                       ORDER BY total_watched DESC
                       LIMIT 5),
     last_month_views AS (SELECT h.user_id,
                                 COUNT(*) AS views_last_month
                          FROM history h
                          WHERE h.watched_date >= CURRENT_DATE - INTERVAL '1 month'
                          GROUP BY h.user_id),
     prev_month_views AS (SELECT h.user_id,
                                 COUNT(*) AS views_prev_month
                          FROM history h
                          WHERE h.watched_date >= CURRENT_DATE - INTERVAL '2 months'
                            AND h.watched_date <= CURRENT_DATE - INTERVAL '1 month'
                          GROUP BY h.user_id)

SELECT CONCAT(EXTRACT(HOUR FROM user_duration.total_watched), 'h ',
              EXTRACT(MINUTE FROM user_duration.total_watched), 'm') AS total_watched,
       u.user_id,
       u.username,
       last_month_views.views_last_month,
       prev_month_views.views_prev_month
FROM user_duration
         JOIN "user" u ON user_duration.user_id = u.user_id
         LEFT JOIN last_month_views ON user_duration.user_id = last_month_views.user_id
         LEFT JOIN prev_month_views ON user_duration.user_id = prev_month_views.user_id
ORDER BY user_duration.total_watched DESC;


-- 7. Показати 3 найпопулярніші фільми, які тривають більше ніж 1 годину і в яких в описі вказано, що це "фільм жахів"
SELECT m.vote_average,
       m.movie_id,
       m.title,
       m.overview
FROM movie m
WHERE m.duration > INTERVAL '2h'
  AND m.overview ~* 'horror'
ORDER BY m.popularity DESC
LIMIT 3;

-- 8. Показати співвідношення лайків до дізлайків на всіх ревью для 5 найактивніших користувачів (за кількістю придбаних фільмів)
WITH TopUsers AS (SELECT u.user_id,
                         COUNT(t.transaction_id) AS purchases
                  FROM "user" u
                           JOIN "transaction" t ON u.user_id = t.user_id
                  GROUP BY u.user_id
                  ORDER BY purchases DESC
                  LIMIT 5)

SELECT tu.purchases,
       r.user_id,
       SUM(r.likes)    AS likes,
       SUM(r.dislikes) AS dislikes
FROM review r
         JOIN TopUsers tu ON r.user_id = tu.user_id
GROUP BY r.user_id, tu.purchases
ORDER BY tu.purchases DESC;

-- 9. Показати 3 найчастіше використовувані види оплати за останній тиждень, які брали участь у транзакціях на фільми до 18
WITH RecentTransactions AS (SELECT t.payment_method,
                                   COUNT(*) AS transaction_count
                            FROM "transaction" t
                            WHERE t.purchase_date >= CURRENT_DATE - INTERVAL '7 days'
                              AND EXISTS (SELECT 1
                                          FROM movie m
                                          WHERE m.movie_id = t.movie_id
                                            AND m.adult = false)
                            GROUP BY t.payment_method
                            ORDER BY transaction_count DESC
                            LIMIT 3)

SELECT rt.transaction_count AS transaction_count,
       pm.*
FROM payment_method pm
         JOIN RecentTransactions rt ON pm.payment_method_id = rt.payment_method;

-- 10. Показати користувачів віком до 10 років, улюблений тип фільмів який (за описом) це жахи чи трилери


SELECT u.user_id,
       u.username,
       u.birth_date,
       f.movie_id,
       m.title,
       m.overview
FROM "user" u
         JOIN favorite f ON u.user_id = f.user_id
         JOIN movie m ON f.movie_id = m.movie_id
WHERE EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM u.birth_date) < 10
  AND (m.overview ~* 'horror' OR m.overview ~* 'thriller');
