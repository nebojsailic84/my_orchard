package com.vocnjak.service;

import com.vocnjak.domain.Orchard;
import com.vocnjak.domain.Tree;
import com.vocnjak.repository.OrchardRepository;
import com.vocnjak.repository.TreeRepository;
import com.vocnjak.service.exception.CellOccupiedException;
import com.vocnjak.service.exception.InvalidImageException;
import com.vocnjak.service.exception.NotFoundException;
import com.vocnjak.service.exception.OutOfBoundsException;
import com.vocnjak.web.dto.TreeForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreeServiceTest {

    @Mock
    private TreeRepository treeRepository;

    @Mock
    private OrchardRepository orchardRepository;

    @TempDir
    private Path tempDir;

    private TreeService treeService;

    private Orchard orchard;

    @BeforeEach
    void setUp() {
        OrchardService orchardService = new OrchardService(orchardRepository);
        treeService = new TreeService(treeRepository, orchardService, tempDir.toString());
        orchard = new Orchard("Backyard", 4, 5);
    }

    @Test
    void addTree_savesTree_whenCellIsFree() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(treeRepository.existsByOrchardIdAndRowIndexAndColIndex(1L, 1, 2)).thenReturn(false);
        when(treeRepository.save(any(Tree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TreeForm form = treeForm("Apple", "Golden Delicious");
        Tree tree = treeService.addTree(1L, 1, 2, form, null);

        assertThat(tree.getOrchard()).isEqualTo(orchard);
        assertThat(tree.getRowIndex()).isEqualTo(1);
        assertThat(tree.getColIndex()).isEqualTo(2);
        assertThat(tree.getSpecies()).isEqualTo("Apple");
        assertThat(tree.getVariety()).isEqualTo("Golden Delicious");
        assertThat(tree.hasImage()).isFalse();
    }

    @Test
    void addTree_throwsCellOccupiedException_whenCellAlreadyHasTree() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(treeRepository.existsByOrchardIdAndRowIndexAndColIndex(1L, 1, 2)).thenReturn(true);

        assertThatThrownBy(() -> treeService.addTree(1L, 1, 2, treeForm("Pear", null), null))
                .isInstanceOf(CellOccupiedException.class);

        verify(treeRepository, never()).save(any());
    }

    @Test
    void addTree_throwsOutOfBoundsException_whenRowOrColOutsideGrid() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));

        assertThatThrownBy(() -> treeService.addTree(1L, orchard.getRows(), 0, treeForm("Pear", null), null))
                .isInstanceOf(OutOfBoundsException.class);

        verify(treeRepository, never()).save(any());
    }

    @Test
    void addTree_throwsNotFoundException_whenOrchardDoesNotExist() {
        when(orchardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> treeService.addTree(99L, 0, 0, treeForm("Pear", null), null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addTree_storesImage_whenValidImageProvided() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(treeRepository.existsByOrchardIdAndRowIndexAndColIndex(1L, 0, 0)).thenReturn(false);
        when(treeRepository.save(any(Tree.class))).thenAnswer(invocation -> {
            Tree saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                setId(saved, 42L);
            }
            return saved;
        });

        MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "fake-bytes".getBytes());
        Tree tree = treeService.addTree(1L, 0, 0, treeForm("Apple", null), image);
        when(treeRepository.findById(42L)).thenReturn(Optional.of(tree));

        assertThat(tree.hasImage()).isTrue();
        assertThat(tree.getImageContentType()).isEqualTo("image/jpeg");
        assertThat(treeService.loadImage(42L)).isEqualTo("fake-bytes".getBytes());
    }

    @Test
    void addTree_throwsInvalidImageException_whenContentTypeUnsupported() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(treeRepository.existsByOrchardIdAndRowIndexAndColIndex(1L, 0, 0)).thenReturn(false);
        when(treeRepository.save(any(Tree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile notAnImage = new MockMultipartFile("image", "notes.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> treeService.addTree(1L, 0, 0, treeForm("Apple", null), notAnImage))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void deleteTree_removesTree_whenExists() {
        treeService.deleteTree(5L);

        verify(treeRepository).deleteById(5L);
    }

    @Test
    void markEmpty_createsPlaceholder_whenCellFree() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(treeRepository.existsByOrchardIdAndRowIndexAndColIndex(1L, 2, 3)).thenReturn(false);
        when(treeRepository.save(any(Tree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tree tree = treeService.markEmpty(1L, 2, 3);

        assertThat(tree.isEmptySpot()).isTrue();
        assertThat(tree.getRowIndex()).isEqualTo(2);
        assertThat(tree.getColIndex()).isEqualTo(3);
        assertThat(tree.getSpecies()).isNull();
    }

    @Test
    void markEmpty_throwsCellOccupiedException_whenOccupied() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(treeRepository.existsByOrchardIdAndRowIndexAndColIndex(1L, 2, 3)).thenReturn(true);

        assertThatThrownBy(() -> treeService.markEmpty(1L, 2, 3))
                .isInstanceOf(CellOccupiedException.class);

        verify(treeRepository, never()).save(any());
    }

    @Test
    void addTree_mapsAllNewTreeDetailFields() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(treeRepository.existsByOrchardIdAndRowIndexAndColIndex(1L, 0, 0)).thenReturn(false);
        when(treeRepository.save(any(Tree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TreeForm form = treeForm("Apple", "Golden Delicious");
        form.setRootstock("M9");
        form.setPlantingMonth(4);
        form.setPlantingYear(2023);
        form.setBudBreakMonth(3);
        form.setBloomMonth(4);
        form.setFruitSetMonth(5);
        form.setHarvestMonth(9);
        form.setPests("Aphids in spring");
        form.setDiseases("Mild scab");
        form.setWeatherDamage("Hail bruising on a few fruits");
        form.setNutrientSymptoms("Slight nitrogen deficiency");
        form.setTreatmentsUsed("Copper spray in March");

        Tree tree = treeService.addTree(1L, 0, 0, form, null);

        assertThat(tree.getRootstock()).isEqualTo("M9");
        assertThat(tree.getPlantingMonth()).isEqualTo(4);
        assertThat(tree.getPlantingYear()).isEqualTo(2023);
        assertThat(tree.getBudBreakMonth()).isEqualTo(3);
        assertThat(tree.getBloomMonth()).isEqualTo(4);
        assertThat(tree.getFruitSetMonth()).isEqualTo(5);
        assertThat(tree.getHarvestMonth()).isEqualTo(9);
        assertThat(tree.getPests()).isEqualTo("Aphids in spring");
        assertThat(tree.getDiseases()).isEqualTo("Mild scab");
        assertThat(tree.getWeatherDamage()).isEqualTo("Hail bruising on a few fruits");
        assertThat(tree.getNutrientSymptoms()).isEqualTo("Slight nitrogen deficiency");
        assertThat(tree.getTreatmentsUsed()).isEqualTo("Copper spray in March");
    }

    @Test
    void copyTree_copiesFieldsIntoNewCell_whenCellFree() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(treeRepository.existsByOrchardIdAndRowIndexAndColIndex(1L, 2, 2)).thenReturn(false);
        when(treeRepository.save(any(Tree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tree source = new Tree(orchard, 1, 1);
        source.setSpecies("Apple");
        source.setVariety("Golden Delicious");
        source.setRootstock("M9");
        source.setPlantingYear(2023);
        setId(source, 7L);
        when(treeRepository.findById(7L)).thenReturn(Optional.of(source));

        Tree copy = treeService.copyTree(1L, 7L, 2, 2);

        assertThat(copy.getRowIndex()).isEqualTo(2);
        assertThat(copy.getColIndex()).isEqualTo(2);
        assertThat(copy.getSpecies()).isEqualTo("Apple");
        assertThat(copy.getVariety()).isEqualTo("Golden Delicious");
        assertThat(copy.getRootstock()).isEqualTo("M9");
        assertThat(copy.getPlantingYear()).isEqualTo(2023);
        assertThat(copy.hasImage()).isFalse();
    }

    @Test
    void copyTree_throwsCellOccupiedException_whenOccupied() {
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(treeRepository.existsByOrchardIdAndRowIndexAndColIndex(1L, 2, 2)).thenReturn(true);

        assertThatThrownBy(() -> treeService.copyTree(1L, 7L, 2, 2))
                .isInstanceOf(CellOccupiedException.class);

        verify(treeRepository, never()).save(any());
    }

    private TreeForm treeForm(String species, String variety) {
        TreeForm form = new TreeForm();
        form.setSpecies(species);
        form.setVariety(variety);
        return form;
    }

    private void setId(Tree tree, Long id) {
        try {
            var field = Tree.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(tree, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
