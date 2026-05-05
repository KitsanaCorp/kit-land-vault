import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Bucket {
  name: string;
  balance: number;
  color: string;
  role: string;
}

@Component({
  selector: 'app-bucket-summary',
  imports: [CommonModule],
  templateUrl: './bucket-summary.html',
  styleUrl: './bucket-summary.scss'
})
export class BucketSummary {
  buckets: Bucket[] = [
    { name: 'BBL', balance: 0, color: '#1E3A5F', role: 'Transit' },
    { name: 'Kasikorn', balance: 18000, color: '#6B8E7B', role: 'Daily' },
    { name: 'LHB You', balance: 35000, color: '#E58E58', role: 'Bills' },
    { name: 'SCB', balance: 14283, color: '#5B428F', role: 'Car Loan' },
    { name: 'Kept', balance: 80000, color: '#5D9C96', role: 'Sinking Fund' },
    { name: 'Dime', balance: 12000, color: '#D96B6B', role: 'Investment' }
  ];
}
