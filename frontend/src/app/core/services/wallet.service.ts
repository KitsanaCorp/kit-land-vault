import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Wallet {
  id: number;
  name: string;
  accountRole: string;
  color?: string;
  balance: number;
  dailyBudget: number | null;
  reserveAmount: number;
  minBalance: number;
  budgetResetDay: number;
}

export interface DailyBudgetInfo {
  walletId: number;
  walletName: string;
  accountRole: string;
  totalBudget: number;
  reserveAmount: number;
  spent: number;
  remaining: number;
  dailyRate: number;
  daysRemaining: number;
}

@Injectable({ providedIn: 'root' })
export class WalletService {
  private readonly API_URL = '/api';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private get userId(): number {
    return this.authService.getUser()?.userId ?? 0;
  }

  getWallets(): Observable<Wallet[]> {
    return this.http.get<Wallet[]>(`${this.API_URL}/wallets?userId=${this.userId}`);
  }

  getDailyBudget(walletId: number): Observable<DailyBudgetInfo> {
    return this.http.get<DailyBudgetInfo>(`${this.API_URL}/wallets/${walletId}/daily-budget`);
  }

  createWallet(wallet: Partial<Wallet>): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.API_URL}/wallets?userId=${this.userId}`, wallet);
  }

  updateWallet(id: number, wallet: Partial<Wallet>): Observable<Wallet> {
    return this.http.put<Wallet>(`${this.API_URL}/wallets/${id}`, wallet);
  }

  deleteWallet(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/wallets/${id}`);
  }
}
