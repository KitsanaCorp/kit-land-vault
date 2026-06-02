package com.kitlandvault.backend.controllers;

import com.kitlandvault.backend.dto.*;
import com.kitlandvault.backend.services.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @RequestParam Long userId,
            @RequestBody GroupRequest request) {
        return ResponseEntity.ok(groupService.createGroup(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getGroups(@RequestParam Long userId) {
        return ResponseEntity.ok(groupService.getGroupsByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroupDetails(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(groupService.getGroupDetails(id, userId));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<GroupTransactionResponse>> getGroupTransactions(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(groupService.getGroupTransactions(id, userId));
    }

    @PostMapping("/{id}/transactions")
    public ResponseEntity<GroupTransactionResponse> createGroupTransaction(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestBody GroupTransactionRequest request) {
        return ResponseEntity.ok(groupService.createGroupTransaction(id, userId, request));
    }

    @PostMapping("/{id}/splits/{splitId}/settle")
    public ResponseEntity<Void> settleSplit(
            @PathVariable Long id,
            @PathVariable Long splitId,
            @RequestParam Long userId) {
        groupService.settleSplit(id, splitId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<GroupSummaryResponse> getGroupSummary(@RequestParam Long userId) {
        return ResponseEntity.ok(groupService.getGroupSummary(userId));
    }
}
