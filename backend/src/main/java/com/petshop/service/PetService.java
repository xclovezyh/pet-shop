package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.pet.PetResponse;
import com.petshop.model.Pet;
import com.petshop.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetService {
    private final PetRepository repository;

    public PetService(PetRepository repository) {
        this.repository = repository;
    }

    public List<PetResponse> list() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PetResponse detail(Long id) {
        Pet pet = repository.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.PET_NOT_FOUND));
        return toResponse(pet);
    }

    private PetResponse toResponse(Pet pet) {
        PetResponse response = new PetResponse();
        response.setId(pet.getId());
        response.setName(pet.getName());
        response.setCategory(pet.getCategory());
        response.setBreed(pet.getBreed());
        response.setAge(pet.getAge());
        response.setCity(pet.getCity());
        response.setCityCode(pet.getCityCode());
        response.setStatus(pet.getStatus());
        response.setPrice(pet.getPrice());
        response.setImageUrl(pet.getImageUrl());
        response.setImageUrls(pet.getImageUrls());
        response.setHealthInfo(pet.getHealthInfo());
        response.setHealthRecords(pet.getHealthRecords());
        response.setPersonality(pet.getPersonality());
        response.setOwnerName(pet.getOwnerName());
        response.setGender(pet.getGender());
        response.setAgeRange(pet.getAgeRange());
        response.setVaccinated(pet.getVaccinated());
        response.setDewormed(pet.getDewormed());
        response.setNeutered(pet.getNeutered());
        response.setCareNotes(pet.getCareNotes());
        return response;
    }
}
