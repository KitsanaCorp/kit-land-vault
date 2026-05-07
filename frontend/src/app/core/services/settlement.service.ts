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

@Injectable({ providedIn: 'root' })
export class SettlementService {
  private readonly API_URL = '/api';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private get userId(): number {
    return this.authService.getUser()?.userId ?? 0;
  }

  // Assuming only 2 users in the system: admin (1) and kit (2).
  private get partnerId(): number {
    return this.userId === 1 ? 2 : 1;
  }

  getBalance(asCreditor: boolean): Observable<SettlementBalance> {
    const creditorId = asCreditor ? this.userId : this.partnerId;
    const debtorId = asCreditor ? this.partnerId : this.userId;
    return this.http.get<SettlementBalance>(
      `${this.API_URL}/settlements/balance?creditorId=${creditorId}&debtorId=${debtorId}`
    );
  }
}
