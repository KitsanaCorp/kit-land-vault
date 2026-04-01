package com.kitlandvault.backend.controllers;

import com.kitlandvault.backend.dto.DailyBudgetResponse;
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

    @GetMapping("/{id}/daily-budget")
    public ResponseEntity<DailyBudgetResponse> getDailyBudget(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getDailyBudget(id));
    }
}
