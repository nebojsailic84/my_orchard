package com.vocnjak.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrchardCreateForm {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Min(1)
    @Max(100)
    private int rows;

    @Min(1)
    @Max(100)
    private int columns;

    @Size(max = 500)
    private String orientationNote;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String getOrientationNote() {
        return orientationNote;
    }

    public void setOrientationNote(String orientationNote) {
        this.orientationNote = orientationNote;
    }
}