package com.kitlandvault.backend.controllers;

import com.kitlandvault.backend.dto.RepaymentRequest;
import com.kitlandvault.backend.dto.SettlementBalanceResponse;
import com.kitlandvault.backend.dto.SettlementResponse;
import com.kitlandvault.backend.services.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/balance")
    public ResponseEntity<SettlementBalanceResponse> getBalance(
            @RequestParam Long creditorId,
            @RequestParam Long debtorId) {
        return ResponseEntity.ok(settlementService.getBalance(creditorId, debtorId));
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> getHistory(
            @RequestParam Long creditorId,
            @RequestParam Long debtorId) {
        return ResponseEntity.ok(settlementService.getHistory(creditorId, debtorId));
    }

    @PostMapping("/repay")
    public ResponseEntity<Void> recordRepayment(
            @RequestParam Long creditorId,
            @RequestBody RepaymentRequest request) {
        settlementService.recordRepayment(creditorId, request);
        return ResponseEntity.ok().build();
    }
}
