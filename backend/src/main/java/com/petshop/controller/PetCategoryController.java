package com.petshop.controller;

import com.petshop.model.PetCategory;
import com.petshop.repository.PetCategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

