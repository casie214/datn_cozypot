package com.example.datn_cozypot_spring_boot.service.implementation;

import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauRequest;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnRequest;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauRequest;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.TopSetLauResponse;
import com.example.datn_cozypot_spring_boot.dto.setLauChiTiet.SetLauChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.setLauChiTiet.SetLauChiTietResponse;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.ChiTietHoaDonRepository;
import com.example.datn_cozypot_spring_boot.repository.monAnRepository.*;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonAnServiceImplementation implements MonAnService {
    private final MonAnRepository monAnRepository;
    private final MonAnChiTietRepository monAnChiTietRepository;
    private final SetLauRepository setLauRepository;
    private final LoaiLauRepository loaiLauRepository;
    private final SetLauChiTietRepository setLauChiTietRepository;
    private final DanhMucRepository danhMucRepository;
    private final DanhMucChiTietRepository danhMucChiTietRepository;
    private final ModelMapper modelMapper;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;

    private String generatePrefixFromData(String name) {
        if (name == null || name.isEmpty()) return "XX";

        // Chuyển về không dấu (ví dụ: "Bò Mỹ" -> "Bo My")
        String temp = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String unAccent = pattern.matcher(temp).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");

        // Tách từ và lấy chữ cái đầu
        String[] words = unAccent.split("\\s+");
        StringBuilder prefix = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                prefix.append(w.charAt(0));
            }
        }
        return prefix.toString().toUpperCase(); // Ví dụ: "BM"
    }

    private String generateNextCode(String name, String entityType) {
        String prefix = generatePrefixFromData(name);
        String lastCode = null;
        switch (entityType) {
            case "MON_AN":
                lastCode = monAnRepository.findMaxCodeByPrefix(prefix);
                break;
            case "CHI_TIET_MON":
                lastCode = monAnChiTietRepository.findMaxCodeByPrefix(prefix);
                break;
            case "SET_LAU":
                lastCode = setLauRepository.findMaxCodeByPrefix(prefix);
                break;
            case "DANH_MUC":
                lastCode = danhMucRepository.findMaxCodeByPrefix(prefix);
                break;
            case "DANH_MUC_CHI_TIET":
                lastCode = danhMucChiTietRepository.findMaxCodeByPrefix(prefix);
                break;
            case "LOAI_SET":
                lastCode = loaiLauRepository.findMaxCodeByPrefix(prefix);
                break;
            default:
                throw new IllegalArgumentException("Loại entity không hợp lệ: " + entityType);
        }

        if (lastCode == null) {
            return prefix + "01";
        }

        // Tách phần số đuôi và tăng lên 1
        String numberPart = lastCode.substring(prefix.length());
        try {
            int number = Integer.parseInt(numberPart);
            number++;
            return prefix + String.format("%02d", number); // Format 01, 02...
        } catch (NumberFormatException e) {
            return prefix + "01"; // Fallback nếu lỗi
        }
    }

    @Override
    public List<MonAnResponse> findAllMonAn() {
        return monAnRepository.findAll().stream()
                .map(entity -> {
                    // 1. Map thông tin cơ bản (Code cũ)
                    MonAnResponse response = modelMapper.map(entity, MonAnResponse.class);

                    // 2. Map tên danh mục (Code cũ)
                    if (entity.getIdDanhMucChiTiet() != null) {
                        response.setTenDanhMucChiTiet(entity.getIdDanhMucChiTiet().getTenDanhMucChiTiet());
                        if (entity.getIdDanhMucChiTiet().getIdDanhMuc() != null) {
                            String tenDanhMuc = entity.getIdDanhMucChiTiet().getIdDanhMuc().getTenDanhMuc();
                            response.setTenDanhMuc(tenDanhMuc);
                        }
                    }

                    // --- 3. PHẦN MỚI: TÍNH KHOẢNG GIÁ (MIN - MAX) ---
                    // Giả sử trong Entity MonAn bạn đã có quan hệ @OneToMany với ChiTietMonAn
                    // và tên getter là getChiTietMonAns() hoặc getListChiTietMonAn()
                    Set<ChiTietMonAn> chiTiets = entity.getChiTietMonAns();

                    if (chiTiets != null && !chiTiets.isEmpty()) {
                        // Tìm giá thấp nhất
                        BigDecimal min = chiTiets.stream()
                                .map(ChiTietMonAn::getGiaBan)
                                .filter(Objects::nonNull) // Lọc bỏ giá trị null để tránh lỗi
                                .min(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);

                        // Tìm giá cao nhất
                        BigDecimal max = chiTiets.stream()
                                .map(ChiTietMonAn::getGiaBan)
                                .filter(Objects::nonNull)
                                .max(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);

                        response.setGiaThapNhat(min);
                        response.setGiaCaoNhat(max);
                    } else {
                        // Nếu món này chưa có chi tiết nào (Size/Topping)
                        response.setGiaThapNhat(BigDecimal.ZERO);
                        response.setGiaCaoNhat(BigDecimal.ZERO);
                    }
                    // ------------------------------------------------

                    return response;
                })
                .toList();
    }

    @Override
    public List<SetLauResponse> findAllSetLau() {
        return setLauRepository
                .findAll()
                .stream()
                .map(setLau -> {
                    SetLauResponse response = modelMapper.map(setLau, SetLauResponse.class);
                    return response;
                })
                .toList();
    }

    @Override
    public List<MonAnChiTietResponse> findAllChiTietMonAn() {
        return monAnChiTietRepository
                .findAll()
                .stream()
                .map(
                        chiTietMonAn -> modelMapper.map(chiTietMonAn, MonAnChiTietResponse.class)
                ).toList();
    }

    @Override
    public MonAnResponse findMonAnById(int id) {
        return monAnRepository.findById(id).map(monAnDiKem -> modelMapper.map(monAnDiKem, MonAnResponse.class)).orElse(null);
    }

    @Override
    public List<MonAnChiTietResponse> findChiTietMonAnByMonAnId(int id) {
        return monAnChiTietRepository.findByIdMonAnDiKem_Id(id).stream().map(chiTietMonAn -> modelMapper.map(chiTietMonAn, MonAnChiTietResponse.class)).toList();
    }

    @Override
    public List<DanhMucResponse> findAllDanhMuc() {
        return danhMucRepository
                .findAll()
                .stream()
                .map(danhMuc -> modelMapper.map(danhMuc, DanhMucResponse.class))
                .toList();
    }

    @Override
    public List<DanhMucChiTietResponse> findAllDanhMucChiTiet() {
        return danhMucChiTietRepository.findAll().stream()
                .map(entity -> {
                    DanhMucChiTietResponse response = modelMapper.map(entity, DanhMucChiTietResponse.class);
                    response.setTenDanhMucChiTiet(entity.getTenDanhMucChiTiet());
                    if (entity.getIdDanhMuc() != null) {
                        response.setTenDanhMuc(entity.getIdDanhMuc().getTenDanhMuc());
                    } else {
                        response.setTenDanhMuc("---");
                    }
                    return response;
                })
                .toList();
    }

    @Override
    public List<LoaiLauResponse> findAllLoaiLau() {
        return loaiLauRepository
                .findAll()
                .stream()
                .map(loaiSetLau -> modelMapper.map(loaiSetLau, LoaiLauResponse.class))
                .toList();
    }

    @Override
    @Transactional // Quan trọng: Để nếu lưu chi tiết lỗi thì rollback cả món ăn
    public MonAnResponse createMonAn(MonAnRequest request) {
        // 1. Map và Lưu Món Ăn Gốc (Parent)
        MonAnDiKem monAn = modelMapper.map(request, MonAnDiKem.class);
        monAn.setId(null); // Đảm bảo tạo mới
        String generatedCode = generateNextCode(request.getTenMonAn(), "CHI_TIET_MON");
        monAn.setMaMonAn(generatedCode);

        // Lưu xuống DB để sinh ra ID
        MonAnDiKem savedMonAn = monAnRepository.save(monAn);

        // 2. Xử lý lưu danh sách Chi Tiết (Children)
        if (request.getListChiTiet() != null && !request.getListChiTiet().isEmpty()) {
            List<ChiTietMonAn> listEntityChiTiet = new ArrayList<>();

            for (MonAnChiTietRequest detailReq : request.getListChiTiet()) {
                // Map từ DTO sang Entity
                ChiTietMonAn detailEntity = modelMapper.map(detailReq, ChiTietMonAn.class);

                // QUAN TRỌNG: Gán khóa ngoại (ID của món ăn vừa tạo) cho chi tiết
                detailEntity.setIdMonAnDiKem(savedMonAn);
                String generatedCodeCT = generateNextCode(detailReq.getTenChiTietMonAn(), "MON_AN");
                detailEntity.setMaChiTietMonAn(generatedCodeCT);

                listEntityChiTiet.add(detailEntity);
            }

            // Lưu tất cả chi tiết xuống DB
            monAnChiTietRepository.saveAll(listEntityChiTiet);
        }

        // 3. Trả về Response
        MonAnResponse monAnResponse = modelMapper.map(savedMonAn, MonAnResponse.class);

        // Map thêm tên danh mục (như code cũ của bạn)
        if (savedMonAn.getIdDanhMucChiTiet() != null) {
            danhMucChiTietRepository.findById(savedMonAn.getIdDanhMucChiTiet().getId())
                    .ifPresent(danhMuc -> {
                        monAnResponse.setTenDanhMucChiTiet(danhMuc.getTenDanhMucChiTiet());
                        monAnResponse.setTenDanhMuc(danhMuc.getTenDanhMucChiTiet());
                    });
        }

        return monAnResponse;
    }

    @Override
    @Transactional
    public SetLauResponse createSetLau(SetLauRequest request) {
        SetLau setLau = new SetLau();
        setLau.setTenSetLau(request.getTenSetLau());
        setLau.setGiaBan(request.getGiaBan());
        setLau.setHinhAnh(request.getHinhAnh());
        setLau.setMoTa(request.getMoTa());
        setLau.setTrangThai(request.getTrangThai());
        setLau.setNgayTao(Instant.now());
<<<<<<< HEAD
=======
        setLau.setMoTaChiTiet(request.getMoTaChiTiet());
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
        String code = generateNextCode(request.getTenSetLau(), "SET_LAU");
        setLau.setMaSetLau(code);

        if (request.getIdLoaiSet() != null) {
            LoaiSetLau loaiSet = loaiLauRepository.findById(request.getIdLoaiSet())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại set!"));
            setLau.setIdLoaiSet(loaiSet);
        }

        SetLau savedSetLau = setLauRepository.save(setLau);

        if (request.getListChiTietSetLau() != null && !request.getListChiTietSetLau().isEmpty()) {

            List<ChiTietSetLau> listToSave = new ArrayList<>();

            for (SetLauChiTietRequest itemRequest : request.getListChiTietSetLau()) {
                ChiTietMonAn monAn = monAnChiTietRepository.findById(itemRequest.getIdChiTietMonAn())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn ID: " + itemRequest.getIdChiTietMonAn()));

                ChiTietSetLau detail = new ChiTietSetLau();
                detail.setSetLau(savedSetLau);
                detail.setChiTietMonAn(monAn);
                detail.setSoLuong(itemRequest.getSoLuong());

                listToSave.add(detail);
            }
            setLauChiTietRepository.saveAll(listToSave);
        }

        return new SetLauResponse();
    }

    @Override
    public MonAnChiTietResponse createMonAnChiTiet(MonAnChiTietRequest request) {
        ChiTietMonAn chiTietMonAn = modelMapper.map(request, ChiTietMonAn.class);
        chiTietMonAn.setId(null);
        String code = generateNextCode(request.getTenChiTietMonAn(), "CHI_TIET_MON");
        chiTietMonAn.setMaChiTietMonAn(code);
        monAnChiTietRepository.save(chiTietMonAn);
        MonAnChiTietResponse chiTietResponse = modelMapper.map(chiTietMonAn, MonAnChiTietResponse.class);
        return chiTietResponse;
    }

    @Override
    public MonAnChiTietResponse putMonAnChiTiet(int id, MonAnChiTietRequest request) {
        return monAnChiTietRepository.findById(id)
                .map(chiTietMonAn -> {
                    // 1. Update các trường cơ bản
                    chiTietMonAn.setTenChiTietMonAn(request.getTenChiTietMonAn());
                    chiTietMonAn.setGiaBan(request.getGiaBan());
                    chiTietMonAn.setGiaVon(request.getGiaVon());
                    chiTietMonAn.setKichCo(request.getKichCo());
                    chiTietMonAn.setDonVi(request.getDonVi());
                    chiTietMonAn.setTrangThai(request.getTrangThai());

                    // 2. Xử lý Món Ăn Đi Kèm (An toàn hơn)
                    // Tìm món cha, nếu không thấy thì báo lỗi (hoặc giữ nguyên cái cũ tùy logic)
                    MonAnDiKem monAnCha = monAnRepository.findById(request.getIdMonAnDiKem())
                            .orElseThrow(() -> new RuntimeException("Món ăn cha ID " + request.getIdMonAnDiKem() + " không tồn tại"));
                    chiTietMonAn.setIdMonAnDiKem(monAnCha);

                    // 3. Xử lý Hình ảnh (Logic thông minh)
                    // Chỉ update nếu request có gửi ảnh lên.
                    // Nếu frontend gửi null hoặc rỗng (do user xóa ảnh), ta cũng set rỗng theo.
                    if (request.getHinhAnh() != null) {
                        chiTietMonAn.setHinhAnh(request.getHinhAnh());
                    }

                    // 4. Lưu và Map
                    ChiTietMonAn savedItem = monAnChiTietRepository.save(chiTietMonAn);
                    return modelMapper.map(savedItem, MonAnChiTietResponse.class);
                })
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Chi tiết món ID: " + id));
    }

    @Override
    public MonAnResponse putMonAn(int id, MonAnRequest request) {
        return monAnRepository.findById(id)
                .map(monAnDiKem -> {
                    monAnDiKem.setTenMonAn(request.getTenMonAn());
                    monAnDiKem.setGiaBan(request.getGiaBan());
                    monAnDiKem.setIdDanhMucChiTiet(danhMucChiTietRepository.findById(request.getIdDanhMucChiTiet()).get());
                    monAnDiKem.setTrangThaiKinhDoanh(request.getTrangThaiKinhDoanh());
                    monAnDiKem.setMoTa(request.getMoTa());
                    monAnDiKem.setHinhAnh(request.getHinhAnh());

                    monAnRepository.save(monAnDiKem);
                    return modelMapper.map(monAnDiKem, MonAnResponse.class);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public SetLauResponse putLau(int id, SetLauRequest request) {
        SetLau existingSet = setLauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Set lẩu ID: " + id));

        existingSet.setTenSetLau(request.getTenSetLau());
        existingSet.setGiaBan(request.getGiaBan());
        existingSet.setHinhAnh(request.getHinhAnh());
        existingSet.setMoTa(request.getMoTa());
        existingSet.setTrangThai(request.getTrangThai());
<<<<<<< HEAD
=======
        existingSet.setMoTaChiTiet(request.getMoTaChiTiet());
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77

        if (request.getIdLoaiSet() != null) {
            LoaiSetLau loaiSet = loaiLauRepository.findById(request.getIdLoaiSet())
                    .orElseThrow(() -> new RuntimeException("Loại set không tồn tại"));
            existingSet.setIdLoaiSet(loaiSet);
        }

        existingSet.getListChiTietSetLau().clear();

        if (request.getListChiTietSetLau() != null) {
            for (SetLauChiTietRequest itemReq : request.getListChiTietSetLau()) {
                ChiTietMonAn monAn = monAnChiTietRepository.findById(itemReq.getIdChiTietMonAn())
                        .orElseThrow(() -> new RuntimeException("Món ăn ID " + itemReq.getIdChiTietMonAn() + " không tồn tại"));

                ChiTietSetLau newDetail = new ChiTietSetLau();
                newDetail.setSetLau(existingSet);
                newDetail.setChiTietMonAn(monAn);
                newDetail.setSoLuong(itemReq.getSoLuong());

                existingSet.getListChiTietSetLau().add(newDetail);
            }
        }
        SetLau savedSet = setLauRepository.save(existingSet);
        return modelMapper.map(savedSet, SetLauResponse.class);
    }

    @Override
    public DanhMucResponse putDanhMuc(int id, DanhMucRequest request) {
        return danhMucRepository.findById(id)
                .map(danhMuc -> {
                    danhMuc.setTenDanhMuc(request.getTenDanhMuc());
                    danhMuc.setMoTa(request.getMoTa());
                    danhMuc.setTrangThai(request.getTrangThai());
                    danhMucRepository.save(danhMuc);
                    return modelMapper.map(danhMuc, DanhMucResponse.class);
                })
                .orElse(null);
    }

    @Override
    public DanhMucResponse addNewDanhMuc(DanhMucRequest request) {
        DanhMuc danhMuc = modelMapper.map(request, DanhMuc.class);
        danhMuc.setId(null);
        danhMuc.setMaDanhMuc(null);
        String code = generateNextCode(request.getTenDanhMuc(), "DANH_MUC");
        danhMuc.setMaDanhMuc(code);
        danhMucRepository.save(danhMuc);
        return modelMapper.map(danhMuc, DanhMucResponse.class);
    }

    @Override
    public LoaiLauResponse putLoaiLau(int id, LoaiLauRequest request) {
        return loaiLauRepository.findById(id)
                .map(loaiSetLau -> {
                    loaiSetLau.setTenLoaiSet(request.getTenLoaiSet());
                    loaiSetLau.setMoTa(request.getMoTa());
                    loaiSetLau.setTrangThai(request.getTrangThai());
                    loaiLauRepository.save(loaiSetLau);
                    return modelMapper.map(loaiSetLau, LoaiLauResponse.class);
                })
                .orElse(null);
    }

    @Override
    public LoaiLauResponse addNewLoaiLau(LoaiLauRequest request) {
        LoaiSetLau loaiSetLau = modelMapper.map(request, LoaiSetLau.class);
        loaiSetLau.setId(null);
        String code = generateNextCode(request.getTenLoaiSet(), "LOAI_SET");
        loaiSetLau.setMaLoaiSet(code);
        loaiLauRepository.save(loaiSetLau);
        return modelMapper.map(loaiSetLau, LoaiLauResponse.class);
    }

    @Override
    public DanhMucChiTietResponse putDanhMucChiTiet(int id, DanhMucChiTietRequest request) {
        return danhMucChiTietRepository.findById(id)
                .map(danhMucChiTiet -> {
                    danhMucChiTiet.setIdDanhMuc(danhMucRepository.findById(request.getIdDanhMuc()).get());
                    danhMucChiTiet.setTenDanhMucChiTiet(request.getTenDanhMucChiTiet());
                    danhMucChiTiet.setMoTa(request.getMoTa());
                    danhMucChiTiet.setTrangThai(request.getTrangThai());
                    danhMucChiTietRepository.save(danhMucChiTiet);
                    return modelMapper.map(danhMucChiTiet, DanhMucChiTietResponse.class);
                })
                .orElse(null);
    }

    @Override
    public DanhMucChiTietResponse addNewDanhMucChiTiet(DanhMucChiTietRequest request) {
        DanhMucChiTiet danhMucChiTiet = modelMapper.map(request, DanhMucChiTiet.class);
        danhMucChiTiet.setId(null);
        String code = generateNextCode(request.getTenDanhMucChiTiet(), "DANH_MUC_CHI_TIET");
        danhMucChiTiet.setMaDanhMucChiTiet(code);
        danhMucChiTietRepository.save(danhMucChiTiet);
        return modelMapper.map(danhMucChiTiet, DanhMucChiTietResponse.class);
    }

    @Override
    @Transactional // Quan trọng để tránh lỗi Lazy Loading
    public SetLauResponse findSetLauById(int id) {
        // 1. Tìm Entity SetLau
        SetLau setLau = setLauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Set lẩu ID: " + id));

        // 2. Map các trường cơ bản sang DTO
        SetLauResponse response = new SetLauResponse();
        response.setId(setLau.getId());
        response.setTenSetLau(setLau.getTenSetLau());
        response.setGiaBan(setLau.getGiaBan());
        response.setHinhAnh(setLau.getHinhAnh());
<<<<<<< HEAD
=======
        response.setMoTaChiTiet(setLau.getMoTaChiTiet());
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
        response.setMoTa(setLau.getMoTa());
        response.setTrangThai(setLau.getTrangThai());

        // Map Loại Set
        if (setLau.getIdLoaiSet() != null) {
            LoaiLauResponse loaiRes = new LoaiLauResponse();
            loaiRes.setId(setLau.getIdLoaiSet().getId());
            loaiRes.setTenLoaiSet(setLau.getIdLoaiSet().getTenLoaiSet());
            response.setIdLoaiSet(loaiRes.getId());
        }

        List<SetLauChiTietResponse> listDetailResponse = new ArrayList<>();

        if (setLau.getListChiTietSetLau() != null) {
            for (ChiTietSetLau detailEntity : setLau.getListChiTietSetLau()) {
                SetLauChiTietResponse detailDTO = new SetLauChiTietResponse();
                detailDTO.setId(detailEntity.getId());
                detailDTO.setSoLuong(detailEntity.getSoLuong());

                ChiTietMonAn monAn = detailEntity.getChiTietMonAn();
                if (monAn != null) {
                    MonAnChiTietResponse monAnDTO = new MonAnChiTietResponse();
                    monAnDTO.setId(monAn.getId());
                    monAnDTO.setTenChiTietMonAn(monAn.getTenChiTietMonAn());
                    monAnDTO.setGiaBan(monAn.getGiaBan());
                    monAnDTO.setDonVi(monAn.getDonVi());
                    monAnDTO.setHinhAnh(monAn.getHinhAnh());

                    detailDTO.setChiTietMonAn(monAnDTO);
                }

                listDetailResponse.add(detailDTO);
            }
        }

        response.setListChiTietSetLau(listDetailResponse);

        return response;
    }

    @Override
    public MonAnChiTietResponse findChiTietMonAnById(int id) {
        return monAnChiTietRepository.findById(id)
                .map(chiTietMonAn -> modelMapper.map(chiTietMonAn, MonAnChiTietResponse.class)).get();
    }

    @Override
    public void deleteFoodDetailById(int id) {
        monAnChiTietRepository.deleteById(id);
    }

    @Override
    public List<SetLauResponse> findSetLauTop(int metric) {
        int limit = (metric > 0) ? metric : 3;
        Pageable topPage = PageRequest.of(0, limit);

        // 2. Gọi Repo lấy danh sách Top (Kiểu TopSetLauResponse)
        List<TopSetLauResponse> topSellingList = chiTietHoaDonRepository.findTopSellingSetLau(topPage);

        // 3. Chuyển đổi (Map) sang List<SetLauResponse>
        return topSellingList.stream()
                .map(item -> {
                    // Lấy Entity SetLau từ kết quả
                    SetLau entity = item.getSetLau();

                    // Map sang DTO Response (Map thủ công hoặc dùng ModelMapper)
                    SetLauResponse response = new SetLauResponse();
                    response.setId(entity.getId());
                    response.setMaSetLau(entity.getMaSetLau());
                    response.setTenSetLau(entity.getTenSetLau());
                    response.setGiaBan(entity.getGiaBan());
                    response.setHinhAnh(entity.getHinhAnh());
                    response.setMoTa(entity.getMoTa());
                    response.setTrangThai(entity.getTrangThai());
                    // ... map nốt các trường khác nếu cần ...

                    // (Tùy chọn) Nếu SetLauResponse có trường 'soLuongDaBan' để hiển thị
                    // response.setSoLuongDaBan(item.getTongSoLuongBan());

                    return response;
                })
                .collect(Collectors.toList());
    }

<<<<<<< HEAD
=======
    @Override
    public List<MonAnResponse> findMonAnActive() {
        return monAnRepository.findByTrangThaiKinhDoanh(1).stream().map(monAnDiKem -> modelMapper.map(monAnDiKem, MonAnResponse.class)).collect(Collectors.toList());
    }

    @Override
    public List<DanhMucResponse> findDanhMucActive() {
        return danhMucRepository.findByTrangThai(1)
                .stream()
                .map(
                        danhMuc -> modelMapper.map(danhMuc, DanhMucResponse.class)
                )
                .toList();
    }

    @Override
    public List<SetLauResponse> findSetLauActive() {
        return setLauRepository.findByTrangThai(1)
                .stream()
                .map(
                        setLau -> modelMapper.map(setLau, SetLauResponse.class)
                )
                .toList();
    }

    @Override
    public List<DanhMucChiTietResponse> findDanhMucChiTietActive() {
        return danhMucChiTietRepository.findByTrangThai(1)
                .stream()
                .map(
                        danhMucChiTiet -> modelMapper.map(danhMucChiTiet, DanhMucChiTietResponse.class)
                )
                .toList();
    }

    @Override
    public List<LoaiLauResponse> findLoaiSetLauActive() {
        return loaiLauRepository.findByTrangThai(1)
                .stream()
                .map(
                        loaiLau -> modelMapper.map(loaiLau, LoaiLauResponse.class)
                )
                .toList();
    }

    @Override
    public List<MonAnChiTietResponse> findChiTietMonAnActive() {
        return monAnChiTietRepository.findByTrangThai(1)
                .stream()
                .map(
                        monAnChiTiet -> modelMapper.map(monAnChiTiet, MonAnChiTietResponse.class)
                )
                .toList();
    }

>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
    public MonAnResponse convertToResponse(MonAnDiKem entity) {
        MonAnResponse dto = new MonAnResponse();
        // ... map các trường khác ...

        // LOGIC TÍNH KHOẢNG GIÁ
        Set<ChiTietMonAn> chiTiets = entity.getChiTietMonAns(); // Giả sử quan hệ OneToMany đã map

        if (chiTiets != null && !chiTiets.isEmpty()) {
            // Tìm giá thấp nhất
            BigDecimal min = chiTiets.stream()
                    .map(ChiTietMonAn::getGiaBan)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            // Tìm giá cao nhất
            BigDecimal max = chiTiets.stream()
                    .map(ChiTietMonAn::getGiaBan)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            dto.setGiaThapNhat(min);
            dto.setGiaCaoNhat(max);
        } else {
            // Nếu không có chi tiết nào
            dto.setGiaThapNhat(BigDecimal.ZERO);
            dto.setGiaCaoNhat(BigDecimal.ZERO);
        }

        return dto;
    }
}
