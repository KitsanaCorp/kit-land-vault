import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GroupService, Group, GroupTransaction, SplitDetail } from '../../../core/services/group.service';
import { WalletService, Wallet } from '../../../core/services/wallet.service';
import { CategoryService, Category } from '../../../core/services/category.service';
import { Sidebar } from '../../dashboard/sidebar/sidebar';
import { MobileHeader } from '../../dashboard/mobile-header/mobile-header';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-group-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, Sidebar, MobileHeader],
  templateUrl: './group-detail.html',
  styleUrl: './group-detail.scss'
})
export class GroupDetail implements OnInit {
  groupId = 0;
  group: Group | null = null;
  transactions: GroupTransaction[] = [];
  wallets: Wallet[] = [];
  categories: Category[] = [];
  currentUserId = 0;
  
  // Transaction Form fields
  showTxForm = false;
  payerId = 0;
  amount: number | null = null;
  description = '';
  categoryId = 0;
  walletId: number | null = null;
  transactionDate = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private groupService: GroupService,
    private walletService: WalletService,
    private categoryService: CategoryService,
    private authService: AuthService
  ) {
    this.currentUserId = this.authService.getUser()?.userId ?? 0;
    this.transactionDate = new Date().toISOString().split('T')[0];
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.groupId = +params['id'];
      if (this.groupId) {
        this.loadAllData();
      }
    });
  }

  loadAllData() {
    // 1. Load group metadata and user balances
    this.groupService.getGroupDetails(this.groupId).subscribe({
      next: (data) => {
        this.group = data;
        // Default payer in form to current user if they are in the group
        this.payerId = this.currentUserId;
      },
      error: (err) => {
        console.error('Failed to load group detail', err);
        this.router.navigate(['/groups']);
      }
    });

    // 2. Load group transaction history
    this.groupService.getGroupTransactions(this.groupId).subscribe({
      next: (data) => this.transactions = data,
      error: (err) => console.error('Failed to load group transactions', err)
    });

    // 3. Load wallets and categories for the forms
    this.walletService.getWallets().subscribe({
      next: (data) => this.wallets = data,
      error: (err) => console.error('Failed to load wallets', err)
    });

    this.categoryService.getCategories().subscribe({
      next: (data) => {
        // Filter to EXPENSE categories to keep shared group items clean
        this.categories = data.filter(c => c.transactionType === 'EXPENSE');
        if (this.categories.length > 0) {
          this.categoryId = this.categories[0].id;
        }
      },
      error: (err) => console.error('Failed to load categories', err)
    });
  }

  toggleTxForm() {
    this.showTxForm = !this.showTxForm;
    if (this.showTxForm) {
      this.amount = null;
      this.description = '';
      this.walletId = null;
      this.transactionDate = new Date().toISOString().split('T')[0];
    }
  }

  submitTransaction() {
    if (!this.amount || this.amount <= 0 || !this.payerId || !this.categoryId) {
      alert('Please fill out all required fields with valid values');
      return;
    }

    this.groupService.createGroupTransaction(
      this.groupId,
      this.payerId,
      this.amount,
      this.description.trim(),
      this.categoryId,
      this.walletId || undefined,
      this.transactionDate
    ).subscribe({
      next: () => {
        this.showTxForm = false;
        this.loadAllData();
      },
      error: (err) => {
        console.error('Failed to add group transaction', err);
        alert('Failed to add transaction. Please check balance/connection.');
      }
    });
  }

  settlePendingSplit(splitId: number) {
    if (confirm('Are you sure you want to record this debt payment as settled?')) {
      this.groupService.settleSplit(this.groupId, splitId).subscribe({
        next: () => this.loadAllData(),
        error: (err) => {
          console.error('Failed to settle split', err);
          alert('Failed to settle split. Please try again.');
        }
      });
    }
  }

  getPendingOwedToMe(): { splitId: number; debtor: string; amount: number; desc: string; date: string }[] {
    const list: any[] = [];
    this.transactions.forEach(tx => {
      // If current user is the payer
      if (tx.payerId === this.currentUserId) {
        tx.splits.forEach(s => {
          // and other user split is pending
          if (s.userId !== this.currentUserId && s.status === 'PENDING') {
            list.push({
              splitId: s.id,
              debtor: s.username,
              amount: s.shareAmount,
              desc: tx.description || 'Shared Expense',
              date: tx.transactionDate
            });
          }
        });
      }
    });
    return list;
  }

  getPendingIOwe(): { splitId: number; creditor: string; amount: number; desc: string; date: string }[] {
    const list: any[] = [];
    this.transactions.forEach(tx => {
      // If someone else is the payer
      if (tx.payerId !== this.currentUserId) {
        tx.splits.forEach(s => {
          // and current user split is pending
          if (s.userId === this.currentUserId && s.status === 'PENDING') {
            list.push({
              splitId: s.id,
              creditor: tx.payerName,
              amount: s.shareAmount,
              desc: tx.description || 'Shared Expense',
              date: tx.transactionDate
            });
          }
        });
      }
    });
    return list;
  }
}
