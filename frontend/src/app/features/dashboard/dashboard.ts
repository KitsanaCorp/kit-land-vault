import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Sidebar } from './sidebar/sidebar';
import { MobileHeader } from './mobile-header/mobile-header';
import { DailyBudget } from './daily-budget/daily-budget';
import { NewTransactionBtn } from './new-transaction-btn/new-transaction-btn';
import { BucketSummary } from './bucket-summary/bucket-summary';
import { PartnerSettlement } from './partner-settlement/partner-settlement';
import { BalanceChart } from './balance-chart/balance-chart';
import { RecentTransactions } from './recent-transactions/recent-transactions';

@Component({
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    Sidebar,
    MobileHeader,
    DailyBudget,
    NewTransactionBtn,
    BucketSummary,
    PartnerSettlement,
    BalanceChart,
    RecentTransactions
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {}
