package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.request.AddBanAnRequest;
import com.example.datn_cozypot_spring_boot.dto.request.DatBanSearchRequest;
import com.example.datn_cozypot_spring_boot.dto.request.DatBanUpdateRequest;
import com.example.datn_cozypot_spring_boot.dto.request.UpdateBanAnRequest;
import com.example.datn_cozypot_spring_boot.dto.response.BanAnResponse;
import com.example.datn_cozypot_spring_boot.dto.response.DatBanDetailResponse;
import com.example.datn_cozypot_spring_boot.dto.response.DatBanListResponse;
import com.example.datn_cozypot_spring_boot.dto.response.KhuVucResponse;
import com.example.datn_cozypot_spring_boot.entity.BanAn;
import com.example.datn_cozypot_spring_boot.entity.KhuVuc;
import com.example.datn_cozypot_spring_boot.entity.PhieuDatBan;
import com.example.datn_cozypot_spring_boot.repository.BanAnRepository;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.KhuVucRepository;
import com.example.datn_cozypot_spring_boot.repository.PhieuDatBanRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class DatBanService {
    @Autowired
    private BanAnRepository banAnRepository;

    @Autowired
    private PhieuDatBanRepository phieuDatBanRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private KhuVucRepository khuVucRepository;

    public List<DatBanListResponse> getAll(){
        return phieuDatBanRepository.findAll().stream().map(DatBanListResponse::new).toList();
    }

    public List<DatBanListResponse> getAllByTrangThai(){
        return phieuDatBanRepository.findWaitingListFuture().stream().map(DatBanListResponse::new).toList();
    }

    public List<BanAnResponse> getAllBanAn(){
        return banAnRepository.findAll().stream().map(BanAnResponse::new).toList();
    }

    public BanAnResponse getBanAnById(Integer id){
        BanAn banAn =  banAnRepository.findById(id).get();
        return new BanAnResponse(banAn);
    }

    public List<KhuVucResponse> getAllKhuVuc(){
        return khuVucRepository.findAll().stream().map(KhuVucResponse::new).toList();
    }

    @Transactional
    public void addBanAn(AddBanAnRequest req) {

        KhuVuc khuVuc = khuVucRepository.findById(req.getIdKhuVuc())
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại"));

        BanAn banAn = new BanAn();
        banAn.setIdKhuVuc(khuVuc);          // ⭐ DÒNG QUYẾT ĐỊNH
        banAn.setSoNguoiToiDa(req.getSoNguoiToiDa());
        banAn.setTrangThai(0);
        banAn.setLoaiDatBan(req.getLoaiDatBan());

        banAnRepository.save(banAn);
    }

    @Transactional
    public void updateBanAn(UpdateBanAnRequest req) {

        BanAn banAn = banAnRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Bàn ăn không tồn tại"));

        KhuVuc khuVuc = khuVucRepository.findById(req.getIdKhuVuc())
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại"));

        banAn.setMaBan(req.getMaBan());
        banAn.setSoNguoiToiDa(req.getSoNguoiToiDa());
        banAn.setTrangThai(req.getTrangThai());     // ✔ lấy từ FE
        banAn.setLoaiDatBan(req.getLoaiDatBan());
        banAn.setIdKhuVuc(khuVuc);                   // ✔ set quan hệ

        banAnRepository.save(banAn); // ✔ UPDATE
    }




    public Page<DatBanListResponse> searchDatBan(
            DatBanSearchRequest request,
            Pageable pageable
    ) {
        Page<PhieuDatBan> page = phieuDatBanRepository.search(
                request.getSoDienThoai(),
                request.getTrangThai(),
                request.getThoiGianDat(),
                pageable
        );

        return page.map(DatBanListResponse::new);
    }


    @Transactional
    public void updateBanChoPhieu(DatBanUpdateRequest req) {

        BanAn banAn = banAnRepository.findById(req.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

        phieuDatBanRepository.updateBanChoPhieu(
                req.getId(),
                banAn
        );
    }

    @Transactional
    public void updateCheckIn(DatBanUpdateRequest req) {

        if (req.getIdBanAn() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thiếu ID bàn"
            );
        }

        // Check-in khách đặt
        if (req.getId() != null) {
            PhieuDatBan phieu = phieuDatBanRepository.findById(req.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

            phieu.setTrangThai(3);
            phieuDatBanRepository.save(phieu);
        }

        // Update bàn bằng query
        banAnRepository.updateTrangThaiBan(
                req.getIdBanAn(),
                req.getTrangThai()
        );
    }

    @Transactional
    public void autoUpdateTrangThaiPhieu() {

        int quaHan = phieuDatBanRepository.updateChoXacNhanQuaHan();
        int daHuy = phieuDatBanRepository.updateDaXacNhanQuaGio();

        System.out.println(
                "Auto update: " + quaHan + " QUÁ HẠN, " + daHuy + " ĐÃ HỦY"
        );
    }

    @Transactional
    public void updateTrangThai(Integer id, Integer trangThai) {
        phieuDatBanRepository.updateTrangThai(id, trangThai);
    }
}
