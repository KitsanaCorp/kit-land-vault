import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TransactionService, Transaction } from '../../../core/services/transaction.service';
import { AuthService } from '../../../core/services/auth.service';

interface MappedTransaction {
  id: number;
  name: string;
  amount: number;
  date: Date;
  icon: string;
  iconBg: string;
  iconColor: string;
  tag: string;
  tagStyle: string;
  walletName: string;
  splitType: string;
}

@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './transaction-history.html',
  styleUrl: './transaction-history.scss'
})
export class TransactionHistory implements OnInit {
  transactions: MappedTransaction[] = [];
  loading = true;
  error = false;
  userId: number = 0;

  constructor(
    private transactionService: TransactionService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.userId = this.authService.getUser()?.userId || 1; // Fallback to 1 if not found
    this.loadTransactions();
  }

  loadTransactions() {
    this.loading = true;
    this.error = false;
    this.cdr.markForCheck();

    this.transactionService.getTransactions().subscribe({
      next: (data: Transaction[]) => {
        this.transactions = data.map(tx => this.mapTransaction(tx));
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error('Error loading transactions', err);
        this.loading = false;
        this.error = true;
        this.cdr.markForCheck();
      }
    });
  }

  private mapTransaction(tx: Transaction): MappedTransaction {
    const isFood = tx.categoryName.toLowerCase().includes('food') || tx.categoryName.toLowerCase().includes('dining');
    const isTransport = tx.categoryName.toLowerCase().includes('transport') || tx.categoryName.toLowerCase().includes('bts');
    const isShopping = tx.categoryName.toLowerCase().includes('shop') || tx.categoryName.toLowerCase().includes('groceries');
    
    let icon = 'fa-receipt';
    let iconBg = 'bg-slate-100';
    let iconColor = 'text-slate-500';
    
    if (isFood) {
      icon = 'fa-burger';
      iconBg = 'bg-orange-100';
      iconColor = 'text-orange-500';
    } else if (isTransport) {
      icon = 'fa-train-subway';
      iconBg = 'bg-blue-100';
      iconColor = 'text-blue-500';
    } else if (isShopping) {
      icon = 'fa-bag-shopping';
      iconBg = 'bg-purple-100';
      iconColor = 'text-purple-500';
    }

    let tagStyle = 'bg-slate-100 text-slate-600';
    if (tx.splitType === 'SHARED') {
      tagStyle = 'bg-indigo-100 text-indigo-700';
    } else if (tx.splitType === 'ON_BEHALF') {
      tagStyle = 'bg-amber-100 text-amber-700';
    }

    return {
      id: tx.id,
      name: tx.description || tx.categoryName,
      amount: tx.amount,
      date: new Date(tx.transactionDate),
      icon,
      iconBg,
      iconColor,
      tag: tx.splitType === 'PERSONAL' ? tx.categoryName : (tx.splitType === 'SHARED' ? 'Shared 50/50' : 'On Behalf'),
      tagStyle,
      walletName: tx.walletName || 'Unknown Wallet',
      splitType: tx.splitType
    };
  }
}
