-- Initial reference data for pet-shop.
-- Use this on a clean database, or convert it into controlled Flyway/Liquibase migrations with unique constraints.

INSERT IGNORE INTO pet_category (name, description, image_url, tags) VALUES
('猫咪', '温顺亲人，适合公寓和家庭陪伴。', '', '新手友好,安静,陪伴型'),
('狗狗', '活泼忠诚，需要规律运动和训练。', '', '互动强,需要遛弯,家庭型'),
('小宠', '仓鼠、兔子、龙猫、豚鼠等，占地小但需要细心照顾。', '', '空间小,易观察,轻陪伴'),
('水族', '观赏性强，适合打造安静的家居角落。', '', '观赏型,低噪音,设备需求'),
('鸟类', '鹦鹉、文鸟、金丝雀等，需要稳定笼舍和互动训练。', '', '鸣叫,训练,环境敏感'),
('爬宠', '龟、守宫、蜥蜴、蛇等，重点关注温湿度和饲养箱。', '', '温控,进阶饲养,低互动'),
('异宠', '蜜袋鼯、刺猬等特殊宠物，适合有经验的饲养者。', '', '特殊护理,经验要求,夜行'),
('用品', '食品、玩具、猫爬架、牵引绳等宠物用品。', '', '闲置交易,日常消耗,养宠装备');

INSERT IGNORE INTO reference_option (option_type, label, sort_order) VALUES
('post_type', '互换', 1),
('post_type', '售卖', 2),
('post_type', '领养', 3),
('post_type', '闲置', 4),
('post_type', '求助', 5),
('post_type', '寄养', 6),
('post_type', '寻宠', 7),
('post_type', '相亲配种', 8),
('pet_status', '在售', 1),
('pet_status', '可领养', 2),
('pet_status', '可互换', 3),
('pet_gender', '公', 1),
('pet_gender', '母', 2),
('pet_gender', '未知', 3),
('age_range', '幼年', 1),
('age_range', '青年', 2),
('age_range', '成年', 3),
('age_range', '老年', 4),
('health_record', '疫苗齐全', 1),
('health_record', '已驱虫', 2),
('health_record', '已绝育', 3),
('health_record', '体检正常', 4),
('personality_tag', '亲人', 1),
('personality_tag', '安静', 2),
('personality_tag', '活泼', 3),
('personality_tag', '胆小', 4),
('service_tag', '站内私信', 1),
('service_tag', '同城自提', 2),
('service_tag', '寄养互助', 3);

INSERT IGNORE INTO region_area (name, level, parent_id, sort_order) VALUES
('上海市', 'province', NULL, 1),
('浙江省', 'province', NULL, 2),
('江苏省', 'province', NULL, 3),
('广东省', 'province', NULL, 4);
