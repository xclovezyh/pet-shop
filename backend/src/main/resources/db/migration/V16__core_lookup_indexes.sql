CREATE INDEX idx_app_user_nickname ON app_user (nickname);

CREATE INDEX idx_region_area_area_code ON region_area (area_code);
CREATE INDEX idx_region_area_parent_sort_order ON region_area (parent_id, sort_order, id);
CREATE INDEX idx_region_area_level_sort_order ON region_area (level, sort_order, id);
