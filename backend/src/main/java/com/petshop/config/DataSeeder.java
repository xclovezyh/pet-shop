package com.petshop.config;

import com.petshop.model.MarketPost;
import com.petshop.model.Moment;
import com.petshop.model.Pet;
import com.petshop.model.PetCategory;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.MomentRepository;
import com.petshop.repository.PetCategoryRepository;
import com.petshop.repository.PetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(PetCategoryRepository categories, PetRepository pets,
                               MarketPostRepository posts, MomentRepository moments) {
        return args -> {
            boolean hasReadableSeed = categories.findAll().stream()
                    .anyMatch(category -> "猫咪".equals(category.getName()));
            if (categories.count() > 0 && !hasReadableSeed) {
                moments.deleteAll();
                posts.deleteAll();
                pets.deleteAll();
                categories.deleteAll();
            }

            if (categories.count() == 0) {
                categories.save(category("猫咪", "温顺亲人，适合公寓和家庭陪伴。", "新手友好,安静,陪伴型"));
                categories.save(category("狗狗", "活泼忠诚，需要规律运动和训练。", "互动强,需要遛弯,家庭型"));
                categories.save(category("小宠", "仓鼠、兔子、龙猫等，占地小但需要细心照顾。", "空间小,易观察,轻陪伴"));
                categories.save(category("水族", "观赏性强，适合打造安静的家居角落。", "观赏型,低噪音,设备需求"));
                categories.save(category("用品", "宠物食品、玩具、猫爬架、牵引绳等闲置或售卖用品。", "闲置交易,日常消耗,养宠装备"));
            }

            if (pets.count() == 0) {
                pets.save(pet("团子", "猫咪", "英短银渐层", "8个月", "上海市 上海市 浦东新区", "在售", "疫苗齐全，已驱虫", "安静亲人，喜欢陪睡", new BigDecimal("1800")));
                pets.save(pet("可乐", "狗狗", "柯基", "1岁", "浙江省 杭州市 西湖区", "可互换", "体检正常，精力充沛", "活泼黏人，会坐下握手", BigDecimal.ZERO));
                pets.save(pet("雪球", "小宠", "侏儒兔", "5个月", "江苏省 南京市 玄武区", "可领养", "健康，饮食稳定", "胆小但熟悉后很亲近", BigDecimal.ZERO));
            }

            if (posts.count() == 0) {
                posts.save(post("想给柯基找同城互换寄养伙伴", "互换", "狗狗", "浙江省 杭州市 西湖区", "工作日偶尔出差，希望找同城稳定互助家庭。"));
                posts.save(post("英短银渐层找新家", "售卖", "猫咪", "上海市 上海市 浦东新区", "自家猫宝宝，疫苗驱虫记录完整，可预约看猫。"));
                posts.save(post("闲置猫爬架转让", "闲置", "用品", "江苏省 苏州市 姑苏区", "九成新，适合小户型，支持站内私信沟通。"));
            }

            if (moments.count() == 0) {
                moments.save(moment("林小满", "团子", "猫咪", "今天第一次学会自己开零食罐，已经开始怀疑家里的安全系统。", 28));
                moments.save(moment("阿舟", "可乐", "狗狗", "雨停后去公园跑了两圈，回来直接睡成一张毯子。", 16));
            }
        };
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
        pet.setImageUrl("");
        return pet;
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
        post.setImageUrl("");
        post.setCreatedAt(LocalDateTime.now());
        return post;
    }

    private Moment moment(String author, String petName, String category, String content, int likes) {
        Moment moment = new Moment();
        moment.setAuthor(author);
        moment.setPetName(petName);
        moment.setCategory(category);
        moment.setContent(content);
        moment.setLikes(likes);
        moment.setImageUrl("");
        moment.setCreatedAt(LocalDateTime.now());
        return moment;
    }
}
