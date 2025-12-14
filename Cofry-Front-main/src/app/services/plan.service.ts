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
  // Adjust the base URL to your backend API
  private readonly baseUrl = '/api';

  constructor(private http: HttpClient) {}

  // Returns all available plans from the backend
  getAllPlans(): Observable<Plan[]> {
    return this.http.get<Plan[]>(`${this.baseUrl}/plans`);
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
