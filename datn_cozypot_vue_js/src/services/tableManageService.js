const baseURL = "http://localhost:8080/dat-ban"

export const fetchAll = async () => {
    const response = await fetch(`${baseURL}/danh-sach`)
    if (!response.ok) {
        throw new Error(response.status + ": " + await response.text())
    }
    return await response.json()
}

export const fetchAllBanAn = async () => {
    const response = await fetch(`${baseURL}/danh-sach-ban-an`)
    if (!response.ok) {
        throw new Error(response.status + ": " + await response.text())
    }
    return await response.json()
}

export const fetchBanAnById = async (id) => {
    const response = await fetch(`${baseURL}/ban-an-detail/${id}`)
    if (!response.ok) {
        throw new Error(response.status + ": " + await response.text())
    }
    return await response.json()
}

export const fetchSearchDatBan = async () => {
    const response = await fetch(`${baseURL}/search`)
    if (!response.ok) {
        throw new Error(response.status + ": " + await response.text())
    }
    return await response.json()
}

export const fetchAllCheckIn = async () => {
    const response = await fetch(`${baseURL}/danh-sach-check-in`)
    if (!response.ok) {
        throw new Error(response.status + ": " + await response.text())
    }
    return await response.json()
}

export const addBanAn = async (banAn) => {
    const response = await fetch(`${baseURL}/add-ban-an`,{
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(banAn)
    })
if (!response.ok) {
        throw new Error(response.status + ": " + await response.text())
    }
}

export const updateBanAn = async (banAn) => {
    const response = await fetch(`${baseURL}/update-ban-an`,{
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(banAn)
    })
if (!response.ok) {
        throw new Error(response.status + ": " + await response.text())
    }
}

export const fetchAllKhuVuc = async () => {
    const response = await fetch(`${baseURL}/danh-sach-khu-vuc`)
    if (!response.ok) {
        throw new Error(response.status + ": " + await response.text())
    }
    return await response.json()
}

export const updateTrangThaiBan = async (payload) => {
  const res = await fetch(
    `${baseURL}/update-trang-thai-ban`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    }
  );

  if (!res.ok) {
    throw new Error("Chưa có phiếu đặt bàn");
  }

  return true; // nếu backend không trả body thì có thể bỏ dòng này
};

export const searchDatBanService = async ({ payload, page, size }) => {
  const res = await fetch(
    `${baseURL}/search?page=${page}&size=${size}`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    }
  );

  if (!res.ok) {
    throw new Error("Lỗi khi tìm kiếm");
  }

  return await res.json();
};


export const updatePhieuDatBanService = async (payload) => {
  const res = await fetch(
    `${baseURL}/update`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    }
  );

  if (!res.ok) {
    throw new Error("Cập nhật thất bại");
  }

  return true; 
};

export const updateTTPhieuDatBan = async (id, trangThai) => {
  const url = `${baseURL}/update-trang-thai-phieu`;

  const res = await fetch(url, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      id: id,
      trangThai: trangThai,
    }),
  });

  if (!res.ok) {
    throw new Error("Cập nhật trạng thái thất bại");
  }

  return true;
};


