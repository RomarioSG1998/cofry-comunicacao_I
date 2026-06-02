import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Invest } from './invest';
import { InvestService } from '../../services/invest.service';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';

describe('Invest', () => {
  let component: Invest;
  let fixture: ComponentFixture<Invest>;

  const mockInvestService = {
    getInvestmentsByUser: (userId: number) => of([]),
    createInvestment: (investment: any) => of({}),
    updateInvestment: (investment: any) => of({}),
    deleteInvestment: (id: number) => of({})
  };

  beforeEach(async () => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('userId', '1');
    }

    await TestBed.configureTestingModule({
      imports: [Invest],
      providers: [
        { provide: InvestService, useValue: mockInvestService },
        provideRouter([])
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Invest);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
