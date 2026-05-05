import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-daily-budget',
  templateUrl: './daily-budget.html',
  styleUrl: './daily-budget.scss'
})
export class DailyBudget implements OnInit {
  walletName = 'Kasikorn';
  balance = 18000;
  groceriesReserve = 3000;
  budgetAmount = 0;
  status = 'Good';

  ngOnInit() {
    this.calculateDailyBudget();
  }

  private calculateDailyBudget() {
    const today = new Date();
    const daysInMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate();
    const daysRemaining = daysInMonth - today.getDate() + 1;
    const spendable = Math.max(this.balance - this.groceriesReserve, 0);
    this.budgetAmount = Math.round(spendable / daysRemaining);

    if (this.budgetAmount >= 500) {
      this.status = 'Great';
    } else if (this.budgetAmount >= 300) {
      this.status = 'Good';
    } else if (this.budgetAmount >= 150) {
      this.status = 'Tight';
    } else {
      this.status = 'Critical';
    }
  }
}

