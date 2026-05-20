import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

type QueryParamValue = string | number | boolean | readonly (string | number | boolean)[];

export type ApiQueryParams = Record<string, QueryParamValue | null | undefined>;

export interface ApiRequestOptions {
  params?: ApiQueryParams;
  headers?: HttpHeaders | Record<string, string | string[]>;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  get<TResponse>(endpoint: string, options: ApiRequestOptions = {}): Observable<TResponse> {
    return this.http.get<TResponse>(this.url(endpoint), this.requestOptions(options));
  }

  post<TResponse, TBody = unknown>(
    endpoint: string,
    body: TBody,
    options: ApiRequestOptions = {}
  ): Observable<TResponse> {
    return this.http.post<TResponse>(this.url(endpoint), body, this.requestOptions(options));
  }

  put<TResponse, TBody = unknown>(
    endpoint: string,
    body: TBody,
    options: ApiRequestOptions = {}
  ): Observable<TResponse> {
    return this.http.put<TResponse>(this.url(endpoint), body, this.requestOptions(options));
  }

  patch<TResponse, TBody = unknown>(
    endpoint: string,
    body: TBody,
    options: ApiRequestOptions = {}
  ): Observable<TResponse> {
    return this.http.patch<TResponse>(this.url(endpoint), body, this.requestOptions(options));
  }

  delete<TResponse>(endpoint: string, options: ApiRequestOptions & { body?: unknown } = {}): Observable<TResponse> {
    return this.http.delete<TResponse>(this.url(endpoint), {
      ...this.requestOptions(options),
      body: options.body
    });
  }

  private url(endpoint: string): string {
    if (/^https?:\/\//i.test(endpoint)) {
      return endpoint;
    }

    const base = this.baseUrl.replace(/\/+$/, '');
    const path = endpoint.replace(/^\/+/, '');

    return `${base}/${path}`;
  }

  private requestOptions(options: ApiRequestOptions): {
    headers?: HttpHeaders | Record<string, string | string[]>;
    params?: HttpParams;
  } {
    return {
      headers: options.headers,
      params: this.params(options.params)
    };
  }

  private params(params?: ApiQueryParams): HttpParams | undefined {
    if (!params) {
      return undefined;
    }

    let httpParams = new HttpParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value === null || value === undefined) {
        return;
      }

      if (Array.isArray(value)) {
        value.forEach((item) => {
          httpParams = httpParams.append(key, String(item));
        });
        return;
      }

      httpParams = httpParams.set(key, String(value));
    });

    return httpParams;
  }
}
