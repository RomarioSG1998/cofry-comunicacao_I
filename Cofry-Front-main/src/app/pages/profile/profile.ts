import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService, User, UserCompleteDTO } from '../../services/user.service';
import { AddressService, Address, State } from '../../services/address.service';
import { AccountService, Account } from '../../services/account.service';
import { PlanService } from '../../services/plan.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class Profile implements OnInit {
  private router = inject(Router);
  private userService = inject(UserService);
  private addressService = inject(AddressService);
  private accountService = inject(AccountService);
  private planService = inject(PlanService);
  private fb = inject(FormBuilder);
  private platformId = inject(PLATFORM_ID);
  private cdr = inject(ChangeDetectorRef);

  // Dados do usuário
  user: User | null = null;
  address: Address | null = null;
  accounts: Account[] = []; // Lista de todas as contas
  selectedAccount: Account | null = null; // Conta selecionada para edição
  userComplete: UserCompleteDTO | null = null; // Dados completos do usuário
  
  // Estados
  isLoading = false;
  isEditingPersonal = false;
  isEditingAddress = false;
  isEditingAccount = false;
  isCreatingAccount = false; // Novo estado para criação
  isLoadingCep = false;

  // Formulários
  personalForm: FormGroup;
  addressForm: FormGroup;
  accountForm: FormGroup;
  passwordForm: FormGroup;

  // Dados para selects
  states: State[] = [];
  cities: string[] = [];
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

  constructor() {
    // Formulário de dados pessoais
    this.personalForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      taxId: ['', [Validators.required]], // CPF pode ser editado
      dateOfBirth: ['', [Validators.required]], // Data pode ser editada
      phoneNumber: [''],
      email: ['', [Validators.required, Validators.email]] // Email pode ser editado
    });

    // Formulário de endereço
    this.addressForm = this.fb.group({
      zipCode: ['', [Validators.required]],
      street: ['', [Validators.required]],
      number: ['', [Validators.required, Validators.minLength(1)]], // Obrigatório e não pode ser vazio
      complement: [''],
      district: ['', [Validators.required]],
      city: [{ value: '', disabled: true }, [Validators.required]], // Desabilitado até selecionar estado
      state: ['', [Validators.required]]
    });

    // Formulário de conta bancária
    this.accountForm = this.fb.group({
      bankCode: ['', [Validators.required]], // Código do banco
      bankName: ['', [Validators.required]], // Nome do banco
      accountNumber: ['', [Validators.required]],
      agency: ['', [Validators.required]], // Mudou de agencyNumber para agency
      accountType: ['CHECKING', [Validators.required]]
    });

    // Formulário de senha
    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    const userId = localStorage.getItem('userId');
    if (userId) {
      // Carrega todos os dados do usuário após login
      this.loadAllUserData(Number(userId));
    } else {
      // Se não houver userId, redireciona para login
      this.router.navigate(['/login']);
    }
  }

  /**
   * Carrega todos os dados do usuário: pessoais, endereço e contas bancárias
   * Usa o endpoint completo /api/users/{id}/complete para obter todos os dados de uma vez
   */
  loadAllUserData(userId: number): void {
    this.isLoading = true;
    
    // Carrega estados primeiro (necessário para o formulário de endereço)
    this.loadStates();
    
    // Usa o endpoint completo para obter todos os dados de uma vez
    this.userService.getUserComplete(userId).subscribe({
      next: (completeData) => {
        this.userComplete = completeData;
        
        // Mapeia dados completos para o objeto User (para compatibilidade)
        this.user = {
          userId: userId,
          planId: completeData.planId,
          firstName: completeData.firstName,
          lastName: completeData.lastName,
          taxId: completeData.cpf,
          email: completeData.email,
          phoneNumber: completeData.phoneNumber || undefined,
          dateOfBirth: completeData.dateOfBirth,
          isActive: true,
          createdAt: '',
          updatedAt: ''
        };
        
        // Preenche formulário de dados pessoais
        this.personalForm.patchValue({
          firstName: completeData.firstName,
          lastName: completeData.lastName,
          taxId: completeData.cpf,
          dateOfBirth: completeData.dateOfBirth,
          phoneNumber: completeData.phoneNumber || '',
          email: completeData.email
        });
        
        // Processa endereços
        if (completeData.addresses && completeData.addresses.length > 0) {
          // Pega o primeiro endereço (ou o mais recente)
          this.address = completeData.addresses[0];
          
          // Preenche o formulário com os dados do endereço completo
          this.addressForm.patchValue({
            zipCode: this.address.zipCode || '',
            street: this.address.street || '',
            number: this.address.number || '',
            complement: this.address.complement || '',
            district: this.address.district || '',
            city: this.address.city || '',
            state: this.address.state || ''
          });
          
          // Habilita o campo cidade e carrega as cidades se houver estado
          if (this.address.state) {
            const cityControl = this.addressForm.get('city');
            cityControl?.enable();
            this.loadCities(this.address.state);
          }
        } else {
          this.address = null;
        }
        
        // Processa contas bancárias (aplica correção UTF-8 se necessário)
        this.accounts = (completeData.accounts || []).map(account => ({
          ...account,
          bankName: this.accountService.fixUtf8Encoding(account.bankName || '')
        }));
        
        if (this.accounts.length > 0) {
          this.selectedAccount = this.accounts[0]; // Seleciona a primeira por padrão
        }
        
        this.isLoading = false;
        this.cdr.detectChanges(); // Força detecção de mudanças
      },
      error: (error) => {
        console.error('Erro ao carregar dados completos do usuário:', error);
        // Em caso de erro, tenta carregar dados separadamente (fallback)
        this.loadUserData(userId);
        this.loadAddress(userId);
        this.loadAccount(userId);
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
      }
    });
  }

  /**
   * Volta para a página After Login
   */
  goBack(): void {
    this.router.navigate(['/nav/Home']);
  }

  loadUserData(userId: number): void {
    this.isLoading = true;
    this.userService.getUserById(userId).subscribe({
      next: (user) => {
        this.user = user;
        this.personalForm.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          taxId: user.taxId,
          dateOfBirth: user.dateOfBirth,
          phoneNumber: user.phoneNumber || '',
          email: user.email
        });
        this.isLoading = false;
        this.cdr.detectChanges(); // Força detecção de mudanças
      },
      error: (error) => {
        console.error('Erro ao carregar dados do usuário:', error);
        this.isLoading = false;
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
      }
    });
  }

  loadAddress(userId: number): void {
    this.addressService.getAddressesByUserId(userId).subscribe({
      next: (addresses) => {
        if (addresses && addresses.length > 0) {
          // Pega o primeiro endereço (ou o mais recente)
          this.address = addresses[0];
          
          // Preenche o formulário com os dados do endereço completo
          this.addressForm.patchValue({
            zipCode: this.address.zipCode || '',
            street: this.address.street || '',
            number: this.address.number || '',
            complement: this.address.complement || '',
            district: this.address.district || '',
            city: this.address.city || '',
            state: this.address.state || ''
          });
          
          // Habilita o campo cidade e carrega as cidades se houver estado
          if (this.address.state) {
            const cityControl = this.addressForm.get('city');
            cityControl?.enable();
            this.loadCities(this.address.state);
          }
        }
        // Verifica se todos os dados foram carregados
        this.checkLoadingComplete();
        this.cdr.detectChanges(); // Força detecção de mudanças
      },
      error: (error) => {
        console.error('Erro ao carregar endereço:', error);
        // Não mostra erro se for 404 (endereço não encontrado)
        if (error.status !== 404) {
          console.error('Erro ao buscar endereço do usuário:', error);
        }
        // Mesmo sem endereço, verifica se pode finalizar o loading
        this.checkLoadingComplete();
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
      }
    });
  }

  loadAccount(userId: number): void {
    this.accountService.getAccountsByUserId(userId).subscribe({
      next: (accounts) => {
        this.accounts = accounts || [];
        if (this.accounts.length > 0) {
          this.selectedAccount = this.accounts[0]; // Seleciona a primeira por padrão
        }
        // Verifica se todos os dados foram carregados
        this.checkLoadingComplete();
        this.cdr.detectChanges(); // Força detecção de mudanças
      },
      error: (error) => {
        console.error('Erro ao carregar contas:', error);
        this.accounts = [];
        // Mesmo sem contas, verifica se pode finalizar o loading
        this.checkLoadingComplete();
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
      }
    });
  }

  /**
   * Verifica se todos os dados foram carregados e desabilita o loading
   */
  private checkLoadingComplete(): void {
    // Aguarda um pequeno delay para garantir que todas as requisições terminem
    setTimeout(() => {
      this.isLoading = false;
      this.cdr.detectChanges(); // Força detecção de mudanças após finalizar loading
    }, 100);
  }

  loadStates(): void {
    this.addressService.getStates().subscribe({
      next: (states) => {
        this.states = states;
      },
      error: (error) => {
        console.error('Erro ao carregar estados:', error);
      }
    });
  }

  // Métodos de edição
  startEditingPersonal(): void {
    this.isEditingPersonal = true;
  }

  cancelEditingPersonal(): void {
    if (this.user) {
      this.personalForm.patchValue({
        firstName: this.user.firstName,
        lastName: this.user.lastName,
        taxId: this.user.taxId,
        dateOfBirth: this.user.dateOfBirth,
        phoneNumber: this.user.phoneNumber || '',
        email: this.user.email
      });
    }
    this.isEditingPersonal = false;
  }

  savePersonalData(): void {
    if (this.personalForm.valid && this.user) {
      this.isLoading = true;
      
      // Separa os campos: alguns vão para /api/users/{id} e outros para /api/form/user/{id}
      const email = this.personalForm.get('email')?.value?.trim();
      const taxId = this.personalForm.get('taxId')?.value?.trim();
      const dateOfBirth = this.personalForm.get('dateOfBirth')?.value;
      
      // Dados básicos (firstName, lastName, phoneNumber, email, taxId, dateOfBirth) - a API exige todos
      const basicData: any = {
        firstName: this.personalForm.get('firstName')?.value?.trim() || '',
        lastName: this.personalForm.get('lastName')?.value?.trim() || '',
        phoneNumber: this.personalForm.get('phoneNumber')?.value?.trim() || undefined
      };
      
      // Email é obrigatório na rota /api/users/{id}
      if (email) {
        basicData.email = email;
      }
      
      // CPF também é obrigatório na rota /api/users/{id}
      if (taxId) {
        basicData.taxId = taxId;
      }
      
      // Data de nascimento também é obrigatória na rota /api/users/{id}
      if (dateOfBirth) {
        basicData.dateOfBirth = dateOfBirth;
      }
      
      // Dados para a rota de formulário (CPF, email, data de nascimento)
      // Só envia campos que foram alterados
      const formData: { cpf?: string; email?: string; dateOfBirth?: string } = {};
      
      // CPF: só envia se foi alterado e não está vazio
      if (taxId && taxId.trim() !== '' && this.user.taxId !== taxId.trim()) {
        formData.cpf = taxId.trim();
      }
      
      // Email: só envia se foi alterado
      if (email && email.trim() !== '' && this.user.email !== email.trim()) {
        formData.email = email.trim();
      }
      
      // Data de nascimento: só envia se foi alterada
      if (dateOfBirth && this.user.dateOfBirth !== dateOfBirth) {
        formData.dateOfBirth = dateOfBirth;
      }
      
      // Valida se os campos obrigatórios estão preenchidos
      if (!basicData.firstName || !basicData.lastName || !email || !taxId || !dateOfBirth) {
        this.isLoading = false;
        alert('Nome, sobrenome, email, CPF e data de nascimento são obrigatórios!');
        return;
      }
      
      // Atualiza dados básicos primeiro (firstName, lastName, phoneNumber)
      this.userService.updateUser(this.user.userId, basicData).subscribe({
        next: (updatedUser) => {
          // Se houver dados para atualizar via formulário, faz a segunda chamada
          if (Object.keys(formData).length > 0 && this.user) {
            this.userService.updateUserFromForm(this.user.userId, formData).subscribe({
              next: (finalUser) => {
                this.user = finalUser;
                this.isEditingPersonal = false;
                this.isLoading = false;
                alert('Dados pessoais atualizados com sucesso!');
              },
              error: (error) => {
                console.error('Erro ao atualizar dados via formulário:', error);
                this.isLoading = false;
                
                // Mesmo com erro na segunda chamada, os dados básicos foram atualizados
                this.user = updatedUser;
                this.isEditingPersonal = false;
                
                let errorMessage = 'Dados básicos atualizados, mas houve erro ao atualizar CPF/Email/Data de nascimento.';
                if (error.error?.error) {
                  errorMessage = `Dados básicos atualizados.\n\nErro ao atualizar: ${error.error.error}`;
                } else if (error.error?.message) {
                  errorMessage = `Dados básicos atualizados.\n\nErro: ${error.error.message}`;
                }
                alert(errorMessage);
              }
            });
          } else {
            // Se não há dados para atualizar via formulário, apenas atualiza o estado
            this.user = updatedUser;
            this.isEditingPersonal = false;
            this.isLoading = false;
            alert('Dados pessoais atualizados com sucesso!');
          }
        },
        error: (error) => {
          console.error('Erro ao atualizar dados básicos:', error);
          this.isLoading = false;
          
          let errorMessage = 'Erro ao atualizar dados. Tente novamente.';
          if (error.error?.error) {
            errorMessage = error.error.error;
          } else if (error.error?.message) {
            errorMessage = error.error.message;
          }
          
          alert(errorMessage);
        }
      });
    } else {
      this.personalForm.markAllAsTouched();
    }
  }

  startEditingAddress(): void {
    this.isEditingAddress = true;
    const cityControl = this.addressForm.get('city');
    
    if (this.address) {
      this.addressForm.patchValue({
        zipCode: this.address.zipCode,
        street: this.address.street,
        number: this.address.number,
        complement: this.address.complement || '',
        district: this.address.district,
        city: this.address.city,
        state: this.address.state
      });
      
      // Habilita o campo city se houver estado
      if (this.address.state) {
        cityControl?.enable();
        this.loadCities(this.address.state);
      }
    } else {
      // Se não há endereço, habilita city apenas quando estado for selecionado
      cityControl?.disable();
    }
  }

  cancelEditingAddress(): void {
    this.isEditingAddress = false;
  }

  saveAddress(): void {
    // Habilita o campo city para obter o valor
    const cityControl = this.addressForm.get('city');
    if (cityControl?.disabled) {
      cityControl.enable();
    }
    
    // Marca todos os campos como touched para mostrar erros
    this.addressForm.markAllAsTouched();
    
    if (this.addressForm.valid && this.user) {
      this.isLoading = true;
      
      // Obtém valores diretamente dos controles para garantir que pegue valores de campos desabilitados
      const streetControl = this.addressForm.get('street');
      const numberControl = this.addressForm.get('number');
      const complementControl = this.addressForm.get('complement');
      const districtControl = this.addressForm.get('district');
      const cityControl = this.addressForm.get('city');
      const stateControl = this.addressForm.get('state');
      const zipCodeControl = this.addressForm.get('zipCode');
      
      const street = streetControl?.value?.trim() || '';
      const number = numberControl?.value?.trim() || '';
      const complement = complementControl?.value?.trim() || '';
      const district = districtControl?.value?.trim() || '';
      const city = cityControl?.value?.trim() || '';
      const state = stateControl?.value || '';
      const zipCode = zipCodeControl?.value?.trim() || '';
      
      // Validação específica do número (campo crítico)
      if (!number || number.length === 0) {
        this.isLoading = false;
        alert('O número do endereço é obrigatório!');
        numberControl?.markAsTouched();
        numberControl?.setErrors({ required: true });
        return;
      }
      
      // Valida se todos os campos obrigatórios estão preenchidos
      if (!street || !district || !city || !state || !zipCode) {
        this.isLoading = false;
        alert('Por favor, preencha todos os campos obrigatórios!');
        return;
      }
      
      // Garante que o número não está vazio antes de enviar
      const finalNumber = number.trim();
      if (!finalNumber || finalNumber.length === 0) {
        this.isLoading = false;
        alert('O número do endereço não pode estar vazio!');
        numberControl?.markAsTouched();
        numberControl?.setErrors({ required: true });
        return;
      }
      
      // Debug: log dos dados antes de enviar
      console.log('Dados do endereço a serem enviados:', {
        userId: this.user.userId,
        street: street.trim(),
        number: finalNumber,
        complement: complement.trim() || undefined,
        district: district.trim(),
        city: city.trim(),
        state: state,
        zipCode: zipCode.trim()
      });
      
      const addressData: Address = {
        userId: this.user.userId,
        street: street.trim(),
        number: finalNumber, // Garantido que não está vazio
        complement: complement.trim() || undefined,
        district: district.trim(),
        city: city.trim(),
        state: state,
        zipCode: zipCode.trim()
      };
      
      // Validação final antes de enviar
      if (!addressData.number || addressData.number.length === 0) {
        this.isLoading = false;
        alert('Erro: O número do endereço está vazio. Por favor, preencha o campo número.');
        return;
      }

      if (this.address?.addressId) {
        // Atualizar endereço existente (se houver rota PUT)
        // Por enquanto, criar novo
        this.addressService.createAddress(addressData).subscribe({
          next: (newAddress) => {
            this.address = newAddress;
            this.isEditingAddress = false;
            this.isLoading = false;
            alert('Endereço atualizado com sucesso!');
          },
          error: (error) => {
            console.error('Erro ao atualizar endereço:', error);
            this.isLoading = false;
            
            let errorMessage = 'Erro ao atualizar endereço. Tente novamente.';
            if (error.error?.error) {
              errorMessage = error.error.error;
            } else if (error.error?.message) {
              errorMessage = error.error.message;
            }
            
            alert(errorMessage);
          }
        });
      } else {
        // Criar novo endereço
        this.addressService.createAddress(addressData).subscribe({
          next: (newAddress) => {
            this.address = newAddress;
            this.isEditingAddress = false;
            this.isLoading = false;
            alert('Endereço criado com sucesso!');
          },
          error: (error) => {
            console.error('Erro ao criar endereço:', error);
            this.isLoading = false;
            
            let errorMessage = 'Erro ao criar endereço. Tente novamente.';
            if (error.error?.error) {
              errorMessage = error.error.error;
            } else if (error.error?.message) {
              errorMessage = error.error.message;
            }
            
            alert(errorMessage);
          }
        });
      }
    } else {
      this.addressForm.markAllAsTouched();
    }
  }

  startCreatingAccount(): void {
    this.isCreatingAccount = true;
    this.isEditingAccount = false;
    this.selectedAccount = null;
    this.accountForm.reset({
      bankCode: '',
      bankName: '',
      accountNumber: '',
      agency: '',
      accountType: 'CHECKING'
    });
  }

  startEditingAccount(account: Account): void {
    this.isEditingAccount = true;
    this.isCreatingAccount = false;
    this.selectedAccount = account;
    this.accountForm.patchValue({
      bankCode: account.bankCode || '',
      bankName: account.bankName || '',
      accountNumber: account.accountNumber,
      agency: account.agency || '',
      accountType: account.accountType
    });
  }

  cancelEditingAccount(): void {
    this.isEditingAccount = false;
    this.isCreatingAccount = false;
    this.selectedAccount = null;
    this.accountForm.reset();
  }

  saveAccount(): void {
    if (this.accountForm.valid && this.user) {
      this.isLoading = true;
      const formData = this.accountForm.value;
      
      if (this.isCreatingAccount) {
        // Criar nova conta
        const bank = this.banks.find(b => b.code === formData.bankCode);
        const accountData = {
          userId: this.user.userId,
          bankCode: formData.bankCode.trim(),
          bankName: bank ? bank.name : formData.bankName.trim(),
          accountNumber: formData.accountNumber.trim(),
          agency: formData.agency.trim(),
          accountType: formData.accountType
        };
        
        this.accountService.createAccountFromForm(accountData).subscribe({
          next: (newAccount) => {
            this.accounts.push(newAccount);
            this.isCreatingAccount = false;
            this.isLoading = false;
            alert('Conta criada com sucesso!');
          },
          error: (error) => {
            console.error('Erro ao criar conta:', error);
            this.isLoading = false;
            let errorMessage = 'Erro ao criar conta. Tente novamente.';
            if (error.error?.error) {
              errorMessage = error.error.error;
            }
            alert(errorMessage);
          }
        });
      } else if (this.selectedAccount) {
        // Atualizar conta existente
        const bank = this.banks.find(b => b.code === formData.bankCode);
        this.accountService.updateAccount(this.selectedAccount.accountId, {
          bankCode: formData.bankCode.trim(),
          bankName: bank ? bank.name : formData.bankName.trim(),
          accountNumber: formData.accountNumber.trim(),
          agency: formData.agency.trim(),
          accountType: formData.accountType
        }).subscribe({
          next: (updatedAccount) => {
            const index = this.accounts.findIndex(a => a.accountId === updatedAccount.accountId);
            if (index !== -1) {
              this.accounts[index] = updatedAccount;
            }
            this.selectedAccount = updatedAccount;
            this.isEditingAccount = false;
            this.isLoading = false;
            alert('Dados bancários atualizados com sucesso!');
          },
          error: (error) => {
            console.error('Erro ao atualizar conta:', error);
            this.isLoading = false;
            let errorMessage = 'Erro ao atualizar dados bancários. Tente novamente.';
            if (error.error?.error) {
              errorMessage = error.error.error;
            }
            alert(errorMessage);
          }
        });
      }
    } else {
      this.accountForm.markAllAsTouched();
    }
  }

  deleteAccount(account: Account): void {
    const confirmed = confirm(
      `Tem certeza que deseja remover a conta ${account.accountNumber}?\n\n` +
      '⚠️ Se houver transações relacionadas, a exclusão não será permitida.'
    );

    if (!confirmed) return;

    this.isLoading = true;
    this.accountService.deleteAccount(account.accountId).subscribe({
      next: () => {
        this.accounts = this.accounts.filter(a => a.accountId !== account.accountId);
        if (this.selectedAccount?.accountId === account.accountId) {
          this.selectedAccount = this.accounts.length > 0 ? this.accounts[0] : null;
        }
        this.isLoading = false;
        alert('Conta removida com sucesso!');
        
        // Recarrega os dados completos do usuário
        const userId = localStorage.getItem('userId');
        if (userId) {
          this.loadAllUserData(Number(userId));
        }
      },
      error: (error: any) => {
        console.error('Erro ao remover conta:', error);
        this.isLoading = false;
        
        // Se o erro for 400 (transações vinculadas), oferece opção de desativar
        if (error.status === 400 || error.hasTransactions) {
          const errorMessage = error.message || 
            'Esta conta possui transações vinculadas e não pode ser removida.';
          
          const shouldDeactivate = confirm(
            `${errorMessage}\n\n` +
            'Deseja desativar a conta em vez de removê-la?'
          );
          
          if (shouldDeactivate) {
            this.accountService.deactivateAccount(account.accountId).subscribe({
              next: (deactivatedAccount) => {
                // Atualiza a conta na lista
                const index = this.accounts.findIndex(a => a.accountId === deactivatedAccount.accountId);
                if (index !== -1) {
                  this.accounts[index] = deactivatedAccount;
                }
                alert('Conta desativada com sucesso!');
              },
              error: (deactivateError) => {
                console.error('Erro ao desativar conta:', deactivateError);
                alert('Erro ao desativar conta. Tente novamente.');
              }
            });
          }
        } else {
          // Outro tipo de erro
          const errorMessage = error.message || 'Erro ao remover conta. Tente novamente.';
          alert(errorMessage);
        }
      }
    });
  }


  onBankChange(): void {
    const bankCode = this.accountForm.get('bankCode')?.value;
    if (bankCode) {
      const bank = this.banks.find(b => b.code === bankCode);
      if (bank) {
        this.accountForm.patchValue({ bankName: bank.name });
      }
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
          // Habilita o campo cidade e carrega as cidades
          const cityControl = this.addressForm.get('city');
          if (address.state) {
            cityControl?.enable();
            this.loadCities(address.state);
          }
          this.isLoadingCep = false;
        },
        error: (error) => {
          console.error('Erro ao buscar CEP:', error);
          this.isLoadingCep = false;
          // Não mostra erro se for 404 (CEP não encontrado), apenas não preenche
          if (error.status !== 404) {
            alert('Erro ao buscar CEP. Verifique se o CEP está correto.');
          }
        }
      });
    }
  }

  onStateChange(): void {
    const state = this.addressForm.get('state')?.value;
    const cityControl = this.addressForm.get('city');
    
    if (state) {
      cityControl?.enable();
      this.loadCities(state);
      this.addressForm.patchValue({ city: '' });
    } else {
      cityControl?.disable();
      this.addressForm.patchValue({ city: '' });
    }
  }

  loadCities(state: string): void {
    this.addressService.getCitiesByState(state).subscribe({
      next: (cities) => {
        this.cities = cities.map(c => c.name);
      },
      error: (error) => {
        console.error('Erro ao carregar cidades:', error);
      }
    });
  }

  // Alterar senha
  changePassword(): void {
    if (this.passwordForm.valid) {
      const formData = this.passwordForm.value;
      
      if (formData.newPassword !== formData.confirmPassword) {
        alert('As senhas não coincidem!');
        return;
      }

      // TODO: Implementar chamada à API para alterar senha
      alert('Funcionalidade de alteração de senha será implementada em breve.');
      this.passwordForm.reset();
    } else {
      this.passwordForm.markAllAsTouched();
    }
  }

  // Helpers
  /**
   * Retorna o nome completo do usuário
   */
  getFullName(): string {
    if (!this.user) return 'Usuário';
    return `${this.user.firstName || ''} ${this.user.lastName || ''}`.trim() || 'Usuário';
  }

  getUserInitials(): string {
    if (this.user) {
      const first = this.user.firstName?.[0] || '';
      const last = this.user.lastName?.[0] || '';
      return (first + last).toUpperCase();
    }
    return 'CO';
  }

  getPlanName(): string {
    if (this.user) {
      return this.planService.getPlanNameById(this.user.planId);
    }
    return 'Plano';
  }

  getClientSince(): string {
    if (this.user?.createdAt) {
      const date = new Date(this.user.createdAt);
      const month = date.toLocaleDateString('pt-BR', { month: 'short' });
      const year = date.getFullYear();
      return `Cliente desde ${month}/${year}`;
    }
    return 'Cliente desde hoje';
  }

  get userPlanName(): string {
    if (this.user) {
      return this.planService.getPlanNameById(this.user.planId);
    }
    return 'Plano';
  }

  getPlanClass(): string {
    const planName = this.userPlanName.toLowerCase();
    if (planName.includes('start')) {
      return 'bg-emerald-500 text-white';
    } else if (planName.includes('pro') && !planName.includes('black')) {
      return 'bg-gray-500 text-white';
    } else if (planName.includes('black')) {
      return 'bg-black text-white';
    }
    return 'bg-gray-200 text-gray-700';
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR');
  }

  isFieldInvalid(form: FormGroup, fieldName: string): boolean {
    const field = form.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
}

