import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WalletService, Wallet } from '../../../core/services/wallet.service';

const ROLE_LABELS: Record<string, string> = {
  TRANSIT: 'Transit',
  DAILY: 'Daily',
  BILLS: 'Bills',
  CAR_LOAN: 'Car Loan',
  SINKING_FUND: 'Sinking Fund',
  INVESTMENT: 'Investment'
};

const ROLE_COLORS: Record<string, string> = {
  TRANSIT: '#1E3A5F',
  DAILY: '#6B8E7B',
  BILLS: '#E58E58',
  CAR_LOAN: '#5B428F',
  SINKING_FUND: '#5D9C96',
  INVESTMENT: '#D96B6B'
};

@Component({
  selector: 'app-bucket-summary',
  imports: [CommonModule],
  templateUrl: './bucket-summary.html',
  styleUrl: './bucket-summary.scss'
})
export class BucketSummary implements OnInit {
  buckets: { name: string; balance: number; color: string; role: string }[] = [];
  loading = true;
  error = false;
  totalBalance = 0;

  constructor(private walletService: WalletService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.walletService.getWallets().subscribe({
      next: (wallets) => {
        this.buckets = wallets.map(w => ({
          name: w.name,
          balance: w.balance,
          color: ROLE_COLORS[w.accountRole] || '#64748b',
          role: ROLE_LABELS[w.accountRole] || w.accountRole
        }));
        this.totalBalance = wallets.reduce((sum, w) => sum + w.balance, 0);
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.error = true;
        this.cdr.markForCheck();
      }
    });
  }
}
