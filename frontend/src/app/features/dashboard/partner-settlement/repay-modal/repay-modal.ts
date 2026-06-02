import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SettlementService } from '../../../../core/services/settlement.service';
import { WalletService, Wallet } from '../../../../core/services/wallet.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-repay-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './repay-modal.html'
})
export class RepayModal implements OnInit {
  @Input() youOwe = 0;
  @Input() partnerOwes = 0;
  @Output() close = new EventEmitter<void>();
  @Output() success = new EventEmitter<void>();

  // Form State
  isPartnerPaying = false; // false = I am paying partner, true = Partner is paying me
  amount = 0;
  targetWalletId = 0;
  note = '';

  wallets: Wallet[] = [];
  submitting = false;
  error = '';

  constructor(
    private settlementService: SettlementService,
    private walletService: WalletService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // Default the direction and amount based on who owes more
    if (this.partnerOwes > this.youOwe) {
      this.isPartnerPaying = true;
      this.amount = this.partnerOwes;
    } else {
      this.isPartnerPaying = false;
      this.amount = this.youOwe;
    }

    this.loadWallets();
  }

  loadWallets() {
    this.walletService.getWallets().subscribe({
      next: (wallets) => {
        this.wallets = wallets;
        if (wallets.length > 0) {
          this.targetWalletId = wallets[0].id;
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Could not load wallets';
        this.cdr.markForCheck();
      }
    });
  }

  onDirectionChange(isPartnerPaying: boolean) {
    this.isPartnerPaying = isPartnerPaying;
    this.amount = isPartnerPaying ? this.partnerOwes : this.youOwe;
  }

  submit() {
    if (this.amount <= 0) {
      this.error = 'Amount must be greater than zero';
      return;
    }
    if (!this.targetWalletId) {
      this.error = 'Please select a target wallet';
      return;
    }

    this.submitting = true;
    this.error = '';

    const myUserId = this.authService.getUser()?.userId || 1;
    let partnerId = myUserId === 1 ? 2 : 1;
    if (myUserId === 3) partnerId = 4;
    else if (myUserId === 4) partnerId = 3;

    // If I am paying partner:
    // Creditor = partner, Debtor = me
    // Target Wallet = partner's wallet (in real life). But for prototype, maybe we select a shared wallet?
    // The backend `addBalance` adds to the target wallet. If I pay my partner, money leaves my account (not tracked here unless we create a withdrawal), and enters their wallet (targetWalletId).
    
    // If Partner is paying me:
    // Creditor = me, Debtor = partner
    // Target Wallet = my wallet
    
    const asCreditor = this.isPartnerPaying; // If partner pays me, I am the creditor.
    const debtorId = this.isPartnerPaying ? partnerId : myUserId;

    this.settlementService.recordRepayment(asCreditor, {
      debtorId: debtorId,
      amount: this.amount,
      targetWalletId: Number(this.targetWalletId),
      note: this.note || 'Debt clearance'
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.success.emit();
      },
      error: (err) => {
        this.error = 'Failed to record repayment. ' + (err.error?.message || '');
        this.submitting = false;
        this.cdr.markForCheck();
      }
    });
  }
}
