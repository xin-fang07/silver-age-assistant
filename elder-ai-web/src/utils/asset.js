// ============================================================
// asset.js - 静态资源地址统一拼接
// 银发智能生活助手 - 解决此前各页面硬编码 http://localhost:8080 的问题
// 后端返回的资源路径多为相对路径（如 /uploads/xxx.jpg），
// 按当前页面 origin 动态拼接，换域名 / 端口 / 部署路径后图片不再 404。
// ============================================================

/**
 * 将后端返回的资源地址拼接为可访问的完整 URL
 * @param {string} url - 后端返回的资源路径
 *   - 已是完整 http(s) 地址：原样返回
 *   - 以 // 开头：补全当前协议
 *   - 以 / 开头：拼当前 origin
 *   - 其他相对路径：拼 origin + /
 *   - 空值：返回空串
 * @returns {string}
 */
export function assetUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('//')) return window.location.protocol + url
  const base = window.location.origin
  return url.startsWith('/') ? base + url : base + '/' + url
}

export default assetUrl
