import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
// Remover FormsModule, pois ReactiveFormsModule já é suficiente.
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms'; 
import { Login } from '../login/login'; // Importar Login
import { SignUp } from '../sign-up/sign-up'; // Importar SignUp
import { AuthService } from '../../services/auth.service';

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
  
  private router = inject(Router);
  private authService = inject(AuthService);

  // ... (isLoggedIn, setView, handleLoginSuccess, goToSignupView, doSignup, logout, toggleBalance)

  // Métodos para a seção de preços (NOVOS)
  setHover(plan: PlanType) {
    this.hoverPlan = plan;
  }

  selectPlan(plan: PlanType) {
    this.selectedPlan = plan;
    alert(`Plano ${plan} selecionado!`);
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
    this.currentView = 'dashboard';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // Novo método para tratar o clique no link de cadastro (emitido pelo componente Login)
  goToSignupView() {
    this.currentView = 'signup';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // doLogin e doSignup removidos ou adaptados se necessário.
  // Mantendo o doSignup apenas como alerta, por enquanto
  doSignup(event?: Event) {
    if(event) event.preventDefault();
    alert('Cadastro realizado! Faça login.');
    this.currentView = 'login'; // Depois de cadastrar, volta para a tela de login
  }

  logout() {
    // Limpar dados do usuário
    this.authService.logout();
    
    // Redirecionar para a página de login
    this.currentView = 'landing';
    window.scrollTo({ top: 0, behavior: 'smooth' });
    
    // Se estiver em uma rota protegida, redirecionar para home
    this.router.navigate(['/']).catch(() => {
      // Se falhar, usar window.location
      window.location.href = '/';
    });
  }

  toggleBalance() {
    this.showBalance = !this.showBalance;
  }
}
