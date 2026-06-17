import request from './request'

export function sendChatMessage(userId: string, message: string) {
  return request.post<{ reply: string }>('/coze/chat', { userId, message })
}
