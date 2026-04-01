import { Component } from '@angular/core';

@Component({
  selector: 'app-daily-budget',
  templateUrl: './daily-budget.html',
  styleUrl: './daily-budget.scss'
})
export class DailyBudget {
  budgetAmount = 388;
  status = 'Good';
}
