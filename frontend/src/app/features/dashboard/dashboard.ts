import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {
  totalBalance = 15200.50;
  monthlyIncome = 4500.00;
  monthlyExpenses = 1200.75;

  recentTransactions = [
    { id: 1, date: '2026-03-21', description: 'Groceries', amount: -150.20, category: 'One-time' },
    { id: 2, date: '2026-03-20', description: 'Salary', amount: 4500.00, category: 'Recurring' },
    { id: 3, date: '2026-03-19', description: 'Electricity Bill', amount: -85.50, category: 'Recurring' },
    { id: 4, date: '2026-03-18', description: 'Dinner Out', amount: -65.00, category: 'One-time' },
    { id: 5, date: '2026-03-17', description: 'Freelance Payment', amount: 1200.00, category: 'One-time' },
    { id: 6, date: '2026-03-16', description: 'Internet Bill', amount: -45.00, category: 'Recurring' }
  ];
}
