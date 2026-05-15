package com.petshop.config;

import com.petshop.model.MarketPost;
import com.petshop.model.Moment;
import com.petshop.model.Pet;
import com.petshop.model.PetCategory;
import com.petshop.model.ReferenceOption;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.MomentRepository;
import com.petshop.repository.PetCategoryRepository;
import com.petshop.repository.PetRepository;
import com.petshop.repository.ReferenceOptionRepository;
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
                               ReferenceOptionRepository referenceOptions) {
        return args -> {
            if (hasUnreadableCategories(categories)) {
                moments.deleteAll();
                posts.deleteAll();
                pets.deleteAll();
                categories.deleteAll();
            }
            if (hasUnreadableReferenceData(referenceOptions)) {
                referenceOptions.deleteAll();
            }

            seedCategories(categories);
            seedReferenceData(referenceOptions);

            if (pets.count() == 0) {
                seedLaunchPets(pets);
            }

            enrichPetProfiles(pets);

            if (posts.count() == 0) {
                seedLaunchPosts(posts);
            }

            if (moments.count() == 0) {
                seedLaunchMoments(moments);
            }
        };
    }

    private boolean hasUnreadableCategories(PetCategoryRepository categories) {
        return categories.count() > 0 && categories.findAll().stream()
                .noneMatch(category -> "猫咪".equals(category.getName()));
    }

    private boolean hasUnreadableReferenceData(ReferenceOptionRepository options) {
        return options.count() > 0 && options.findAll().stream()
                .noneMatch(option -> "互换".equals(option.getLabel()));
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

    private void seedReferenceData(ReferenceOptionRepository referenceOptions) {
        ensureOptions(referenceOptions, "post_type", "互换", "售卖", "领养", "闲置", "求助", "寄养", "寻宠", "相亲配种");
        ensureOptions(referenceOptions, "pet_status", "在售", "可领养", "可互换", "已预订", "已成交", "暂不开放");
        ensureOptions(referenceOptions, "pet_gender", "公", "母", "未知");
        ensureOptions(referenceOptions, "age_range", "幼年", "青年", "成年", "老年");
        ensureOptions(referenceOptions, "health_record", "疫苗齐全", "已驱虫", "已绝育", "体检正常", "需复查", "特殊护理");
        ensureOptions(referenceOptions, "personality_tag", "亲人", "安静", "活泼", "胆小", "独立", "粘人", "适合新手", "适合有经验家庭");
        ensureOptions(referenceOptions, "service_tag", "站内私信", "同城自提", "线下看宠", "寄养互助", "闲置转让", "领养审核");
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

    private void seedLaunchPets(PetRepository pets) {
        String[][] profiles = {
                {"猫咪", "英短银渐层", "疫苗齐全，已驱虫", "安静亲人，适合公寓陪伴"},
                {"狗狗", "柯基", "体检正常，精力充沛", "活泼黏人，会基础指令"},
                {"小宠", "侏儒兔", "饮食稳定，笼舍干净", "胆小但熟悉后亲近"},
                {"水族", "孔雀鱼", "水质稳定，状态活跃", "观赏性强，适合安静角落"},
                {"鸟类", "虎皮鹦鹉", "羽毛状态良好，已适应手养", "好奇心强，会简单互动"},
                {"爬宠", "草龟", "进食稳定，背甲完整", "安静耐看，需要温控"},
                {"异宠", "蜜袋鼯", "精神状态稳定，夜间活跃", "需要经验家庭和固定互动"},
                {"用品", "猫爬架", "九成新，结构稳固", "适合小户型，支持自提"}
        };
        String[] cities = {
                "上海市 上海市 浦东新区",
                "浙江省 杭州市 西湖区",
                "江苏省 南京市 玄武区",
                "广东省 深圳市 南山区",
                "四川省 成都市 武侯区",
                "湖北省 武汉市 武昌区"
        };
        String[] names = {"团子", "可乐", "雪球", "蓝宝", "小满", "栗子", "奶盖", "豆包"};
        String[] statuses = {"在售", "可领养", "可互换", "已预订", "暂不开放", "在售"};

        for (int i = 0; i < profiles.length; i++) {
            for (int j = 0; j < cities.length; j++) {
                String[] profile = profiles[i];
                String name = names[(i + j) % names.length] + categorySuffix(profile[0], j);
                String age = ageFor(profile[0], j);
                BigDecimal price = priceFor(profile[0], statuses[j]);
                pets.save(pet(name, profile[0], profile[1], age, cities[j], statuses[j], profile[2], profile[3], price));
            }
        }
    }

    private void seedLaunchPosts(MarketPostRepository posts) {
        String[] types = {"售卖", "领养", "互换", "闲置", "求助", "寄养", "寻宠", "相亲配种"};
        String[] categories = {"猫咪", "狗狗", "小宠", "水族", "鸟类", "爬宠", "异宠", "用品"};
        String[] cities = {
                "上海市 上海市 浦东新区",
                "浙江省 杭州市 西湖区",
                "江苏省 苏州市 姑苏区",
                "广东省 深圳市 南山区",
                "四川省 成都市 武侯区",
                "湖北省 武汉市 武昌区",
                "北京市 北京市 朝阳区",
                "重庆市 重庆市 渝中区"
        };
        for (int i = 0; i < categories.length; i++) {
            for (int j = 0; j < types.length; j++) {
                String category = categories[i];
                String type = types[j];
                posts.save(post(postTitle(type, category, i + j), type, category, cities[(i + j) % cities.length],
                        postDescription(type, category, cities[(i + j) % cities.length])));
            }
        }
    }

    private void seedLaunchMoments(MomentRepository moments) {
        String[] authors = {"林小满", "阿舟", "南栀", "橙子妈", "周予安", "闻星", "小北", "若竹"};
        String[] categories = {"猫咪", "狗狗", "小宠", "水族", "鸟类", "爬宠", "异宠", "用品"};
        String[] petNames = {"团子", "可乐", "雪球", "蓝宝", "栗子", "奶盖", "豆包", "小满"};
        String[] fragments = {
                "今天状态很好，主动靠近镜头，拍照时一点也不紧张。",
                "新环境适应得比预期快，吃饭、喝水和休息都很规律。",
                "刚做完清洁和整理，生活区舒服了很多，看起来也更放松。",
                "第一次和邻居家的小伙伴见面，保持了礼貌距离但很好奇。",
                "晚上互动时间明显更积极，最喜欢的小零食依然很有用。",
                "记录一下成长变化，毛色、精神和胃口都保持得不错。"
        };
        String[] cities = {
                "上海市 上海市 浦东新区",
                "浙江省 杭州市 西湖区",
                "江苏省 南京市 玄武区",
                "广东省 深圳市 南山区",
                "四川省 成都市 武侯区",
                "湖北省 武汉市 武昌区"
        };

        for (int i = 0; i < categories.length; i++) {
            for (int j = 0; j < fragments.length; j++) {
                Moment moment = moment(authors[(i + j) % authors.length], petNames[(i * 2 + j) % petNames.length],
                        categories[i], fragments[j], 8 + i * 5 + j);
                moment.setCity(cities[j % cities.length]);
                moments.save(moment);
            }
        }
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
        post.setPrice(priceFor(category, type));
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

    private String categorySuffix(String category, int index) {
        String[] suffixes = {"一号", "二号", "三号", "四号", "五号", "六号"};
        if ("用品".equals(category)) {
            return "装备" + suffixes[index % suffixes.length];
        }
        if ("水族".equals(category)) {
            return "鱼" + suffixes[index % suffixes.length];
        }
        return suffixes[index % suffixes.length];
    }

    private String ageFor(String category, int index) {
        if ("用品".equals(category)) {
            return "使用" + (index + 1) + "个月";
        }
        if ("水族".equals(category)) {
            return (index + 3) + "个月";
        }
        if ("爬宠".equals(category) || "异宠".equals(category)) {
            return (index + 1) + "岁";
        }
        return (index + 5) + "个月";
    }

    private BigDecimal priceFor(String category, String statusOrType) {
        if ("可领养".equals(statusOrType) || "领养".equals(statusOrType) || "互换".equals(statusOrType) || "求助".equals(statusOrType) || "寻宠".equals(statusOrType)) {
            return BigDecimal.ZERO;
        }
        if ("用品".equals(category)) {
            return new BigDecimal("168");
        }
        if ("水族".equals(category) || "小宠".equals(category)) {
            return new BigDecimal("88");
        }
        if ("爬宠".equals(category)) {
            return new BigDecimal("128");
        }
        if ("异宠".equals(category)) {
            return new BigDecimal("680");
        }
        if ("狗狗".equals(category)) {
            return new BigDecimal("1200");
        }
        return new BigDecimal("980");
    }

    private String postTitle(String type, String category, int offset) {
        if ("寻宠".equals(type)) {
            return "寻宠互助：" + category + "走失线索征集";
        }
        if ("寄养".equals(type)) {
            return category + "同城短期寄养互助";
        }
        if ("闲置".equals(type)) {
            return category + "相关用品闲置转让";
        }
        if ("相亲配种".equals(type)) {
            return category + "健康档案齐全，寻找合适伙伴";
        }
        String[] prefixes = {"靠谱同城", "家庭自养", "周末可看", "资料齐全"};
        return prefixes[offset % prefixes.length] + category + type + "信息";
    }

    private String postDescription(String type, String category, String city) {
        if ("求助".equals(type)) {
            return city + "附近需要有经验的朋友提供照护建议，优先站内私信沟通。";
        }
        if ("寻宠".equals(type)) {
            return city + "附近走失，性格亲人，已整理照片和特征，希望同城用户帮忙留意。";
        }
        if ("寄养".equals(type)) {
            return "短期出差需要寄养互助，希望对方有稳定住所和基础照护经验。";
        }
        if ("闲置".equals(type)) {
            return "自用闲置，清洁后保存良好，适合同城自提，细节可站内私信。";
        }
        return "家庭自养" + category + "相关信息，健康记录清晰，支持先沟通再线下确认。";
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
