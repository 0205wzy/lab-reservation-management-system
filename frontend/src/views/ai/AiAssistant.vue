<script setup>
import { computed, nextTick, ref } from 'vue'
import api from '../../api'
import { ElMessage } from 'element-plus'

const welcome = '你好，我是实验室智能助手。你可以向我咨询实验室预约、设备使用和报修流程等问题。'
const quickQuestions = [
  '实验室预约需要注意什么？',
  '已经审核通过的时间段还能预约吗？',
  '深度学习服务器应该怎么使用？',
  '发现设备损坏后应该怎么办？'
]
const messages = ref([])
const question = ref('')
const sending = ref(false)
const conversation = ref(null)
const canSend = computed(() => question.value.trim().length > 0 && !sending.value)

async function sendQuestion(value = question.value) {
  const text = value.trim()
  if (!text || sending.value) return
  if (text.length > 500) {
    ElMessage.warning('问题最多500个字符')
    return
  }

  question.value = ''
  messages.value.push({ role: 'user', content: text })
  sending.value = true
  await scrollToBottom()

  try {
    const response = await api.post('/ai/ask', { question: text })
    messages.value.push({ role: 'assistant', content: response.data.answer, source: response.data.source })
  } catch {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，助手暂时无法处理这个问题。请稍后重试，或联系实验室管理员。',
      source: 'LOCAL'
    })
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

function onInputKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
    event.preventDefault()
    sendQuestion()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (conversation.value) conversation.value.scrollTop = conversation.value.scrollHeight
}
</script>

<template>
  <div class="assistant-page">
    <header class="assistant-header">
      <div class="assistant-mark"><ChatDotRound /></div>
      <div>
        <span class="assistant-eyebrow">LAB ASSISTANT</span>
        <h1>AI实验室助手</h1>
        <p>{{ welcome }}</p>
      </div>
    </header>

    <section class="assistant-panel">
      <div ref="conversation" class="conversation" aria-live="polite">
        <div v-if="messages.length === 0" class="welcome-message">
          <span class="bot-avatar"><MagicStick /></span>
          <div class="message-content">
            <span class="message-author">实验室智能助手</span>
            <p>{{ welcome }}</p>
          </div>
        </div>

        <template v-for="(message, index) in messages" :key="index">
          <div v-if="message.role === 'user'" class="chat-row user-row">
            <div class="user-bubble">{{ message.content }}</div>
          </div>
          <div v-else class="chat-row assistant-row">
            <span class="bot-avatar"><MagicStick /></span>
            <div class="message-content">
              <span class="message-author">实验室智能助手 <small v-if="message.source">{{ message.source === 'AI' ? 'AI回答' : '本地知识' }}</small></span>
              <p>{{ message.content }}</p>
            </div>
          </div>
        </template>

        <div v-if="sending" class="chat-row assistant-row loading-row">
          <span class="bot-avatar"><MagicStick /></span>
          <div class="message-content"><span class="message-author">实验室智能助手</span><p class="loading-text">正在生成回答……</p></div>
        </div>
      </div>

      <div class="quick-questions">
        <span>你可以试着问</span>
        <div class="quick-list">
          <button v-for="item in quickQuestions" :key="item" type="button" :disabled="sending" @click="sendQuestion(item)">{{ item }}</button>
        </div>
      </div>

      <div class="composer">
        <el-input
          v-model="question"
          type="textarea"
          :rows="2"
          :maxlength="500"
          resize="none"
          placeholder="请输入实验室相关问题，最多500字"
          @keydown="onInputKeydown"
        />
        <div class="composer-actions">
          <span>{{ question.length }}/500 · Enter发送，Shift+Enter换行</span>
          <el-button type="primary" :loading="sending" :disabled="!canSend" @click="sendQuestion()">
            <Promotion />发送
          </el-button>
        </div>
      </div>
    </section>

    <p class="disclaimer"><InfoFilled />回答由AI生成，仅供参考，请以实验室实际规定和系统数据为准。</p>
  </div>
</template>

<style scoped>
.assistant-page{max-width:1100px;margin:0 auto;padding:28px 30px 36px;color:var(--ink)}
.assistant-header{display:flex;align-items:center;gap:17px;padding:24px 28px;border:1px solid #dfeded;border-radius:14px;background:linear-gradient(112deg,#eef8f8,#f3f7fb)}
.assistant-mark{display:grid;place-items:center;flex:none;width:50px;height:50px;border-radius:14px;background:var(--primary);color:#fff}
.assistant-mark svg{width:25px}
.assistant-eyebrow{font-size:10px;letter-spacing:1.7px;font-weight:700;color:var(--primary)}
.assistant-header h1{margin:5px 0;font-size:25px;color:#24454c}
.assistant-header p{margin:0;color:#71868b;font-size:13px;line-height:1.65}
.assistant-panel{display:flex;flex-direction:column;min-height:510px;margin-top:18px;border:1px solid var(--border);border-radius:13px;background:#fff;box-shadow:0 5px 18px #1a718008;overflow:hidden}
.conversation{display:flex;flex:1;flex-direction:column;gap:17px;min-height:300px;max-height:440px;overflow:auto;padding:22px 24px}
.welcome-message,.chat-row{display:flex;align-items:flex-start;gap:10px}
.assistant-row{align-self:flex-start;max-width:88%}
.user-row{justify-content:flex-end}
.bot-avatar{display:grid;place-items:center;flex:none;width:32px;height:32px;border-radius:10px;background:#e8f6f5;color:var(--primary)}
.bot-avatar svg{width:17px}
.message-content{min-width:0;padding:10px 13px;border:1px solid #e6eeee;border-radius:3px 11px 11px 11px;background:#f8fbfb}
.message-author{display:block;margin-bottom:5px;color:#547177;font-size:11px;font-weight:700}
.message-author small{margin-left:6px;color:#93a4a8;font-weight:400}
.message-content p{margin:0;color:#485f64;font-size:13px;line-height:1.75;white-space:pre-wrap;overflow-wrap:anywhere}
.user-bubble{max-width:min(76%,560px);padding:10px 14px;border-radius:11px 3px 11px 11px;background:#168a92;color:#fff;font-size:13px;line-height:1.7;white-space:pre-wrap;overflow-wrap:anywhere}
.loading-text{color:#859a9e!important}
.quick-questions{padding:13px 22px 15px;border-top:1px solid #edf2f3}
.quick-questions>span{display:block;margin-bottom:9px;color:#87999d;font-size:11px}
.quick-list{display:flex;flex-wrap:wrap;gap:8px}
.quick-list button{padding:7px 10px;border:1px solid #dbe9e9;border-radius:18px;background:#f8fbfb;color:#55777c;font-size:11px;cursor:pointer;transition:all .15s}
.quick-list button:hover:not(:disabled){border-color:#8ec4c5;background:#eff8f7;color:#267c82}
.quick-list button:disabled{cursor:not-allowed;opacity:.6}
.composer{padding:13px 18px 15px;border-top:1px solid #edf2f3;background:#fbfdfd}
.composer :deep(.el-textarea__inner){min-height:54px!important;border-color:#dfe9e9;box-shadow:none;font-size:13px}
.composer :deep(.el-textarea__inner:focus){border-color:#58a9ab}
.composer-actions{display:flex;align-items:center;justify-content:space-between;margin-top:8px}
.composer-actions>span{color:#97a6a9;font-size:10px}
.composer-actions .el-button{min-width:94px}
.composer-actions .el-button svg{width:15px;margin-right:5px}
.disclaimer{display:flex;align-items:center;justify-content:center;gap:6px;margin:13px 0 0;color:#91a0a4;font-size:11px}
.disclaimer svg{width:14px;color:#7faeaf}
@media(max-width:650px){.assistant-page{padding:17px 13px 25px}.assistant-header{align-items:flex-start;padding:19px 17px}.assistant-mark{width:42px;height:42px}.assistant-header h1{font-size:21px}.assistant-header p{font-size:12px}.assistant-panel{min-height:480px}.conversation{padding:17px 13px}.assistant-row{max-width:95%}.quick-questions{padding:12px}.composer{padding:12px}.disclaimer{align-items:flex-start;text-align:center;line-height:1.5}}
</style>
