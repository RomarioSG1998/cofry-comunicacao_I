import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AfterLogin } from './after-login';

describe('AfterLogin', () => {
  let component: AfterLogin;
  let fixture: ComponentFixture<AfterLogin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AfterLogin]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AfterLogin);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
