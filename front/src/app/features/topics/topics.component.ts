import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButton } from '@angular/material/button';

import { TopicCard } from './topic-card/topic-card.component';
import { TopicsApi } from './topics-api.service';

/**
 * The topics page: every topic, and whether the user already follows it.
 */
@Component({
  imports: [MatButton, TopicCard],
  selector: 'app-topics',
  styleUrl: './topics.component.scss',
  templateUrl: './topics.component.html',
})
export class Topics {
  private readonly topicsApi = inject(TopicsApi);

  // Loaded when the page opens
  protected readonly topics = rxResource({ stream: () => this.topicsApi.list() });
}
