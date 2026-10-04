package com.vocnjak.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class TreeForm {

    // --- Tree Details ---

    @NotBlank
    @Size(max = 100)
    private String species;

    @Size(max = 100)
    private String variety;

    @Size(max = 100)
    private String rootstock;

    @Min(1)
    @Max(12)
    private Integer plantingMonth;

    @Min(1900)
    @Max(2100)
    private Integer plantingYear;

    // --- Seasonal Development ---

    @Min(1)
    @Max(12)
    private Integer budBreakMonth;

    @Min(1)
    @Max(12)
    private Integer bloomMonth;

    @Min(1)
    @Max(12)
    private Integer fruitSetMonth;

    @Min(1)
    @Max(12)
    private Integer harvestMonth;

    // --- Problems Observed ---

    @Size(max = 500)
    private String pests;

    @Size(max = 500)
    private String diseases;

    @Size(max = 500)
    private String weatherDamage;

    @Size(max = 500)
    private String nutrientSymptoms;

    @Size(max = 500)
    private String treatmentsUsed;

    // --- Notes and Photo ---

    @Size(max = 2000)
    private String notes;

    private MultipartFile image;

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getRootstock() {
        return rootstock;
    }

    public void setRootstock(String rootstock) {
        this.rootstock = rootstock;
    }

    public Integer getPlantingMonth() {
        return plantingMonth;
    }

    public void setPlantingMonth(Integer plantingMonth) {
        this.plantingMonth = plantingMonth;
    }

    public Integer getPlantingYear() {
        return plantingYear;
    }

    public void setPlantingYear(Integer plantingYear) {
        this.plantingYear = plantingYear;
    }

    public Integer getBudBreakMonth() {
        return budBreakMonth;
    }

    public void setBudBreakMonth(Integer budBreakMonth) {
        this.budBreakMonth = budBreakMonth;
    }

    public Integer getBloomMonth() {
        return bloomMonth;
    }

    public void setBloomMonth(Integer bloomMonth) {
        this.bloomMonth = bloomMonth;
    }

    public Integer getFruitSetMonth() {
        return fruitSetMonth;
    }

    public void setFruitSetMonth(Integer fruitSetMonth) {
        this.fruitSetMonth = fruitSetMonth;
    }

    public Integer getHarvestMonth() {
        return harvestMonth;
    }

    public void setHarvestMonth(Integer harvestMonth) {
        this.harvestMonth = harvestMonth;
    }

    public String getPests() {
        return pests;
    }

    public void setPests(String pests) {
        this.pests = pests;
    }

    public String getDiseases() {
        return diseases;
    }

    public void setDiseases(String diseases) {
        this.diseases = diseases;
    }

    public String getWeatherDamage() {
        return weatherDamage;
    }

    public void setWeatherDamage(String weatherDamage) {
        this.weatherDamage = weatherDamage;
    }

    public String getNutrientSymptoms() {
        return nutrientSymptoms;
    }

    public void setNutrientSymptoms(String nutrientSymptoms) {
        this.nutrientSymptoms = nutrientSymptoms;
    }

    public String getTreatmentsUsed() {
        return treatmentsUsed;
    }

    public void setTreatmentsUsed(String treatmentsUsed) {
        this.treatmentsUsed = treatmentsUsed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}
