export interface CategoryCreateRequest {
  name: string;
  description?: string | null;
  parentId?: number | null;
}

export interface CategoryUpdateRequest {
  name: string;
  description?: string | null;
  parentId?: number | null;
}
