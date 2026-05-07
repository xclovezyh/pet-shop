package com.petshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reference-data")
public class ReferenceDataController {
    @GetMapping
    public Map<String, Object> all() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("regions", regions());
        data.put("postTypes", Arrays.asList("互换", "售卖", "领养", "闲置", "求助", "寄养", "寻宠", "相亲配种"));
        data.put("petStatuses", Arrays.asList("在售", "可领养", "可互换", "已预订", "已成交", "暂不开放"));
        data.put("petGenders", Arrays.asList("公", "母", "未知"));
        data.put("ageRanges", Arrays.asList("幼年", "青年", "成年", "老年"));
        data.put("healthRecords", Arrays.asList("疫苗齐全", "已驱虫", "已绝育", "体检正常", "需复查", "特殊护理"));
        data.put("personalityTags", Arrays.asList("亲人", "安静", "活泼", "胆小", "独立", "粘人", "适合新手", "适合有经验家庭"));
        data.put("serviceTags", Arrays.asList("站内私信", "同城自提", "线下看宠", "寄养互助", "闲置转让", "领养审核"));
        return data;
    }

    private List<Map<String, Object>> regions() {
        List<Map<String, Object>> regions = new ArrayList<>();
        regions.add(region("北京市", city("北京市", "东城区", "西城区", "朝阳区", "海淀区", "丰台区", "通州区")));
        regions.add(region("上海市", city("上海市", "浦东新区", "徐汇区", "静安区", "闵行区", "杨浦区", "松江区")));
        regions.add(region("广东省",
                city("广州市", "天河区", "越秀区", "海珠区", "番禺区", "白云区"),
                city("深圳市", "南山区", "福田区", "罗湖区", "宝安区", "龙岗区"),
                city("佛山市", "禅城区", "南海区", "顺德区")));
        regions.add(region("浙江省",
                city("杭州市", "西湖区", "拱墅区", "滨江区", "余杭区", "萧山区"),
                city("宁波市", "海曙区", "鄞州区", "江北区"),
                city("温州市", "鹿城区", "瓯海区", "龙湾区")));
        regions.add(region("江苏省",
                city("南京市", "玄武区", "秦淮区", "建邺区", "鼓楼区"),
                city("苏州市", "姑苏区", "吴中区", "工业园区", "昆山市"),
                city("无锡市", "梁溪区", "滨湖区", "惠山区")));
        regions.add(region("四川省",
                city("成都市", "锦江区", "青羊区", "武侯区", "高新区", "双流区"),
                city("绵阳市", "涪城区", "游仙区")));
        regions.add(region("湖北省",
                city("武汉市", "江岸区", "武昌区", "洪山区", "汉阳区"),
                city("宜昌市", "西陵区", "伍家岗区")));
        regions.add(region("山东省",
                city("济南市", "历下区", "市中区", "槐荫区"),
                city("青岛市", "市南区", "市北区", "崂山区", "黄岛区")));
        regions.add(region("福建省",
                city("福州市", "鼓楼区", "台江区", "仓山区"),
                city("厦门市", "思明区", "湖里区", "集美区")));
        regions.add(region("陕西省",
                city("西安市", "雁塔区", "碑林区", "莲湖区", "未央区"),
                city("咸阳市", "秦都区", "渭城区")));
        return regions;
    }

    @SafeVarargs
    private final Map<String, Object> region(String name, Map<String, Object>... cities) {
        Map<String, Object> region = new LinkedHashMap<>();
        region.put("name", name);
        region.put("cities", Arrays.asList(cities));
        return region;
    }

    private Map<String, Object> city(String name, String... districts) {
        Map<String, Object> city = new LinkedHashMap<>();
        city.put("name", name);
        city.put("districts", Arrays.asList(districts));
        return city;
    }
}
