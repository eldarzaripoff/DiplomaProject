CREATE EXTENSION if NOT EXISTS pgcrypto;

UPDATE netology.USERS SET password = crypt(password, gen_salt('bf', 8));
