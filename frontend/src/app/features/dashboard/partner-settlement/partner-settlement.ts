import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettlementService } from '../../../core/services/settlement.service';
import { RepayModal } from './repay-modal/repay-modal';

@Component({
  selector: 'app-partner-settlement',
  imports: [CommonModule, RepayModal],
  templateUrl: './partner-settlement.html',
  styleUrl: './partner-settlement.scss'
})
export class PartnerSettlement implements OnInit {
  youOwe = 0;
  partnerOwes = 0;
  loading = true;
  showRepayModal = false;

  constructor(private settlementService: SettlementService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadBalances();
  }

  loadBalances() {
    this.loading = true;
    // Check how much partner owes me (I am creditor)
    this.settlementService.getBalance(true).subscribe({
      next: (res) => {
        this.partnerOwes = Math.max(0, res.netBalance);
      },
      error: (err) => console.error('Error fetching partner owes', err)
    });

    // Check how much I owe partner (Partner is creditor)
    this.settlementService.getBalance(false).subscribe({
      next: (res) => {
        this.youOwe = Math.max(0, res.netBalance);
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error fetching you owe', err);
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  openRepayModal() {
    this.showRepayModal = true;
  }

  closeRepayModal() {
    this.showRepayModal = false;
  }

  onRepaySuccess() {
    this.showRepayModal = false;
    this.loadBalances();
  }
}
