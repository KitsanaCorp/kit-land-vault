package com.kitlandvault.backend.controllers;

import com.kitlandvault.backend.dto.DailyBudgetResponse;
import com.kitlandvault.backend.dto.WalletRequest;
import com.kitlandvault.backend.dto.WalletResponse;
import com.kitlandvault.backend.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getWallets(@RequestParam Long userId) {
        return ResponseEntity.ok(walletService.getWalletsByUser(userId));
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<DailyBudgetResponse> getDailySummary(@RequestParam Long userId) {
        return ResponseEntity.ok(walletService.getDailySummary(userId));
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@RequestParam Long userId, @RequestBody WalletRequest request) {
        return ResponseEntity.ok(walletService.createWallet(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WalletResponse> updateWallet(@PathVariable Long id, @RequestBody WalletRequest request) {
        return ResponseEntity.ok(walletService.updateWallet(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }
}
