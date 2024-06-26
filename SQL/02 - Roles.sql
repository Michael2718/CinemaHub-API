-- Create roles
CREATE ROLE user_role NOINHERIT;
CREATE ROLE auth_role NOINHERIT;
CREATE ROLE admin_role NOINHERIT;

-- Grant necessary privileges to user_role
GRANT SELECT, INSERT, UPDATE, DELETE ON favorite TO user_role;
GRANT SELECT ON genre TO user_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON history TO user_role;
GRANT SELECT ON movie TO user_role;
GRANT SELECT ON movie_genre TO user_role;
GRANT SELECT ON payment_method TO user_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON review TO user_role;
GRANT SELECT, INSERT ON transaction TO user_role;
GRANT USAGE ON SEQUENCE "CinemaHub-DEV".public.transaction_transaction_id_seq TO user_role;
GRANT SELECT, UPDATE ON "user" TO user_role;

-- Revoke all privileges from auth_role on data tables
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM auth_role;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM auth_role;
GRANT user_role TO auth_role WITH ADMIN OPTION;

-- Create specialized authentication user
CREATE USER auth_user WITH PASSWORD 'secure_password123';
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM auth_user;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM auth_user;
GRANT auth_role TO auth_user;
GRANT INSERT, SELECT ON "CinemaHub-DEV".public."user" TO auth_user;
GRANT USAGE ON SEQUENCE "CinemaHub-DEV".public.user_user_id_seq TO auth_user;
ALTER USER auth_user WITH createrole;

-- Admin
GRANT ALL ON ALL TABLES IN SCHEMA public TO admin_role;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO admin_role;

CREATE USER admin1 WITH PASSWORD '12345';
GRANT admin_role TO admin1;
