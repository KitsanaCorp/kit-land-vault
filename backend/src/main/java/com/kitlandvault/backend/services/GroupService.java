package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.*;
import com.kitlandvault.backend.entities.*;
import com.kitlandvault.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupTransactionRepository groupTransactionRepository;
    private final GroupExpenseSplitRepository groupExpenseSplitRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final WalletService walletService;

    @Transactional
    public GroupResponse createGroup(Long currentUserId, GroupRequest request) {
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> memberIds = new ArrayList<>(request.getMemberIds() != null ? request.getMemberIds() : new ArrayList<>());
        if (!memberIds.contains(currentUserId)) {
            memberIds.add(currentUserId);
        }

        List<User> members = userRepository.findAllById(memberIds);

        Group group = Group.builder()
                .name(request.getName())
                .familyGroup(creator.getFamilyGroup())
                .members(members)
                .build();

        group = groupRepository.save(group);

        return toGroupResponse(group, currentUserId);
    }

    public List<GroupResponse> getGroupsByUser(Long userId) {
        List<Group> groups = groupRepository.findByMembersId(userId);
        return groups.stream()
                .map(g -> toGroupResponse(g, userId))
                .collect(Collectors.toList());
    }

    public GroupResponse getGroupDetails(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        boolean isMember = group.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
        if (!isMember) {
            throw new RuntimeException("User is not a member of this group");
        }

        return toGroupResponse(group, userId);
    }

    public List<GroupTransactionResponse> getGroupTransactions(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        boolean isMember = group.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
        if (!isMember) {
            throw new RuntimeException("User is not a member of this group");
        }

        List<GroupTransaction> transactions = groupTransactionRepository.findByGroupIdOrderByTransactionDateDescIdDesc(groupId);
        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupTransactionResponse createGroupTransaction(Long groupId, Long currentUserId, GroupTransactionRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User payer = userRepository.findById(request.getPayerId())
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Wallet wallet = null;
        if (request.getWalletId() != null) {
            wallet = walletService.getWalletEntity(request.getWalletId());
        }

        BigDecimal amount = request.getAmount();
        List<User> members = group.getMembers();
        int memberCount = members.size();

        if (memberCount == 0) {
            throw new RuntimeException("Group has no members");
        }

        // 1. Save GroupTransaction first
        GroupTransaction tx = GroupTransaction.builder()
                .group(group)
                .payer(payer)
                .amount(amount)
                .description(request.getDescription())
                .category(category)
                .wallet(wallet)
                .transactionDate(LocalDate.parse(request.getTransactionDate()))
                .build();

        tx = groupTransactionRepository.save(tx);

        // 2. Compute splits equally, handles rounding scale
        BigDecimal baseShare = amount.divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP);
        BigDecimal accumulated = BigDecimal.ZERO;
        List<GroupExpenseSplit> splits = new ArrayList<>();

        for (int i = 0; i < memberCount; i++) {
            User member = members.get(i);
            BigDecimal share;
            
            if (i == memberCount - 1) {
                // Last member takes the rounding remainder
                share = amount.subtract(accumulated);
            } else {
                share = baseShare;
                accumulated = accumulated.add(share);
            }

            GroupExpenseSplit.Status status = member.getId().equals(payer.getId()) 
                    ? GroupExpenseSplit.Status.SETTLED 
                    : GroupExpenseSplit.Status.PENDING;

            GroupExpenseSplit split = GroupExpenseSplit.builder()
                    .groupTransaction(tx)
                    .user(member)
                    .shareAmount(share)
                    .status(status)
                    .settledAt(status == GroupExpenseSplit.Status.SETTLED ? LocalDateTime.now() : null)
                    .build();

            splits.add(split);
        }

        tx.setSplits(splits);
        tx = groupTransactionRepository.save(tx);

        // 3. If wallet is specified, deduct total amount from wallet balance
        if (wallet != null) {
            walletService.deductBalance(wallet.getId(), amount);
        }

        return toTransactionResponse(tx);
    }

    @Transactional
    public void settleSplit(Long groupId, Long splitId, Long currentUserId) {
        GroupExpenseSplit split = groupExpenseSplitRepository.findById(splitId)
                .orElseThrow(() -> new RuntimeException("Split not found"));

        if (!split.getGroupTransaction().getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Split does not belong to this group");
        }

        split.setStatus(GroupExpenseSplit.Status.SETTLED);
        split.setSettledAt(LocalDateTime.now());
        groupExpenseSplitRepository.save(split);
    }

    public GroupSummaryResponse getGroupSummary(Long userId) {
        List<Group> groups = groupRepository.findByMembersId(userId);
        
        BigDecimal totalOwedToMe = BigDecimal.ZERO;
        BigDecimal totalIOweToOthers = BigDecimal.ZERO;
        List<GroupSummaryResponse.GroupBalanceDetail> details = new ArrayList<>();

        for (Group group : groups) {
            BigDecimal netBalance = calculateUserNetBalanceInGroup(group.getId(), userId);
            
            details.add(GroupSummaryResponse.GroupBalanceDetail.builder()
                    .groupId(group.getId())
                    .groupName(group.getName())
                    .netBalance(netBalance)
                    .build());

            if (netBalance.compareTo(BigDecimal.ZERO) > 0) {
                totalOwedToMe = totalOwedToMe.add(netBalance);
            } else if (netBalance.compareTo(BigDecimal.ZERO) < 0) {
                totalIOweToOthers = totalIOweToOthers.add(netBalance.abs());
            }
        }

        return GroupSummaryResponse.builder()
                .totalOwedToMe(totalOwedToMe)
                .totalIOweToOthers(totalIOweToOthers)
                .overallNetBalance(totalOwedToMe.subtract(totalIOweToOthers))
                .groupBalances(details)
                .build();
    }

    // Helper methods for DTO conversions and financial balance computations
    private GroupResponse toGroupResponse(Group group, Long currentUserId) {
        List<GroupMemberResponse> members = group.getMembers().stream()
                .map(member -> GroupMemberResponse.builder()
                        .id(member.getId())
                        .username(member.getUsername())
                        .netBalance(calculateUserNetBalanceInGroup(group.getId(), member.getId()))
                        .build())
                .collect(Collectors.toList());

        BigDecimal myNet = calculateUserNetBalanceInGroup(group.getId(), currentUserId);

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .members(members)
                .myNetBalance(myNet)
                .createdBy(group.getCreatedBy())
                .createdAt(group.getCreatedAt())
                .build();
    }

    private GroupTransactionResponse toTransactionResponse(GroupTransaction tx) {
        List<GroupTransactionResponse.SplitDetail> splits = tx.getSplits().stream()
                .map(s -> GroupTransactionResponse.SplitDetail.builder()
                        .id(s.getId())
                        .userId(s.getUser().getId())
                        .username(s.getUser().getUsername())
                        .shareAmount(s.getShareAmount())
                        .status(s.getStatus().name())
                        .settledAt(s.getSettledAt())
                        .build())
                .collect(Collectors.toList());

        return GroupTransactionResponse.builder()
                .id(tx.getId())
                .groupId(tx.getGroup().getId())
                .groupName(tx.getGroup().getName())
                .payerId(tx.getPayer().getId())
                .payerName(tx.getPayer().getUsername())
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .categoryId(tx.getCategory().getId())
                .categoryName(tx.getCategory().getName())
                .walletId(tx.getWallet() != null ? tx.getWallet().getId() : null)
                .walletName(tx.getWallet() != null ? tx.getWallet().getName() : null)
                .transactionDate(tx.getTransactionDate())
                .splits(splits)
                .createdBy(tx.getCreatedBy())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private BigDecimal calculateUserNetBalanceInGroup(Long groupId, Long userId) {
        List<GroupTransaction> groupTxs = groupTransactionRepository.findByGroupIdOrderByTransactionDateDescIdDesc(groupId);
        BigDecimal owedToMe = BigDecimal.ZERO;
        BigDecimal iOwe = BigDecimal.ZERO;

        for (GroupTransaction tx : groupTxs) {
            boolean isPayer = tx.getPayer().getId().equals(userId);
            for (GroupExpenseSplit split : tx.getSplits()) {
                if (split.getStatus() == GroupExpenseSplit.Status.PENDING) {
                    if (isPayer && !split.getUser().getId().equals(userId)) {
                        // User is payer, other user owes their split
                        owedToMe = owedToMe.add(split.getShareAmount());
                    } else if (!isPayer && split.getUser().getId().equals(userId)) {
                        // Other user is payer, user owes their split
                        iOwe = iOwe.add(split.getShareAmount());
                    }
                }
            }
        }
        return owedToMe.subtract(iOwe);
    }
}
