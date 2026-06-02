import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Cards } from './cards';
import { CardService } from '../../services/card.service';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';

describe('Cards', () => {
  let component: Cards;
  let fixture: ComponentFixture<Cards>;

  const mockCardService = {
    getCreditCardsByUser: (userId: number) => of([]),
    createCreditCard: (card: any) => of({}),
    updateCreditCard: (card: any) => of({}),
    deleteCreditCard: (id: number) => of({})
  };

  beforeEach(async () => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('userId', '1');
    }

    await TestBed.configureTestingModule({
      imports: [Cards],
      providers: [
        { provide: CardService, useValue: mockCardService },
        provideRouter([])
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Cards);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
