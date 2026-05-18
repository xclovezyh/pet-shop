-- ============================================================
-- V17 手动数据库迁移脚本
-- 执行前请确保已备份数据库！
-- ============================================================

-- 1. 为 app_user 表添加 city_code 字段
ALTER TABLE app_user ADD COLUMN city_code VARCHAR(12) AFTER city;
CREATE INDEX idx_app_user_city_code ON app_user (city_code);

-- 2. 为 pet 表添加 city_code 字段
ALTER TABLE pet ADD COLUMN city_code VARCHAR(12) AFTER city;
CREATE INDEX idx_pet_city_code ON pet (city_code);

-- 3. 为 market_post 表添加 city_code 字段
ALTER TABLE market_post ADD COLUMN city_code VARCHAR(12) AFTER city;
CREATE INDEX idx_market_post_city_code ON market_post (city_code);

-- 4. 为 moment 表添加 city_code 字段
ALTER TABLE moment ADD COLUMN city_code VARCHAR(12) AFTER city;
CREATE INDEX idx_moment_city_code ON moment (city_code);

-- 5. 数据回填 - 从 region_area 表获取 area_code 并填充到各个业务表
-- 注意：只有当 region_area 表中存在对应的城市名称时才会填充

-- 回填 app_user 的 city_code
UPDATE app_user u
INNER JOIN region_area ra ON ra.name = u.city AND ra.level = 'city'
SET u.city_code = ra.area_code
WHERE u.city_code IS NULL AND u.city IS NOT NULL;

-- 回填 pet 的 city_code
UPDATE pet p
INNER JOIN region_area ra ON ra.name = p.city AND ra.level = 'city'
SET p.city_code = ra.area_code
WHERE p.city_code IS NULL AND p.city IS NOT NULL;

-- 回填 market_post 的 city_code
UPDATE market_post mp
INNER JOIN region_area ra ON ra.name = mp.city AND ra.level = 'city'
SET mp.city_code = ra.area_code
WHERE mp.city_code IS NULL AND mp.city IS NOT NULL;

-- 回填 moment 的 city_code
UPDATE moment m
INNER JOIN region_area ra ON ra.name = m.city AND ra.level = 'city'
SET m.city_code = ra.area_code
WHERE m.city_code IS NULL AND m.city IS NOT NULL;

-- 验证迁移结果
SELECT 'app_user' as table_name, COUNT(*) as total, SUM(CASE WHEN city_code IS NOT NULL THEN 1 ELSE 0 END) as filled FROM app_user
UNION ALL
SELECT 'pet', COUNT(*), SUM(CASE WHEN city_code IS NOT NULL THEN 1 ELSE 0 END) FROM pet
UNION ALL
SELECT 'market_post', COUNT(*), SUM(CASE WHEN city_code IS NOT NULL THEN 1 ELSE 0 END) FROM market_post
UNION ALL
SELECT 'moment', COUNT(*), SUM(CASE WHEN city_code IS NOT NULL THEN 1 ELSE 0 END) FROM moment;
