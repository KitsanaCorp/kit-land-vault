import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

/**
 * Shared event bus for dashboard components.
 * Allows components to notify each other when data changes
 * without direct parent-child coupling.
 */
@Injectable({ providedIn: 'root' })
export class DashboardEventService {
  private walletUpdatedSubject = new Subject<void>();
  private transactionAddedSubject = new Subject<void>();

  /** Emits when a wallet is created, updated, or deleted */
  walletUpdated$ = this.walletUpdatedSubject.asObservable();

  /** Emits when a new transaction is saved */
  transactionAdded$ = this.transactionAddedSubject.asObservable();

  emitWalletUpdated(): void {
    this.walletUpdatedSubject.next();
  }

  emitTransactionAdded(): void {
    this.transactionAddedSubject.next();
  }
}
