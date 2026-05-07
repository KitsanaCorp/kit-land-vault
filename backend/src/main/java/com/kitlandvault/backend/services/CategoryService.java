package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.CategoryResponse;
import com.kitlandvault.backend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .type(cat.getType().name())
                        .transactionType(cat.getTransactionType().name())
                        .build())
                .toList();
    }
}
