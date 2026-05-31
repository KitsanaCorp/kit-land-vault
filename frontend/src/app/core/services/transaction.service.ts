import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Transaction {
  id: number;
  userId: number;
  walletName: string;
  walletId: number;
  categoryName: string;
  amount: number;
  splitType: string;
  myShare: number;
  partnerShare: number;
  transactionDate: string;
  description: string;
  transactionType?: string;
  createdAt?: string;
  createdBy?: string;
}

export interface TransactionRequest {
  categoryId: number;
  walletId: number;
  amount: number;
  splitType: string;
  transactionDate: string;
  description: string;
}

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly API_URL = '/api';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private get userId(): number {
    return this.authService.getUser()?.userId ?? 0;
  }

  getTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.API_URL}/transactions?userId=${this.userId}`);
  }

  createTransaction(request: TransactionRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.API_URL}/transactions?userId=${this.userId}`, request);
  }
}
