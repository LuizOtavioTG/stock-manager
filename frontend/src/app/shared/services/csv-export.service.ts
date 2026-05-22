import { Injectable } from '@angular/core';

export interface CsvColumn<T> {
  header: string;
  value: (row: T) => string | number | null | undefined;
}

@Injectable({
  providedIn: 'root'
})
export class CsvExportService {
  exportToCsv<T>(filename: string, rows: T[], columns: CsvColumn<T>[]): void {
    const csvRows = [
      columns.map((column) => this.escapeCell(column.header)).join(';'),
      ...rows.map((row) => columns.map((column) => this.escapeCell(column.value(row))).join(';'))
    ];
    const content = `\uFEFF${csvRows.join('\r\n')}`;
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');

    link.href = url;
    link.download = filename.endsWith('.csv') ? filename : `${filename}.csv`;
    link.style.display = 'none';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  private escapeCell(value: string | number | null | undefined): string {
    const text = value === null || value === undefined ? '' : String(value);
    return `"${text.replace(/"/g, '""')}"`;
  }
}
