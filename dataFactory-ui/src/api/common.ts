import request from './request'

/** 文件上传 - 返回 fileId */
export function uploadFile(file: File, bizType?: string) {
  const formData = new FormData()
  formData.append('file', file)
  if (bizType) formData.append('bizType', bizType)
  return request.post('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
