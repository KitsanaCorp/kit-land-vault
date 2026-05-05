package com.kitlandvault.backend.controllers;

import com.kitlandvault.backend.dto.InternalTransferRequest;
import com.kitlandvault.backend.dto.InternalTransferResponse;
import com.kitlandvault.backend.services.InternalTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/internal-transfers")
@RequiredArgsConstructor
public class InternalTransferController {

    private final InternalTransferService transferService;

    @PostMapping
    public ResponseEntity<InternalTransferResponse> create(
            @RequestParam Long userId,
            @RequestBody InternalTransferRequest request) {
        return ResponseEntity.ok(transferService.createTransfer(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<InternalTransferResponse>> getActive(
            @RequestParam Long userId) {
        return ResponseEntity.ok(transferService.getActiveTransfers(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InternalTransferResponse>> getAll(
            @RequestParam Long userId) {
        return ResponseEntity.ok(transferService.getAllTransfers(userId));
    }

    @PostMapping("/{id}/repay")
    public ResponseEntity<InternalTransferResponse> repay(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(transferService.repayTransfer(id, amount));
    }
}
