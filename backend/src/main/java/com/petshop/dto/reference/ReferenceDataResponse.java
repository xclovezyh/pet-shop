package com.petshop.dto.reference;

import java.util.List;

public class ReferenceDataResponse {
    private List<RegionTreeResponse> regions;
    private List<String> postTypes;
    private List<String> petStatuses;
    private List<String> petGenders;
    private List<String> ageRanges;
    private List<String> healthRecords;
    private List<String> personalityTags;
    private List<String> serviceTags;

    public List<RegionTreeResponse> getRegions() {
        return regions;
    }

    public void setRegions(List<RegionTreeResponse> regions) {
        this.regions = regions;
    }

    public List<String> getPostTypes() {
        return postTypes;
    }

    public void setPostTypes(List<String> postTypes) {
        this.postTypes = postTypes;
    }

    public List<String> getPetStatuses() {
        return petStatuses;
    }

    public void setPetStatuses(List<String> petStatuses) {
        this.petStatuses = petStatuses;
    }

    public List<String> getPetGenders() {
        return petGenders;
    }

    public void setPetGenders(List<String> petGenders) {
        this.petGenders = petGenders;
    }

    public List<String> getAgeRanges() {
        return ageRanges;
    }

    public void setAgeRanges(List<String> ageRanges) {
        this.ageRanges = ageRanges;
    }

    public List<String> getHealthRecords() {
        return healthRecords;
    }

    public void setHealthRecords(List<String> healthRecords) {
        this.healthRecords = healthRecords;
    }

    public List<String> getPersonalityTags() {
        return personalityTags;
    }

    public void setPersonalityTags(List<String> personalityTags) {
        this.personalityTags = personalityTags;
    }

    public List<String> getServiceTags() {
        return serviceTags;
    }

    public void setServiceTags(List<String> serviceTags) {
        this.serviceTags = serviceTags;
    }
}
