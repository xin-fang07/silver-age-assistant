import { ElMessageBox } from 'element-plus'

export function confirmDanger(message, title = '请确认危险操作', confirmButtonText = '确认操作') {
  return ElMessageBox.confirm(message, title, {
    type: 'error', confirmButtonText, cancelButtonText: '取消',
    confirmButtonClass: 'app-danger-confirm', autofocus: false,
    distinguishCancelAndClose: true, closeOnClickModal: false
  })
}
