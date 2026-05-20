import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { ApiError, ApiErrorResponse } from '../api/api-error.model';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        return throwError(() => new ApiError(normalizeHttpError(error), error));
      }

      return throwError(() => error);
    })
  );
};

function normalizeHttpError(error: HttpErrorResponse): ApiErrorResponse {
  if (isApiErrorResponse(error.error)) {
    return {
      timestamp: error.error.timestamp,
      status: error.error.status ?? error.status,
      error: error.error.error ?? error.statusText,
      message: error.error.message,
      path: error.error.path ?? error.url ?? undefined
    };
  }

  if (typeof error.error === 'string' && error.error.trim()) {
    return {
      status: error.status,
      error: error.statusText,
      message: error.error,
      path: error.url ?? undefined
    };
  }

  if (error.status === 0) {
    return {
      status: 0,
      error: 'Erro de comunicação',
      message: 'Não foi possível conectar ao servidor.',
      path: error.url ?? undefined
    };
  }

  return {
    status: error.status,
    error: error.statusText || 'Erro na requisição',
    message: error.message || 'Não foi possível concluir a requisição.',
    path: error.url ?? undefined
  };
}

function isApiErrorResponse(value: unknown): value is ApiErrorResponse {
  return typeof value === 'object' && value !== null && ('message' in value || 'status' in value || 'error' in value);
}
