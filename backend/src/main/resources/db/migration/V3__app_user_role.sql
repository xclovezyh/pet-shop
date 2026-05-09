ALTER TABLE app_user ADD COLUMN role VARCHAR(255);

UPDATE app_user SET role = 'USER' WHERE role IS NULL OR role = '';
