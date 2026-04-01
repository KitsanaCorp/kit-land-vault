import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-partner-settlement',
  imports: [CommonModule],
  templateUrl: './partner-settlement.html',
  styleUrl: './partner-settlement.scss'
})
export class PartnerSettlement {
  youOwe = 1250;
  partnerOwes = 750;
}
