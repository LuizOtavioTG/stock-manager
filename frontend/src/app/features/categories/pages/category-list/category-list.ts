import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';

import { Page } from '../../../../models/page.model';
import {
  PaginatedListBodyDirective,
  PaginatedListActionsDirective,
  PaginatedListColumn,
  PaginatedListComponent,
  PaginatedListEmptyDirective
} from '../../../../shared/components/paginated-list/paginated-list';
import { Category } from '../../models/category.model';
import { CategoryCreateRequest } from '../../models/category-request.model';
import { CategoryService } from '../../services/category.service';

const EMPTY_PAGE: Page<Category> = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: true
};

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [
    ButtonModule,
    ConfirmDialogModule,
    DialogModule,
    InputTextModule,
    PaginatedListActionsDirective,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    ReactiveFormsModule,
    SelectModule,
    TableModule,
    TagModule,
    TextareaModule
  ],
  templateUrl: './category-list.html',
  styleUrl: './category-list.scss'
})
export class CategoryListComponent implements OnInit {
  private readonly categoryService = inject(CategoryService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly messageService = inject(MessageService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'Nome', field: 'name' },
    { label: 'Descrição', field: 'description' },
    { label: 'Categoria pai', field: 'parent.name' },
    { label: 'Subcategorias', sortable: false, styleClass: 'numeric-column' },
    { label: 'Status', field: 'active' },
    { label: 'Ações', sortable: false, styleClass: 'actions-column' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('name,asc');
  protected readonly categoriesPage = signal<Page<Category>>(EMPTY_PAGE);
  protected readonly selectedCategory = signal<Category | null>(null);
  protected readonly isDetailsDialogVisible = signal(false);
  protected readonly isDetailsLoading = signal(false);
  protected readonly isFormDialogVisible = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly categoryBeingEdited = signal<Category | null>(null);
  protected readonly parentCategories = signal<Category[]>([]);
  protected readonly isLoadingParentCategories = signal(false);

  protected readonly categories = computed(() => this.categoriesPage().content);
  protected readonly totalElements = computed(() => this.categoriesPage().totalElements);
  protected readonly first = computed(() => this.categoriesPage().number * this.categoriesPage().size);
  protected readonly formTitle = computed(() =>
    this.categoryBeingEdited() ? 'Editar categoria' : 'Nova categoria'
  );
  protected readonly parentCategoryOptions = computed(() => {
    const editingId = this.categoryBeingEdited()?.id;

    return this.parentCategories().filter((category) => category.id !== editingId);
  });

  protected readonly categoryForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]],
    description: this.formBuilder.control<string | null>(null, [Validators.maxLength(255)]),
    parentId: this.formBuilder.control<number | null>(null)
  });

  ngOnInit(): void {
    this.loadCategories(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadCategories(page, rows, sort);
  }

  protected loadCategories(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    this.categoryService
      .listCategories(page, size, sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (categoriesPage) => {
          this.categoriesPage.set(categoriesPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.categoriesPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  protected childrenCount(category: Category): number {
    return category.children?.length ?? 0;
  }

  protected childrenSummary(category: Category | null): string {
    const children = category?.children ?? [];

    if (!children.length) {
      return '-';
    }

    return children.map((child) => child.name).join(', ');
  }

  protected openCreateDialog(): void {
    this.categoryBeingEdited.set(null);
    this.categoryForm.reset({
      name: '',
      description: null,
      parentId: null
    });
    this.categoryForm.markAsPristine();
    this.categoryForm.markAsUntouched();
    this.isFormDialogVisible.set(true);
    this.loadParentCategories();
  }

  protected openEditDialog(category: Category): void {
    this.categoryService
      .getCategoryById(category.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (categoryDetail) => {
          this.categoryBeingEdited.set(categoryDetail);
          this.categoryForm.reset({
            name: categoryDetail.name,
            description: categoryDetail.description,
            parentId: categoryDetail.parent?.id ?? null
          });
          this.categoryForm.markAsPristine();
          this.categoryForm.markAsUntouched();
          this.isFormDialogVisible.set(true);
          this.loadParentCategories();
        },
        error: () => this.showDetailsLoadError()
      });
  }

  protected saveCategory(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    const payload = this.categoryPayload();
    const editingCategory = this.categoryBeingEdited();
    const request = editingCategory
      ? this.categoryService.updateCategory(editingCategory.id, payload)
      : this.categoryService.createCategory(payload);

    this.isSaving.set(true);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isFormDialogVisible.set(false);
        this.showSuccess(editingCategory ? 'Categoria atualizada com sucesso.' : 'Categoria criada com sucesso.');
        this.loadCategories(this.categoriesPage().number, this.pageSize(), this.sort());
      },
      error: () => {
        this.isSaving.set(false);
        this.showSaveError();
      }
    });
  }

  protected confirmDeleteCategory(category: Category): void {
    this.confirmationService.confirm({
      header: 'Excluir categoria',
      message: `Deseja excluir a categoria "${category.name}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deleteCategory(category)
    });
  }

  protected shouldShowError(controlName: 'name' | 'description' | 'parentId'): boolean {
    const control = this.categoryForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  protected openCategoryDetails(category: Category): void {
    this.isDetailsDialogVisible.set(true);
    this.isDetailsLoading.set(true);
    this.selectedCategory.set(null);

    this.categoryService
      .getCategoryById(category.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (categoryDetail) => {
          this.selectedCategory.set(categoryDetail);
          this.isDetailsLoading.set(false);
        },
        error: () => {
          this.isDetailsDialogVisible.set(false);
          this.isDetailsLoading.set(false);
          this.showDetailsLoadError();
        }
      });
  }

  private resolveSort(sortField?: string | string[] | null, sortOrder?: number | null): string {
    if (!sortField || Array.isArray(sortField)) {
      return this.sort();
    }

    return `${sortField},${sortOrder === -1 ? 'desc' : 'asc'}`;
  }

  private loadParentCategories(): void {
    this.isLoadingParentCategories.set(true);

    this.categoryService
      .listCategories(0, 100, 'name,asc')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (categoriesPage) => {
          this.parentCategories.set(categoriesPage.content);
          this.isLoadingParentCategories.set(false);
        },
        error: () => {
          this.parentCategories.set([]);
          this.isLoadingParentCategories.set(false);
          this.showLoadError();
        }
      });
  }

  private categoryPayload(): CategoryCreateRequest {
    const value = this.categoryForm.getRawValue();

    return {
      name: value.name.trim(),
      description: value.description?.trim() || null,
      parentId: value.parentId ?? null
    };
  }

  private deleteCategory(category: Category): void {
    this.categoryService
      .deleteCategory(category.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.showSuccess('Categoria excluída com sucesso.');
          const currentPage = this.categoriesPage();
          const shouldGoBack = currentPage.content.length === 1 && currentPage.number > 0;
          const nextPage = shouldGoBack ? currentPage.number - 1 : currentPage.number;

          this.loadCategories(nextPage, this.pageSize(), this.sort());
        },
        error: () => this.showDeleteError()
      });
  }

  private showSuccess(detail: string): void {
    this.messageService.add({
      severity: 'success',
      summary: 'Sucesso',
      detail,
      life: 3000
    });
  }

  private showSaveError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao salvar categoria',
      detail: 'Não foi possível salvar a categoria. Verifique os dados e tente novamente.',
      life: 5000
    });
  }

  private showDeleteError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao excluir categoria',
      detail: 'Não foi possível excluir a categoria. Verifique se ela não está em uso.',
      life: 5000
    });
  }

  private showLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar categorias',
      detail: 'Não foi possível buscar as categorias cadastradas. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showDetailsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar categoria',
      detail: 'Não foi possível buscar os detalhes da categoria. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
