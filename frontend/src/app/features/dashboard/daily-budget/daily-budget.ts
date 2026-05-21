import { Component, OnInit, ChangeDetectorRef, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { WalletService, DailyBudgetInfo } from '../../../core/services/wallet.service';
import { DashboardEventService } from '../../../core/services/dashboard-event.service';

@Component({
  selector: 'app-daily-budget',
  imports: [CommonModule],
  templateUrl: './daily-budget.html',
  styleUrl: './daily-budget.scss'
})
export class DailyBudget implements OnInit {
  walletNames = '';
  walletCount = 0;
  budgetAmount = 0;
  remaining = 0;
  spentThisMonth = 0;
  totalBalance = 0;
  daysRemaining = 0;
  status = 'Good';
  statusColor = '#22c55e';
  loading = true;
  error = false;
  noWallet = false;

  private destroyRef = inject(DestroyRef);

  constructor(
    private walletService: WalletService,
    private dashboardEvents: DashboardEventService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadBudget();

    // Auto-refresh when a transaction is added or a wallet is updated
    this.dashboardEvents.transactionAdded$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadBudget());

    this.dashboardEvents.walletUpdated$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadBudget());
  }

  loadBudget() {
    this.loading = true;
    this.walletService.getDailySummary().subscribe({
      next: (info: DailyBudgetInfo) => {
        if (info.walletCount === 0) {
          this.loading = false;
          this.noWallet = true;
          this.cdr.markForCheck();
          return;
        }
        this.walletNames = info.walletNames.join(', ');
        this.walletCount = info.walletCount;
        this.budgetAmount = info.dailyRate;
        this.remaining = info.remaining;
        this.spentThisMonth = info.spentThisMonth;
        this.totalBalance = info.totalBalance;
        this.daysRemaining = info.daysRemaining;
        this.noWallet = false;
        this.updateStatus();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.error = true; this.cdr.markForCheck(); }
    });
  }

  private updateStatus() {
    if (this.budgetAmount >= 500) {
      this.status = 'Great'; this.statusColor = '#22c55e';
    } else if (this.budgetAmount >= 300) {
      this.status = 'Good'; this.statusColor = '#3b82f6';
    } else if (this.budgetAmount >= 150) {
      this.status = 'Tight'; this.statusColor = '#f59e0b';
    } else {
      this.status = 'Critical'; this.statusColor = '#ef4444';
    }
  }
}
