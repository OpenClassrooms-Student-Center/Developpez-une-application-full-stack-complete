import { Component, input } from '@angular/core';

import { Topic } from '../topic.model';

/**
 * A topic as a card: its title, its description cut after three lines, then the button that the
 * page puts between the tags.
 */
@Component({
  selector: 'app-topic-card',
  styleUrl: './topic-card.component.scss',
  templateUrl: './topic-card.component.html',
})
export class TopicCard {
  readonly topic = input.required<Topic>();
}
