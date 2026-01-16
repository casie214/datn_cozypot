const getUrl = "http://localhost:8080/api/hoa-don-thanh-toan"
const systemUrl = "http://localhost:8080/api/tham-so-he-thong"

export const BeGetAllHoaDon = async()=>{
    const result = await fetch(`${getUrl}/get-all`)
    if(!result.ok){
        throw new Error(result.status + ": " + result.text())
    }
        return await result.json()
}

export const BeGetChiTietHoaDon = async(maHoaDon)=>{
    const result = await fetch(`${getUrl}/chi-tiet-hoa-don/${maHoaDon}`)
    if(!result.ok){
        throw new Error(result.status + ": " + result.text())
    }
        return await result.json()
}

export const BeGetThamSoHeThong = async () => {
    const result = await fetch(`${systemUrl}/get-all-system`);
    if (!result.ok) {
        throw new Error("Lỗi lấy cấu hình hệ thống");
    }
    return await result.json();
};

