import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WalletService, Wallet } from '../../../core/services/wallet.service';
import { DashboardEventService } from '../../../core/services/dashboard-event.service';

const ROLE_LABELS: Record<string, string> = {
  TRANSIT: 'Transit',
  DAILY: 'Daily',
  BILLS: 'Bills',
  CAR_LOAN: 'Car Loan',
  SINKING_FUND: 'Sinking Fund',
  INVESTMENT: 'Investment',
  CUSTOM: 'Custom'
};

const ROLE_COLORS: Record<string, string> = {
  TRANSIT: '#1E3A5F',
  DAILY: '#6B8E7B',
  BILLS: '#E58E58',
  CAR_LOAN: '#5B428F',
  SINKING_FUND: '#5D9C96',
  INVESTMENT: '#D96B6B',
  CUSTOM: '#10B981'
};

const PALETTE_COLORS = [
  { name: 'Emerald', hex: '#10B981' },
  { name: 'Teal', hex: '#0D9488' },
  { name: 'Indigo', hex: '#4F46E5' },
  { name: 'Violet', hex: '#7C3AED' },
  { name: 'Rose', hex: '#F43F5E' },
  { name: 'Amber', hex: '#F59E0B' },
  { name: 'Slate', hex: '#64748B' },
  { name: 'Cyan', hex: '#06B6D4' }
];

@Component({
  selector: 'app-bucket-summary',
  imports: [CommonModule, FormsModule],
  templateUrl: './bucket-summary.html',
  styleUrl: './bucket-summary.scss'
})
export class BucketSummary implements OnInit {
  buckets: any[] = [];
  loading = true;
  error = false;
  totalBalance = 0;

  // Modal & Form States
  isModalOpen = false;
  isFormOpen = false;
  editingWallet: Partial<Wallet> | null = null;
  formName = '';
  formBalance = 0;
  formDailyBudget: number | null = null;
  formColor = '#10B981';
  formRole = 'CUSTOM';
  palette = PALETTE_COLORS;
  originalWallets: Wallet[] = [];

  constructor(private walletService: WalletService, private dashboardEvents: DashboardEventService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadWallets();
  }

  loadWallets() {
    this.walletService.getWallets().subscribe({
      next: (wallets) => {
        this.originalWallets = wallets;
        this.buckets = wallets.map(w => ({
          id: w.id,
          name: w.name,
          balance: w.balance,
          color: w.color || ROLE_COLORS[w.accountRole] || '#64748b',
          role: ROLE_LABELS[w.accountRole] || w.accountRole,
          accountRole: w.accountRole,
          dailyBudget: w.dailyBudget,
          reserveAmount: w.reserveAmount,
          minBalance: w.minBalance,
          budgetResetDay: w.budgetResetDay
        }));
        this.totalBalance = wallets.reduce((sum, w) => sum + w.balance, 0);
        this.loading = false;
        this.error = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.error = true;
        this.cdr.markForCheck();
      }
    });
  }

  openManageModal() {
    this.loadWallets();
    this.isModalOpen = true;
    this.cdr.markForCheck();
  }

  closeManageModal() {
    this.isModalOpen = false;
    this.isFormOpen = false;
    this.editingWallet = null;
    this.cdr.markForCheck();
  }

  openAddForm() {
    this.editingWallet = null;
    this.formName = '';
    this.formBalance = 0;
    this.formDailyBudget = null;
    this.formColor = '#10B981';
    this.formRole = 'CUSTOM';
    this.isFormOpen = true;
    this.cdr.markForCheck();
  }

  openEditForm(bucket: any) {
    this.editingWallet = {
      id: bucket.id,
      name: bucket.name,
      balance: bucket.balance,
      color: bucket.color,
      accountRole: bucket.accountRole,
      dailyBudget: bucket.dailyBudget,
      reserveAmount: bucket.reserveAmount,
      minBalance: bucket.minBalance,
      budgetResetDay: bucket.budgetResetDay
    };
    this.formName = bucket.name;
    this.formBalance = bucket.balance;
    this.formDailyBudget = bucket.dailyBudget;
    this.formColor = bucket.color;
    this.formRole = bucket.accountRole;
    this.isFormOpen = true;
    this.cdr.markForCheck();
  }

  selectColor(colorHex: string) {
    this.formColor = colorHex;
    this.cdr.markForCheck();
  }

  saveWallet() {
    if (!this.formName.trim()) return;

    const payload: Partial<Wallet> = {
      name: this.formName,
      color: this.formColor,
      dailyBudget: this.formDailyBudget,
      accountRole: this.formRole,
      balance: this.formBalance
    };

    if (this.editingWallet) {
      this.walletService.updateWallet(this.editingWallet.id!, payload).subscribe({
        next: () => {
          this.loadWallets();
          this.isFormOpen = false;
          this.editingWallet = null;
          this.dashboardEvents.emitWalletUpdated();
          this.cdr.markForCheck();
        }
      });
    } else {
      payload.balance = this.formBalance;
      this.walletService.createWallet(payload).subscribe({
        next: () => {
          this.loadWallets();
          this.isFormOpen = false;
          this.dashboardEvents.emitWalletUpdated();
          this.cdr.markForCheck();
        }
      });
    }
  }

  deleteWallet(id: number) {
    if (confirm('คุณแน่ใจหรือไม่ที่จะลบ Bucket นี้? ประวัติธุรกรรมจะไม่ถูกลบ แต่จะไม่ผูกกับ Bucket ใดๆ')) {
      this.walletService.deleteWallet(id).subscribe({
        next: () => {
          this.loadWallets();
          this.isFormOpen = false;
          this.editingWallet = null;
          this.dashboardEvents.emitWalletUpdated();
          this.cdr.markForCheck();
        }
      });
    }
  }
}
