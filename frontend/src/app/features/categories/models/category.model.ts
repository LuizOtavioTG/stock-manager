export interface Category {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
  parent: CategorySummary | null;
  children: CategorySummary[];
}

export interface CategorySummary {
  id: number;
  name: string;
  active?: boolean;
}
