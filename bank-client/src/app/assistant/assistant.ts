import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BankApi, errorMessage } from '../api.service';

interface ChatMessage {
  role: 'user' | 'assistant' | 'error';
  text: string;
}

@Component({
  selector: 'app-assistant',
  imports: [FormsModule],
  templateUrl: './assistant.html',
  styleUrl: './assistant.css',
})
export class Assistant {
  private readonly api = inject(BankApi);

  protected readonly messages = signal<ChatMessage[]>([]);
  protected readonly input = signal('');
  protected readonly loading = signal(false);
  private conversationId: string | null = null;

  protected send() {
    const text = this.input().trim();
    if (!text || this.loading()) {
      return;
    }
    this.messages.update((messages) => [...messages, { role: 'user', text }]);
    this.input.set('');
    this.loading.set(true);

    this.api.chat(text, this.conversationId).subscribe({
      next: (response) => {
        this.conversationId = response.conversationId;
        this.messages.update((messages) => [...messages, { role: 'assistant', text: response.reply }]);
        this.loading.set(false);
      },
      error: (err) => {
        this.messages.update((messages) => [...messages, { role: 'error', text: errorMessage(err) }]);
        this.loading.set(false);
      },
    });
  }

  protected reset() {
    this.conversationId = null;
    this.messages.set([]);
  }
}
