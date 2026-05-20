export interface ApiErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
}

export class ApiError extends Error {
  readonly status: number;
  readonly title: string;
  readonly path?: string;
  readonly timestamp?: string;
  readonly originalError: unknown;

  constructor(response: ApiErrorResponse, originalError: unknown) {
    super(response.message ?? 'Não foi possível concluir a requisição.');

    this.name = 'ApiError';
    this.status = response.status ?? 0;
    this.title = response.error ?? 'Erro de comunicação';
    this.path = response.path;
    this.timestamp = response.timestamp;
    this.originalError = originalError;
  }
}
