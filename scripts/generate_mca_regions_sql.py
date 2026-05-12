from __future__ import print_function

import io
import os
import re
import sys

try:
    # Python 3
    from html import unescape
    from urllib.request import urlopen
except ImportError:
    # Python 2 fallback for older local runtimes
    from HTMLParser import HTMLParser
    from urllib2 import urlopen
    unescape = HTMLParser().unescape


SOURCE_URL = "https://www.mca.gov.cn/mzsj/xzqh/2023/202301xzqh.html"

DIRECT_MUNICIPALITY_CODES = {"11", "12", "31", "50"}


def clean_text(value):
    value = re.sub(r"<[^>]+>", "", value)
    value = value.replace("&nbsp;", " ")
    value = re.sub(r"\s+", " ", unescape(value)).strip()
    return value.replace("*", "")


def load_records():
    html = urlopen(SOURCE_URL, timeout=30).read().decode("utf-8", "ignore")
    rows = re.findall(r"<tr[^>]*>(.*?)</tr>", html, flags=re.S | re.I)
    records = []
    for row in rows:
        cells = re.findall(r"<td[^>]*>(.*?)</td>", row, flags=re.S | re.I)
        if len(cells) < 3:
            continue
        code = clean_text(cells[1])
        name = clean_text(cells[2])
        if code.isdigit() and len(code) == 6 and name:
            records.append((code, name))
    return records


def synthetic_direct_city_name(province_name):
    return province_name


def synthetic_managed_city_name(province_name):
    if province_name.endswith("省"):
        return "省直辖县级行政区划"
    if province_name.endswith("自治区"):
        return "自治区直辖县级行政区划"
    return province_name


def escape_sql(value):
    return value.replace("'", "''")


def build_tree(records):
    provinces = []
    cities = []
    districts = []

    province_map = {}
    city_map = {}

    province_sort = 0
    city_sort_by_province = {}
    district_sort_by_city = {}

    for code, name in records:
        if code.endswith("0000"):
            province_sort += 1
            province_map[code] = {
                "code": code,
                "name": name,
                "sort": province_sort,
            }
            provinces.append(province_map[code])

    prefectures = [(code, name) for code, name in records if code.endswith("00") and not code.endswith("0000")]
    counties = [(code, name) for code, name in records if not code.endswith("00")]

    for code, name in prefectures:
        province_code = code[:2] + "0000"
        city_sort_by_province[province_code] = city_sort_by_province.get(province_code, 0) + 1
        city_map[code] = {
            "code": code,
            "name": name,
            "province_code": province_code,
            "sort": city_sort_by_province[province_code],
        }
        cities.append(city_map[code])

    for province_code, province in province_map.items():
        prefix = province_code[:2]
        if prefix in DIRECT_MUNICIPALITY_CODES:
            code = prefix + "0100"
            if code not in city_map:
                city_map[code] = {
                    "code": code,
                    "name": synthetic_direct_city_name(province["name"]),
                    "province_code": province_code,
                    "sort": 1,
                }
                cities.append(city_map[code])

    for code, name in counties:
        parent_city_code = code[:4] + "00"
        province_code = code[:2] + "0000"
        province_name = province_map[province_code]["name"]

        if province_code[:2] in DIRECT_MUNICIPALITY_CODES:
            parent_city_code = province_code[:2] + "0100"

        if parent_city_code not in city_map:
            city_sort_by_province[province_code] = city_sort_by_province.get(province_code, 0) + 1
            city_map[parent_city_code] = {
                "code": parent_city_code,
                "name": synthetic_managed_city_name(province_name),
                "province_code": province_code,
                "sort": city_sort_by_province[province_code],
            }
            cities.append(city_map[parent_city_code])

        district_sort_by_city[parent_city_code] = district_sort_by_city.get(parent_city_code, 0) + 1
        districts.append({
            "code": code,
            "name": name,
            "city_code": parent_city_code,
            "sort": district_sort_by_city[parent_city_code],
        })

    cities.sort(key=lambda item: (province_map[item["province_code"]]["sort"], item["sort"], item["code"]))
    districts.sort(key=lambda item: (city_map[item["city_code"]]["province_code"], item["city_code"], item["sort"], item["code"]))
    return provinces, cities, districts


def render_sql(provinces, cities, districts):
    out = io.StringIO()
    out.write("-- Source: Ministry of Civil Affairs of the People's Republic of China\n")
    out.write("-- Dataset: 2023 county-level and above administrative division codes\n")
    out.write("-- Source URL: {0}\n\n".format(SOURCE_URL))
    out.write("ALTER TABLE region_area ADD COLUMN area_code VARCHAR(12) NULL AFTER name;\n")
    out.write("ALTER TABLE region_area MODIFY COLUMN level VARCHAR(255) COMMENT '地区层级：province/city/district';\n")
    out.write("ALTER TABLE region_area MODIFY COLUMN name VARCHAR(255) COMMENT '地区名称';\n")
    out.write("ALTER TABLE region_area MODIFY COLUMN area_code VARCHAR(12) COMMENT '行政区划代码';\n")
    out.write("ALTER TABLE region_area MODIFY COLUMN parent_id BIGINT COMMENT '上级地区 ID';\n")
    out.write("ALTER TABLE region_area MODIFY COLUMN sort_order INT COMMENT '展示排序';\n")
    out.write("DELETE FROM region_area;\n")
    out.write("ALTER TABLE region_area AUTO_INCREMENT = 1;\n\n")

    for item in provinces:
        out.write(
            "INSERT INTO region_area (name, area_code, level, parent_id, sort_order) "
            "VALUES ('{0}', '{1}', 'province', NULL, {2});\n".format(
                escape_sql(item["name"]), item["code"], item["sort"]
            )
        )

    out.write("\n")
    for item in cities:
        out.write(
            "INSERT INTO region_area (name, area_code, level, parent_id, sort_order) "
            "VALUES ('{0}', '{1}', 'city', "
            "(SELECT id FROM (SELECT id FROM region_area WHERE area_code = '{2}' LIMIT 1) p), {3});\n".format(
                escape_sql(item["name"]), item["code"], item["province_code"], item["sort"]
            )
        )

    out.write("\n")
    for item in districts:
        out.write(
            "INSERT INTO region_area (name, area_code, level, parent_id, sort_order) "
            "VALUES ('{0}', '{1}', 'district', "
            "(SELECT id FROM (SELECT id FROM region_area WHERE area_code = '{2}' LIMIT 1) c), {3});\n".format(
                escape_sql(item["name"]), item["code"], item["city_code"], item["sort"]
            )
        )

    out.write("\n")
    out.write("CREATE UNIQUE INDEX uk_region_area_code ON region_area (area_code);\n")
    out.write("CREATE INDEX idx_region_area_parent_id ON region_area (parent_id);\n")
    return out.getvalue()


def main():
    target = sys.argv[1] if len(sys.argv) > 1 else os.path.join(
        "backend", "src", "main", "resources", "db", "migration", "V8__national_regions.sql"
    )
    records = load_records()
    provinces, cities, districts = build_tree(records)
    sql = render_sql(provinces, cities, districts)
    with io.open(target, "w", encoding="utf-8", newline="\n") as handle:
        handle.write(sql)
    print("generated", target)
    print("provinces", len(provinces), "cities", len(cities), "districts", len(districts))


if __name__ == "__main__":
    main()
