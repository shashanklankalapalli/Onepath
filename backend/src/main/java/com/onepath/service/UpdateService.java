package com.onepath.service;

import com.onepath.entity.Update;
import com.onepath.repository.UpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UpdateService {

    private final UpdateRepository updateRepository;

    public UpdateService(UpdateRepository updateRepository) {
        this.updateRepository = updateRepository;
    }

    @Transactional(readOnly = true)
    public List<Update> getUpdates(String category, Boolean urgentOnly) {
        if (urgentOnly != null && urgentOnly) {
            return updateRepository.findByUrgentTrueOrderByCreatedAtDesc();
        }
        if (category != null && !category.trim().isEmpty()) {
            return updateRepository.findByCategoryIgnoreCaseOrderByCreatedAtDesc(category.trim());
        }
        return updateRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Update createUpdate(Update update) {
        return updateRepository.save(update);
    }
}
