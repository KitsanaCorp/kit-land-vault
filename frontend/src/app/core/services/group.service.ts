import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface GroupMember {
  id: number;
  username: string;
  netBalance: number; // positive if owed, negative if owes
}

export interface Group {
  id: number;
  name: string;
  members: GroupMember[];
  myNetBalance: number;
  createdBy?: string;
  createdAt?: string;
}

export interface SplitDetail {
  id: number;
  userId: number;
  username: string;
  shareAmount: number;
  status: string; // 'PENDING' | 'SETTLED'
  settledAt?: string;
}

export interface GroupTransaction {
  id: number;
  groupId: number;
  groupName: string;
  payerId: number;
  payerName: string;
  amount: number;
  description: string;
  categoryId: number;
  categoryName: string;
  walletId?: number;
  walletName?: string;
  transactionDate: string;
  splits: SplitDetail[];
  createdBy?: string;
  createdAt?: string;
}

export interface GroupBalanceDetail {
  groupId: number;
  groupName: string;
  netBalance: number;
}

export interface GroupSummary {
  totalOwedToMe: number;
  totalIOweToOthers: number;
  overallNetBalance: number;
  groupBalances: GroupBalanceDetail[];
}

@Injectable({ providedIn: 'root' })
export class GroupService {
  private readonly API_URL = '/api/groups';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private get userId(): number {
    return this.authService.getUser()?.userId ?? 0;
  }

  createGroup(name: string, memberIds: number[]): Observable<Group> {
    return this.http.post<Group>(`${this.API_URL}?userId=${this.userId}`, { name, memberIds });
  }

  getGroups(): Observable<Group[]> {
    return this.http.get<Group[]>(`${this.API_URL}?userId=${this.userId}`);
  }

  getGroupDetails(id: number): Observable<Group> {
    return this.http.get<Group>(`${this.API_URL}/${id}?userId=${this.userId}`);
  }

  getGroupTransactions(id: number): Observable<GroupTransaction[]> {
    return this.http.get<GroupTransaction[]>(`${this.API_URL}/${id}/transactions?userId=${this.userId}`);
  }

  createGroupTransaction(
    groupId: number,
    payerId: number,
    amount: number,
    description: string,
    categoryId: number,
    walletId?: number,
    transactionDate?: string
  ): Observable<GroupTransaction> {
    const payload = {
      payerId,
      amount,
      description,
      categoryId,
      walletId,
      transactionDate: transactionDate || new Date().toISOString().split('T')[0]
    };
    return this.http.post<GroupTransaction>(`${this.API_URL}/${groupId}/transactions?userId=${this.userId}`, payload);
  }

  settleSplit(groupId: number, splitId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${groupId}/splits/${splitId}/settle?userId=${this.userId}`, {});
  }

  getGroupSummary(): Observable<GroupSummary> {
    return this.http.get<GroupSummary>(`${this.API_URL}/summary?userId=${this.userId}`);
  }
}
