import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlanService, Plan } from '../../services/plan.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-plans',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './plans.html',
  styleUrl: './plans.css',
})
export class Plans implements OnInit {
  private planService = inject(PlanService);
  private authService = inject(AuthService);

  plans: Plan[] = [];
  userPlanId: string | null = null;
  isLoading = true;

  ngOnInit(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      this.planService.getUserPlanById(userId).subscribe({
        next: (res) => {
          if (res && res.plan) {
            this.userPlanId = res.plan.id;
          }
        },
        error: (err) => console.error('Erro ao buscar plano do usuário:', err)
      });
    }

    this.planService.getAllPlans().subscribe({
      next: (plans) => {
        this.plans = plans;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar planos:', err);
        this.isLoading = false;
      }
    });
  }

  isCurrentPlan(planId: string): boolean {
    if (!this.userPlanId) {
      // Default to "Gratuito" (ID 1) if no active subscription returned
      return planId === '1';
    }
    return this.userPlanId === planId;
  }
}
