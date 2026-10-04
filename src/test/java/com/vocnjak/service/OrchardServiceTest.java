package com.vocnjak.service;

import com.vocnjak.domain.Orchard;
import com.vocnjak.repository.OrchardRepository;
import com.vocnjak.service.exception.NotFoundException;
import com.vocnjak.service.exception.OutOfBoundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrchardServiceTest {

    @Mock
    private OrchardRepository orchardRepository;

    private OrchardService orchardService;

    @BeforeEach
    void setUp() {
        orchardService = new OrchardService(orchardRepository);
    }

    @Test
    void createOrchard_savesWithGivenDimensions() {
        when(orchardRepository.save(any(Orchard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Orchard orchard = orchardService.createOrchard("Backyard", 4, 5, "Row 1 starts at the gate");

        assertThat(orchard.getName()).isEqualTo("Backyard");
        assertThat(orchard.getRows()).isEqualTo(4);
        assertThat(orchard.getColumns()).isEqualTo(5);
        assertThat(orchard.getOrientationNote()).isEqualTo("Row 1 starts at the gate");
    }

    @Test
    void getOrchard_throwsNotFoundException_whenMissing() {
        when(orchardRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orchardService.getOrchard(42L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addRow_incrementsRowCount() {
        Orchard orchard = new Orchard("Backyard", 4, 5);
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(orchardRepository.save(any(Orchard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Orchard updated = orchardService.addRow(1L);

        assertThat(updated.getRows()).isEqualTo(5);
    }

    @Test
    void addRow_throwsOutOfBoundsException_whenAtMaxSize() {
        Orchard orchard = new Orchard("Backyard", 100, 5);
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));

        assertThatThrownBy(() -> orchardService.addRow(1L))
                .isInstanceOf(OutOfBoundsException.class);
    }

    @Test
    void addColumn_incrementsColumnCount() {
        Orchard orchard = new Orchard("Backyard", 4, 5);
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));
        when(orchardRepository.save(any(Orchard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Orchard updated = orchardService.addColumn(1L);

        assertThat(updated.getColumns()).isEqualTo(6);
    }

    @Test
    void addColumn_throwsOutOfBoundsException_whenAtMaxSize() {
        Orchard orchard = new Orchard("Backyard", 4, 100);
        when(orchardRepository.findById(1L)).thenReturn(Optional.of(orchard));

        assertThatThrownBy(() -> orchardService.addColumn(1L))
                .isInstanceOf(OutOfBoundsException.class);
    }
}