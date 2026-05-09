package com.petshop.config;

import com.petshop.model.MarketPost;
import com.petshop.model.Moment;
import com.petshop.model.Pet;
import com.petshop.model.PetCategory;
import com.petshop.model.ReferenceOption;
import com.petshop.model.RegionArea;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.MomentRepository;
import com.petshop.repository.PetCategoryRepository;
import com.petshop.repository.PetRepository;
import com.petshop.repository.ReferenceOptionRepository;
import com.petshop.repository.RegionAreaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(PetCategoryRepository categories, PetRepository pets,
                               MarketPostRepository posts, MomentRepository moments,
                               ReferenceOptionRepository referenceOptions, RegionAreaRepository regions) {
        return args -> {
            if (hasUnreadableCategories(categories)) {
                moments.deleteAll();
                posts.deleteAll();
                pets.deleteAll();
                categories.deleteAll();
            }
            if (hasUnreadableReferenceData(referenceOptions, regions)) {
                referenceOptions.deleteAll();
                regions.deleteAll();
            }

            seedCategories(categories);
            seedReferenceData(referenceOptions, regions);

            if (pets.count() == 0) {
                pets.save(pet("团子", "猫咪", "英短银渐层", "8个月", "上海市 上海市 浦东新区", "在售", "疫苗齐全，已驱虫", "安静亲人，喜欢陪睡", new BigDecimal("1800")));
                pets.save(pet("可乐", "狗狗", "柯基", "1岁", "浙江省 杭州市 西湖区", "可互换", "体检正常，精力充沛", "活泼黏人，会坐下握手", BigDecimal.ZERO));
                pets.save(pet("雪球", "小宠", "侏儒兔", "5个月", "江苏省 南京市 玄武区", "可领养", "健康，饮食稳定", "胆小但熟悉后很亲近", BigDecimal.ZERO));
                pets.save(pet("蓝宝", "鸟类", "虎皮鹦鹉", "7个月", "广东省 深圳市 南山区", "可领养", "羽毛状态良好，已适应手养", "好奇心强，会简单互动", BigDecimal.ZERO));
                pets.save(pet("小龟", "爬宠", "草龟", "2岁", "四川省 成都市 武侯区", "在售", "进食稳定，背甲完整", "安静耐看，适合有温控设备家庭", new BigDecimal("120")));
            }

            enrichPetProfiles(pets);

            if (posts.count() == 0) {
                posts.save(post("想给柯基找同城互换寄养伙伴", "互换", "狗狗", "浙江省 杭州市 西湖区", "工作日偶尔出差，希望找同城稳定互助家庭。"));
                posts.save(post("英短银渐层找新家", "售卖", "猫咪", "上海市 上海市 浦东新区", "自家猫宝宝，疫苗驱虫记录完整，可预约看猫。"));
                posts.save(post("闲置猫爬架转让", "闲置", "用品", "江苏省 苏州市 姑苏区", "九成新，适合小户型，支持站内私信沟通。"));
                posts.save(post("寻宠互助：橘猫走失", "寻宠", "猫咪", "湖北省 武汉市 武昌区", "橘猫亲人，戴蓝色项圈，希望附近用户帮忙留意。"));
            }

            if (moments.count() == 0) {
                moments.save(moment("林小满", "团子", "猫咪", "今天第一次学会自己开零食罐，已经开始怀疑家里的安全系统。", 28));
                moments.save(moment("阿舟", "可乐", "狗狗", "雨停后去公园跑了两圈，回来直接睡成一张毯子。", 16));
                moments.save(moment("南栀", "蓝宝", "鸟类", "蓝宝今天终于愿意站上手指，奖励了最喜欢的小米穗。", 12));
            }
        };
    }

    private boolean hasUnreadableCategories(PetCategoryRepository categories) {
        return categories.count() > 0 && categories.findAll().stream()
                .noneMatch(category -> "猫咪".equals(category.getName()));
    }

    private boolean hasUnreadableReferenceData(ReferenceOptionRepository options, RegionAreaRepository regions) {
        boolean badOptions = options.count() > 0 && options.findAll().stream()
                .noneMatch(option -> "互换".equals(option.getLabel()));
        boolean badRegions = regions.count() > 0 && regions.findAll().stream()
                .noneMatch(region -> "上海市".equals(region.getName()));
        return badOptions || badRegions;
    }

    private void seedCategories(PetCategoryRepository categories) {
        ensureCategory(categories, "猫咪", "温顺亲人，适合公寓和家庭陪伴。", "新手友好,安静,陪伴型");
        ensureCategory(categories, "狗狗", "活泼忠诚，需要规律运动和训练。", "互动强,需要遛弯,家庭型");
        ensureCategory(categories, "小宠", "仓鼠、兔子、龙猫、豚鼠等，占地小但需要细心照顾。", "空间小,易观察,轻陪伴");
        ensureCategory(categories, "水族", "观赏性强，适合打造安静的家居角落。", "观赏型,低噪音,设备需求");
        ensureCategory(categories, "鸟类", "鹦鹉、文鸟、金丝雀等，需要稳定笼舍和互动训练。", "鸣叫,训练,环境敏感");
        ensureCategory(categories, "爬宠", "龟、守宫、蜥蜴、蛇等，重点关注温湿度和饲养箱。", "温控,进阶饲养,低互动");
        ensureCategory(categories, "异宠", "蜜袋鼯、刺猬等特殊宠物，适合有经验的饲养者。", "特殊护理,经验要求,夜行");
        ensureCategory(categories, "用品", "食品、玩具、猫爬架、牵引绳等宠物用品。", "闲置交易,日常消耗,养宠装备");
    }

    private void ensureCategory(PetCategoryRepository categories, String name, String description, String tags) {
        boolean exists = categories.findAll().stream()
                .anyMatch(category -> name.equals(category.getName()));
        if (!exists) {
            categories.save(category(name, description, tags));
        }
    }

    private void seedReferenceData(ReferenceOptionRepository referenceOptions, RegionAreaRepository regions) {
        ensureOptions(referenceOptions, "post_type", "互换", "售卖", "领养", "闲置", "求助", "寄养", "寻宠", "相亲配种");
        ensureOptions(referenceOptions, "pet_status", "在售", "可领养", "可互换", "已预订", "已成交", "暂不开放");
        ensureOptions(referenceOptions, "pet_gender", "公", "母", "未知");
        ensureOptions(referenceOptions, "age_range", "幼年", "青年", "成年", "老年");
        ensureOptions(referenceOptions, "health_record", "疫苗齐全", "已驱虫", "已绝育", "体检正常", "需复查", "特殊护理");
        ensureOptions(referenceOptions, "personality_tag", "亲人", "安静", "活泼", "胆小", "独立", "粘人", "适合新手", "适合有经验家庭");
        ensureOptions(referenceOptions, "service_tag", "站内私信", "同城自提", "线下看宠", "寄养互助", "闲置转让", "领养审核");

        ensureRegion(regions, "北京市", new String[][]{{"北京市", "东城区", "西城区", "朝阳区", "海淀区", "丰台区", "通州区"}});
        ensureRegion(regions, "上海市", new String[][]{{"上海市", "浦东新区", "徐汇区", "静安区", "闵行区", "杨浦区", "松江区"}});
        ensureRegion(regions, "广东省", new String[][]{
                {"广州市", "天河区", "越秀区", "海珠区", "番禺区", "白云区"},
                {"深圳市", "南山区", "福田区", "罗湖区", "宝安区", "龙岗区"},
                {"佛山市", "禅城区", "南海区", "顺德区"}
        });
        ensureRegion(regions, "浙江省", new String[][]{
                {"杭州市", "西湖区", "拱墅区", "滨江区", "余杭区", "萧山区"},
                {"宁波市", "海曙区", "鄞州区", "江北区"},
                {"温州市", "鹿城区", "瓯海区", "龙湾区"}
        });
        ensureRegion(regions, "江苏省", new String[][]{
                {"南京市", "玄武区", "秦淮区", "建邺区", "鼓楼区"},
                {"苏州市", "姑苏区", "吴中区", "工业园区", "昆山市"},
                {"无锡市", "梁溪区", "滨湖区", "惠山区"}
        });
        ensureRegion(regions, "四川省", new String[][]{
                {"成都市", "锦江区", "青羊区", "武侯区", "高新区", "双流区"},
                {"绵阳市", "涪城区", "游仙区"}
        });
        ensureRegion(regions, "湖北省", new String[][]{
                {"武汉市", "江岸区", "武昌区", "洪山区", "汉阳区"},
                {"宜昌市", "西陵区", "伍家岗区"}
        });
    }

    private void ensureOptions(ReferenceOptionRepository repository, String optionType, String... labels) {
        for (int i = 0; i < labels.length; i++) {
            ensureOption(repository, optionType, labels[i], i + 1);
        }
    }

    private void ensureOption(ReferenceOptionRepository repository, String optionType, String label, int sortOrder) {
        boolean exists = repository.findByOptionTypeOrderBySortOrderAscIdAsc(optionType).stream()
                .anyMatch(option -> label.equals(option.getLabel()));
        if (!exists) {
            ReferenceOption option = new ReferenceOption();
            option.setOptionType(optionType);
            option.setLabel(label);
            option.setSortOrder(sortOrder);
            repository.save(option);
        }
    }

    private void ensureRegion(RegionAreaRepository repository, String provinceName, String[][] cityRows) {
        RegionArea province = ensureArea(repository, "province", provinceName, null, nextSort(repository, "province", null));
        for (int i = 0; i < cityRows.length; i++) {
            String[] cityRow = cityRows[i];
            RegionArea city = ensureArea(repository, "city", cityRow[0], province.getId(), i + 1);
            for (int j = 1; j < cityRow.length; j++) {
                ensureArea(repository, "district", cityRow[j], city.getId(), j);
            }
        }
    }

    private RegionArea ensureArea(RegionAreaRepository repository, String level, String name, Long parentId, int sortOrder) {
        return repository.findAll().stream()
                .filter(area -> level.equals(area.getLevel()))
                .filter(area -> name.equals(area.getName()))
                .filter(area -> parentId == null ? area.getParentId() == null : parentId.equals(area.getParentId()))
                .findFirst()
                .orElseGet(() -> {
                    RegionArea area = new RegionArea();
                    area.setLevel(level);
                    area.setName(name);
                    area.setParentId(parentId);
                    area.setSortOrder(sortOrder);
                    return repository.save(area);
                });
    }

    private int nextSort(RegionAreaRepository repository, String level, Long parentId) {
        return (int) repository.findAll().stream()
                .filter(area -> level.equals(area.getLevel()))
                .filter(area -> parentId == null ? area.getParentId() == null : parentId.equals(area.getParentId()))
                .count() + 1;
    }

    private PetCategory category(String name, String description, String tags) {
        PetCategory category = new PetCategory();
        category.setName(name);
        category.setDescription(description);
        category.setTags(tags);
        category.setImageUrl("");
        return category;
    }

    private Pet pet(String name, String category, String breed, String age, String city, String status,
                    String health, String personality, BigDecimal price) {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setCategory(category);
        pet.setBreed(breed);
        pet.setAge(age);
        pet.setCity(city);
        pet.setStatus(status);
        pet.setHealthInfo(health);
        pet.setPersonality(personality);
        pet.setPrice(price);
        pet.setOwnerName("平台示例用户");
        String[] images = petImages(category, name);
        pet.setImageUrl(firstImage(images));
        pet.setImageUrls(String.join(",", images));
        applyPetProfile(pet);
        return pet;
    }

    private void enrichPetProfiles(PetRepository pets) {
        pets.findAll().forEach(pet -> {
            applyPetProfile(pet);
            pets.save(pet);
        });
    }

    private void applyPetProfile(Pet pet) {
        String category = pet.getCategory();
        String name = pet.getName();
        if (isBlank(pet.getOwnerName())) {
            pet.setOwnerName("平台示例用户");
        }
        if (isBlank(pet.getImageUrls())) {
            String[] images = petImages(category, name);
            pet.setImageUrl(firstImage(images));
            pet.setImageUrls(String.join(",", images));
        }
        if (isBlank(pet.getGender())) {
            pet.setGender("狗狗".equals(category) || "鐙楃嫍".equals(category) ? "公" : "母");
        }
        if (isBlank(pet.getAgeRange())) {
            pet.setAgeRange("2宀?".equals(pet.getAge()) ? "成年" : "幼年");
        }
        if (pet.getVaccinated() == null) {
            pet.setVaccinated(!("爬宠".equals(category) || "鐖疇".equals(category)));
        }
        if (pet.getDewormed() == null) {
            pet.setDewormed(true);
        }
        if (pet.getNeutered() == null) {
            pet.setNeutered("猫咪".equals(category) || "鐚挭".equals(category));
        }
        if (isBlank(pet.getHealthRecords())) {
            pet.setHealthRecords(healthRecordsFor(category));
        }
        if (isBlank(pet.getCareNotes())) {
            pet.setCareNotes(careNotesFor(category));
        }
    }

    private MarketPost post(String title, String type, String category, String city, String description) {
        MarketPost post = new MarketPost();
        post.setTitle(title);
        post.setType(type);
        post.setCategory(category);
        post.setCity(city);
        post.setDescription(description);
        post.setAuthor("示例用户");
        post.setContact("站内私信");
        post.setStatus("在售");
        String[] images = postImages(category, type);
        post.setImageUrl(firstImage(images));
        post.setImageUrls(String.join(",", images));
        post.setCreatedAt(LocalDateTime.now());
        return post;
    }

    private Moment moment(String author, String petName, String category, String content, int likes) {
        Moment moment = new Moment();
        moment.setAuthor(author);
        moment.setPetName(petName);
        moment.setCategory(category);
        moment.setCity("上海市 上海市 浦东新区");
        moment.setContent(content);
        moment.setLikes(likes);
        String[] images = momentImages(category);
        moment.setImageUrl(firstImage(images));
        moment.setImageUrls(String.join(",", images));
        moment.setCreatedAt(LocalDateTime.now());
        return moment;
    }

    private String[] petImages(String category, String name) {
        String first = petImage(category, name);
        if ("鐙楃嫍".equals(category) || "狗狗".equals(category)) {
            return new String[]{
                    first,
                    "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1534361960057-19889db9621e?auto=format&fit=crop&w=900&q=80"
            };
        }
        if ("灏忓疇".equals(category) || "小宠".equals(category)) {
            return new String[]{
                    first,
                    "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?auto=format&fit=crop&w=900&q=80"
            };
        }
        if ("楦熺被".equals(category) || "鸟类".equals(category)) {
            return new String[]{
                    first,
                    "https://images.unsplash.com/photo-1522926193341-e9ffd686c60f?auto=format&fit=crop&w=900&q=80"
            };
        }
        return new String[]{
                first,
                "https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=900&q=80"
        };
    }

    private String petImage(String category, String name) {
        if ("狗狗".equals(category)) {
            return "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=900&q=80";
        }
        if ("小宠".equals(category)) {
            return "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=900&q=80";
        }
        if ("鸟类".equals(category)) {
            return "https://images.unsplash.com/photo-1452857297128-d9c29adba80b?auto=format&fit=crop&w=900&q=80";
        }
        if ("爬宠".equals(category)) {
            return "https://images.unsplash.com/photo-1597776941486-054bf5529210?auto=format&fit=crop&w=900&q=80";
        }
        return "雪球".equals(name)
                ? "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=900&q=80"
                : "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=80";
    }

    private String[] postImages(String category, String type) {
        if ("狗狗".equals(category)) {
            return new String[]{
                    "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=900&q=80"
            };
        }
        if ("用品".equals(category)) {
            return new String[]{
                    "https://images.unsplash.com/photo-1545249390-6bdfa286032f?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1543852786-1cf6624b9987?auto=format&fit=crop&w=900&q=80"
            };
        }
        if ("寻宠".equals(type)) {
            return new String[]{
                    "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1574158622682-e40e69881006?auto=format&fit=crop&w=900&q=80"
            };
        }
        return new String[]{
                "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=900&q=80"
        };
    }

    private String[] momentImages(String category) {
        if ("狗狗".equals(category)) {
            return new String[]{
                    "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1534361960057-19889db9621e?auto=format&fit=crop&w=900&q=80"
            };
        }
        if ("鸟类".equals(category)) {
            return new String[]{
                    "https://images.unsplash.com/photo-1452857297128-d9c29adba80b?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1522926193341-e9ffd686c60f?auto=format&fit=crop&w=900&q=80"
            };
        }
        return new String[]{
                "https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=900&q=80"
        };
    }

    private String firstImage(String[] images) {
        return images.length == 0 ? "" : images[0];
    }

    private String healthRecordsFor(String category) {
        if ("鐖疇".equals(category) || "爬宠".equals(category)) {
            return "体表完整,进食稳定,温湿度记录正常";
        }
        if ("楦熺被".equals(category) || "鸟类".equals(category)) {
            return "羽毛状态良好,精神状态稳定,已适应手养";
        }
        if ("灏忓疇".equals(category) || "小宠".equals(category)) {
            return "体检正常,饮食稳定,笼舍清洁";
        }
        return "疫苗齐全,已驱虫,体检正常";
    }

    private String careNotesFor(String category) {
        if ("鐖疇".equals(category) || "爬宠".equals(category)) {
            return "需要稳定温控和安静环境，建议有基础饲养经验。";
        }
        if ("楦熺被".equals(category) || "鸟类".equals(category)) {
            return "需要固定互动时间，换环境初期请减少惊扰。";
        }
        if ("灏忓疇".equals(category) || "小宠".equals(category)) {
            return "适合安静家庭，注意垫料清洁和少量多次喂食。";
        }
        return "适合家庭陪伴，建议继续保持疫苗、驱虫和定期体检。";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
