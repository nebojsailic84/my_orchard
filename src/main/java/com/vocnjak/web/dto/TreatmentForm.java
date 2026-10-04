package com.vocnjak.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class TreatmentForm {

    @NotBlank
    @Size(max = 150)
    private String productName;

    @Size(max = 100)
    private String rate;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate applicationDate;

    @Min(0)
    private Integer harvestIntervalDays;

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
