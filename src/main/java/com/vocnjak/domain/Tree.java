package com.vocnjak.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tree",
        uniqueConstraints = @UniqueConstraint(columnNames = {"orchard_id", "row_index", "col_index"})
)
public class Tree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orchard_id", nullable = false)
    private Orchard orchard;

    @Column(name = "row_index", nullable = false)
    @Min(0)
    private int rowIndex;

    @Column(name = "col_index", nullable = false)
    @Min(0)
    private int colIndex;

    private boolean emptySpot;

    // --- Tree Details ---

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

    // --- Inputs and Safety ---

    @OneToMany(mappedBy = "tree", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TreatmentRecord> treatmentRecords = new ArrayList<>();

    // --- Notes and Photo ---

    @Column(length = 2000)
    @Size(max = 2000)
    private String notes;

    private String imageContentType;

    protected Tree() {
    }

    public Tree(Orchard orchard, int rowIndex, int colIndex) {
        this.orchard = orchard;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }

    public Long getId() {
        return id;
    }

    public boolean isEmptySpot() {
        return emptySpot;
    }

    public void setEmptySpot(boolean emptySpot) {
        this.emptySpot = emptySpot;
    }

    public Orchard getOrchard() {
        return orchard;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

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

    public List<TreatmentRecord> getTreatmentRecords() {
        return treatmentRecords;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public boolean hasImage() {
        return imageContentType != null;
    }

    public String getSpeciesImageFileName() {
        return FruitSpeciesCatalog.imageFileNameFor(species);
    }
}
