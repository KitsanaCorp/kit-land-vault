import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WalletService, Wallet, DailyBudgetInfo } from '../../../core/services/wallet.service';

@Component({
  selector: 'app-daily-budget',
  imports: [CommonModule],
  templateUrl: './daily-budget.html',
  styleUrl: './daily-budget.scss'
})
export class DailyBudget implements OnInit {
  walletName = '';
  budgetAmount = 0;
  remaining = 0;
  reserveAmount = 0;
  daysRemaining = 0;
  status = 'Good';
  statusColor = '#22c55e';
  loading = true;
  error = false;
  noWallet = false;

  constructor(private walletService: WalletService) {}

  ngOnInit() {
    // First get wallets, find the DAILY one, then get its budget info
    this.walletService.getWallets().subscribe({
      next: (wallets) => {
        const dailyWallet = wallets.find(w => w.accountRole === 'DAILY');
        if (dailyWallet) {
          this.walletName = dailyWallet.name;
          this.walletService.getDailyBudget(dailyWallet.id).subscribe({
            next: (info) => {
              this.budgetAmount = info.dailyRate;
              this.remaining = info.remaining;
              this.reserveAmount = info.reserveAmount;
              this.daysRemaining = info.daysRemaining;
              this.updateStatus();
              this.loading = false;
            },
            error: () => { this.loading = false; this.error = true; }
          });
        } else {
          this.loading = false;
          this.noWallet = true;
        }
      },
      error: () => { this.loading = false; this.error = true; }
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
