const getUrl = "http://localhost:8080/api/hoa-don-thanh-toan";

export const BeGetAllHoaDon = async () => {
    const result = await fetch(`${getUrl}/get-all`);
    if (!result.ok) {
        const errorText = await result.text();
        throw new Error(result.status + ": " + errorText);
    }
    return await result.json();
};

export const BeGetHoaDonById = async (id) => {
    const result = await fetch(`${getUrl}/get-by-id/${id}`);
    if (!result.ok) {
        const errorText = await result.text();
        throw new Error(result.status + ": " + errorText);
    }
    return await result.json();
};

export const BeSearchHoaDon = async (key, trangThai, tuNgay, denNgay) => {
    const params = new URLSearchParams();
    if (key !== null && key !== undefined && key !== "") {
        params.append("key", key);
    }
    if (trangThai !== null && trangThai !== undefined && trangThai !== "") {
        params.append("trangThai", trangThai);
    }
    if (tuNgay) params.append("tuNgay", tuNgay);
    if (denNgay) params.append("denNgay", denNgay);

    const result = await fetch(`${getUrl}/search?${params.toString()}`);
    if (!result.ok) {
        const errorText = await result.text();
        throw new Error(result.status + ": " + errorText);
    }
    return await result.json();
};

export const BeGetChiTietHoaDon = async (idHoaDon) => {
    const result = await fetch(`${getUrl}/chi-tiet-hoa-don/${idHoaDon}`);
    if (!result.ok) {
        const errorText = await result.text();
        throw new Error(result.status + ": " + errorText);
    }
    return await result.json();
};

export const BeXacNhanThanhToan = async (payload) => {
    const result = await fetch(`${getUrl}/xac-nhan-thanh-toan`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!result.ok) throw new Error(await result.text());
    return true;
};

export const BeGetLichSuHoaDon = async (idHoaDon) => {
    const result = await fetch(`${getUrl}/lich-su/${idHoaDon}`);
    if (!result.ok) {
        const errorText = await result.text();
        throw new Error(result.status + ": " + errorText);
    }
    return await result.json();
};

export const BeUpdateMonDaLen = async (idChiTiet) => {
    const result = await fetch(`${getUrl}/chi-tiet-hoa-don/cap-nhat-da-len/${idChiTiet}`, {
        method: 'PUT'
    });
    if (!result.ok) {
        const errorText = await result.text();
        throw new Error(errorText);
    }
    return true;
};

export const BeUpdateTatCaDaLen = async (idHoaDon) => {
    const result = await fetch(`${getUrl}/chi-tiet-hoa-don/cap-nhat-tat-ca-da-len/${idHoaDon}`, {
        method: 'PUT'
    });
    if (!result.ok) {
        const errorText = await result.text();
        throw new Error(errorText);
    }
    return true;
};

export const BeHuyHoaDon = async (payload) => {
    const result = await fetch(`${getUrl}/huy-don`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload)
    });

    if (!result.ok) {
        const errorText = await result.text();
        throw new Error(errorText);
    }
    return true;
};

const systemUrl = "http://localhost:8080/api/tham-so-he-thong";
export const BeGetThamSoHeThong = async () => {
    const result = await fetch(`${systemUrl}/get-all-system`);
    if (!result.ok) {
        throw new Error("Lỗi lấy cấu hình hệ thống");
    }
    return await result.json();
};