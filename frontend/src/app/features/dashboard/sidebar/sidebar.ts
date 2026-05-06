import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar {
  username = '';

  constructor(private authService: AuthService) {
    const user = this.authService.getUser();
    this.username = user?.username ?? '';
  }

  logout() {
    this.authService.logout();
  }
}
