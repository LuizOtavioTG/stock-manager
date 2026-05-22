import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { MultiSelectModule } from 'primeng/multiselect';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import { ToggleSwitchModule } from 'primeng/toggleswitch';

import { Page } from '../../../../models/page.model';
import {
  PaginatedListActionsDirective,
  PaginatedListBodyDirective,
  PaginatedListColumn,
  PaginatedListComponent,
  PaginatedListEmptyDirective
} from '../../../../shared/components/paginated-list/paginated-list';
import { Category } from '../../../categories/models/category.model';
import { CategoryService } from '../../../categories/services/category.service';
import { Supplier } from '../../../suppliers/models/supplier.model';
import { SupplierService } from '../../../suppliers/services/supplier.service';
import { Product } from '../../models/product.model';
import { ProductCreateRequest, ProductUpdateRequest } from '../../models/product-request.model';
import { ProductService } from '../../services/product.service';

function minArrayLengthValidator(minLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;

    return Array.isArray(value) && value.length >= minLength
      ? null
      : { minArrayLength: { requiredLength: minLength } };
  };
}

function futureOrPresentDateValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string | null;

    if (!value) {
      return null;
    }

    const selectedDate = new Date(`${value}T00:00:00`);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    return selectedDate >= today ? null : { futureOrPresent: true };
  };
}

const EMPTY_PAGE: Page<Product> = {
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
  selector: 'app-product-list',
  imports: [
    ButtonModule,
    ConfirmDialogModule,
    CurrencyPipe,
    DatePipe,
    DialogModule,
    InputNumberModule,
    InputTextModule,
    MultiSelectModule,
    PaginatedListActionsDirective,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    ReactiveFormsModule,
    SelectModule,
    TableModule,
    TagModule,
    TextareaModule,
    ToggleSwitchModule
  ],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss'
})
export class ProductListComponent implements OnInit {
  private readonly categoryService = inject(CategoryService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly productService = inject(ProductService);
  private readonly messageService = inject(MessageService);
  private readonly supplierService = inject(SupplierService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'SKU', field: 'sku' },
    { label: 'Nome', field: 'name' },
    { label: 'Marca', field: 'brand' },
    { label: 'Categoria', field: 'category.name' },
    { label: 'Fornecedores', sortable: false },
    { label: 'Unidade', field: 'unitOfMeasure' },
    { label: 'Preço de custo', field: 'costPrice', styleClass: 'numeric-column' },
    { label: 'Preço de venda', field: 'salePrice', styleClass: 'numeric-column' },
    { label: 'Status', field: 'active' },
    { label: 'Ações', sortable: false, styleClass: 'actions-column' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('name,asc');
  protected readonly productsPage = signal<Page<Product>>(EMPTY_PAGE);
  protected readonly selectedProduct = signal<Product | null>(null);
  protected readonly isDetailsDialogVisible = signal(false);
  protected readonly isDetailsLoading = signal(false);
  protected readonly isFormDialogVisible = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly productBeingEdited = signal<Product | null>(null);
  protected readonly categoryOptions = signal<Category[]>([]);
  protected readonly supplierOptions = signal<Supplier[]>([]);
  protected readonly isLoadingCategoryOptions = signal(false);
  protected readonly isLoadingSupplierOptions = signal(false);

  protected readonly products = computed(() => this.productsPage().content);
  protected readonly totalElements = computed(() => this.productsPage().totalElements);
  protected readonly first = computed(() => this.productsPage().number * this.productsPage().size);
  protected readonly formTitle = computed(() => (this.productBeingEdited() ? 'Editar produto' : 'Novo produto'));
  protected readonly minExpirationDate = this.todayString();

  protected readonly productForm = this.formBuilder.nonNullable.group({
    sku: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/\S/)]],
    name: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]],
    description: this.formBuilder.control<string | null>(null, [Validators.maxLength(255)]),
    brand: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/\S/)]],
    categoryId: this.formBuilder.control<number | null>(null, [Validators.required]),
    unitOfMeasure: ['', [Validators.required, Validators.maxLength(20), Validators.pattern(/\S/)]],
    costPrice: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    salePrice: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    expirationDate: this.formBuilder.control<string | null>(null, [futureOrPresentDateValidator()]),
    supplierIds: this.formBuilder.nonNullable.control<number[]>([], [minArrayLengthValidator(1)]),
    active: this.formBuilder.control<boolean | null>(true)
  });

  ngOnInit(): void {
    this.loadProducts(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadProducts(page, rows, sort);
  }

  protected loadProducts(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    this.productService
      .listProducts(page, size, sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (productsPage) => {
          this.productsPage.set(productsPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.productsPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  protected openCreateDialog(): void {
    this.productBeingEdited.set(null);
    this.productForm.reset({
      sku: '',
      name: '',
      description: null,
      brand: '',
      categoryId: null,
      unitOfMeasure: '',
      costPrice: null,
      salePrice: null,
      expirationDate: null,
      supplierIds: [],
      active: true
    });
    this.productForm.markAsPristine();
    this.productForm.markAsUntouched();
    this.isFormDialogVisible.set(true);
    this.loadFormOptions();
  }

  protected openEditDialog(product: Product): void {
    this.productService
      .getProductById(product.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (productDetail) => {
          this.productBeingEdited.set(productDetail);
          this.productForm.reset({
            sku: productDetail.sku,
            name: productDetail.name,
            description: productDetail.description,
            brand: productDetail.brand ?? '',
            categoryId: productDetail.category?.id ?? null,
            unitOfMeasure: productDetail.unitOfMeasure ?? '',
            costPrice: productDetail.costPrice,
            salePrice: productDetail.salePrice,
            expirationDate: productDetail.expirationDate?.slice(0, 10) ?? null,
            supplierIds: productDetail.suppliers?.map((supplier) => supplier.id) ?? [],
            active: productDetail.active
          });
          this.productForm.markAsPristine();
          this.productForm.markAsUntouched();
          this.isFormDialogVisible.set(true);
          this.loadFormOptions();
        },
        error: () => this.showDetailsLoadError()
      });
  }

  protected saveProduct(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    const editingProduct = this.productBeingEdited();
    const request = editingProduct
      ? this.productService.updateProduct(editingProduct.id, this.productUpdatePayload())
      : this.productService.createProduct(this.productCreatePayload());

    this.isSaving.set(true);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isFormDialogVisible.set(false);
        this.showSuccess(editingProduct ? 'Produto atualizado com sucesso.' : 'Produto criado com sucesso.');
        this.loadProducts(this.productsPage().number, this.pageSize(), this.sort());
      },
      error: () => {
        this.isSaving.set(false);
        this.showSaveError();
      }
    });
  }

  protected confirmDeleteProduct(product: Product): void {
    this.confirmationService.confirm({
      header: 'Excluir produto',
      message: `Deseja excluir o produto "${product.name}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deleteProduct(product)
    });
  }

  protected shouldShowError(
    controlName:
      | 'sku'
      | 'name'
      | 'description'
      | 'brand'
      | 'categoryId'
      | 'unitOfMeasure'
      | 'costPrice'
      | 'salePrice'
      | 'expirationDate'
      | 'supplierIds'
  ): boolean {
    const control = this.productForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  protected supplierSummary(product: Product): string {
    const suppliers = product.suppliers ?? [];

    if (!suppliers.length) {
      return '-';
    }

    if (suppliers.length === 1) {
      return suppliers[0].name;
    }

    return `${suppliers[0].name} +${suppliers.length - 1}`;
  }

  protected supplierList(product: Product | null): string {
    const suppliers = product?.suppliers ?? [];

    if (!suppliers.length) {
      return '-';
    }

    return suppliers.map((supplier) => supplier.name).join(', ');
  }

  protected openProductDetails(product: Product): void {
    this.isDetailsDialogVisible.set(true);
    this.isDetailsLoading.set(true);
    this.selectedProduct.set(null);

    this.productService
      .getProductById(product.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (productDetail) => {
          this.selectedProduct.set(productDetail);
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

  private loadFormOptions(): void {
    this.loadCategoryOptions();
    this.loadSupplierOptions();
  }

  private loadCategoryOptions(): void {
    this.isLoadingCategoryOptions.set(true);

    this.categoryService
      .listCategories(0, 100, 'name,asc')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (categoriesPage) => {
          this.categoryOptions.set(categoriesPage.content);
          this.isLoadingCategoryOptions.set(false);
        },
        error: () => {
          this.categoryOptions.set([]);
          this.isLoadingCategoryOptions.set(false);
          this.showOptionsLoadError();
        }
      });
  }

  private loadSupplierOptions(): void {
    this.isLoadingSupplierOptions.set(true);

    this.supplierService
      .listSuppliers(0, 100, 'name,asc')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (suppliersPage) => {
          this.supplierOptions.set(suppliersPage.content);
          this.isLoadingSupplierOptions.set(false);
        },
        error: () => {
          this.supplierOptions.set([]);
          this.isLoadingSupplierOptions.set(false);
          this.showOptionsLoadError();
        }
      });
  }

  private productCreatePayload(): ProductCreateRequest {
    const value = this.productForm.getRawValue();

    return {
      sku: value.sku.trim(),
      name: value.name.trim(),
      description: this.optionalText(value.description),
      brand: value.brand.trim(),
      categoryId: value.categoryId as number,
      unitOfMeasure: value.unitOfMeasure.trim(),
      costPrice: value.costPrice as number,
      salePrice: value.salePrice as number,
      expirationDate: value.expirationDate || null,
      supplierIds: value.supplierIds
    };
  }

  private productUpdatePayload(): ProductUpdateRequest {
    const value = this.productForm.getRawValue();

    return {
      ...this.productCreatePayload(),
      active: value.active ?? true
    };
  }

  private optionalText(value: string | null): string | null {
    return value?.trim() || null;
  }

  private deleteProduct(product: Product): void {
    this.productService
      .deleteProduct(product.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.showSuccess('Produto excluído com sucesso.');
          const currentPage = this.productsPage();
          const shouldGoBack = currentPage.content.length === 1 && currentPage.number > 0;
          const nextPage = shouldGoBack ? currentPage.number - 1 : currentPage.number;

          this.loadProducts(nextPage, this.pageSize(), this.sort());
        },
        error: () => this.showDeleteError()
      });
  }

  private todayString(): string {
    const today = new Date();
    const month = `${today.getMonth() + 1}`.padStart(2, '0');
    const day = `${today.getDate()}`.padStart(2, '0');

    return `${today.getFullYear()}-${month}-${day}`;
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
      summary: 'Erro ao salvar produto',
      detail: 'Não foi possível salvar o produto. Verifique os dados e tente novamente.',
      life: 5000
    });
  }

  private showDeleteError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao excluir produto',
      detail: 'Não foi possível excluir o produto. Verifique se ele não está em uso.',
      life: 5000
    });
  }

  private showOptionsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar opções',
      detail: 'Não foi possível buscar categorias ou fornecedores. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar produtos',
      detail: 'Não foi possível buscar a lista de produtos. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showDetailsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar produto',
      detail: 'Não foi possível buscar os detalhes do produto. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
