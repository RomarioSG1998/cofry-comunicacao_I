import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FeedbackComponent } from './shared/feedback/feedback';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, FeedbackComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Cofry-FrontEnd');
}
