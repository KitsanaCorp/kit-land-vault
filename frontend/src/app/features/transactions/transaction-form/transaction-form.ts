import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CategoryService, Category } from '../../../core/services/category.service';
import { WalletService, Wallet } from '../../../core/services/wallet.service';
import { TransactionService, TransactionRequest } from '../../../core/services/transaction.service';

@Component({
  selector: 'app-transaction-form',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transaction-form.html',
  styleUrl: './transaction-form.scss'
})
export class TransactionForm implements OnInit {
  form: FormGroup;
  categories: Category[] = [];
  wallets: Wallet[] = [];
  
  loadingData = true;
  saving = false;
  errorMessage = '';

  splitTypes = [
    { value: 'PERSONAL', label: 'Personal (จ่ายคนเดียว)' },
    { value: 'SHARED', label: 'Shared (ออกคนละครึ่ง)' },
    { value: 'ON_BEHALF', label: 'On Behalf (ออกให้ก่อน)' }
  ];

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private walletService: WalletService,
    private transactionService: TransactionService,
    private location: Location,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    const today = new Date().toISOString().split('T')[0];
    this.form = this.fb.group({
      amount: ['', [Validators.required, Validators.min(0.01)]],
      categoryId: ['', Validators.required],
      walletId: ['', Validators.required],
      splitType: ['PERSONAL', Validators.required],
      transactionDate: [today, Validators.required],
      description: ['']
    });
  }

  ngOnInit() {
    console.log('ngOnInit started');
    let catsLoaded = false;
    let walletsLoaded = false;

    const checkDone = () => {
      console.log('checkDone', { catsLoaded, walletsLoaded });
      if (catsLoaded && walletsLoaded) {
        this.loadingData = false;
        this.cdr.detectChanges();
      }
    };

    console.log('Fetching categories...');
    this.categoryService.getCategories().subscribe({
      next: (res) => {
        console.log('Categories fetched:', res);
        this.categories = res || [];
        catsLoaded = true;
        checkDone();
      },
      error: (err) => {
        console.error('Error fetching categories:', err);
        this.errorMessage = 'Failed to load categories.';
        catsLoaded = true;
        checkDone();
      }
    });

    console.log('Fetching wallets...');
    this.walletService.getWallets().subscribe({
      next: (res) => {
        console.log('Wallets fetched:', res);
        this.wallets = res || [];
        walletsLoaded = true;
        checkDone();
      },
      error: (err) => {
        console.error('Error fetching wallets:', err);
        this.errorMessage = 'Failed to load wallets.';
        walletsLoaded = true;
        checkDone();
      }
    });
  }

  goBack() {
    this.location.back();
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const req: TransactionRequest = this.form.value;

    this.transactionService.createTransaction(req).subscribe({
      next: () => {
        this.saving = false;
        // Go back to dashboard after saving
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = 'Failed to save transaction.';
        console.error(err);
      }
    });
  }
}
