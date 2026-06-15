import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { Cards } from './pages/cards/cards';
import { Invest } from './pages/invest/invest';
import { Plans } from './pages/plans/plans';
import { Boletos } from './pages/boletos/boletos';
import { AfterLogin } from './pages/after-login/after-login';
import { SignUp } from './pages/sign-up/sign-up';
import { Pix } from './pages/pix/pix';
import { Pagar } from './pages/pagar/pagar';
import { Transferir } from './pages/transferir/transferir';
import { Extrato } from './pages/extrato/extrato';
import { MainLayout } from './shared/layout/main-layout';
import { TransactionListComponent } from './pages/transaction-list/transaction-list.component';
import { PixKeysComponent } from './pages/pix-keys/pix-keys.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: '', component: Home },      // Rota de abertura
    { path: 'login', component: Login },
    { path: 'Login', component: Login }, // Mantido para compatibilidade
    { path: 'cadastrar', component: SignUp },
    { path: 'Cadastrar', component: SignUp }, // Mantido para compatibilidade

    // Rota de Layout Principal (Com Navbar) — protegida pelo AuthGuard
    {
        path: 'nav',
        component: MainLayout,
        canActivate: [authGuard],
        children: [
            { path: '', redirectTo: 'home', pathMatch: 'full' },
            { path: 'home',          component: AfterLogin },
            { path: 'Home',          component: AfterLogin }, // compatibilidade
            { path: 'Cards',         component: Cards },
            { path: 'Invest',        component: Invest },
            { path: 'Plans',         component: Plans },
            { path: 'Dda',           component: Boletos },
            { path: 'Pix',           component: Pix },
            { path: 'Pagar',         component: Pagar },
            { path: 'Transferir',    component: Transferir },
            { path: 'Extrato',       component: TransactionListComponent },
            { path: 'Minhas-Chaves', component: PixKeysComponent },
            { path: 'minhas-chaves', component: PixKeysComponent }
        ]
    },

    // Fallback
    { path: '**', redirectTo: '' }
];