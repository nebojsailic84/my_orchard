package com.vocnjak.repository;

import com.vocnjak.domain.Orchard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrchardRepository extends JpaRepository<Orchard, Long> {
}