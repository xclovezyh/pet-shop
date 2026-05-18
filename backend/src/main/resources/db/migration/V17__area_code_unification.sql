-- Area Code Unification
-- Add city_code field to business tables for standardized region reference

-- 1. Add city_code to app_user
ALTER TABLE app_user ADD COLUMN city_code VARCHAR(12) AFTER city;
CREATE INDEX idx_app_user_city_code ON app_user (city_code);

-- 2. Add city_code to pet
ALTER TABLE pet ADD COLUMN city_code VARCHAR(12) AFTER city;
CREATE INDEX idx_pet_city_code ON pet (city_code);

-- 3. Add city_code to market_post
ALTER TABLE market_post ADD COLUMN city_code VARCHAR(12) AFTER city;
CREATE INDEX idx_market_post_city_code ON market_post (city_code);

-- 4. Add city_code to moment
ALTER TABLE moment ADD COLUMN city_code VARCHAR(12) AFTER city;
CREATE INDEX idx_moment_city_code ON moment (city_code);

-- 5. Backfill city_code from existing city names using region_area data
-- This is a best-effort migration; unmatched cities will be left NULL
UPDATE app_user u
INNER JOIN region_area ra ON ra.name = u.city AND ra.level = 'city'
SET u.city_code = ra.area_code
WHERE u.city_code IS NULL AND u.city IS NOT NULL;

UPDATE pet p
INNER JOIN region_area ra ON ra.name = p.city AND ra.level = 'city'
SET p.city_code = ra.area_code
WHERE p.city_code IS NULL AND p.city IS NOT NULL;

UPDATE market_post mp
INNER JOIN region_area ra ON ra.name = mp.city AND ra.level = 'city'
SET mp.city_code = ra.area_code
WHERE mp.city_code IS NULL AND mp.city IS NOT NULL;

UPDATE moment m
INNER JOIN region_area ra ON ra.name = m.city AND ra.level = 'city'
SET m.city_code = ra.area_code
WHERE m.city_code IS NULL AND m.city IS NOT NULL;
