INSERT INTO pet_category (name, description, image_url, tags)
SELECT '猫咪', '温顺亲人，适合公寓和家庭陪伴。', '', '新手友好,安静,陪伴型'
WHERE NOT EXISTS (SELECT 1 FROM pet_category WHERE name = '猫咪');

INSERT INTO pet_category (name, description, image_url, tags)
SELECT '狗狗', '活泼忠诚，需要规律运动和训练。', '', '互动强,需要遛弯,家庭型'
WHERE NOT EXISTS (SELECT 1 FROM pet_category WHERE name = '狗狗');

INSERT INTO pet_category (name, description, image_url, tags)
SELECT '小宠', '仓鼠、兔子、龙猫、豚鼠等，占地小但需要细心照顾。', '', '空间小,易观察,轻陪伴'
WHERE NOT EXISTS (SELECT 1 FROM pet_category WHERE name = '小宠');

INSERT INTO pet_category (name, description, image_url, tags)
SELECT '用品', '食品、玩具、猫爬架、牵引绳等宠物用品。', '', '闲置交易,日常消耗,养宠装备'
WHERE NOT EXISTS (SELECT 1 FROM pet_category WHERE name = '用品');

INSERT INTO reference_option (option_type, label, sort_order)
SELECT 'post_type', '互换', 1
WHERE NOT EXISTS (SELECT 1 FROM reference_option WHERE option_type = 'post_type' AND label = '互换');

INSERT INTO reference_option (option_type, label, sort_order)
SELECT 'post_type', '售卖', 2
WHERE NOT EXISTS (SELECT 1 FROM reference_option WHERE option_type = 'post_type' AND label = '售卖');

INSERT INTO reference_option (option_type, label, sort_order)
SELECT 'post_type', '领养', 3
WHERE NOT EXISTS (SELECT 1 FROM reference_option WHERE option_type = 'post_type' AND label = '领养');

INSERT INTO reference_option (option_type, label, sort_order)
SELECT 'post_type', '闲置', 4
WHERE NOT EXISTS (SELECT 1 FROM reference_option WHERE option_type = 'post_type' AND label = '闲置');

INSERT INTO region_area (name, level, parent_id, sort_order)
SELECT '上海市', 'province', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM region_area WHERE level = 'province' AND name = '上海市');

INSERT INTO region_area (name, level, parent_id, sort_order)
SELECT '浙江省', 'province', NULL, 2
WHERE NOT EXISTS (SELECT 1 FROM region_area WHERE level = 'province' AND name = '浙江省');

INSERT INTO region_area (name, level, parent_id, sort_order)
SELECT '江苏省', 'province', NULL, 3
WHERE NOT EXISTS (SELECT 1 FROM region_area WHERE level = 'province' AND name = '江苏省');
