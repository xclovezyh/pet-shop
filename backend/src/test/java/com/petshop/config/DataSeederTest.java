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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.CommandLineRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;

class DataSeederTest {
    @Test
    void seedDataShouldCreateLaunchSizedColdStartContent() throws Exception {
        PetCategoryRepository categories = mock(PetCategoryRepository.class);
        PetRepository pets = mock(PetRepository.class);
        MarketPostRepository posts = mock(MarketPostRepository.class);
        MomentRepository moments = mock(MomentRepository.class);
        ReferenceOptionRepository referenceOptions = mock(ReferenceOptionRepository.class);

        when(categories.count()).thenReturn(0L);
        when(categories.findAll()).thenReturn(Collections.emptyList());
        when(referenceOptions.count()).thenReturn(0L);
        when(referenceOptions.findAll()).thenReturn(Collections.emptyList());
        when(referenceOptions.findByOptionTypeOrderBySortOrderAscIdAsc(any())).thenReturn(Collections.emptyList());
        when(pets.count()).thenReturn(0L);
        when(posts.count()).thenReturn(0L);
        when(moments.count()).thenReturn(0L);
        when(categories.save(any(PetCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(referenceOptions.save(any(ReferenceOption.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pets.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(posts.save(any(MarketPost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(moments.save(any(Moment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommandLineRunner runner = new DataSeeder().seedData(categories, pets, posts, moments, referenceOptions);
        runner.run();

        ArgumentCaptor<Pet> petCaptor = ArgumentCaptor.forClass(Pet.class);
        ArgumentCaptor<MarketPost> postCaptor = ArgumentCaptor.forClass(MarketPost.class);
        ArgumentCaptor<Moment> momentCaptor = ArgumentCaptor.forClass(Moment.class);
        verify(pets, atLeast(48)).save(petCaptor.capture());
        verify(posts, atLeast(64)).save(postCaptor.capture());
        verify(moments, atLeast(48)).save(momentCaptor.capture());

        assertThat(petCaptor.getAllValues())
                .extracting(Pet::getCategory)
                .contains("猫咪", "狗狗", "小宠", "水族", "鸟类", "爬宠", "异宠", "用品");
        assertThat(postCaptor.getAllValues())
                .extracting(MarketPost::getType)
                .contains("售卖", "领养", "互换", "闲置", "求助", "寄养", "寻宠", "相亲配种");
        assertThat(momentCaptor.getAllValues())
                .allSatisfy(moment -> {
                    assertThat(moment.getAuthor()).isNotBlank();
                    assertThat(moment.getContent()).hasSizeGreaterThan(12);
                    assertThat(moment.getImageUrls()).isNotBlank();
                });
    }
}
