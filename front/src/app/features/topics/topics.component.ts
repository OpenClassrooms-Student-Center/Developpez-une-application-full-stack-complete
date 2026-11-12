import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButton } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';

import { failureMessage } from '../../shared/forms/server-errors.util';
import { Topic } from './topic.model';
import { TopicCard } from './topic-card/topic-card.component';
import { TopicsApi } from './topics-api.service';

/**
 * The topics page: every topic, and a button to follow the ones the user does not follow yet.
 */
@Component({
  imports: [MatButton, TopicCard],
  selector: 'app-topics',
  styleUrl: './topics.component.scss',
  templateUrl: './topics.component.html',
})
export class Topics {
  private readonly topicsApi = inject(TopicsApi);
  private readonly snackBar = inject(MatSnackBar);

  // Loaded when the page opens
  protected readonly topics = rxResource({ stream: () => this.topicsApi.list() });

  /**
   * Follows a topic. The button changes at once, which also stops a second click, and comes back
   * if the API fails.
   */
  protected async subscribe(topic: Topic): Promise<void> {
    this.setSubscribed(topic, true);
    try {
      await firstValueFrom(this.topicsApi.subscribe(topic.id));
    } catch (error) {
      this.setSubscribed(topic, false);
      this.snackBar.open(failureMessage(error), undefined, { duration: 3000 });
    }
  }

  // Changes the topic in the list on screen, without asking the API again
  private setSubscribed(topic: Topic, subscribed: boolean): void {
    this.topics.update((topics) =>
      topics?.map((shown) => (shown.id === topic.id ? { ...shown, subscribed } : shown)),
    );
  }
}
