// 浏览器 Geolocation 通常返回 WGS-84；高德地图使用 GCJ-02。
const PI = Math.PI
const A = 6378245.0
const EE = 0.006693421622965943

const outsideChina = (lat, lng) => lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
const transformLat = (x, y) => {
  let r = -100 + 2 * x + 3 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
  r += (20 * Math.sin(6 * x * PI) + 20 * Math.sin(2 * x * PI)) * 2 / 3
  r += (20 * Math.sin(y * PI) + 40 * Math.sin(y / 3 * PI)) * 2 / 3
  r += (160 * Math.sin(y / 12 * PI) + 320 * Math.sin(y * PI / 30)) * 2 / 3
  return r
}
const transformLng = (x, y) => {
  let r = 300 + x + 2 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
  r += (20 * Math.sin(6 * x * PI) + 20 * Math.sin(2 * x * PI)) * 2 / 3
  r += (20 * Math.sin(x * PI) + 40 * Math.sin(x / 3 * PI)) * 2 / 3
  r += (150 * Math.sin(x / 12 * PI) + 300 * Math.sin(x / 30 * PI)) * 2 / 3
  return r
}

export const wgs84ToGcj02 = (latitude, longitude) => {
  const lat = Number(latitude)
  const lng = Number(longitude)
  if (!Number.isFinite(lat) || !Number.isFinite(lng) || outsideChina(lat, lng)) return { latitude: lat, longitude: lng }
  let dLat = transformLat(lng - 105, lat - 35)
  let dLng = transformLng(lng - 105, lat - 35)
  const radLat = lat / 180 * PI
  let magic = Math.sin(radLat)
  magic = 1 - EE * magic * magic
  const sqrtMagic = Math.sqrt(magic)
  dLat = dLat * 180 / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
  dLng = dLng * 180 / (A / sqrtMagic * Math.cos(radLat) * PI)
  return { latitude: lat + dLat, longitude: lng + dLng }
}

export const amapMarkerUrl = (item, fallbackName = '求助位置') => {
  const point = wgs84ToGcj02(item.latitude, item.longitude)
  return `https://uri.amap.com/marker?position=${point.longitude},${point.latitude}&name=${encodeURIComponent(item.locationText || fallbackName)}`
}

export const formatLocationAccuracy = value => {
  const accuracy = Number(value)
  if (!Number.isFinite(accuracy)) return ''
  return `定位精度约 ±${Math.max(1, Math.round(accuracy))} 米`
}
