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
    <router-outlet></router-outlet>
  `
})
export class MainLayout {
  constructor() {
    console.log('MainLayout - Componente carregado');
  }
}