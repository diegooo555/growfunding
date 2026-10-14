export interface ContributeRequest {
  amount: number;
  anonymous: boolean;
  message: string | null;
  idempotencyKey: string;
}

export interface ContributionReceipt {
  contributionId: string;
  campaignId: string;
  amount: number;
  currency: string;
  createdAt: string;
  replayed: boolean;
  campaignRaisedAmount: number;
  campaignContributionsCount: number;
  campaignContributorsCount: number;
  campaignGoalReached: boolean;
}

export interface CampaignContribution {
  contributionId: string;
  contributorId: string | null;
  contributorName: string | null;
  anonymous: boolean;
  amount: number;
  currency: string;
  message: string | null;
  createdAt: string;
}

export interface ContributionHistoryItem {
  contributionId: string;
  campaignId: string;
  campaignTitle: string;
  campaignStatus: string;
  amount: number;
  currency: string;
  anonymous: boolean;
  message: string | null;
  createdAt: string;
}

export interface ContributionDraft {
  amount: number;
  anonymous: boolean;
  message: string;
}
