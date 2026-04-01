package com.kitlandvault.backend.controllers;

import com.kitlandvault.backend.dto.TransactionRequest;
import com.kitlandvault.backend.dto.TransactionResponse;
import com.kitlandvault.backend.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestParam Long userId,
            @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.createTransaction(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll(@RequestParam Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
    }
}
