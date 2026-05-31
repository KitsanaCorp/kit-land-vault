import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GroupService, Group, GroupSummary } from '../../../core/services/group.service';
import { Sidebar } from '../../dashboard/sidebar/sidebar';
import { MobileHeader } from '../../dashboard/mobile-header/mobile-header';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-group-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, Sidebar, MobileHeader],
  templateUrl: './group-dashboard.html',
  styleUrl: './group-dashboard.scss'
})
export class GroupDashboard implements OnInit {
  groups: Group[] = [];
  summary: GroupSummary = {
    totalOwedToMe: 0,
    totalIOweToOthers: 0,
    overallNetBalance: 0,
    groupBalances: []
  };
  
  showModal = false;
  newGroupName = '';
  
  // Available users list for group membership selection
  availableUsers = [
    { id: 4, username: 'kit', selected: false },
    { id: 5, username: 'test_user1', selected: false },
    { id: 6, username: 'test_user2', selected: false },
    { id: 7, username: 'test_user3', selected: false },
    { id: 8, username: 'test_user4', selected: false },
    { id: 3, username: 'admin', selected: false }
  ];

  constructor(
    private groupService: GroupService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.groupService.getGroups().subscribe({
      next: (data) => this.groups = data,
      error: (err) => console.error('Failed to load groups', err)
    });

    this.groupService.getGroupSummary().subscribe({
      next: (data) => this.summary = data,
      error: (err) => console.error('Failed to load group summary', err)
    });
  }

  openCreateModal() {
    this.newGroupName = '';
    const currentUserId = this.authService.getUser()?.userId;
    this.availableUsers.forEach(u => {
      // Pre-select the current user, others unselected
      u.selected = u.id === currentUserId;
    });
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
  }

  submitGroup() {
    if (!this.newGroupName.trim()) return;

    const selectedMemberIds = this.availableUsers
      .filter(u => u.selected)
      .map(u => u.id);

    if (selectedMemberIds.length < 2) {
      alert('Please select at least 2 members for the group');
      return;
    }

    this.groupService.createGroup(this.newGroupName.trim(), selectedMemberIds).subscribe({
      next: (newGroup) => {
        this.closeModal();
        this.router.navigate(['/groups', newGroup.id]);
      },
      error: (err) => {
        console.error('Failed to create group', err);
        alert('Failed to create group. Please check connection.');
      }
    });
  }

  viewGroup(groupId: number) {
    this.router.navigate(['/groups', groupId]);
  }
}
