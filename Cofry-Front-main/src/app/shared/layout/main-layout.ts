import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { Navbar } from '../navbar/navbar'; // Assumindo que este import está correto

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, Navbar, RouterOutlet],
  template: `
    <app-navbar></app-navbar> 
    <main> 
      <router-outlet></router-outlet>
    </main>
  `
})
export class MainLayout {
    // ... navItems estão corretos, mas o path no AfterLogin deve ser resolvido no roteador
}