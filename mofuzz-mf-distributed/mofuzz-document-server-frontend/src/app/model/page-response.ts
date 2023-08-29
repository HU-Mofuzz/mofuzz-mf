export interface PageResponse<T> {
  data: T[];
  totalPages: number;
  totalElements: number;
}
