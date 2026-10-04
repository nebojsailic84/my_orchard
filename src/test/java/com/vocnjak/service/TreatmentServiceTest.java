package com.vocnjak.service;

import com.vocnjak.domain.Orchard;
import com.vocnjak.domain.Tree;
import com.vocnjak.domain.TreatmentRecord;
import com.vocnjak.repository.OrchardRepository;
import com.vocnjak.repository.TreatmentRecordRepository;
import com.vocnjak.repository.TreeRepository;
import com.vocnjak.service.exception.NotFoundException;
import com.vocnjak.web.dto.TreatmentForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceTest {

    @Mock
    private TreatmentRecordRepository treatmentRecordRepository;

    @Mock
    private TreeRepository treeRepository;

    @Mock
    private OrchardRepository orchardRepository;

    @TempDir
    private Path tempDir;

    private TreatmentService treatmentService;

    @BeforeEach
    void setUp() {
        OrchardService orchardService = new OrchardService(orchardRepository);
        TreeService treeService = new TreeService(treeRepository, orchardService, tempDir.toString());
        treatmentService = new TreatmentService(treatmentRecordRepository, treeService);
    }

    @Test
    void addTreatment_savesRecord_whenTreeExists() {
        Tree tree = new Tree(new Orchard("Backyard", 4, 5), 0, 0);
        when(treeRepository.findById(7L)).thenReturn(Optional.of(tree));
        when(treatmentRecordRepository.save(any(TreatmentRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TreatmentForm form = new TreatmentForm();
        form.setProductName("Copper fungicide");
        form.setRate("30ml / 10L");
        form.setApplicationDate(LocalDate.of(2024, 3, 15));
        form.setHarvestIntervalDays(14);

        TreatmentRecord record = treatmentService.addTreatment(7L, form);

        assertThat(record.getTree()).isEqualTo(tree);
        assertThat(record.getProductName()).isEqualTo("Copper fungicide");
        assertThat(record.getRate()).isEqualTo("30ml / 10L");
        assertThat(record.getApplicationDate()).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(record.getHarvestIntervalDays()).isEqualTo(14);
    }

    @Test
    void addTreatment_throwsNotFoundException_whenTreeMissing() {
        when(treeRepository.findById(99L)).thenReturn(Optional.empty());

        TreatmentForm form = new TreatmentForm();
        form.setProductName("Copper fungicide");
        form.setApplicationDate(LocalDate.now());

        assertThatThrownBy(() -> treatmentService.addTreatment(99L, form))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteTreatment_removesRecord() {
        treatmentService.deleteTreatment(3L);

        verify(treatmentRecordRepository).deleteById(3L);
    }
}
