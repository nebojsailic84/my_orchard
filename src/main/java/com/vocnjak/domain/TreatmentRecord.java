package com.vocnjak.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
public class TreatmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tree_id", nullable = false)
    private Tree tree;

    @NotBlank
    @Size(max = 150)
    private String productName;

    @Size(max = 100)
    private String rate;

    @NotNull
    private LocalDate applicationDate;

    @Min(0)
    private Integer harvestIntervalDays;

    protected TreatmentRecord() {
    }

    public TreatmentRecord(Tree tree) {
        this.tree = tree;
    }

    public Long getId() {
        return id;
    }

    public Tree getTree() {
        return tree;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public Integer getHarvestIntervalDays() {
        return harvestIntervalDays;
    }

    public void setHarvestIntervalDays(Integer harvestIntervalDays) {
        this.harvestIntervalDays = harvestIntervalDays;
    }
}
