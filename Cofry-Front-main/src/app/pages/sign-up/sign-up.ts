import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SignUpService } from '../../services/sign-up.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true, // Define como Standalone
  templateUrl: './sign-up.html',
  styleUrls: ['./sign-up.css'],
  // IMPORTS CRITICOS: Sem isso o form e o router não funcionam no HTML
  imports: [CommonModule, ReactiveFormsModule] 
})
export class SignUp {


  signupForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private signUpService: SignUpService,
    private authService: AuthService,
    private router: Router
  ) 
  {
    // Definindo o Modelo do Formulário (Model-Driven)
    this.signupForm = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.\d{3}\.\d{3}\-\d{2}$/)]], // Ex: regex simples
      dateOfBirth: ['', Validators.required],// Obrigatório estar marcado (true)
    });
  }

  formatCPF(event: any) {
    const input = event.target.value;
    let clean = input.replace(/\D/g, '');
    if (clean.length > 11) {
      clean = clean.substring(0, 11);
    }
    
    let formatted = '';
    if (clean.length > 9) {
      formatted = `${clean.substring(0, 3)}.${clean.substring(3, 6)}.${clean.substring(6, 9)}-${clean.substring(9)}`;
    } else if (clean.length > 6) {
      formatted = `${clean.substring(0, 3)}.${clean.substring(3, 6)}.${clean.substring(6)}`;
    } else if (clean.length > 3) {
      formatted = `${clean.substring(0, 3)}.${clean.substring(3)}`;
    } else {
      formatted = clean;
    }
    
    this.signupForm.patchValue({ cpf: formatted });
  }

  onSubmit() {
    // Valida o formulário antes de enviar
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return;
    }

    // 2. Prepara o objeto para o Java (JSON)
    // O Java espera os nomes exatos das variáveis da classe User
    const userData = {
      fullName: this.signupForm.get('nome')?.value,  // Mapeando 'nome' do form para 'name' do Java
      email: this.signupForm.get('email')?.value,
      password: this.signupForm.get('password')?.value, // Mapeando 'senha' para 'password'
      taxId: this.signupForm.get('cpf')?.value,
      dateOfBirth: this.signupForm.get('dateOfBirth')?.value
    };

    console.log('Enviando para API:', userData);

    this.signUpService.register(userData).subscribe({
      next: (res) => {
        // Salvar dados do usuário usando o AuthService (notifica outros componentes)
        if (res.data) {
          this.authService.setUserData({
            firstName: res.data.firstName,
            email: res.data.email,
            userId: res.data.userId,
            token: res.data.token
          });
        }
        alert('Conta criada com sucesso!');
        this.router.navigate(['/nav/Home']);
      },
      error: (err) => {
        console.error('Erro:', err);
        alert('Erro ao cadastrar.');
      }
    });
  }
}