package com.vocnjak.service;

import com.vocnjak.domain.Tree;
import com.vocnjak.domain.TreatmentRecord;
import com.vocnjak.repository.TreatmentRecordRepository;
import com.vocnjak.service.exception.NotFoundException;
import com.vocnjak.web.dto.TreatmentForm;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TreatmentService {

    private final TreatmentRecordRepository treatmentRecordRepository;
    private final TreeService treeService;

    public TreatmentService(TreatmentRecordRepository treatmentRecordRepository, TreeService treeService) {
        this.treatmentRecordRepository = treatmentRecordRepository;
        this.treeService = treeService;
    }

    public TreatmentRecord addTreatment(Long treeId, TreatmentForm form) {
        Tree tree = treeService.getTree(treeId);
        TreatmentRecord record = new TreatmentRecord(tree);
        record.setProductName(form.getProductName());
        record.setRate(form.getRate());
        record.setApplicationDate(form.getApplicationDate());
        record.setHarvestIntervalDays(form.getHarvestIntervalDays());
        return treatmentRecordRepository.save(record);
    }

    public List<TreatmentRecord> listTreatments(Long treeId) {
        return treatmentRecordRepository.findByTreeIdOrderByApplicationDateDesc(treeId);
    }

    public TreatmentRecord getTreatment(Long treatmentId) {
        return treatmentRecordRepository.findById(treatmentId)
                .orElseThrow(() -> new NotFoundException("error.treatmentNotFound", treatmentId));
    }

    public void deleteTreatment(Long treatmentId) {
        treatmentRecordRepository.deleteById(treatmentId);
    }
}
