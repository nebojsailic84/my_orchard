package com.vocnjak.service;

import com.vocnjak.domain.Orchard;
import com.vocnjak.domain.Tree;
import com.vocnjak.repository.TreeRepository;
import com.vocnjak.service.exception.CellOccupiedException;
import com.vocnjak.service.exception.InvalidImageException;
import com.vocnjak.service.exception.NotFoundException;
import com.vocnjak.service.exception.OutOfBoundsException;
import com.vocnjak.web.dto.TreeForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TreeService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final TreeRepository treeRepository;
    private final OrchardService orchardService;
    private final Path uploadDir;

    public TreeService(TreeRepository treeRepository,
                        OrchardService orchardService,
                        @Value("${app.upload-dir}") String uploadDir) {
        this.treeRepository = treeRepository;
        this.orchardService = orchardService;
        this.uploadDir = Path.of(uploadDir);
    }

    public Tree addTree(Long orchardId, int row, int col, TreeForm form, MultipartFile image) {
        Orchard orchard = orchardService.getOrchard(orchardId);
        requireWithinBounds(orchard, row, col);
        if (treeRepository.existsByOrchardIdAndRowIndexAndColIndex(orchardId, row, col)) {
            throw new CellOccupiedException("error.cellOccupied", row, col);
        }
        Tree tree = new Tree(orchard, row, col);
        applyForm(tree, form);
        tree = treeRepository.save(tree);
        if (image != null && !image.isEmpty()) {
            tree.setImageContentType(storeImage(tree.getId(), image));
            tree = treeRepository.save(tree);
        }
        return tree;
    }

    public Tree markEmpty(Long orchardId, int row, int col) {
        Orchard orchard = orchardService.getOrchard(orchardId);
        requireWithinBounds(orchard, row, col);
        if (treeRepository.existsByOrchardIdAndRowIndexAndColIndex(orchardId, row, col)) {
            throw new CellOccupiedException("error.cellOccupied", row, col);
        }
        Tree tree = new Tree(orchard, row, col);
        tree.setEmptySpot(true);
        return treeRepository.save(tree);
    }

    public Tree updateTree(Long treeId, TreeForm form, MultipartFile image) {
        Tree tree = getTree(treeId);
        applyForm(tree, form);
        if (image != null && !image.isEmpty()) {
            tree.setImageContentType(storeImage(treeId, image));
        }
        return treeRepository.save(tree);
    }

    public Tree getTree(Long treeId) {
        return treeRepository.findById(treeId)
                .orElseThrow(() -> new NotFoundException("error.treeNotFound", treeId));
    }

    public byte[] loadImage(Long treeId) {
        Tree tree = getTree(treeId);
        if (!tree.hasImage()) {
            throw new NotFoundException("error.treeHasNoImage", treeId);
        }
        try {
            return Files.readAllBytes(imagePath(treeId));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void deleteTree(Long treeId) {
        treeRepository.deleteById(treeId);
        deleteImageIfExists(treeId);
    }

    public List<List<Tree>> buildGrid(Orchard orchard) {
        List<List<Tree>> grid = new ArrayList<>();
        for (int row = 0; row < orchard.getRows(); row++) {
            List<Tree> rowCells = new ArrayList<>();
            for (int col = 0; col < orchard.getColumns(); col++) {
                rowCells.add(null);
            }
            grid.add(rowCells);
        }
        for (Tree tree : treeRepository.findByOrchardId(orchard.getId())) {
            grid.get(tree.getRowIndex()).set(tree.getColIndex(), tree);
        }
        return grid;
    }

    private void requireWithinBounds(Orchard orchard, int row, int col) {
        if (row < 0 || row >= orchard.getRows() || col < 0 || col >= orchard.getColumns()) {
            throw new OutOfBoundsException(
                    "error.positionOutsideGrid", row, col, orchard.getRows(), orchard.getColumns());
        }
    }

    private void applyForm(Tree tree, TreeForm form) {
        tree.setSpecies(form.getSpecies());
        tree.setVariety(form.getVariety());
        tree.setRootstock(form.getRootstock());
        tree.setPlantingMonth(form.getPlantingMonth());
        tree.setPlantingYear(form.getPlantingYear());
        tree.setBudBreakMonth(form.getBudBreakMonth());
        tree.setBloomMonth(form.getBloomMonth());
        tree.setFruitSetMonth(form.getFruitSetMonth());
        tree.setHarvestMonth(form.getHarvestMonth());
        tree.setPests(form.getPests());
        tree.setDiseases(form.getDiseases());
        tree.setWeatherDamage(form.getWeatherDamage());
        tree.setNutrientSymptoms(form.getNutrientSymptoms());
        tree.setTreatmentsUsed(form.getTreatmentsUsed());
        tree.setNotes(form.getNotes());
    }

    private String storeImage(Long treeId, MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new InvalidImageException("error.invalidImageType");
        }
        try {
            Files.createDirectories(uploadDir);
            Files.copy(image.getInputStream(), imagePath(treeId),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return contentType;
    }

    private void deleteImageIfExists(Long treeId) {
        try {
            Files.deleteIfExists(imagePath(treeId));
        } catch (IOException ignored) {
        }
    }

    private Path imagePath(Long treeId) {
        return uploadDir.resolve(String.valueOf(treeId));
    }
}
