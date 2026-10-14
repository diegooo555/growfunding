export type NotificationType = 'NEW_CONTRIBUTION' | 'GOAL_REACHED' | 'CLOSING_SOON' | 'CAMPAIGN_CLOSED';

export interface AppNotification {
  id: string;
  type: NotificationType;
  campaignId: string | null;
  title: string;
  message: string;
  createdAt: string;
  read: boolean;
}
