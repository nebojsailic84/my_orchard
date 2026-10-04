package com.vocnjak.repository;

import com.vocnjak.domain.TreatmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreatmentRecordRepository extends JpaRepository<TreatmentRecord, Long> {

    List<TreatmentRecord> findByTreeIdOrderByApplicationDateDesc(Long treeId);
}
