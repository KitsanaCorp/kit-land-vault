import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Bucket {
  name: string;
  balance: number;
  color: string;
}

@Component({
  selector: 'app-bucket-summary',
  imports: [CommonModule],
  templateUrl: './bucket-summary.html',
  styleUrl: './bucket-summary.scss'
})
export class BucketSummary {
  buckets: Bucket[] = [
    { name: 'SCB', balance: 45000, color: '#5B428F' },
    { name: 'LHB', balance: 3000, color: '#E58E58' },
    { name: 'Kept', balance: 80000, color: '#5D9C96' },
    { name: 'DIME', balance: 12000, color: '#D96B6B' },
    { name: 'K Mobile', balance: 5200, color: '#6B8E7B' }
  ];
}
