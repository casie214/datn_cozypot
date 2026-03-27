<template>
    <div class="voucher-manager-wrapper ">
        <div v-if="isSubmitting" class="global-loading">
            <div class="loading-box">
                <i class="fas fa-paper-plane fa-spin fa-2x mb-3"></i>
                <p>Hệ thống đang gửi email đến khách hàng...</p>
                <small>Vui lòng chờ trong giây lát</small>
            </div>
        </div>

        <div v-if="!isFormActive">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="title-page">Quản lý phiếu giảm giá</h2>
            </div>

            <div class="filter-card mb-4 shadow-sm p-3 bg-white rounded">
                <div class="row g-3 align-items-end">
                    <div class="col-md-6">
                        <label class="filter-label fw-bold">Tìm kiếm</label>
                        <input v-model="filters.keyword" class="form-control custom-input"
                            placeholder="Mã, code hoặc tên..." @input="onSearchInput">
                    </div>
                    <div class="col-md-3">
                        <label class="filter-label fw-bold">Từ ngày</label>
                        <input type="date" v-model="filters.ngayBatDau" class="form-control custom-input"
                            @change="() => { pagination.currentPage = 1; handleSearch(); }">
                    </div>
                    <div class="col-md-3">
                        <label class="filter-label fw-bold">Đến ngày</label>
                        <input type="date" v-model="filters.ngayKetThuc" class="form-control custom-input"
                            @change="() => { pagination.currentPage = 1; handleSearch(); }">
                    </div>
                    <div class="col-md-3">
                        <label class="filter-label fw-bold">Đối tượng</label>
                        <Multiselect class="custom-multiselect-theme" v-model="filters.doiTuong"
                            :options="doiTuongOptions" mode="single" label="label" valueProp="value" trackBy="value"
                            placeholder="Chọn tất cả đối tượng" />

                    </div>

                    <div class="col-md-3">
                        <label class="filter-label fw-bold">Loại giảm giá</label>
                        <Multiselect class="custom-multiselect-theme" v-model="filters.loaiGiamGia"
                            :options="loaiGiamGiaOptions" mode="single" label="label" valueProp="value" trackBy="value"
                            placeholder="Chọn tất cả loại" />
                    </div>

                    <div class="col-md-3">
                        <label class="filter-label fw-bold">Trạng thái</label>
                        <Multiselect class="custom-multiselect-theme" v-model="filters.trangThai"
                            :options="trangThaiOptions" mode="single" label="label" valueProp="value" trackBy="value"
                            placeholder="Chọn tất cả trạng thái" />
                    </div>
                    <div class="col-md-2 d-flex align-items-end gap-2">
                        <button class="btn-reset-filter" @click="resetFilters">
                            Xóa bộ lọc
                        </button>
                    </div>

                </div>
            </div>

            <div class="d-flex justify-content-end mb-3 gap-2">
                <div class="icon-tooltip">

                    <!-- Xuất Excel -->
                    <button class="btn-red-dark d-flex align-items-center justify-content-center" @click="exportExcel"
                        type="button">
                        <i class="fas fa-file-excel"></i>
                        <span class="tooltip-text">Xuất Excel</span>
                    </button>
                </div>
                <div class="icon-tooltip">

                    <!-- Thêm mới -->
                    <button class="btn-red-dark d-flex align-items-center justify-content-center" @click="openFormAdd"
                        type="button">
                        <i class="fas fa-plus"></i>
                        <span class="tooltip-text">Thêm phiếu giảm giá</span>
                    </button>
                </div>
            </div>

            <div class="table-container shadow-sm bg-white rounded overflow-hidden">
                <table class="table mb-0 custom-table">
                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>THÔNG TIN PHIẾU</th>
                            <th>GIÁ TRỊ & ĐIỀU KIỆN</th>
                            <th>ĐỐI TƯỢNG</th>
                            <th>SỐ LƯỢNG</th>
                            <th>THỜI GIAN HẠN</th>
                            <th>TRẠNG THÁI</th>
                            <th class="col-action">HÀNH ĐỘNG</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(pg, index) in listPhieuGiamGia" :key="pg.id">
                            <td>{{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}</td>
                            <td>
                                <div class="fw-bold text-dark pgg-name">
                                    {{ pg.tenPhieuGiamGia }}
                                </div>
                                <small class="text-muted">
                                    Mã: {{ pg.maPhieuGiamGia }} | Code: {{ pg.codeGiamGia }}
                                </small>
                            </td>


                            <td>
                                <div class=" fw-bold">
                                    Giảm: {{ pg.loaiGiamGia === 1 ? pg.giaTriGiam + '%' : formatPrice(pg.giaTriGiam) }}
                                </div>
                                <div class="small text-muted" style="font-size: 0.8rem;">
                                    - Tối đa: {{ formatPrice(pg.giaTriGiamToiDa) }} <br>
                                    - Đơn tối thiểu: {{ formatPrice(pg.donHangToiThieu) }}
                                </div>
                            </td>
                            <td>
                                <span class="doi-tuong">
                                    <i v-if="pg.doiTuong === 0" class="fa-solid fa-users me-1"></i>
                                    <i v-else class="fa-solid fa-user-lock me-1"></i>

                                    {{ pg.doiTuong === 0 ? 'Công khai' : 'Cá nhân' }}
                                </span>
                            </td>
                            <td class="text-center">{{ pg.soLuong }}</td>
                            <td>
                                <div style="font-size: 14px;">
                                    <span>Bắt đầu:</span> {{ formatDate(pg.ngayBatDau) }} <br>
                                    <span>Kết thúc:</span> {{ formatDate(pg.ngayKetThuc) }}
                                </div>
                            </td>
                            <td>
                                <span :class="getStatusDisplay(pg).class">
                                    {{ getStatusDisplay(pg).text }}
                                </span>
                            </td>
                            <td class="text-center">
                                <div class="action-group d-flex justify-content-center gap-3">

                                    <div class="icon-tooltip">
                                        <i class="fas fa-eye edit-icon" @click="openFormView(pg.id)"></i>
                                        <span class="tooltip-text">Xem chi tiết</span>
                                    </div>

                                    <div class="icon-tooltip">
                                        <i class="fas fa-pen edit-icon"
                                            :class="{ 'disabled-icon': getStatusDisplay(pg).text === 'Hết hạn' }"
                                            @click="getStatusDisplay(pg).text !== 'Hết hạn' && openFormEdit(pg.id)"></i>
                                        <span class="tooltip-text">Chỉnh sửa</span>
                                    </div>

                                    <div class="icon-tooltip d-inline-block">
                                        <div class="form-check form-switch mb-0">
                                            <input class="form-check-input custom-red-switch" type="checkbox"
                                                :checked="pg.trangThai === 1" :disabled="isExpired(pg.ngayKetThuc)"
                                                @click.prevent="!isExpired(pg.ngayKetThuc) && triggerToggleStatus(pg)" />
                                        </div>

                                        <span class="tooltip-text">
                                            {{ isExpired(pg.ngayKetThuc)
                                                ? 'Phiếu giảm giá đã hết hạn'
                                                : 'Bật / Tắt phiếu giảm giá' }}
                                        </span>
                                    </div>
                                </div>

                            </td>
                        </tr>
                        <tr v-if="listPhieuGiamGia.length === 0">
                            <td colspan="8" class="text-center py-4 text-muted">Không tìm thấy dữ liệu phù hợp</td>
                        </tr>
                    </tbody>
                </table>

                <div class="pagination-wrapper d-flex justify-content-between align-items-center mt-4 px-2">

                    <div class="d-flex align-items-center">
                        <span class="me-2 text-muted">Hiển thị</span>
                        <select v-model="pagination.pageSize"
                            @change="() => { pagination.currentPage = 1; handleSearch(); }">

                            <option :value="5">5 dòng</option>
                            <option :value="8">8 dòng</option>
                            <option :value="10">10 dòng</option>
                            <option :value="20">20 dòng</option>
                        </select>
                    </div>

                    <div class="pagination-controls d-flex align-items-center">
                        <button class="btn-page" :disabled="pagination.currentPage === 1" @click="goToPage(1)">
                            <i class="fas fa-step-backward"></i>
                        </button>

                        <button class="btn-page" :disabled="pagination.currentPage === 1" @click="changePage(-1)">
                            <i class="fas fa-chevron-left"></i>
                        </button>

                        <div class="mx-3 d-flex align-items-center">
                            <template v-if="pagination.totalPages <= 5">
                                <span v-for="p in pagination.totalPages" :key="p" class="page-number"
                                    :class="{ active: pagination.currentPage === p }" @click="goToPage(p)">
                                    {{ p }}
                                </span>
                            </template>

                            <template v-else>
                                <div class="input-page-wrapper d-flex align-items-center">
                                    <input type="number" v-model.number="inputPage" class="input-go-to"
                                        @keyup.enter="jumpToPage">
                                    <span class="ms-2 text-muted">
                                        / {{ pagination.totalPages }}
                                    </span>
                                </div>
                            </template>
                        </div>

                        <button class="btn-page" :disabled="pagination.currentPage === pagination.totalPages"
                            @click="changePage(1)">
                            <i class="fas fa-chevron-right"></i>
                        </button>

                        <button class="btn-page" :disabled="pagination.currentPage === pagination.totalPages"
                            @click="goToPage(pagination.totalPages)">
                            <i class="fas fa-step-forward"></i>
                        </button>
                    </div>

                    <div class="total-info text-muted">
                        Hiển thị {{ listPhieuGiamGia.length }} /
                        {{ pagination.totalElements }} phiếu giảm giá
                    </div>

                </div>

            </div>
        </div>

        <div v-else class="form-page-container animate__animated animate__fadeIn">
            <div class="d-flex align-items-center mb-4">
                <button class="btn btn-red-dark me-3" @click="closeForm">
                    <i class="fas fa-arrow-left"></i> Quay lại
                </button>
                <h2 class="title-page mb-0">
                    {{ isReadOnly ? 'Chi tiết phiếu' : (selectedId ? 'Cập nhật phiếu' : 'Thêm mới phiếu giảm giá') }}
                </h2>

            </div>


            <form @submit.prevent="triggerSubmit">
                <div class="row g-4">
                    <div :class="isCustomerListOpen ? 'col-md-12' : 'col-md-12'">
                        <div class="card border-0 shadow-sm p-4 h-100">
                            <h5 class="mb-4 text-maroon"><i class="fa-solid fa-circle-info me-2"></i>Thông tin phiếu
                                giảm giá</h5>
                            <div class="row mb-3">
                                <!-- TÊN PHIẾU GIẢM GIÁ -->
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Tên phiếu giảm giá <span
                                            class="text-danger">*</span></label>
                                    <input v-model="formData.tenPhieuGiamGia" type="text" maxlength="200"
                                        class="form-control custom-input"
                                        :class="{ 'is-invalid': errors.tenPhieuGiamGia }" :disabled="isReadOnly"
                                        placeholder="Ví dụ: Khuyến mãi Tết Nguyên Đán">
                                    <div class="invalid-feedback">{{ errors.tenPhieuGiamGia }}</div>

                                    <small class="text-muted">
                                        {{ formData.tenPhieuGiamGia.length }}/200 ký tự
                                    </small>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Mã CODE <span class="text-danger">*</span></label>
                                    <input v-model="formData.codeGiamGia" type="text" maxlength="20"
                                        class="form-control custom-input text-uppercase"
                                        :class="{ 'is-invalid': errors.codeGiamGia }" :disabled="isReadOnly"
                                        placeholder="Ví dụ: TET2026">
                                    <div class="invalid-feedback">{{ errors.codeGiamGia }}</div>

                                    <small class="text-muted">
                                        {{ formData.codeGiamGia.length }}/20 ký tự
                                    </small>

                                </div>
                            </div>
                            <div class="row mb-3">
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">
                                        Loại giảm giá <span class="text-danger">*</span>
                                    </label>
                                    <div class="d-flex gap-3 mt-2">
                                        <div class="option-card" :class="{ active: formData.loaiGiamGia === 1 }"
                                            @click="!isReadOnly && (formData.loaiGiamGia = 1)">
                                            <input type="radio" class="d-none" :value="1"
                                                v-model="formData.loaiGiamGia" />
                                            <i class="fa-solid fa-percent me-2"></i>
                                            Giảm %
                                        </div>

                                        <div class="option-card" :class="{ active: formData.loaiGiamGia === 0 }"
                                            @click="!isReadOnly && (formData.loaiGiamGia = 0)">
                                            <input type="radio" class="d-none" :value="0"
                                                v-model="formData.loaiGiamGia" />
                                            <i class="fa-solid fa-money-bill-wave me-2"></i>
                                            Giảm tiền
                                        </div>
                                    </div>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label fw-bold">
                                        Đối tượng áp dụng
                                    </label>

                                    <div class="d-flex gap-3 mt-2">
                                        <div class="option-card" :class="{ active: formData.doiTuong === 0 }"
                                            @click="!isReadOnly && (formData.doiTuong = 0, isCustomerListOpen = false)">
                                            <input type="radio" class="d-none" :value="0" v-model="formData.doiTuong" />
                                            <i class="fa-solid fa-users me-2"></i>
                                            Công khai
                                        </div>

                                        <div class="option-card" :class="{ active: formData.doiTuong === 1 }"
                                            @click="!isReadOnly && (formData.doiTuong = 1, isCustomerListOpen = true)">
                                            <input type="radio" class="d-none" :value="1" v-model="formData.doiTuong" />
                                            <i class="fa-solid fa-user-lock me-2"></i>
                                            Cá nhân
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">Giá trị giảm <span
                                            class="text-danger">*</span></label>
                                    <div class="input-group has-validation"> <input
                                            :value="formatNumberInput(formData.giaTriGiam)"
                                            @input="e => formData.giaTriGiam = parseNumber(e.target.value)" type="text"
                                            inputmode="numeric" class="form-control custom-input"
                                            :class="{ 'is-invalid': errors.giaTriGiam }" :disabled="isReadOnly" />
                                        <span class="input-group-text">{{ formData.loaiGiamGia === 1 ? '%' : 'đ'
                                        }}</span>
                                        <div class="invalid-feedback">{{ errors.giaTriGiam }}</div>
                                    </div>
                                </div>

                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">Số lượng <span
                                            class="text-danger">*</span></label>
                                    <input v-model.number="formData.soLuong" type="number"
                                        class="form-control custom-input" :class="{ 'is-invalid': errors.soLuong }"
                                        :disabled="isReadOnly || formData.doiTuong === 1">
                                    <div class="invalid-feedback">{{ errors.soLuong }}</div>
                                </div>

                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">Giảm tối đa <span
                                            class="text-danger">*</span></label>

                                    <input :value="formatNumberInput(formData.giaTriGiamToiDa)"
                                        @input="e => formData.giaTriGiamToiDa = parseNumber(e.target.value)" type="text"
                                        inputmode="numeric" class="form-control custom-input"
                                        :class="{ 'is-invalid': errors.giaTriGiamToiDa }" />

                                    <div class="invalid-feedback">
                                        {{ errors.giaTriGiamToiDa }}
                                    </div>
                                </div>

                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">Đơn tối thiểu <span
                                            class="text-danger">*</span></label>
                                    <input :value="formatNumberInput(formData.donHangToiThieu)"
                                        @input="e => formData.donHangToiThieu = parseNumber(e.target.value)" type="text"
                                        inputmode="numeric" class="form-control custom-input"
                                        :class="{ 'is-invalid': errors.donHangToiThieu }" :disabled="isReadOnly" />
                                    <div class="invalid-feedback">{{ errors.donHangToiThieu }}</div>
                                </div>


                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">Ngày bắt đầu <span
                                            class="text-danger">*</span></label>
                                    <input v-model="formData.ngayBatDau" type="datetime-local"
                                        class="form-control custom-input" :class="{ 'is-invalid': errors.ngayBatDau }"
                                        :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.ngayBatDau }}</div>
                                </div>

                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">Ngày kết thúc<span
                                            class="text-danger">*</span></label>
                                    <input v-model="formData.ngayKetThuc" type="datetime-local"
                                        class="form-control custom-input" :class="{ 'is-invalid': errors.ngayKetThuc }"
                                        :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.ngayKetThuc }}</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div v-if="isCustomerListOpen" class="col-md-12">
                        <transition name="fade-slide">
                            <div class="card border-0 shadow-sm p-4 h-100" style="background-color: #f8f9fa;">

                                <div class="d-flex justify-content-between align-items-center mb-4">
                                    <h5 class="mb-0 text-maroon"><i class="fa-solid fa-users me-2"></i>Khách hàng áp
                                        dụng
                                    </h5>
                                    <div class="form-check" v-if="formData.doiTuong === 1">
                                        <input class="form-check-input" type="checkbox" id="selectAll"
                                            :checked="isAllCustomersSelected" @change="toggleAllCustomers">
                                        <label class="form-check-label fw-bold" for="selectAll">Chọn tất cả</label>
                                    </div>
                                </div>

                                <div class="mb-4" customer-dual-wrapper>
                                    <div class="row g-3 align-items-end">

                                        <!-- Search -->
                                        <div class="col-md-12">
                                            <label class="fw-bold mb-1">Tìm khách hàng</label>

                                            <div class="input-group search-customer">
                                                <span class="input-group-text">
                                                    <i class="fas fa-search"></i>
                                                </span>

                                                <input v-model="customerSearch" type="text" class="form-control"
                                                    placeholder="Tên, email, SĐT hoặc mã khách hàng..." />

                                                <!-- 🔴 BUTTON BỎ LỌC -->
                                                <button class="btn btn-outline-secondary" type="button"
                                                    @click="resetCustomerFilter" :disabled="!customerSearch"
                                                    title="Bỏ lọc">
                                                    <i class="fas fa-times"></i>
                                                </button>
                                            </div>

                                        </div>

                                        <div class="row g-3 align-items-end">
                                            <!-- Tháng thống kê -->
                                            <div class="col-md-4">
                                                <label class="fw-bold mb-1">Tháng thống kê</label>
                                                <select v-model="customerMonth" class="form-select custom-input">
                                                    <option v-for="m in 12" :key="m"
                                                        :value="`${dayjs().year()}-${String(m).padStart(2, '0')}`">
                                                        Tháng {{ m }}/{{ dayjs().year() }}
                                                    </option>
                                                </select>
                                            </div>

                                            <!-- Sắp xếp -->
                                            <div class="col-md-4">
                                                <label class="fw-bold mb-1">Sắp xếp theo</label>
                                                <select v-model="sortConfig.field" class="form-select custom-input">
                                                    <option :value="null">Mặc định</option>
                                                    <option value="soLanDatTrongThang">Số lần đặt</option>
                                                    <option value="tongChiTieuTrongThang">Chi tiêu (tháng)</option>
                                                </select>
                                            </div>

                                            <!-- Chiều sắp xếp -->
                                            <div class="col-md-4">
                                                <label class="fw-bold mb-1">Thứ tự</label>
                                                <select v-model="sortConfig.direction" class="form-select custom-input">
                                                    <option value="desc">Giảm dần</option>
                                                    <option value="asc">Tăng dần</option>
                                                </select>
                                            </div>
                                        </div>

                                    </div>
                                </div>
                                <div class="table-responsive customer-table-wrapper">
                                    <table class="table table-hover align-middle mb-0">
                                        <thead class="table-light sticky-top">
                                            <tr>
                                                <th class="text-center" style="width:50px">Chọn</th>
                                                <th>Mã KH</th>
                                                <th>Tên KH</th>
                                                <th>Ngày sinh</th>
                                                <th>SĐT</th>
                                                <th>Email</th>

                                                <th class="text-center">Số lần đặt</th>
                                                <th class="text-end">Chi tiêu (tháng)</th>
                                                <th>Ngày mua gần nhất</th>
                                            </tr>
                                        </thead>

                                        <tbody v-if="formData.doiTuong === 1">
                                            <tr v-for="kh in pagedCustomers" :key="kh.id">
                                                <td class="text-center">
                                                    <input type="checkbox" :value="kh.id"
                                                        v-model="formData.listIdKhachHang" class="form-check-input"
                                                        :disabled="isReadOnly" />
                                                </td>

                                                <td>
                                                    <span class="fw-bold">{{ kh.maKhachHang }}</span>
                                                </td>

                                                <td>{{ kh.tenKhachHang }}</td>

                                                <td>{{ formatDateOnly(kh.ngaySinh) }}</td>

                                                <td>{{ kh.soDienThoai }}</td>

                                                <td class="text-muted small">{{ kh.email }}</td>

                                                <td>
                                                    <span>
                                                        {{ kh.soLanDatTrongThang }}
                                                    </span>
                                                </td>

                                                <td class="text-danger fw-bold">
                                                    {{ formatCurrency(kh.tongChiTieuTrongThang) }}
                                                </td>

                                                <td>
                                                    {{ kh.lanDatGanNhat ? formatDateTime(kh.lanDatGanNhat) : '—' }}
                                                </td>

                                            </tr>
                                        </tbody>
                                        <tr v-if="filteredCustomers.length === 0">
                                            <td colspan="9" class="text-center text-muted py-4">
                                                Không có dữ liệu khách hàng trong tháng này
                                            </td>
                                        </tr>
                                    </table>
                                    <!-- Pagination Khách Hàng -->
                                    <div class="d-flex justify-content-center mt-3 gap-2">

                                        <button class="btn btn-sm btn-light"
                                            :disabled="customerPagination.currentPage === 1"
                                            @click="customerPagination.currentPage--">
                                            «
                                        </button>

                                        <span class="fw-bold">
                                            {{ customerPagination.currentPage }} /
                                            {{ customerTotalPages }}
                                        </span>

                                        <button class="btn btn-sm btn-light"
                                            :disabled="customerPagination.currentPage === customerTotalPages"
                                            @click="customerPagination.currentPage++">
                                            »
                                        </button>

                                    </div>

                                </div>
                                <div class="customer-selected-card mt-4 customer-table-wrapper">

                                    <div class="d-flex justify-content-between align-items-center mb-3">
                                        <h6 class="mb-0 text-maroon fw-bold">
                                            <i class=" me-2"></i>
                                            Danh sách khách hàng đã chọn
                                        </h6>

                                        <span class="badge badge-neutral">
                                            {{ selectedCustomers.length }} khách hàng
                                        </span>
                                    </div>

                                    <div class="table-responsive custom-scrollbar">
                                        <table class="table table-hover align-middle mb-0 customer-table">
                                            <thead>
                                                <tr>
                                                    <th>#</th>
                                                    <th>Mã KH</th>
                                                    <th>Tên khách hàng</th>
                                                    <th>Email</th>
                                                    <th>Thao tác</th>
                                                </tr>
                                            </thead>

                                            <tbody>
                                                <tr v-for="(c, index) in selectedCustomers" :key="c.id">
                                                    <td>{{ index + 1 }}</td>
                                                    <td>{{ c.maKhachHang }}</td>
                                                    <td class="text-start">{{ c.tenKhachHang }}</td>
                                                    <td class="text-start">{{ c.email }}</td>
                                                    <td>
                                                        <button class="btn btn-sm btn-remove-customer"
                                                            @click="removeCustomer(c.id)">
                                                            <i class="fa-solid fa-xmark"></i>
                                                        </button>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>

                            </div>
                        </transition>
                    </div>
                </div>
                <div class="mt-4 d-flex justify-content-end gap-2">
                    <div class="card-footer bg-white border-top p-4 d-flex gap-3" style="align-items: end;">
                        <button type="button"
                            class="btn btn-outline-custom px-4 fw-bold d-flex align-items-center justify-content-center"
                            style="height: 42px;" @click="closeForm">
                            HỦY BỎ
                        </button>

                        <button v-if="!isReadOnly" type="submit"
                            class="btn btn-red-dark px-5 fw-bold shadow-sm d-flex align-items-center justify-content-center"
                            :disabled="isSubmitting">
                            <span v-if="isSubmitting">
                                <i class="fas fa-spinner fa-spin me-2"></i>
                                Đang gửi email...
                            </span>

                            <span v-else>
                                <i class="fas fa-save me-2"></i>
                                LƯU DỮ LIỆU
                            </span>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch, nextTick } from 'vue';
import Multiselect from '@vueform/multiselect'
import '@vueform/multiselect/themes/default.css'
import axios from 'axios';
import voucherService from '@/services/voucherService';
import Swal from 'sweetalert2'
const formatCurrency = (value) => {
    if (!value) return '0 đ'
    return value.toLocaleString('vi-VN') + ' đ'
}
const doiTuongOptions = [

    { value: 0, label: 'Công khai' },
    { value: 1, label: 'Cá nhân' }
]
const formatNumberInput = (value) => {
    if (!value && value !== 0) return ''
    return Number(value).toLocaleString('en-US') // 👉 đổi ở đây
}

const parseNumber = (value) => {
    if (!value) return null   // 🔴 quan trọng
    return Number(value.replace(/,/g, ''))
}
const loaiGiamGiaOptions = [

    { value: 1, label: 'Giảm theo %' },
    { value: 0, label: 'Giảm theo tiền' }
]
const trangThaiOptions = [
    { value: 1, label: 'Đang hoạt động' },
    { value: 3, label: 'Sắp diễn ra' },
    { value: 2, label: 'Hết hạn' },
    { value: 0, label: 'Ngừng hoạt động' }
]
const formatDateTime = (value) => {
    return new Date(value).toLocaleString('vi-VN')
}
const sortConfig = reactive({
    field: null, // 'soLanDatTrongThang' | 'tongChiTieuTrongThang' | 'lanDatGanNhat'
    direction: 'desc' // 'asc' | 'desc'
});

const exportExcel = async () => {
    try {
        const response = await axios.get(
            '/phieu-giam-gia/export-excel',
            {
                params: {
                    keyword: filters.keyword,
                    trangThai: filters.trangThai,
                    doiTuong: filters.doiTuong,
                    loaiGiamGia: filters.loaiGiamGia,
                    ngayBatDau: filters.ngayBatDau,
                    ngayKetThuc: filters.ngayKetThuc
                },
                responseType: 'blob'
            }
        );

        const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });

        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'danh_sach_phieu_giam_gia.xlsx';
        link.click();
        window.URL.revokeObjectURL(url);

        showAlert('Thành công', 'Xuất Excel thành công!');
    } catch (error) {
        console.error(error);
        showAlert('Lỗi', 'Xuất Excel thất bại!', 'error');
    }
};

const customerPagination = reactive({
    currentPage: 1,
    pageSize: 6, // mỗi trang 6 KH
});

const cleanParams = (obj) => {
    const cleaned = {};
    Object.keys(obj).forEach(key => {
        const v = obj[key];
        if (v !== null && v !== undefined && v !== '') {
            cleaned[key] = v;
        }
    });
    return cleaned;
};



// --- TRẠNG THÁI GIAO DIỆN ---
const isFormActive = ref(false);
const isReadOnly = ref(false);
const selectedId = ref(null);
const customerSearch = ref('');
const listPhieuGiamGia = ref([]);
const listKhachHang = ref([]);
const customerMonth = ref(dayjs().format('YYYY-MM')); // mặc định tháng hiện tại

// --- DỮ LIỆU PHIẾU GIẢM GIÁ ---
const formData = reactive({
    maPhieuGiamGia: '',
    codeGiamGia: '',
    tenPhieuGiamGia: '',
    loaiGiamGia: 1,
    giaTriGiam: 0,
    giaTriGiamToiDa: null,   // sửa ở đây
    donHangToiThieu: null,
    doiTuong: 0,
    ngayBatDau: '',
    ngayKetThuc: '',
    soLuong: 0,
    listIdKhachHang: [],
    listEmails: []
});
const listDotKhuyenMai = ref([]);


// --- PHÂN TRANG & BỘ LỌC ---
const filters = reactive({
    keyword: '',
    trangThai: null,
    doiTuong: null,
    loaiGiamGia: null,
    ngayBatDau: '',
    ngayKetThuc: '',

    // ⭐ THÊM
    phanTramMin: null,
    phanTramMax: null,
    tienMin: null,
    tienMax: null
});
const showAlert = (title, text, icon = 'success') => {
    Swal.fire({
        icon,
        title,
        text,
        timer: 2000,              // ⏳ 2 giây tự tắt
        timerProgressBar: true,   // thanh chạy thời gian
        showConfirmButton: false, // ❌ bỏ nút OK
        background: '#fff',
        color: '#333',
        confirmButtonText: '✔ Đồng ý',
        confirmButtonColor: '#8b0000',
    })
}

// ===== XÁC NHẬN =====
const showConfirm = async (title, text) => {
    const result = await Swal.fire({
        title,
        text,
        icon: 'question',
        iconColor: '#7D161A',
        showCancelButton: true,
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        confirmButtonColor: '#800000',
        cancelButtonColor: '#6c757d',
        reverseButtons: true
    })

    return result.isConfirmed
}

const triggerSubmit = async () => {

    clearErrors()

    if (!validateForm()) {
        showAlert(
            "Lỗi nhập liệu",
            "Vui lòng kiểm tra lại các trường thông tin",
            "error"
        )
        return
    }

    const confirmed = await showConfirm(
        "Xác nhận lưu dữ liệu",
        "Hệ thống sẽ lưu phiếu và gửi email đến khách hàng."
    )

    if (!confirmed) return

    try {
        isSubmitting.value = true

        formData.listEmails = listKhachHang.value
            .filter(kh => formData.listIdKhachHang.includes(kh.id))
            .map(kh => kh.email)

        if (formData.doiTuong === 1 && formData.listEmails.length === 0) {
            showAlert("Lỗi", "Không tìm thấy email khách hàng", "error")
            return
        }

        if (selectedId.value) {
            await voucherService.update(selectedId.value, formData)
            showAlert("Thành công", "Phiếu đã được cập nhật")
        } else {
            await voucherService.create(formData)
            showAlert("Thành công", "Tạo phiếu mới thành công")
        }

        closeForm()
        handleSearch()

    } catch (err) {

        const message =
            err.response?.data?.message ||
            err.message ||
            "Có lỗi xảy ra"

        // 🔴 Nếu lỗi trùng code
        if (message.toLowerCase().includes("code")) {
            errors.codeGiamGia = "Code đã tồn tại"
        }

        showAlert("Lỗi", message, "error")
    }
    finally {
        isSubmitting.value = false
    }
}

const pagination = reactive({
    currentPage: 1,
    pageSize: 5,
    totalPages: 0,
    totalElements: 0
});

// --- QUẢN LÝ LỖI (VALIDATE) ---
const errors = reactive({});

const clearErrors = () => {
    Object.keys(errors).forEach(key => delete errors[key]);
};

const validateForm = () => {
    let isValid = true
    clearErrors()
const nameRegex = /^[a-zA-ZÀ-ỹ0-9\s]+$/;

if (!formData.tenPhieuGiamGia) {
    errors.tenPhieuGiamGia = "Tên phiếu giảm giá không được để trống";
    isValid = false;
}
else if (!nameRegex.test(formData.tenPhieuGiamGia)) {
    errors.tenPhieuGiamGia = "Tên không được chứa ký tự đặc biệt";
    isValid = false;
}
else if (formData.tenPhieuGiamGia.length < 2 || formData.tenPhieuGiamGia.length > 200) {
    errors.tenPhieuGiamGia = "Tên phiếu phải từ 2–200 ký tự";
    isValid = false;
}
    /* ========================
       1. Chuẩn hóa dữ liệu
    ======================== */
    formData.tenPhieuGiamGia = formData.tenPhieuGiamGia?.trim() || ''
    formData.codeGiamGia = formData.codeGiamGia?.trim().toUpperCase() || ''

    /* ========================
       2. Tên phiếu giảm giá
       2 – 200 ký tự
    ======================== */
    if (!formData.tenPhieuGiamGia) {
        errors.tenPhieuGiamGia = "Tên phiếu giảm giá không được để trống"
        isValid = false
    }
    else if (formData.tenPhieuGiamGia.length < 2 || formData.tenPhieuGiamGia.length > 200) {
        errors.tenPhieuGiamGia = "Tên phiếu phải từ 2–200 ký tự"
        isValid = false
    }

    /* ========================
       3. Code giảm giá
       3–20 ký tự
       chỉ chữ và số
    ======================== */
    const codeRegex = /^[A-Z0-9]{3,20}$/

    if (!formData.codeGiamGia) {
        errors.codeGiamGia = "Mã CODE không được để trống"
        isValid = false
    }
    else if (!codeRegex.test(formData.codeGiamGia)) {
        errors.codeGiamGia = "CODE chỉ chứa chữ và số, từ 3–20 ký tự"
        isValid = false
    }

    /* ========================
       4. Loại giảm giá
    ======================== */
    if (formData.loaiGiamGia === null || formData.loaiGiamGia === undefined) {
        errors.loaiGiamGia = "Vui lòng chọn loại giảm giá"
        isValid = false
    }

    /* ========================
       5. Giá trị giảm
    ======================== */
    if (!formData.giaTriGiam) {
        errors.giaTriGiam = "Giá trị giảm không được để trống"
        isValid = false
    }
    else {
        if (formData.loaiGiamGia === 1) {
            if (formData.giaTriGiam < 1 || formData.giaTriGiam > 100) {
                errors.giaTriGiam = "Giảm % phải từ 1–100"
                isValid = false
            }
        }

        if (formData.loaiGiamGia === 0) {
            if (formData.giaTriGiam < 1000 || formData.giaTriGiam > 10000000) {
                errors.giaTriGiam = "Giảm tiền từ 1.000 – 10.000.000"
                isValid = false
            }
        }
    }

    /* ========================
       6. Giảm tối đa
       >= 0
    ======================== */
    if (formData.giaTriGiamToiDa === null || formData.giaTriGiamToiDa === undefined) {
        errors.giaTriGiamToiDa = "Giảm tối đa không được để trống"
        isValid = false
    }
    else if (formData.giaTriGiamToiDa <= 0) {
        errors.giaTriGiamToiDa = "Giảm tối đa phải > 0"
        isValid = false
    }

    /* ========================
       7. Đơn tối thiểu
       >= 0
    ======================== */
    if (formData.donHangToiThieu === null || formData.donHangToiThieu === undefined) {
        errors.donHangToiThieu = "Đơn tối thiểu không được để trống"
        isValid = false
    }
    else if (formData.donHangToiThieu <= 0) {
        errors.donHangToiThieu = "Đơn tối thiểu phải > 0"
        isValid = false
    }
    /* ========================
       8. Số lượng
       số nguyên > 0
    ======================== */
    if (!formData.soLuong) {
        errors.soLuong = "Số lượng không được để trống"
        isValid = false
    }
    else if (!Number.isInteger(formData.soLuong) || formData.soLuong <= 0) {
        errors.soLuong = "Số lượng phải là số nguyên > 0"
        isValid = false
    }

    /* ========================
       9. Đối tượng áp dụng
    ======================== */
    if (formData.doiTuong === null || formData.doiTuong === undefined) {
        errors.doiTuong = "Vui lòng chọn đối tượng áp dụng"
        isValid = false
    }

    /* ========================
       10. Cá nhân phải chọn KH
    ======================== */
    if (formData.doiTuong === 1) {
        if (!formData.listIdKhachHang || formData.listIdKhachHang.length === 0) {
            errors.khachHang = "Phải chọn ít nhất 1 khách hàng"
            isValid = false
        }
    }

    /* ========================
       11. Ngày bắt đầu
    ======================== */
    if (!formData.ngayBatDau) {
        errors.ngayBatDau = "Ngày bắt đầu không được để trống"
        isValid = false
    }

    /* ========================
       12. Ngày kết thúc
    ======================== */
    if (!formData.ngayKetThuc) {
        errors.ngayKetThuc = "Ngày kết thúc không được để trống"
        isValid = false
    }

    if (formData.ngayBatDau && formData.ngayKetThuc) {
        const start = new Date(formData.ngayBatDau)
        const end = new Date(formData.ngayKetThuc)

        if (end <= start) {
            errors.ngayKetThuc = "Ngày kết thúc phải lớn hơn ngày bắt đầu"
            isValid = false
        }
    }

    return isValid
}
const filteredCustomers = computed(() => {
    if (!Array.isArray(listKhachHang.value)) return [];

    let result = [...listKhachHang.value];

    // 🔍 Search
    const k = customerSearch.value?.toLowerCase().trim();
    if (k) {
        result = result.filter(kh => {
            const ten = (kh.hoTen || kh.tenKhachHang || '').toLowerCase();
            const email = (kh.email || '').toLowerCase();
            const sdt = (kh.soDienThoai || '');
            const ma = (kh.maKhachHang || '').toLowerCase();

            return ten.includes(k)
                || email.includes(k)
                || sdt.includes(k)
                || ma.includes(k);
        });
    }

    // 🔃 SORT
    if (sortConfig.field) {
        result.sort((a, b) => {
            const v1 = a[sortConfig.field] ?? 0;
            const v2 = b[sortConfig.field] ?? 0;

            return sortConfig.direction === 'asc'
                ? v1 - v2
                : v2 - v1;
        });
    }

    return result;
});

const pagedCustomers = computed(() => {
    const start =
        (customerPagination.currentPage - 1) *
        customerPagination.pageSize;

    return filteredCustomers.value.slice(
        start,
        start + customerPagination.pageSize
    );
});

const customerTotalPages = computed(() => {
    return Math.ceil(
        filteredCustomers.value.length /
        customerPagination.pageSize
    );
});


const isAllCustomersSelected = computed(() => {
    return filteredCustomers.value.length > 0 &&
        filteredCustomers.value.every(kh => formData.listIdKhachHang.includes(kh.id));
});

const toggleAllCustomers = (e) => {
    const visibleIds = filteredCustomers.value.map(kh => Number(kh.id));
    if (e.target.checked) {
        formData.listIdKhachHang = [...new Set([...formData.listIdKhachHang, ...visibleIds])];
    } else {
        formData.listIdKhachHang = formData.listIdKhachHang.filter(id => !visibleIds.includes(id));
    }
};

const handleSearch = async () => {
    try {

        const trangThaiFilter = filters.trangThai !== null
            ? Number(filters.trangThai)
            : null;

        const doiTuong = filters.doiTuong !== null
            ? Number(filters.doiTuong)
            : null;

        const loaiGiamGia = filters.loaiGiamGia !== null
            ? Number(filters.loaiGiamGia)
            : null;

        // ⚠ Chỉ gửi 0 hoặc 1 cho backend
        const backendTrangThai =
            trangThaiFilter === 0 || trangThaiFilter === 1
                ? trangThaiFilter
                : null;

        const res = await voucherService.fetchData(
            {
                ...filters,
                keyword: filters.keyword?.trim(), // ⭐ bỏ khoảng trắng

                trangThai: backendTrangThai,
                doiTuong,
                loaiGiamGia
            },
            pagination
        );

        let rawData = res.content || [];

        const now = Date.now();

        // =============================
        // 🔥 LỌC FRONTEND CHO 2 & 3
        // =============================
        if (trangThaiFilter === 2) {
            // HẾT HẠN
            rawData = rawData.filter(pg =>
                new Date(pg.ngayKetThuc).getTime() < now
            );
        }

        if (trangThaiFilter === 3) {
            // SẮP DIỄN RA
            rawData = rawData.filter(pg =>
                new Date(pg.ngayBatDau).getTime() > now
            );
        }

        if (trangThaiFilter === 1) {
            // ĐANG HOẠT ĐỘNG
            rawData = rawData.filter(pg =>
                pg.trangThai === 1 &&
                new Date(pg.ngayBatDau).getTime() <= now &&
                new Date(pg.ngayKetThuc).getTime() >= now
            );
        }

        listPhieuGiamGia.value = rawData;
        pagination.totalPages = res.totalPages || 0;
        pagination.totalElements = res.totalElements || 0;

    } catch (e) {
        console.error("Lỗi tải danh sách:", e);
        showAlert("Lỗi", "Không thể tải danh sách phiếu giảm giá", "error");
    }
};

const dotKhuyenMaiMap = computed(() => {
    const map = {};
    listDotKhuyenMai.value.forEach(d => {
        map[Number(d.id)] = d.tenDotKhuyenMai;
    });
    return map;
});

import dayjs from 'dayjs';
import axiosClient from '@/services/axiosClient';

// Hàm định dạng ngày tháng dd/mm/yyyy HH:mm
const formatDate = (dateString) => {
    if (!dateString) return '---';
    return dayjs(dateString).format('DD/MM/YYYY HH:mm');
};
// Thêm vào cùng nhóm với các ref trạng thái giao diện
const isCustomerListOpen = ref(false);
// Hàm chỉ lấy ngày dd/mm/yyyy
const formatDateOnly = (dateString) => {
    if (!dateString) return '---';
    return dayjs(dateString).format('DD/MM/YYYY');
};

let originalData = {};
const closeForm = () => {
    clearErrors(); // ⭐⭐ THÊM
    isFormActive.value = false;
    isCustomerListOpen.value = false;
};
const isFilterActive = computed(() => {
    return !!customerSearch.value;
});

const loadDetail = async (id) => {
    clearErrors(); // ⭐⭐ THÊM DÒNG NÀY

    try {
        const res = await voucherService.getById(id);

        Object.assign(formData, res);

        originalData = {
            codeGiamGia: res.codeGiamGia,
            tenPhieuGiamGia: res.tenPhieuGiamGia
        };

        formData.doiTuong = Number(res.doiTuong);
        isCustomerListOpen.value = formData.doiTuong === 1;

        if (res.ngayBatDau) formData.ngayBatDau = res.ngayBatDau.substring(0, 16);
        if (res.ngayKetThuc) formData.ngayKetThuc = res.ngayKetThuc.substring(0, 16);

        formData.listIdKhachHang = res.danhSachKhachHang
            ? res.danhSachKhachHang
                .map(item => item.khachHang?.id ?? item.id)
                .filter(Boolean)
                .map(Number)
            : [];

    } catch (err) {
        showAlert("Lỗi", "Không thể tải dữ liệu chi tiết", "error");
    }
};
const isExpired = (ngayKetThuc) => {
    if (!ngayKetThuc) return false;
    return new Date(ngayKetThuc).getTime() < Date.now();
};

const isSubmitting = ref(false);
const triggerToggleStatus = async (pg) => {

    const actionText = pg.trangThai === 1
        ? 'ngừng hoạt động'
        : 'kích hoạt'

    const confirmed = await showConfirm(
        "Xác nhận thay đổi",
        `Bạn có chắc muốn ${actionText} phiếu ${pg.maPhieuGiamGia}?`
    )

    if (!confirmed) return

    try {
        await voucherService.toggleStatus(pg.id, pg.trangThai)
        showAlert("Thành công", "Trạng thái đã được cập nhật")
        handleSearch()
    } catch (e) {
        showAlert("Lỗi", "Không thể cập nhật trạng thái", "error")
    }
}
const resetCustomerFilter = () => {
    customerSearch.value = '';
};

// --- ĐIỀU HƯỚNG FORM ---
const openFormAdd = async () => {
    resetFormData();
    clearErrors();
    selectedId.value = null;
    isReadOnly.value = false;
    isFormActive.value = true;
    isCustomerListOpen.value = false; // Reset khi thêm mới
};

const openFormEdit = async (id) => {
    clearErrors();
    selectedId.value = id;
    isReadOnly.value = false;
    if (listKhachHang.value.length === 0) {
        await loadCustomers();
    }
    await loadDetail(id);
    // ⭐ TRUYỀN idDotKhuyenMai
    isFormActive.value = true;
};


const openFormView = async (id) => {
    selectedId.value = id;
    isReadOnly.value = true;

    await loadDetail(id);

    isFormActive.value = true; // Ẩn bảng, hiện form
};


const loadCustomers = async () => {
    try {
        const [year, month] = customerMonth.value.split('-');

        const res = await axios.get(
            'http://localhost:8080/api/khach-hang/thong-ke',
            {
                params: {
                    thang: Number(month),
                    nam: Number(year)
                }
            }
        );
        console.log("KH API DATA:", res.data);

        listKhachHang.value = (res.data || []).map(kh => ({
            ...kh,
            soLanDatTrongThang: kh.soLanDatTrongThang ?? 0,
            tongChiTieuTrongThang: kh.tongChiTieuTrongThang ?? 0,
            lanDatGanNhat: kh.lanDatGanNhat ?? null
        }));

    } catch (err) {
        console.error("Lỗi tải khách hàng", err);
        showAlert("Lỗi", "Không tải được danh sách khách hàng", "error");
    }
};

const resetFormData = () => {
    Object.assign(formData, {
        maPhieuGiamGia: '', codeGiamGia: '', tenPhieuGiamGia: '', loaiGiamGia: 1,
        giaTriGiam: 0, giaTriGiamToiDa: 0, donHangToiThieu: 0, doiTuong: 0,
        ngayBatDau: '', ngayKetThuc: '', soLuong: 0, listIdKhachHang: [], listEmails: [], idDotKhuyenMai: null,

    });
};

const selectedCustomers = computed(() => {
    return listKhachHang.value.filter(kh =>
        formData.listIdKhachHang.includes(kh.id)
    );
});
const removeCustomer = (id) => {
    formData.listIdKhachHang =
        formData.listIdKhachHang.filter(cid => cid !== id);
};

const resetFilters = async () => {
    // Reset dữ liệu Vue
    filters.keyword = '';
    filters.trangThai = null;
    filters.doiTuong = null;
    filters.loaiGiamGia = null;
    filters.ngayBatDau = '';
    filters.ngayKetThuc = '';

    pagination.currentPage = 1;

    await nextTick();

    handleSearch();
};


const changePage = (delta) => {
    const newPage = pagination.currentPage + delta;
    if (newPage >= 1 && newPage <= pagination.totalPages) {
        pagination.currentPage = newPage;
        handleSearch();
    }
};

const goToPage = (page) => {
    if (page >= 1 && page <= pagination.totalPages) {
        pagination.currentPage = page;
        handleSearch();
    }
};

const onSearchInput = () => {
    filters.keyword = filters.keyword?.trimStart() // bỏ khoảng trắng đầu

    clearTimeout(window.debounceSearch);
    window.debounceSearch = setTimeout(() => {
        pagination.currentPage = 1;
        handleSearch();
    }, 500);
};

const onDoiTuongChange = () => {
    // Chuyển sang Cá nhân (1) thì mở bảng, ngược lại đóng
    isCustomerListOpen.value = (Number(formData.doiTuong) === 1);
};

// --- HELPERS HIỂN THỊ ---
const formatPrice = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);
const getStatusDisplay = (pg) => {
    const now = new Date().getTime();
    const start = new Date(pg.ngayBatDau).getTime();
    const end = new Date(pg.ngayKetThuc).getTime();

    if (pg.trangThai === 0) return { text: 'Ngừng hoạt động', class: 'badge bg-secondary' };
    if (now < start) return { text: 'Sắp diễn ra', class: 'badge bg-info' };
    if (now > end) return { text: 'Hết hạn', class: 'badge bg-danger' };
    return { text: 'Hoạt động', class: 'badge bg-success' };
};

onMounted(async () => {

    handleSearch();

});

watch(
    () => formData.listIdKhachHang,
    (newVal) => {
        if (formData.doiTuong === 1) {
            formData.soLuong = newVal.length;
        }
    },
    { deep: true }
);

watch(
    () => formData.doiTuong,
    async (val) => {
        if (Number(val) === 1) {
            isCustomerListOpen.value = true;
            await loadCustomers(); // 🔥 QUAN TRỌNG
        } else {
            isCustomerListOpen.value = false;
            formData.listIdKhachHang = [];
        }
    }
);

watch(customerMonth, async () => {
    if (formData.doiTuong === 1) {
        await loadCustomers();
    }
});
watch(sortConfig, () => {
    customerPagination.currentPage = 1;
}, { deep: true });


const visiblePages = computed(() => {
    const total = pagination.totalPages;
    const current = pagination.currentPage;
    const delta = 2; // số trang hai bên

    if (total <= 7) {
        return Array.from({ length: total }, (_, i) => i + 1);
    }

    const pages = [];
    const left = Math.max(2, current - delta);
    const right = Math.min(total - 1, current + delta);

    pages.push(1);

    if (left > 2) pages.push('...');

    for (let i = left; i <= right; i++) {
        pages.push(i);
    }

    if (right < total - 1) pages.push('...');

    pages.push(total);

    return pages;
});

const fetchData = (filters, pagination) => {
    return axios.get('/api/phieu-giam-gia', {
        params: {
            ...filters,
            page: pagination.currentPage - 1,
            size: pagination.pageSize
        }
    }).then(res => res.data);
};

watch(
    () => [filters.trangThai, filters.doiTuong],
    () => {
        pagination.currentPage = 1
        handleSearch()
    }
)
watch(
    () => filters.loaiGiamGia,
    () => {
        filters.phanTramMin = null;
        filters.phanTramMax = null;
        filters.tienMin = null;
        filters.tienMax = null;
        pagination.currentPage = 1;
        handleSearch();
    }
);

watch(
    () => formData.loaiGiamGia,
    (val) => {
        if (val === 0) {
            formData.giaTriGiamToiDa = formData.giaTriGiam;
        }
    }
);

watch(
    () => formData.giaTriGiam,
    () => {
        if (formData.loaiGiamGia === 0) {
            formData.giaTriGiamToiDa = formData.giaTriGiam;
        }
    }
);

onMounted(async () => {
    await nextTick();
});

</script>

<style scoped>
@import '../voucherStyle.css';
</style>
