package com.petshop.controller;

import com.petshop.model.PetCategory;
import com.petshop.repository.PetCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class PetCategoryController {
    private final PetCategoryRepository repository;

    public PetCategoryController(PetCategoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<PetCategory> list() {
        return repository.findAll();
    }

    @PostMapping
    public PetCategory create(@RequestBody PetCategory category) {
        validate(category);
        category.setId(null);
        return repository.save(category);
    }

    @PutMapping("/{id}")
    public PetCategory update(@PathVariable Long id, @RequestBody PetCategory category) {
        PetCategory existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));
        validate(category);
        existing.setName(category.getName().trim());
        existing.setDescription(safe(category.getDescription()));
        existing.setImageUrl(safe(category.getImageUrl()));
        existing.setTags(safe(category.getTags()));
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        PetCategory existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));
        repository.delete(existing);
    }

    private void validate(PetCategory category) {
        if (category == null || safe(category.getName()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请填写分类名称");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
