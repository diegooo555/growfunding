export type CampaignStatus =
  | 'DRAFT'
  | 'ACTIVE'
  | 'CLOSED_BY_DEADLINE'
  | 'CLOSED_MANUALLY'
  | 'CANCELLED'
  | 'SUSPENDED';

export type ContributorsVisibility = 'PUBLIC' | 'PRIVATE';

export type Category =
  | 'TECHNOLOGY'
  | 'ART'
  | 'EDUCATION'
  | 'HEALTH'
  | 'COMMUNITY'
  | 'ENVIRONMENT'
  | 'OTHER';

export const CATEGORY_LABELS: Record<Category, string> = {
  TECHNOLOGY: 'Tecnología',
  ART: 'Arte y cultura',
  EDUCATION: 'Educación',
  HEALTH: 'Salud',
  COMMUNITY: 'Comunidad',
  ENVIRONMENT: 'Medio ambiente',
  OTHER: 'Otra',
};

export const CATEGORIES = Object.keys(CATEGORY_LABELS) as Category[];

export type CampaignSort = 'deadline' | 'newest' | 'progress' | 'raised';

export interface CampaignSummary {
  id: string;
  title: string;
  summary: string;
  category: Category;
  creatorId: string;
  creatorName: string;
  goalAmount: number;
  raisedAmount: number;
  currency: string;
  progressPercent: number;
  contributionsCount: number;
  contributorsCount: number;
  deadline: string;
  status: CampaignStatus;
  contributorsVisibility: ContributorsVisibility;
  goalReached: boolean;
  closingSoon: boolean;
  acceptingContributions: boolean;
  cancellable: boolean;
  createdAt: string;
  publishedAt: string | null;
  closedAt: string | null;
}

export interface Milestone {
  type: 'PUBLISHED' | 'GOAL_REACHED' | 'CLOSING_SOON' | 'CLOSED';
  reachedAt: string;
  description: string;
}

export interface CampaignDetail {
  summary: CampaignSummary;
  description: string;
  milestones: Milestone[];
}

export interface CampaignFilters {
  q: string;
  category: Category | null;
  minProgress: number | null;
  closingSoon: boolean;
  sort: CampaignSort;
  page: number;
  size: number;
}

export const DEFAULT_FILTERS: CampaignFilters = {
  q: '',
  category: null,
  minProgress: null,
  closingSoon: false,
  sort: 'deadline',
  page: 0,
  size: 9,
};

export interface CreateCampaignRequest {
  title: string;
  description: string;
  category: Category;
  goalAmount: number;
  currency: string;
  deadline: string;
  contributorsVisibility: ContributorsVisibility;
}
