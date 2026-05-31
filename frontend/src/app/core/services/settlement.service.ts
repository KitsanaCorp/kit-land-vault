import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface SettlementBalance {
  creditorId: number;
  debtorId: number;
  totalReceivable: number;
  totalRepaid: number;
  netBalance: number;
}

export interface RepaymentRequest {
  debtorId: number;
  amount: number;
  targetWalletId: number;
  note: string;
}

@Injectable({ providedIn: 'root' })
export class SettlementService {
  private readonly API_URL = '/api';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private get userId(): number {
    return this.authService.getUser()?.userId ?? 0;
  }

  // Assuming only 2 users in the system: admin and kit.
  private get partnerId(): number {
    if (this.userId === 3) return 4;
    if (this.userId === 4) return 3;
    return this.userId === 1 ? 2 : 1;
  }

  getBalance(asCreditor: boolean): Observable<SettlementBalance> {
    const creditorId = asCreditor ? this.userId : this.partnerId;
    const debtorId = asCreditor ? this.partnerId : this.userId;
    return this.http.get<SettlementBalance>(
      `${this.API_URL}/settlements/balance?creditorId=${creditorId}&debtorId=${debtorId}`
    );
  }

  recordRepayment(asCreditor: boolean, request: RepaymentRequest): Observable<void> {
    const creditorId = asCreditor ? this.userId : this.partnerId;
    return this.http.post<void>(
      `${this.API_URL}/settlements/repay?creditorId=${creditorId}`,
      request
    );
  }
}
