import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { LoginService } from '../../services/login.service';
import { Router } from '@angular/router'; // <-- NOVO: Importa o Router

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
  private router = inject(Router); // <-- NOVO: Injeta o Router
  isLoading = false;

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]], // Apenas email
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
      
      // Obtém os valores do formulário e remove espaços em branco
      const email = (this.loginForm.get('email')?.value || '').trim();
      const password = (this.loginForm.get('password')?.value || '').trim();
      
      // Valida se os campos não estão vazios
      if (!email || !password) {
        this.isLoading = false;
        alert('Por favor, preencha todos os campos.');
        return;
      }
      
      const payload = {
        email: email,
        password: password
      };

      console.log('Enviando para API:', payload);

      this.loginService.login(payload).subscribe({
        
        // Em caso de sucesso
        next: (response) => {
          console.log('Login bem-sucedido!', response);
          this.isLoading = false;
          
          // Salvar dados do usuário no localStorage
          if (response.data) {
            localStorage.setItem('userId', response.data.userId.toString());
            localStorage.setItem('userEmail', response.data.email);
            localStorage.setItem('userData', JSON.stringify(response.data));
            
            // Dispara evento customizado para atualizar a navbar imediatamente
            window.dispatchEvent(new Event('userLoggedIn'));
          }
          
          // Emite evento para o componente Home redirecionar
          this.loginSuccess.emit();
        },
        
        // Em caso de erro
        error: (error) => {
          console.error('Erro no Login:', error);
          this.isLoading = false;
          
          // Exibe mensagem de erro amigável
          let errorMessage = 'Erro ao fazer login. Tente novamente.';
          
          if (error.error) {
            if (error.error.error) {
              errorMessage = error.error.error;
            } else if (error.error.message) {
              errorMessage = error.error.message;
            }
          }
          
          alert(errorMessage);
        },
        
        // Finaliza a subscrição
        complete: () => {
          console.log('Chamada de login concluída.');
        }
      });
      
    } else {
      this.loginForm.markAllAsTouched();
      alert('Por favor, preencha todos os campos corretamente.');
    }
  }

  onSignupClick() {
    // Emite evento para trocar o card para Signup
    this.goToSignup.emit();
  }
}



