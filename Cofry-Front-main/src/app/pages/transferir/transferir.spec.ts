import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Transferir } from './transferir';

describe('Transferir', () => {
  let component: Transferir;
  let fixture: ComponentFixture<Transferir>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Transferir]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Transferir);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
