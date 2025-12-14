import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { LoginService } from '../../services/login.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  // Eventos para o componente Pai (Home) saber o que fazer
  @Output() loginSuccess = new EventEmitter<void>();
  @Output() goToSignup = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private loginService = inject(LoginService);
  private authService = inject(AuthService);
  private router = inject(Router);
  isLoading = false;
  errorMessage = '';

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  // Helpers de validação visual
  isFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  isFieldValid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.valid && (field.dirty || field.touched));
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading = true;
      const payload = this.loginForm.value;

      this.loginService.login(payload).subscribe({
        
        // Em caso de sucesso
        next: (response) => {
          console.log('Login bem-sucedido!', response);
          this.isLoading = false;
          
          // Salvar dados do usuário usando o AuthService (notifica outros componentes)
          if (response && response.data) {
            const firstName = response.data.firstName || '';
            const email = response.data.email || '';
            const userId = response.data.userId || '';
            
            console.log('Login - Salvando dados do usuário:', { firstName, email, userId });
            console.log('Login - Resposta completa:', JSON.stringify(response, null, 2));
            
            // Salvar no AuthService (que salva no localStorage e notifica)
            this.authService.setUserData({
              firstName: firstName,
              email: email,
              userId: userId
            });
            
            // Verificar se foi salvo corretamente
            const savedName = localStorage.getItem('userName');
            const savedEmail = localStorage.getItem('userEmail');
            const savedUserId = localStorage.getItem('userId');
            console.log('Login - Dados salvos no localStorage:', { 
              userName: savedName, 
              userEmail: savedEmail, 
              userId: savedUserId 
            });
            
            if (!savedName) {
              console.error('ERRO: Nome não foi salvo no localStorage!');
            }
          } else {
            console.error('Login - Resposta do login sem dados:', response);
          }
          
          // Redireciona o usuário para a rota '/nav/Home'
          console.log('Login - Redirecionando para /nav/Home');
          
          // Aguardar um ciclo para garantir que o estado foi salvo
          setTimeout(() => {
            this.router.navigateByUrl('/nav/Home').catch((error) => {
              console.error('Login - Erro ao navegar com Router, usando window.location:', error);
              window.location.href = '/nav/Home';
            });
          }, 100);
          
          this.loginSuccess.emit();
        },
        
        // Em caso de erro
        error: (error) => {
          console.error('Erro no Login:', error);
          this.isLoading = false;
          
          // Extrair mensagem de erro da resposta
          if (error.error && error.error.message) {
            this.errorMessage = error.error.message;
          } else if (error.status === 0) {
            this.errorMessage = 'Erro de conexão. Verifique se o servidor está rodando.';
          } else if (error.status === 401) {
            this.errorMessage = 'Email ou senha inválidos.';
          } else {
            this.errorMessage = 'Erro ao fazer login. Tente novamente.';
          }
        },
        
        // Finaliza a subscrição
        complete: () => {
          console.log('Chamada de login concluída.');
        }
      });
      
    } else {
      this.loginForm.markAllAsTouched();
    }
  }

  onSignupClick() {
    // Emite evento para trocar o card para Signup
    this.goToSignup.emit();
  }
}
