import { NgTemplateOutlet } from '@angular/common';
import { Component, ContentChild, Directive, TemplateRef, input, output } from '@angular/core';

import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';

export interface PaginatedListColumn {
  label: string;
  field?: string;
  sortable?: boolean;
  styleClass?: string;
}

@Directive({
  selector: 'ng-template[appPaginatedListHeader]'
})
export class PaginatedListHeaderDirective {
  constructor(readonly templateRef: TemplateRef<unknown>) {}
}

@Directive({
  selector: 'ng-template[appPaginatedListBody]'
})
export class PaginatedListBodyDirective {
  constructor(readonly templateRef: TemplateRef<unknown>) {}

  static ngTemplateContextGuard(
    _directive: PaginatedListBodyDirective,
    context: unknown
  ): context is { $implicit: any } {
    return true;
  }
}

@Directive({
  selector: 'ng-template[appPaginatedListEmpty]'
})
export class PaginatedListEmptyDirective {
  constructor(readonly templateRef: TemplateRef<unknown>) {}
}

@Directive({
  selector: 'ng-template[appPaginatedListActions]'
})
export class PaginatedListActionsDirective {
  constructor(readonly templateRef: TemplateRef<unknown>) {}
}

@Component({
  selector: 'app-paginated-list',
  standalone: true,
  imports: [ButtonModule, CardModule, NgTemplateOutlet, TableModule],
  templateUrl: './paginated-list.html',
  styleUrl: './paginated-list.scss'
})
export class PaginatedListComponent {
  readonly section = input.required<string>();
  readonly pageTitle = input.required<string>();
  readonly cardTitle = input.required<string>();
  readonly value = input.required<unknown[]>();
  readonly totalRecords = input.required<number>();
  readonly loading = input(false);
  readonly rows = input(10);
  readonly first = input(0);
  readonly rowsPerPageOptions = input<number[]>([10, 20, 50]);
  readonly tableStyle = input<Record<string, string>>({ 'min-width': '64rem' });
  readonly dataKey = input('id');
  readonly columns = input<PaginatedListColumn[]>([]);

  readonly refresh = output<void>();
  readonly lazyLoad = output<TableLazyLoadEvent>();

  @ContentChild(PaginatedListHeaderDirective)
  protected readonly headerTemplate?: PaginatedListHeaderDirective;

  @ContentChild(PaginatedListBodyDirective)
  protected readonly bodyTemplate?: PaginatedListBodyDirective;

  @ContentChild(PaginatedListEmptyDirective)
  protected readonly emptyTemplate?: PaginatedListEmptyDirective;

  @ContentChild(PaginatedListActionsDirective)
  protected readonly actionsTemplate?: PaginatedListActionsDirective;

  protected bodyContext(item: unknown): { $implicit: unknown } {
    return { $implicit: item };
  }
}
