import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Transaction {
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
export class RecentTransactions {
  transactions: Transaction[] = [
    {
      icon: 'fa-utensils', iconBg: 'bg-orange-100', iconColor: 'text-orange-600',
      name: 'Shabu Buffet', tag: 'Split 50/50 • K Mobile',
      tagStyle: 'bg-slate-100 text-slate-500', amount: 800
    },
    {
      icon: 'fa-bolt', iconBg: 'bg-blue-100', iconColor: 'text-blue-600',
      name: 'Electric Bill', tag: 'Personal • SCB',
      tagStyle: 'bg-slate-100 text-slate-500', amount: 1250
    },
    {
      icon: 'fa-cart-shopping', iconBg: 'bg-purple-100', iconColor: 'text-purple-600',
      name: 'Groceries', tag: 'Advance • LHB',
      tagStyle: 'bg-amber-100 text-amber-700', amount: 500
    }
  ];
}
