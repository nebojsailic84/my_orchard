package com.vocnjak.service;

import com.vocnjak.domain.Orchard;
import com.vocnjak.repository.OrchardRepository;
import com.vocnjak.service.exception.NotFoundException;
import com.vocnjak.service.exception.OutOfBoundsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrchardService {

    private static final int MAX_GRID_SIZE = 100;

    private final OrchardRepository orchardRepository;

    public OrchardService(OrchardRepository orchardRepository) {
        this.orchardRepository = orchardRepository;
    }

    public Orchard createOrchard(String name, int rows, int columns, String orientationNote) {
        Orchard orchard = new Orchard(name, rows, columns);
        orchard.setOrientationNote(orientationNote);
        return orchardRepository.save(orchard);
    }

    public Orchard addRow(Long id) {
        Orchard orchard = getOrchard(id);
        if (orchard.getRows() >= MAX_GRID_SIZE) {
            throw new OutOfBoundsException("error.maxRows", MAX_GRID_SIZE);
        }
        orchard.setRows(orchard.getRows() + 1);
        return orchardRepository.save(orchard);
    }

    public Orchard addColumn(Long id) {
        Orchard orchard = getOrchard(id);
        if (orchard.getColumns() >= MAX_GRID_SIZE) {
            throw new OutOfBoundsException("error.maxColumns", MAX_GRID_SIZE);
        }
        orchard.setColumns(orchard.getColumns() + 1);
        return orchardRepository.save(orchard);
    }

    public Orchard getOrchard(Long id) {
        return orchardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.orchardNotFound", id));
    }

    public List<Orchard> listOrchards() {
        return orchardRepository.findAll();
    }

    public void deleteOrchard(Long id) {
        orchardRepository.deleteById(id);
    }
}