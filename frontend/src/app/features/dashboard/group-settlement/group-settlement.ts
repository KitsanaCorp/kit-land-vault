import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { GroupService, GroupSummary } from '../../../core/services/group.service';

@Component({
  selector: 'app-group-settlement',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './group-settlement.html',
  styleUrl: './group-settlement.scss'
})
export class GroupSettlement implements OnInit {
  summary: GroupSummary = {
    totalOwedToMe: 0,
    totalIOweToOthers: 0,
    overallNetBalance: 0,
    groupBalances: []
  };
  loading = true;

  constructor(private groupService: GroupService, private router: Router) {}

  ngOnInit() {
    this.loadBalances();
  }

  loadBalances() {
    this.loading = true;
    this.groupService.getGroupSummary().subscribe({
      next: (data) => {
        this.summary = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load group summary on dashboard', err);
        this.loading = false;
      }
    });
  }

  goToGroups() {
    this.router.navigate(['/groups']);
  }
}
