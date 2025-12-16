import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

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
  private readonly baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  // Returns all available plans from the backend
  getAllPlans(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/subscription-plans`).pipe(
      map((response: any) => {
        if (response.status === 'sucesso' && response.data) {
          // Mapear do formato do backend para o formato do frontend
          return response.data.map((plan: any) => ({
            id: plan.idPlano?.toString() || '',
            name: plan.nome || '',
            price: plan.preco ? parseFloat(plan.preco.toString()) : 0,
            features: plan.recursos ? plan.recursos.split(',') : []
          }));
        }
        return [];
      })
    );
  }

  // Returns the current user's plan. You can pass token or userId.
  getUserPlanByToken(token: string): Observable<UserPlanResponse> {
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<UserPlanResponse>(`${this.baseUrl}/users/me/plan`, { headers });
  }

  getUserPlanById(userId: string): Observable<UserPlanResponse> {
    return this.http.get<UserPlanResponse>(`${this.baseUrl}/users/${userId}/plan`);
  }
}
