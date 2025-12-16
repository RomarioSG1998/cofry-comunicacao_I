import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SignUpService } from '../../services/sign-up.service';
import { AddressService, State } from '../../services/address.service';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  templateUrl: './sign-up.html',
  styleUrls: ['./sign-up.css'],
  imports: [CommonModule, ReactiveFormsModule] 
})
export class SignUp implements OnInit {
  @Output() signupSuccess = new EventEmitter<void>();
  @Output() goToLogin = new EventEmitter<void>();

  currentStep = 1;
  totalSteps = 3;
  isLoading = false;
  isLoadingCep = false;
  Math = Math; // Para usar no template

  // Formulários
  personalDataForm: FormGroup;
  addressForm: FormGroup;
  accountForm: FormGroup;

  // Dados para selects
  states: State[] = [];
  cities: string[] = [];
  accountTypes = [
    { value: 'CHECKING', label: 'Conta Corrente' },
    { value: 'SAVINGS', label: 'Conta Poupança' }
  ];
  
  // Planos disponíveis
  plans = [
    { id: 1, name: 'Cofry Start', price: 0, description: 'Para começar com o essencial' },
    { id: 2, name: 'Cofry Pro', price: 7.77, description: 'Para aproveitar benefícios e cashback' },
    { id: 3, name: 'Cofry Black', price: 49.90, description: 'Exclusividade máxima e benefícios premium' }
  ];

  // Bancos brasileiros
  banks = [
    { code: '001', name: 'Banco do Brasil' },
    { code: '033', name: 'Banco Santander' },
    { code: '104', name: 'Caixa Econômica Federal' },
    { code: '237', name: 'Banco Bradesco' },
    { code: '341', name: 'Banco Itaú' },
    { code: '356', name: 'Banco Real' },
    { code: '422', name: 'Banco Safra' },
    { code: '748', name: 'Banco Sicredi' },
    { code: '756', name: 'Bancoob' }
  ];

  // Dados salvos entre steps
  savedUserId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private signUpService: SignUpService,
    private addressService: AddressService,
    private accountService: AccountService
  ) {
    // Step 1: Dados Pessoais (inclui plano)
    this.personalDataForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      cpf: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      dateOfBirth: ['', Validators.required],
      phoneNumber: [''],
      planId: [1, [Validators.required]] // Plano agora é do usuário, não da conta
    });

    // Step 2: Endereço
    this.addressForm = this.fb.group({
      zipCode: ['', [Validators.required]],
      street: ['', [Validators.required]],
      number: ['', [Validators.required]],
      complement: [''],
      district: ['', [Validators.required]],
      city: ['', [Validators.required]],
      state: ['', [Validators.required]]
    });

    // Step 3: Conta Bancária
    this.accountForm = this.fb.group({
      bankCode: ['', [Validators.required]], // Código do banco
      bankName: ['', [Validators.required]], // Nome do banco (preenchido automaticamente)
      accountType: ['CHECKING', [Validators.required]],
      agency: ['', [Validators.required]], // Mudou de agencyNumber para agency
      accountNumber: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadStates();
  }

  // Navegação entre steps
  nextStep(): void {
    if (this.currentStep === 1 && this.personalDataForm.valid) {
      this.currentStep = 2;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else if (this.currentStep === 2 && this.addressForm.valid) {
      this.currentStep = 3;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      // Marca campos como touched para mostrar erros
      if (this.currentStep === 1) {
        this.personalDataForm.markAllAsTouched();
      } else if (this.currentStep === 2) {
        this.addressForm.markAllAsTouched();
      }
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  // Busca CEP
  onCepBlur(): void {
    const zipCode = this.addressForm.get('zipCode')?.value?.trim();
    if (zipCode && zipCode.length >= 8) {
      this.isLoadingCep = true;
      this.addressService.lookupByZipCode(zipCode).subscribe({
        next: (address) => {
          this.addressForm.patchValue({
            street: address.street,
            district: address.district,
            city: address.city,
            state: address.state,
            zipCode: address.zipCode
          });
          this.loadCities(address.state);
          this.isLoadingCep = false;
        },
        error: () => {
          this.isLoadingCep = false;
          // CEP não encontrado, usuário preencherá manualmente
        }
      });
    }
  }

  // Carrega estados
  loadStates(): void {
    this.addressService.getStates().subscribe({
      next: (states) => {
        this.states = states;
      },
      error: (err) => {
        console.error('Erro ao carregar estados:', err);
      }
    });
  }

  // Carrega cidades quando estado muda
  onStateChange(): void {
    const state = this.addressForm.get('state')?.value;
    if (state) {
      this.loadCities(state);
      this.addressForm.patchValue({ city: '' });
    }
  }

  loadCities(state: string): void {
    this.addressService.getCitiesByState(state).subscribe({
      next: (cities) => {
        this.cities = cities.map(c => c.name);
      },
      error: (err) => {
        console.error('Erro ao carregar cidades:', err);
      }
    });
  }

  // Preenche bankName quando bankCode é selecionado
  onBankChange(): void {
    const bankCode = this.accountForm.get('bankCode')?.value;
    if (bankCode) {
      const bank = this.banks.find(b => b.code === bankCode);
      if (bank) {
        this.accountForm.patchValue({ bankName: bank.name });
      }
    }
  }

  // Submissão final
  onSubmit(): void {
    if (this.accountForm.invalid) {
      this.accountForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    // Step 1: Criar usuário (com planId do step 1)
    const planId = Number(this.personalDataForm.get('planId')?.value) || 1;
    const userData = {
      fullName: this.personalDataForm.get('fullName')?.value?.trim(),
      email: this.personalDataForm.get('email')?.value?.trim(),
      cpf: this.personalDataForm.get('cpf')?.value?.trim(),
      password: this.personalDataForm.get('password')?.value,
      dateOfBirth: this.personalDataForm.get('dateOfBirth')?.value,
      phoneNumber: this.personalDataForm.get('phoneNumber')?.value?.trim() || undefined,
      planId: planId
    };

    console.log('Criando usuário:', userData);

    this.signUpService.register(userData).subscribe({
      next: (userResponse) => {
        const userId = userResponse.userId || (userResponse as any).id;
        
        if (!userId) {
          this.isLoading = false;
          alert('Erro: ID do usuário não retornado. Tente novamente.');
          return;
        }

        this.savedUserId = userId;
        
        // Salvar dados do usuário
        localStorage.setItem('userId', userId.toString());
        if (userResponse.email) {
          localStorage.setItem('userEmail', userResponse.email);
        }

        // Step 2: Criar endereço
        const addressData = {
          userId: userId,
          street: this.addressForm.get('street')?.value?.trim(),
          number: this.addressForm.get('number')?.value?.trim(),
          complement: this.addressForm.get('complement')?.value?.trim() || undefined,
          district: this.addressForm.get('district')?.value?.trim(),
          city: this.addressForm.get('city')?.value?.trim(),
          state: this.addressForm.get('state')?.value,
          zipCode: this.addressForm.get('zipCode')?.value?.trim()
        };

        console.log('Criando endereço:', addressData);

        this.addressService.createAddress(addressData).subscribe({
          next: () => {
            // Step 3: Criar conta (sem balance)
            const bankCode = this.accountForm.get('bankCode')?.value?.trim();
            const bank = this.banks.find(b => b.code === bankCode);
            const accountData = {
              userId: userId,
              bankCode: bankCode,
              bankName: bank ? bank.name : this.accountForm.get('bankName')?.value?.trim(),
              accountNumber: this.accountForm.get('accountNumber')?.value?.trim(),
              agency: this.accountForm.get('agency')?.value?.trim(),
              accountType: this.accountForm.get('accountType')?.value
            };

            console.log('Criando conta:', accountData);

            this.accountService.createAccountFromForm(accountData).subscribe({
              next: () => {
                this.isLoading = false;
                alert('Cadastro realizado com sucesso!');
                this.goToLogin.emit();
              },
              error: (err) => {
                console.error('Erro ao criar conta:', err);
                this.isLoading = false;
                alert('Usuário e endereço criados, mas houve erro ao criar conta. Entre em contato com o suporte.');
                this.goToLogin.emit();
              }
            });
          },
          error: (err) => {
            console.error('Erro ao criar endereço:', err);
            this.isLoading = false;
            alert('Usuário criado, mas houve erro ao criar endereço. Você pode atualizar depois.');
            this.goToLogin.emit();
          }
        });
      },
      error: (err) => {
        console.error('Erro ao criar usuário:', err);
        this.isLoading = false;
        let errorMessage = 'Erro ao cadastrar. Verifique os dados e tente novamente.';
        if (err.error?.error) {
          errorMessage = err.error.error;
        }
        alert(errorMessage);
      }
    });
  }

  // Helpers de validação
  isFieldInvalid(form: FormGroup, fieldName: string): boolean {
    const field = form.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  isFieldValid(form: FormGroup, fieldName: string): boolean {
    const field = form.get(fieldName);
    return !!(field && field.valid && (field.dirty || field.touched));
  }
}