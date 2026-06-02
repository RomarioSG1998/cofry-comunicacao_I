import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Boletos } from './boletos';
import { BoletoService } from '../../services/boleto.service';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';

describe('Boletos', () => {
  let component: Boletos;
  let fixture: ComponentFixture<Boletos>;

  const mockBoletoService = {
    getBoletosByUser: (userId: number) => of([]),
    createBoleto: (boleto: any) => of({}),
    updateBoleto: (boleto: any) => of({}),
    deleteBoleto: (id: number) => of({})
  };

  beforeEach(async () => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('userId', '1');
    }

    await TestBed.configureTestingModule({
      imports: [Boletos],
      providers: [
        { provide: BoletoService, useValue: mockBoletoService },
        provideRouter([])
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Boletos);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
