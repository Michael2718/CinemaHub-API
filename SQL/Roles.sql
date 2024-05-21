-- Create roles
CREATE ROLE user_role NOINHERIT;
CREATE ROLE auth_role NOINHERIT;

-- Grant necessary privileges to user_role
-- GRANT ADMIN FOR user_role TO auth_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON favorite TO user_role;
GRANT SELECT ON genre TO user_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON history TO user_role;
GRANT SELECT ON movie TO user_role;
GRANT SELECT ON movie_genre TO user_role;
GRANT SELECT ON payment_method TO user_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON review TO user_role;
GRANT SELECT, INSERT ON transaction TO user_role;
GRANT SELECT, UPDATE ON "user" TO user_role;

-- Revoke all privileges from auth_role on data tables
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM auth_role;
GRANT user_role TO auth_role WITH ADMIN OPTION;

-- Create specialized authentication user
CREATE USER auth_user WITH PASSWORD 'secure_password123';
GRANT auth_role TO auth_user;
ALTER USER auth_user WITH createrole;

-- Create usual users
CREATE USER user1 WITH PASSWORD '12345';
GRANT user_role TO user1;

CREATE USER user2 WITH PASSWORD '12345';
GRANT user_role TO user2;
