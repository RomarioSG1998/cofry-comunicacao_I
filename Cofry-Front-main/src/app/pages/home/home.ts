import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
// Remover FormsModule, pois ReactiveFormsModule já é suficiente.
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms'; 
import { Login } from '../login/login'; // Importar Login
import { SignUp } from '../sign-up/sign-up'; // Importar SignUp

// Definindo todos os estados possíveis da aplicação
type ViewState = 'landing' | 'about' | 'services' | 'prices' | 'login' | 'signup' | 'dashboard' | 'pix' | 'statement';
type PlanType = 'start' | 'pro' | 'black' | null;

@Component({
  selector: 'app-root',
  standalone: true,
  // IMPORTS CRITICOS: Adicionar Login e SignUp aqui para poderem ser usados no HTML
  imports: [CommonModule, ReactiveFormsModule, Login, SignUp], 
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
  currentView: ViewState = 'landing';
  showBalance = true;
  // Removido this.fb e os FormGroups de Login/Signup que estavam duplicados.
  hoverPlan: PlanType = null;
  selectedPlan: PlanType = 'start'; // Define 'start' como padrão

  // ... (isLoggedIn, setView, handleLoginSuccess, goToSignupView, doSignup, logout, toggleBalance)

  // Métodos para a seção de preços (NOVOS)
  setHover(plan: PlanType) {
    this.hoverPlan = plan;
  }

  selectPlan(plan: PlanType) {
    this.selectedPlan = plan;
    
  }
  isLoggedIn(): boolean {
    return ['dashboard', 'pix', 'statement'].includes(this.currentView);
  }

  setView(view: ViewState) {
    this.currentView = view;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // Novo método para tratar o sucesso do login (emitido pelo componente Login)
  handleLoginSuccess() {
    // Redireciona para a rota de dashboard após login bem-sucedido
    window.location.href = '/nav/Home';
  }

  // Novo método para tratar o clique no link de cadastro (emitido pelo componente Login)
  goToSignupView() {
    this.currentView = 'signup';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // Método chamado quando o signup é bem-sucedido
  doSignup(event?: Event) {
    if(event) event.preventDefault();
    // Muda para a view de login após cadastro bem-sucedido
    this.currentView = 'login';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  logout() {
    this.currentView = 'landing';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  toggleBalance() {
    this.showBalance = !this.showBalance;
  }
}



