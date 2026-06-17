<template>
  <div class="ai-assistant" ref="assistantRef" @mouseenter="isHovered = true" @mouseleave="isHovered = false">
    <!-- 气泡 -->
    <transition name="bubble">
      <div v-if="isHovered && !dialogVisible" class="speech-bubble" @click.stop="openDialog">
        <span>需要帮忙吗？</span>
        <div class="bubble-arrow"></div>
      </div>
    </transition>

    <!-- 角色 -->
    <div
      class="character"
      :class="{ 'character--peek': !isHovered && !dialogVisible, 'character--happy': dialogVisible }"
      @click="openDialog"
      ref="characterRef"
    >
      <!-- 天线 -->
      <div class="antenna">
        <div class="antenna-ball"></div>
      </div>
      <!-- 眼睛 -->
      <div class="eyes">
        <div class="eye eye-left">
          <div class="pupil" :style="{ transform: pupilTransform }" ref="leftPupilRef"></div>
        </div>
        <div class="eye eye-right">
          <div class="pupil" :style="{ transform: pupilTransform }" ref="rightPupilRef"></div>
        </div>
      </div>
      <!-- 嘴巴 -->
      <div class="mouth" :class="{ 'mouth--open': dialogVisible }"></div>
    </div>

    <!-- 聊天面板 -->
    <transition name="panel">
      <div v-if="dialogVisible" class="chat-panel">
        <div class="panel-header">
          <span>AI 助手</span>
          <el-button link size="small" style="color: #fff; font-size: 18px;" @click="dialogVisible = false">✕</el-button>
        </div>
        <div class="chat-messages" ref="messagesRef">
          <div v-if="messages.length === 0" class="chat-empty">
            <p>你好！我是数据工厂 AI 助手，有什么可以帮助你的吗？</p>
          </div>
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="chat-message"
            :class="msg.role === 'user' ? 'message-user' : 'message-ai'"
          >
            <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
          </div>
          <div v-if="sending" class="chat-message message-ai">
            <div class="message-content message-thinking">
              <span class="dot-pulse"></span>
            </div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            placeholder="输入消息..."
            :disabled="sending"
            @keyup.enter="sendMessage"
          >
            <template #append>
              <el-button :loading="sending" @click="sendMessage">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { sendChatMessage } from '@/api/coze'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

// 配置 marked 支持 GFM 和代码高亮
marked.setOptions({
  breaks: true,
  gfm: true,
  highlight(code: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch { /* fallthrough */ }
    }
    return hljs.highlightAuto(code).value
  }
})

function renderMarkdown(content: string): string {
  return marked.parse(content) as string
}

const authStore = useAuthStore()

const userId = authStore.userInfo?.id?.toString() || authStore.userInfo?.username || 'anonymous'

// 交互状态
const isHovered = ref(false)
const dialogVisible = ref(false)
const inputMessage = ref('')
const sending = ref(false)
const messages = ref<{ role: string; content: string }[]>([])
const assistantRef = ref<HTMLElement | null>(null)
const messagesRef = ref<HTMLElement | null>(null)

// 瞳孔追踪
const mouseX = ref(0)
const mouseY = ref(0)
const leftPupilRef = ref<HTMLElement | null>(null)
const rightPupilRef = ref<HTMLElement | null>(null)

const pupilTransform = computed(() => {
  if (isHovered.value || dialogVisible.value) {
    return 'translate(-2px, -2px)'
  }
  const maxDist = 3
  const dx = mouseX.value
  const dy = mouseY.value
  const angle = Math.atan2(dy, dx)
  const dist = Math.min(Math.sqrt(dx * dx + dy * dy), maxDist)
  return `translate(${Math.cos(angle) * dist}px, ${Math.sin(angle) * dist}px)`
})

function onMouseMove(e: MouseEvent) {
  // 以角色眼睛位置为原点
  const pupil = leftPupilRef.value || rightPupilRef.value
  if (!pupil) return
  const eye = pupil.parentElement
  if (!eye) return
  const rect = eye.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  mouseX.value = e.clientX - cx
  mouseY.value = e.clientY - cy
}

function openDialog() {
  dialogVisible.value = true
}

function onDocumentClick(e: MouseEvent) {
  if (!dialogVisible.value) return
  if (assistantRef.value && !assistantRef.value.contains(e.target as Node)) {
    dialogVisible.value = false
  }
}

async function sendMessage() {
  const msg = inputMessage.value.trim()
  if (!msg || sending.value) return

  messages.value.push({ role: 'user', content: msg })
  inputMessage.value = ''
  sending.value = true
  scrollToBottom()

  try {
    const res = await sendChatMessage(userId, msg) as any
    const reply = res?.reply || res?.data?.reply || ''
    messages.value.push({ role: 'ai', content: reply || '抱歉，我没有理解你的问题。' })
  } catch {
    messages.value.push({ role: 'ai', content: '网络连接失败，请稍后重试。' })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove)
  document.addEventListener('click', onDocumentClick)
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('click', onDocumentClick)
})



</script>

<style scoped lang="scss">
.ai-assistant {
  position: fixed;
  right: 0;
  bottom: 98px;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

// ===== 气泡 =====
.speech-bubble {
  position: relative;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 8px 16px;
  margin-bottom: 8px;
  margin-right: 16px;
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

  &:hover {
    background: #f5f5f5;
  }

  .bubble-arrow {
    position: absolute;
    bottom: -6px;
    right: 30px;
    width: 10px;
    height: 10px;
    background: #fff;
    border-right: 1px solid #e0e0e0;
    border-bottom: 1px solid #e0e0e0;
    transform: rotate(45deg);
    clip-path: polygon(0 0, 100% 0, 0 100%);
  }
}

.bubble-enter-active {
  animation: bubbleIn 0.3s ease-out;
}
.bubble-leave-active {
  animation: bubbleIn 0.2s ease-in reverse;
}
@keyframes bubbleIn {
  0% { opacity: 0; transform: translateY(10px) scale(0.8); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}

// ===== 角色 =====
.character {
  width: 80px;
  height: 90px;
  background: linear-gradient(180deg, #7c3aed, #6d28d9);
  border-radius: 20px 0 0 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: -4px 4px 16px rgba(124, 58, 237, 0.35);
  position: relative;
  gap: 4px;

  // 默认：半隐藏状态（只显示右侧圆角部分）
  &.character--peek {
    transform: translateX(30px);
    opacity: 0.8;
    border-radius: 50% 0 0 50%;
    width: 70px;
    height: 80px;
  }

  // 悬停/默认显示状态 — 保持圆形
  &:hover,
  &:not(.character--peek) {
    transform: translateX(0);
    opacity: 1;
    border-radius: 50% 0 0 50%;
    width: 70px;
    height: 80px;
  }

  // 悬停/聊天时嘴巴由 mouth--open 单独控制
}

// 天线
.antenna {
  position: absolute;
  top: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 14px;
  background: rgba(255,255,255,0.4);
  border-radius: 2px;

  .antenna-ball {
    position: absolute;
    top: -6px;
    left: 50%;
    transform: translateX(-50%);
    width: 10px;
    height: 10px;
    background: #fbbf24;
    border-radius: 50%;
    box-shadow: 0 0 6px rgba(251, 191, 36, 0.6);
  }
}

// 眼睛
.eyes {
  display: flex;
  gap: 16px;
}

.eye {
  width: 16px;
  height: 16px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.pupil {
  width: 6px;
  height: 6px;
  background: #1a1a2e;
  border-radius: 50%;
  transition: transform 0.06s linear;
}

// 嘴巴
.mouth {
  width: 16px;
  height: 2px;
  background: rgba(255,255,255,0.6);
  border-radius: 1px;
  margin-top: 4px;
  transition: all 0.3s;
}

// 默认（半隐藏）是一条直线
.character--peek .mouth {
  width: 12px;
  height: 2px;
  background: rgba(255,255,255,0.4);
  border-radius: 1px;
}

// 悬停/展开时变为笑脸
.character:not(.character--peek) .mouth {
  width: 20px;
  height: 6px;
  border: none;
  background: transparent;
  border-bottom: 2.5px solid rgba(255,255,255,0.7);
  border-radius: 0 0 10px 10px;
  margin-top: 2px;
}

.character:not(.character--peek) .mouth--open {
  width: 14px;
  height: 14px;
  border: none !important;
  background: rgba(255,255,255,0.5);
  border-radius: 50%;
}

// ===== 聊天面板 =====
.chat-panel {
  position: fixed;
  right: 16px;
  bottom: 210px;
  width: 380px;
  height: 460px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #7c3aed, #6d28d9);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}

.panel-enter-active {
  animation: panelIn 0.25s ease-out;
}
.panel-leave-active {
  animation: panelIn 0.2s ease-in reverse;
}
@keyframes panelIn {
  0% { opacity: 0; transform: translateY(20px) scale(0.95); transform-origin: bottom right; }
  100% { opacity: 1; transform: translateY(0) scale(1); transform-origin: bottom right; }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-empty {
  text-align: center;
  padding: 60px 20px;
  color: #909399;

  .chat-empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }

  p {
    font-size: 14px;
    line-height: 1.6;
  }
}

.chat-message {
  display: flex;

  .message-content {
    max-width: 75%;
    padding: 10px 14px;
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.5;
    word-break: break-word;
  }
}

.message-user {
  justify-content: flex-end;

  .message-content {
    background: #409eff;
    color: #fff;
    border-bottom-right-radius: 4px;
  }
}

.message-ai {
  justify-content: flex-start;

  .message-content {
    background: #f0f0f0;
    color: #303133;
    border-bottom-left-radius: 4px;

    // Markdown 渲染样式
    :deep(p) {
      margin: 0 0 8px 0;
      &:last-child { margin-bottom: 0; }
    }

    :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
      margin: 12px 0 6px 0;
      font-weight: 600;
      color: #1a1a2e;
    }

    :deep(h1) { font-size: 18px; }
    :deep(h2) { font-size: 16px; }
    :deep(h3) { font-size: 15px; }
    :deep(h4) { font-size: 14px; }

    :deep(ul), :deep(ol) {
      margin: 4px 0 8px 0;
      padding-left: 20px;
    }

    :deep(li) {
      margin-bottom: 2px;
    }

    :deep(a) {
      color: #409eff;
      text-decoration: none;
      &:hover { text-decoration: underline; }
    }

    :deep(blockquote) {
      margin: 8px 0;
      padding: 6px 12px;
      border-left: 3px solid #409eff;
      background: rgba(64, 158, 255, 0.06);
      border-radius: 0 4px 4px 0;
      color: #606266;
    }

    :deep(pre) {
      margin: 8px 0;
      padding: 0;
      border-radius: 8px;
      overflow: hidden;
      background: #1e1e1e;

      code {
        display: block;
        padding: 14px 16px;
        font-size: 13px;
        line-height: 1.5;
        font-family: 'Cascadia Code', 'Fira Code', 'Consolas', 'Monaco', monospace;
        overflow-x: auto;
        background: transparent !important;
        color: #d4d4d4;
      }
    }

    :deep(code) {
      padding: 2px 6px;
      background: rgba(0, 0, 0, 0.06);
      border-radius: 4px;
      font-size: 13px;
      font-family: 'Cascadia Code', 'Fira Code', 'Consolas', 'Monaco', monospace;
      color: #d63384;
    }

    // 代码块内的 code 不应用行内样式
    :deep(pre code) {
      padding: 0;
      background: transparent !important;
      color: #d4d4d4;
    }

    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin: 8px 0;
      font-size: 13px;

      th, td {
        border: 1px solid #e0e0e0;
        padding: 6px 10px;
        text-align: left;
      }

      th {
        background: #e8e8e8;
        font-weight: 600;
      }
    }

    :deep(hr) {
      border: none;
      border-top: 1px solid #e0e0e0;
      margin: 12px 0;
    }

    :deep(img) {
      max-width: 100%;
      border-radius: 6px;
      margin: 8px 0;
    }

    :deep(strong) {
      font-weight: 600;
    }
  }
}

.message-thinking {
  padding: 12px 16px !important;
}

.dot-pulse {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: dotPulse 1s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

.chat-input {
  border-top: 1px solid #ebeef5;
  padding: 8px 12px;

  :deep(.el-input-group__append) {
    padding: 0;
    width: 60px;

    .el-button {
      width: 100%;
      margin: 0;
    }
  }
}
</style>
