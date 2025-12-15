import { ChangeDetectionStrategy, Component, signal, OnInit, OnDestroy, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common'; 
import { PlanService } from '../../services/plan.service';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';
@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.Default
})
export class Navbar implements OnInit, OnDestroy { 
    primaryGreen = '#10b981';

    // Propriedades e Sinais
    menuOpen = signal(false);
    userMenuOpen = signal(false); // Estado para controlar o dropdown do usuário
    userName = signal<string>('Usuário');            
    userInitials = 'CO';      
    userPlanName = signal<string>('Plano');        
    userEmail = '';
    private userNameSubscription?: Subscription;
    
    // Getter que sempre retorna o nome atualizado
    get displayUserName(): string {
        if (isPlatformBrowser(this.platformId)) {
            // Sempre verifica o localStorage para ter o valor mais atual
            const storedName = localStorage.getItem('userName');
            if (storedName && storedName.trim() !== '') {
                const name = storedName.trim();
                // Atualiza o signal se for diferente para manter sincronizado
                if (this.userName() !== name) {
                    this.userName.set(name);
                    this.userInitials = this.computeInitials(name) || 'CO';
                }
                return name;
            }
        }
        const currentName = this.userName();
        return currentName && currentName !== 'Usuário' ? currentName : 'Usuário';
    }

    // Injete PlanService (se necessário) e PLATFORM_ID
    // Mantenho PlanService aqui, mas seu método será ignorado/removido
    constructor(
        private planService: PlanService,
        private authService: AuthService,
        private cdr: ChangeDetectorRef,
        private router: Router,
        @Inject(PLATFORM_ID) private platformId: Object 
    ) {}


    ngOnInit(): void {
        if (isPlatformBrowser(this.platformId)) {
            // Carrega o nome do usuário inicial
            this.loadUserData();
            
            // Escuta mudanças no nome do usuário (quando login acontece)
            this.userNameSubscription = this.authService.userName$.subscribe(userName => {
                console.log('Navbar - Recebeu atualização do userName$:', userName);
                if (userName && userName.trim() !== '') {
                    this.userName.set(userName.trim());
                    this.userInitials = this.computeInitials(userName) || 'CO';
                    console.log('Navbar - Atualizado userName signal para:', this.userName());
                } else {
                    // Se userName está vazio, tenta recarregar do localStorage
                    const storedName = localStorage.getItem('userName');
                    if (storedName && storedName.trim() !== '') {
                        console.log('Navbar - Recarregando nome do localStorage:', storedName);
                        this.userName.set(storedName.trim());
                        this.userInitials = this.computeInitials(storedName) || 'CO';
                    }
                }
            });
            
            // Verifica periodicamente se há mudanças no localStorage
            const checkInterval = setInterval(() => {
                if (isPlatformBrowser(this.platformId)) {
                    const storedName = localStorage.getItem('userName');
                    const storedPlan = localStorage.getItem('userPlan') || 'Plano';
                    
                    if (storedName && storedName.trim() !== '' && this.userName() !== storedName.trim()) {
                        console.log('Navbar - Atualizando do localStorage (check periódico):', storedName);
                        this.userName.set(storedName.trim());
                        this.userInitials = this.computeInitials(storedName) || 'CO';
                    }
                    
                    if (storedPlan && this.userPlanName() !== storedPlan) {
                        this.userPlanName.set(storedPlan);
                    }
                }
            }, 300);
            
            // Fechar menu ao clicar fora
            const clickHandler = (event: MouseEvent) => {
                const target = event.target as HTMLElement;
                if (!target.closest('.user-menu-container')) {
                    this.closeUserMenu();
                }
            };
            document.addEventListener('click', clickHandler);
            
            // Limpa o interval e remove listener no destroy
            if (this.userNameSubscription) {
                this.userNameSubscription.add(() => {
                    clearInterval(checkInterval);
                    document.removeEventListener('click', clickHandler);
                });
            }
        }
    }

    ngOnDestroy(): void {
        if (this.userNameSubscription) {
            this.userNameSubscription.unsubscribe();
        }
    }

    private loadUserData(): void {
        // Tenta obter do AuthService primeiro, depois do localStorage
        let userName = this.authService.getUserName();
        if (!userName || userName.trim() === '') {
            userName = localStorage.getItem('userName') || '';
        }
        const email = localStorage.getItem('userEmail') || '';
        
        console.log('Navbar - Carregando dados do usuário:', { 
            userNameFromService: this.authService.getUserName(),
            userNameFromStorage: localStorage.getItem('userName'),
            finalUserName: userName,
            email 
        });
        
        if (userName && userName.trim() !== '') {
            this.userName.set(userName.trim());
            this.userEmail = email;
            this.userInitials = this.computeInitials(userName) || 'CO';
            this.userPlanName.set(this.getPlanNameLocally(1)); // Usa plano padrão
            console.log('Navbar - Nome do usuário carregado e aplicado:', this.userName());
        } else {
            // Se não tem nome, mantém o padrão "Usuário"
            // O nome será atualizado quando o login acontecer através do subscription
            console.log('Navbar - Nome do usuário não encontrado, usando padrão');
        }
    }

    /**
     * Mapeamento LOCAL paliativo para evitar o erro do PlanService.
     * REMOVA esta função e a lógica do PlanService assim que o backend for corrigido.
     */
     private getPlanNameLocally(planId: number): string {
        const plansMap: { [key: number]: string } = {
            1: 'Cofry Start',
            2: 'Cofry Basic',
            3: 'Premium Member',
            4: 'Cofry Invest Plus',
            5: 'Cofry Max'
        };
        return plansMap[planId] || 'Plano';
    }

    /**
     * Retorna as iniciais do usuário. (Função necessária no HTML)
     */
    getUser(): string {
        return this.userInitials;
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

    /**
     * Alterna o estado do menu dropdown do usuário.
     */
    toggleUserMenu(): void {
        this.userMenuOpen.update((value: boolean) => !value);
    }

    /**
     * Fecha o menu dropdown do usuário.
     */
    closeUserMenu(): void {
        this.userMenuOpen.set(false);
    }
    
    // Lista de itens
    navItems = [
        { path: '/nav/Home', name: 'Home' },
        { path: '/nav/Cards', name: 'Card' },
        { path: '/nav/Invest', name: 'Investir' },
        { path: '/nav/Plans', name: 'Planos' },
        { path: '/nav/Dda', name: 'Boletos' }
    ];

    /**
     * Realiza o logout do usuário
     */
    logout(): void {
        console.log('Navbar - logout() chamado');
        // Fechar menu antes de fazer logout
        this.closeUserMenu();
        
        // Limpar dados do usuário
        this.authService.logout();
        
        // Redirecionar para a página inicial
        this.router.navigate(['/']).catch(() => {
            window.location.href = '/';
        });
    }
}