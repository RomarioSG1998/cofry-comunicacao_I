import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Plan {
  id: string;
  name: string;
  price: number;
  features?: string[];
}

export interface UserPlanResponse {
  userId: string;
  plan: Plan | null;
}

@Injectable({ providedIn: 'root' })
export class PlanService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Retorna todos os planos disponíveis
   * NOTA: Esta rota pode não existir na API. O planId vem do objeto User.
   */
  getAllPlans(): Observable<Plan[]> {
    // Se a rota não existir, retornar array vazio ou usar dados do UserService
    return this.http.get<Plan[]>(`${this.baseUrl}/plans`);
  }

  /**
   * Retorna o plano do usuário atual
   * NOTA: Esta rota pode não existir. Use UserService.getUserById() e pegue o planId.
   */
  getUserPlanByToken(token: string): Observable<UserPlanResponse> {
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<UserPlanResponse>(`${this.baseUrl}/users/me/plan`, { headers });
  }

  /**
   * Retorna o plano de um usuário por ID
   * NOTA: Esta rota pode não existir. Use UserService.getUserById() e pegue o planId.
   */
  getUserPlanById(userId: string): Observable<UserPlanResponse> {
    return this.http.get<UserPlanResponse>(`${this.baseUrl}/users/${userId}/plan`);
  }

  /**
   * Mapeamento local de planos (fallback se API não tiver rota de planos)
   */
  getPlanNameById(planId: number): string {
    const plansMap: { [key: number]: string } = {
      1: 'Cofry Start',
      2: 'Cofry Pro',
      3: 'Cofry Black',
      4: 'Cofry Invest Plus',
      5: 'Cofry Max'
    };
    return plansMap[planId] || 'Plano';
  }
}
