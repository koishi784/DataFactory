<template>
  <div class="character-scene" ref="sceneRef" @click="onSceneClick">
    <!-- 背景装饰 -->
    <div class="bg-grid"></div>
    <div class="bg-blur bg-blur-1"></div>
    <div class="bg-blur bg-blur-2"></div>

    <!-- 4 个角色 -->
    <div
      v-for="(ch, idx) in characters"
      :key="idx"
      class="character"
      :class="[`char-${idx + 1}`, { shaking: isShaking }]"
      :style="{ left: ch.x + '%', bottom: ch.y + '%', transform: `skewX(${bodyTilts[idx] + errorTilt + emptyTilt}deg)` }"
      @click.stop="tiltCharacter(idx)"
    >
      <!-- 头部 / 眼组（整体左右转动） -->
      <div class="eye-group" :ref="(el) => setEyeGroupRef(idx, el)">
        <div class="eye eye-left">
          <div class="pupil" :ref="(el) => setPupilRef(idx, 0, el)"></div>
        </div>
        <div class="eye eye-right">
          <div class="pupil" :ref="(el) => setPupilRef(idx, 1, el)"></div>
        </div>
      </div>
      <!-- 问号气泡 -->
      <div v-show="isAccountEmptyHint" class="question-mark" :style="{ color: ch.color }">?</div>
      <!-- 嘴巴 -->
      <div class="mouth" :class="{ sad: showSad, shaking: isShaking }"></div>
      <!-- 身体（点击时倾斜） -->
      <div class="body-decoration"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps<{
  isPasswordFocused?: boolean
  isLoginError?: boolean
  accountEmptyCount?: number
}>()

// 错误摇头状态
const isShaking = ref(false)
const errorTilt = ref(0) // 0=正常, 12=错误倾斜
const showSad = ref(false) // 本地控制嘴巴sad，动画结束后清除
// 账号为空提示状态
const isAccountEmptyHint = ref(false)
const emptyTilt = ref(0)
// 超时管理，防止重复点击造成冲突
let errorTimeout: number | null = null
let emptyTimeout: number | null = null

const sceneRef = ref<HTMLElement | null>(null)
const mouseX = ref(0)
const mouseY = ref(0)
const screenCenterX = ref(window.innerWidth * 0.3) // 角色区中心约在屏幕左侧30%
const sceneCenterX = ref(window.innerWidth * 0.275) // 角色区物理中心
const headOffsets = reactive([0, 0, 0, 0])
const bodyTilts = reactive([0, 0, 0, 0])
const blinkStates = reactive([false, false, false, false])

// 角色配置：所有角色底部对齐（同一地平线上），高度决定视觉高低
const characters = reactive([
  { x: 63, y: 38, width: 160, height: 400, color: '#7c3aed', label: 'char1' },
  { x: 46, y: 38, width: 100, height: 310, color: '#1e293b', label: 'char2' },
  { x: 51, y: 38, width: 140, height: 200, color: '#ea580c', label: 'char3' },
  { x: 35, y: 38, width: 110, height: 230, color: '#eab308', label: 'char4' },
])

// 每个角色是否正在倾斜
// 存储瞳孔元素引用和中心坐标: pupilRefs[idx][0|1]
const pupilRefs: (HTMLElement | null)[][] = [[null, null], [null, null], [null, null], [null, null]]
const eyeGroupRefs: (HTMLElement | null)[] = [null, null, null, null]

function setPupilRef(idx: number, side: number, el: any) {
  pupilRefs[idx][side] = el as HTMLElement | null
}
function setEyeGroupRef(idx: number, el: any) {
  eyeGroupRefs[idx] = el as HTMLElement | null
}

function onMouseMove(e: MouseEvent) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  updateHeads()
  updatePupils()
  updateBodyTilt()
}

function updateHeads() {
  const cx = screenCenterX.value
  for (let i = 0; i < 4; i++) {
    const offset = mouseX.value < cx ? -6 : 6
    headOffsets[i] = offset
    if (eyeGroupRefs[i]) {
      eyeGroupRefs[i]!.style.transform = `translateX(${offset}px)`
    }
  }
}

function updatePupils() {
  for (let i = 0; i < 4; i++) {
    for (let s = 0; s < 2; s++) {
      const pupil = pupilRefs[i][s]
      if (!pupil) continue
      const rect = pupil.parentElement?.getBoundingClientRect()
      if (!rect) continue
      const cx = rect.left + rect.width / 2
      const cy = rect.top + rect.height / 2

      // 密码聚焦时瞳孔转向下方（"害羞/低头"）
      let targetX = mouseX.value
      let targetY = mouseY.value
      if (props.isPasswordFocused) {
        targetX = cx
        targetY = cy + 200
      }

      const dx = targetX - cx
      const dy = targetY - cy
      const angle = Math.atan2(dy, dx)
      const maxDist = 4
      const dist = Math.min(Math.sqrt(dx * dx + dy * dy), maxDist)
      const px = Math.cos(angle) * dist
      const py = Math.sin(angle) * dist
      pupil.style.transform = `translate(${px}px, ${py}px)`
    }
  }
}

// 身体随鼠标倾斜——鼠标越靠左，身体向左倾；越靠右，向右倾
function updateBodyTilt() {
  const maxTilt = 4 // 最大倾斜度数
  const cx = sceneCenterX.value
  const dx = mouseX.value - cx
  // 屏幕宽度一半作为归一化分母
  const ratio = dx / (window.innerWidth / 2)
  const tilt = Math.max(-maxTilt, Math.min(maxTilt, -ratio * maxTilt))
  for (let i = 0; i < 4; i++) {
    bodyTilts[i] = tilt
  }
}

// 随机眨眼
let blinkTimers: number[] = []

function scheduleBlink(idx: number) {
  const delay = 2000 + Math.random() * 3000
  blinkTimers[idx] = window.setTimeout(() => {
    blinkStates[idx] = true
    setTimeout(() => {
      blinkStates[idx] = false
      scheduleBlink(idx)
    }, 120)
  }, delay)
}

// 身体点击倾斜——临时覆盖 bodyTilts 实现弹跳效果
function tiltCharacter(idx: number) {
  const origTilt = bodyTilts[idx]
  bodyTilts[idx] = 12
  setTimeout(() => { bodyTilts[idx] = -8 }, 80)
  setTimeout(() => { bodyTilts[idx] = 5 }, 160)
  setTimeout(() => { bodyTilts[idx] = -3 }, 240)
  setTimeout(() => { bodyTilts[idx] = origTilt }, 320)
}

function onSceneClick() {
  // 不做额外处理，点击角色由 tiltCharacter 处理
}

// 窗口 resize 时更新中心
function onResize() {
  screenCenterX.value = window.innerWidth * 0.3
  sceneCenterX.value = window.innerWidth * 0.275
}

// 账号为空计数器变化时触发问号提示（每次都触发）
watch(() => props.accountEmptyCount, (val, oldVal) => {
  if (typeof oldVal === 'undefined' || val === oldVal) return
  if (emptyTimeout) clearTimeout(emptyTimeout)
  isAccountEmptyHint.value = true
  emptyTilt.value = -12
  emptyTimeout = window.setTimeout(() => {
    isAccountEmptyHint.value = false
    emptyTilt.value = 0
    updateBodyTilt()
    emptyTimeout = null
  }, 550)
})

// 登录错误时触发摇头动画
watch(() => props.isLoginError, (val) => {
  if (!val) return
  if (errorTimeout) clearTimeout(errorTimeout)
  showSad.value = true
  isShaking.value = true
  errorTilt.value = 12
  // 0.5s 后完全恢复（嘴巴、倾斜、摇头全部复原）
  errorTimeout = window.setTimeout(() => {
    isShaking.value = false
    showSad.value = false
    errorTilt.value = 0
    updateBodyTilt()
    errorTimeout = null
  }, 550)
})

onMounted(() => {
  window.addEventListener('resize', onResize)
  window.addEventListener('mousemove', onMouseMove)
  for (let i = 0; i < 4; i++) scheduleBlink(i)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('mousemove', onMouseMove)
  blinkTimers.forEach((t) => clearTimeout(t))
})
</script>

<style scoped lang="scss">
.character-scene {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 40%, #3730a3 100%);
}

// 背景网格
.bg-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

// 模糊彩色装饰
.bg-blur {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
}
.bg-blur-1 {
  width: 400px;
  height: 400px;
  background: #8b5cf6;
  top: -100px;
  right: -100px;
}
.bg-blur-2 {
  width: 300px;
  height: 300px;
  background: #f59e0b;
  bottom: -50px;
  left: -50px;
}

// 角色基础
.character {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.12s ease-out;
  transform-origin: bottom center;

  &.shaking {
    animation: shakeHead 0.1s ease-in-out 5;
  }
}

@keyframes shakeHead {
  0%, 100% { margin-left: 0; }
  25% { margin-left: -8px; }
  75% { margin-left: 8px; }
}

// --- 角色1：紫色高矩形（机器人） ---
.char-1 {
  width: 180px;
  height: 400px;
  background: linear-gradient(180deg, #7c3aed, #6d28d9);
  border-radius: 40px 40px 12px 12px;
  justify-content: flex-start;
  padding-top: 50px;
  box-shadow: 0 8px 32px rgba(124, 58, 237, 0.4);

  .eye-group { gap: 28px; }
  .eye { width: 22px; height: 22px; }
  .pupil { width: 8px; height: 8px; }
  .mouth {
    width: 40px;
    height: 6px;
    background: rgba(255,255,255,0.6);
    border-radius: 3px;
    margin-top: 20px;
    transition: all 0.3s;
    &.sad { width: 24px; border-radius: 50%; height: 8px; }
    &.shaking { width: 28px; height: 28px; border-radius: 50%; background: rgba(255,255,255,0.7); }
  }
}

// --- 角色2：深色矮矩形 ---
.char-2 {
  width: 120px;
  height: 310px;
  background: linear-gradient(180deg, #334155, #1e293b);
  border-radius: 30px;
  justify-content: flex-start;
  padding-top: 40px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.3);
  z-index: 0;

  .eye-group { gap: 20px; }
  .eye { width: 18px; height: 18px; }
  .pupil { width: 6px; height: 6px; }
  .mouth {
    width: 30px;
    height: 4px;
    background: rgba(255,255,255,0.4);
    border-radius: 2px;
    margin-top: 16px;
    transition: all 0.3s;
    &.sad { width: 18px; border-radius: 50%; height: 6px; }
    &.shaking { width: 22px; height: 22px; border-radius: 50%; background: rgba(255,255,255,0.6); }
  }
}

// --- 角色3：橙色半圆（太阳） ---
.char-3 {
  width: 240px;
  height: 200px;
  background: linear-gradient(180deg, #ea580c, #c2410c);
  border-radius: 50% 50% 0 0;
  justify-content: flex-start;
  padding-top: 50px;
  box-shadow: 0 8px 32px rgba(234, 88, 12, 0.4);

  .eye-group { gap: 36px; margin-top: 10px; }
  .eye { width: 28px; height: 28px; }
  .pupil { width: 10px; height: 10px; }
  .mouth {
    width: 50px;
    height: 6px;
    background: rgba(255,255,255,0.5);
    border-radius: 3px;
    margin-top: 10px;
    transition: all 0.3s;
    &.sad { width: 28px; border-radius: 50%; }
    &.shaking { width: 32px; height: 32px; border-radius: 50%; background: rgba(255,255,255,0.7); }
  }
}

// --- 角色4：黄色圆角矩形（怪物） ---
.char-3 {
  z-index: 2;
}

.char-4 {
  width: 140px;
  height: 230px;
  background: linear-gradient(180deg, #eab308, #ca8a04);
  border-radius: 30px;
  justify-content: flex-start;
  padding-top: 35px;
  box-shadow: 0 8px 32px rgba(234, 179, 8, 0.4);
  z-index: 2;

  .eye-group { gap: 32px; }
  .eye { width: 24px; height: 24px; }
  .pupil { width: 8px; height: 8px; }
  .mouth {
    width: 36px;
    height: 10px;
    background: rgba(255,255,255,0.5);
    border-radius: 5px;
    margin-top: 16px;
    transition: all 0.3s;
    &.sad { width: 20px; border-radius: 50%; }
    &.shaking { width: 26px; height: 26px; border-radius: 50%; background: rgba(255,255,255,0.7); }
  }
}

// 身体装饰
.body-decoration {
  display: none;
}

// 问号气泡
.question-mark {
  position: absolute;
  top: -45px;
  font-size: 32px;
  font-weight: bold;
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
  animation: floatUp 0.5s ease-out forwards;
  pointer-events: none;
}

@keyframes floatUp {
  0% { opacity: 0; transform: translateY(10px) scale(0.5); }
  30% { opacity: 1; transform: translateY(-5px) scale(1.2); }
  60% { opacity: 1; transform: translateY(0px) scale(1); }
  100% { opacity: 0; transform: translateY(-15px) scale(0.8); }
}

// 眼睛公共样式
.eye-group {
  display: flex;
  transition: transform 0.15s ease-out;
}

.eye {
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.pupil {
  background: #1a1a2e;
  border-radius: 50%;
  transition: transform 0.08s linear;
}
</style>
