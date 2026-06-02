import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

interface BackendPlan {
  idPlano: number;
  nome: string;
  preco: number;
  recursos: string;
}

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class PlanService {
  private readonly baseUrl = 'http://localhost:8082/api';

  constructor(private http: HttpClient) {}

  // Returns all available plans from the backend
  getAllPlans(): Observable<Plan[]> {
    return this.http.get<ApiResponse<BackendPlan[]>>(`${this.baseUrl}/subscription-plans`).pipe(
      map(response => {
        if (response && response.data && Array.isArray(response.data)) {
          return response.data.map(p => this.mapBackendPlanToPlan(p));
        }
        return [];
      })
    );
  }

  getUserPlanById(userId: string): Observable<UserPlanResponse> {
    return this.http.get<ApiResponse<BackendPlan>>(`${this.baseUrl}/users/${userId}/plan`).pipe(
      map(response => {
        return {
          userId: userId,
          plan: response && response.data ? this.mapBackendPlanToPlan(response.data) : null
        };
      })
    );
  }

  private mapBackendPlanToPlan(p: BackendPlan): Plan {
    return {
      id: p.idPlano.toString(),
      name: p.nome,
      price: p.preco,
      features: p.recursos ? p.recursos.split(',').map(f => f.trim()) : []
    };
  }
}
