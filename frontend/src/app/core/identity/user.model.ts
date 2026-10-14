export type Role = 'CREATOR' | 'CONTRIBUTOR' | 'ADMIN';

export interface User {
  id: string;
  displayName: string;
  email: string;
  roles: Role[];
}
