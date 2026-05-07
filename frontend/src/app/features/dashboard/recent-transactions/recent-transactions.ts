import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransactionService, Transaction } from '../../../core/services/transaction.service';

interface MappedTransaction {
  id: number;
  icon: string;
  iconBg: string;
  iconColor: string;
  name: string;
  tag: string;
  tagStyle: string;
  amount: number;
}

@Component({
  selector: 'app-recent-transactions',
  imports: [CommonModule],
  templateUrl: './recent-transactions.html',
  styleUrl: './recent-transactions.scss'
})
export class RecentTransactions implements OnInit {
  transactions: MappedTransaction[] = [];
  loading = true;
  error = false;

  constructor(private transactionService: TransactionService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.transactionService.getTransactions().subscribe({
      next: (data) => {
        // Take the latest 5 transactions
        this.transactions = data.slice(0, 5).map(tx => this.mapTransaction(tx));
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.error = true;
        this.cdr.detectChanges();
      }
    });
  }

  private mapTransaction(tx: Transaction): MappedTransaction {
    // Map icons based on category (basic matching)
    let icon = 'fa-money-bill';
    let iconBg = 'bg-slate-100';
    let iconColor = 'text-slate-600';
    
    const cat = tx.categoryName.toLowerCase();
    if (cat.includes('food') || cat.includes('dining') || cat.includes('buffet')) {
      icon = 'fa-utensils'; iconBg = 'bg-orange-100'; iconColor = 'text-orange-600';
    } else if (cat.includes('groceries')) {
      icon = 'fa-cart-shopping'; iconBg = 'bg-purple-100'; iconColor = 'text-purple-600';
    } else if (cat.includes('transport') || cat.includes('car')) {
      icon = 'fa-car'; iconBg = 'bg-blue-100'; iconColor = 'text-blue-600';
    } else if (cat.includes('utilit') || cat.includes('internet') || cat.includes('electric')) {
      icon = 'fa-bolt'; iconBg = 'bg-yellow-100'; iconColor = 'text-yellow-600';
    } else if (cat.includes('health') || cat.includes('medical')) {
      icon = 'fa-heart-pulse'; iconBg = 'bg-red-100'; iconColor = 'text-red-600';
    } else if (cat.includes('salary') || cat.includes('income')) {
      icon = 'fa-arrow-down'; iconBg = 'bg-green-100'; iconColor = 'text-green-600';
    }

    // Map tags
    let tagStyle = 'bg-slate-100 text-slate-500';
    let splitLabel = 'Personal';
    
    if (tx.splitType === 'SPLIT_EQUAL') {
      splitLabel = 'Split 50/50';
      tagStyle = 'bg-orange-100 text-orange-700';
    } else if (tx.splitType === 'ON_BEHALF_OF_PARTNER') {
      splitLabel = 'Advance';
      tagStyle = 'bg-amber-100 text-amber-700';
    }

    return {
      id: tx.id,
      name: tx.description || tx.categoryName,
      amount: tx.amount,
      tag: `${splitLabel} • ${tx.walletName}`,
      tagStyle,
      icon,
      iconBg,
      iconColor
    };
  }
}
