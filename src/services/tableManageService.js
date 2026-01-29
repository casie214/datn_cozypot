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