import { ChangeDetectionStrategy, Component, signal, OnInit, OnDestroy, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common'; 
import { UserService } from '../../services/user.service'; 
import { PlanService } from '../../services/plan.service';
@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive], 
})
export class Navbar implements OnInit, OnDestroy { 
    primaryGreen = '#10b981';

    // Propriedades e Sinais
    menuOpen = signal(false); 
    userName = '';            
    userInitials = 'CO';      
    userPlanName = '';        
    userEmail = '';
    
    // Para limpar intervalos e event listeners
    private checkInterval: any = null;
    private storageHandler = (e: StorageEvent) => this.handleStorageChange(e);
    private loginHandler = () => this.handleUserLoggedIn();           

    // Injete PlanService (se necessário) e PLATFORM_ID
    // Mantenho PlanService aqui, mas seu método será ignorado/removido
    constructor(
        private userService: UserService, 
        private planService: PlanService, // Remova se o backend retornar o planName
        @Inject(PLATFORM_ID) private platformId: Object,
        private cdr: ChangeDetectorRef
    ) {}


    ngOnInit(): void {
        if (isPlatformBrowser(this.platformId)) {
            // Carrega dados imediatamente
            this.loadUserData();
            
            // Escuta mudanças no localStorage (funciona entre abas)
            window.addEventListener('storage', this.storageHandler);
            
            // Escuta eventos customizados de login (mesma aba)
            window.addEventListener('userLoggedIn', this.loginHandler);
            
            // Verifica periodicamente se o userId foi adicionado (para mesma aba)
            // Isso é útil quando o login acontece na mesma aba
            this.checkInterval = setInterval(() => {
                const userId = localStorage.getItem('userId');
                if (userId && !this.userName) {
                    // Se há userId mas não há nome, carrega os dados
                    this.loadUserData();
                } else if (!userId && this.userName) {
                    // Se o userId foi removido (logout), limpa os dados
                    this.userName = '';
                    this.userInitials = 'CO';
                    this.userPlanName = '';
                    this.userEmail = '';
                }
            }, 500);
        }
    }

    ngOnDestroy(): void {
        // Limpa o intervalo quando o componente for destruído
        if (this.checkInterval) {
            clearInterval(this.checkInterval);
        }
        // Remove event listeners
        if (isPlatformBrowser(this.platformId)) {
            window.removeEventListener('storage', this.storageHandler);
            window.removeEventListener('userLoggedIn', this.loginHandler);
        }
    }

    /**
     * Handler para mudanças no localStorage (entre abas)
     */
    private handleStorageChange(e: StorageEvent): void {
        if (e.key === 'userId' || e.key === 'userData') {
            this.loadUserData();
        }
    }

    /**
     * Handler para evento customizado de login (mesma aba)
     */
    private handleUserLoggedIn(): void {
        this.loadUserData();
    }

    /**
     * Carrega os dados do usuário da API
     * Chamado automaticamente no ngOnInit e quando detecta mudanças no localStorage
     */
    loadUserData(): void {
        if (!isPlatformBrowser(this.platformId)) {
            return;
        }

        const userId = localStorage.getItem('userId');
        const email = localStorage.getItem('userEmail') || '';
        
        if (userId) {
            this.userEmail = email; 

            this.userService.getUserById(Number(userId)).subscribe({
                next: (user) => {
                    // Mostra apenas o primeiro nome
                    this.userName = user.firstName || 'Usuário';
                    this.userInitials = this.computeInitials(`${user.firstName || ''} ${user.lastName || ''}`) || 'CO';
                    
                    // Usa o mapeamento local do PlanService para obter o nome do plano
                    const planId = user.planId || 1;
                    this.userPlanName = this.planService.getPlanNameById(planId);
                    
                    // Salva no localStorage para uso futuro
                    localStorage.setItem('userData', JSON.stringify(user));
                    
                    // Força detecção de mudanças após atualizar os dados
                    setTimeout(() => {
                        this.cdr.detectChanges();
                    }, 0);
                },
                error: (error) => {
                    console.error('Erro ao carregar dados do usuário:', error);
                    // Fallback para dados do localStorage se houver
                    const storedData = localStorage.getItem('userData');
                    if (storedData) {
                        try {
                            const user = JSON.parse(storedData);
                            // Mostra apenas o primeiro nome
                            this.userName = user.firstName || 'Usuário';
                            this.userInitials = this.computeInitials(`${user.firstName || ''} ${user.lastName || ''}`) || 'CO';
                            this.userPlanName = this.planService.getPlanNameById(user.planId || 1);
                            
                            // Força detecção de mudanças após atualizar os dados
                            setTimeout(() => {
                                this.cdr.detectChanges();
                            }, 0);
                        } catch (e) {
                            console.error('Erro ao parsear dados do usuário:', e);
                        }
                    }
                }
            });
        } else {
            // Se não houver userId, tenta usar dados do localStorage
            const storedData = localStorage.getItem('userData');
            if (storedData) {
                try {
                    const user = JSON.parse(storedData);
                    // Mostra apenas o primeiro nome
                    this.userName = user.firstName || 'Usuário';
                    this.userInitials = this.computeInitials(`${user.firstName || ''} ${user.lastName || ''}`) || 'CO';
                    this.userPlanName = this.planService.getPlanNameById(user.planId || 1);
                    
                    // Força detecção de mudanças após atualizar os dados
                    setTimeout(() => {
                        this.cdr.detectChanges();
                    }, 0);
                } catch (e) {
                    console.error('Erro ao parsear dados do usuário:', e);
                }
            }
        }
    }

    /**
     * Retorna as iniciais do usuário. (Função necessária no HTML)
     */
    getUser(): string {
        return this.userInitials;
    }

    /**
     * Retorna a classe CSS para o plano baseado no nome
     */
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

    private computeInitials(name: string): string {
        const parts = name.trim().split(/\s+/).filter(Boolean);
        if (!parts.length) return '';
        const first = parts[0][0] ?? '';
        const last = (parts.length > 1 ? parts[parts.length - 1][0] : '') ?? '';
        return (first + last).toUpperCase();
    }

    /**
     * Alterna o estado do menu mobile.
     */
    toggleMenu(): void {
        this.menuOpen.update((value: boolean) => !value);
    }
    
    // Lista de itens
    navItems = [
        { path: '/nav/Home', name: 'Home' },
        { path: '/nav/Cards', name: 'Card' },
        { path: '/nav/Invest', name: 'Investir' },
        { path: '/nav/Plans', name: 'Planos' },
        { path: '/nav/Dda', name: 'Boletos' }
    ];
}