import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButton } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom, map } from 'rxjs';

import { failureMessage } from '../../../shared/forms/server-errors.util';
import { Topic } from '../../topics/topic.model';
import { TopicCard } from '../../topics/topic-card/topic-card.component';
import { TopicsApi } from '../../topics/topics-api.service';

/**
 * The Subscriptions section: the topics the user follows, each with a button to stop.
 */
@Component({
  imports: [MatButton, TopicCard],
  selector: 'app-subscriptions',
  styleUrl: './subscriptions.component.scss',
  templateUrl: './subscriptions.component.html',
})
export class Subscriptions {
  private readonly topicsApi = inject(TopicsApi);
  private readonly snackBar = inject(MatSnackBar);

  // Loaded when the profile opens
  protected readonly topics = rxResource({
    stream: () =>
      this.topicsApi.list().pipe(map((topics) => topics.filter((topic) => topic.subscribed))),
  });

  /**
   * Stops following a topic, then takes it out of the list. A second click while the request runs
   * does no harm, since the API accepts a second unsubscription.
   */
  protected async unsubscribe(topic: Topic): Promise<void> {
    try {
      await firstValueFrom(this.topicsApi.unsubscribe(topic.id));
    } catch (error) {
      this.snackBar.open(failureMessage(error), undefined, { duration: 3000 });
      return;
    }
    this.topics.update((topics) => topics?.filter((followed) => followed.id !== topic.id));
  }
}
