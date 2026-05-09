ALTER TABLE app_user ADD COLUMN username VARCHAR(255);
ALTER TABLE app_user ADD COLUMN phone VARCHAR(255);
ALTER TABLE app_user ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE app_user ADD COLUMN password_salt VARCHAR(255);
ALTER TABLE app_user ADD COLUMN last_login_at DATETIME(6);

UPDATE app_user SET username = nickname WHERE username IS NULL OR username = '';

CREATE UNIQUE INDEX uk_app_user_username ON app_user (username);
CREATE UNIQUE INDEX uk_app_user_phone ON app_user (phone);
