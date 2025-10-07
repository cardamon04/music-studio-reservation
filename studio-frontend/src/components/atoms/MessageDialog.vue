<template>
  <div v-if="isVisible" class="dialog-overlay" @click="handleOverlayClick">
    <div class="dialog-content" @click.stop>
      <div class="dialog-header">
        <h3 class="dialog-title" :class="titleClass">{{ title }}</h3>
        <button v-if="showCloseButton" @click="close" class="close-button">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
      
      <div class="dialog-body">
        <div class="message-content">
          <div v-if="icon" class="message-icon" :class="iconClass">
            <component :is="icon" />
          </div>
          <div class="message-text">
            <p v-if="message" class="message">{{ message }}</p>
            <div v-if="details" class="details">{{ details }}</div>
          </div>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button
          v-for="button in buttons"
          :key="button.text"
          @click="handleButtonClick(button)"
          :class="['dialog-button', button.class]"
          :disabled="button.disabled"
        >
          {{ button.text }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

export interface MessageButton {
  text: string;
  class?: string;
  disabled?: boolean;
  action?: () => void;
}

export interface MessageDialogProps {
  isVisible: boolean;
  title: string;
  message?: string;
  details?: string;
  type?: 'success' | 'error' | 'warning' | 'info';
  showCloseButton?: boolean;
  buttons?: MessageButton[];
}

const emit = defineEmits<{
  close: [];
  buttonClick: [button: MessageButton];
}>();

const props = withDefaults(defineProps<MessageDialogProps>(), {
  type: 'info',
  showCloseButton: true,
  buttons: () => [
    { text: 'OK', class: 'primary' }
  ]
});

const titleClass = computed(() => {
  switch (props.type) {
    case 'success': return 'text-green-600';
    case 'error': return 'text-red-600';
    case 'warning': return 'text-yellow-600';
    case 'info': return 'text-blue-600';
    default: return 'text-gray-600';
  }
});

const iconClass = computed(() => {
  switch (props.type) {
    case 'success': return 'text-green-500';
    case 'error': return 'text-red-500';
    case 'warning': return 'text-yellow-500';
    case 'info': return 'text-blue-500';
    default: return 'text-gray-500';
  }
});

const icon = computed(() => {
  switch (props.type) {
    case 'success': return 'SuccessIcon';
    case 'error': return 'ErrorIcon';
    case 'warning': return 'WarningIcon';
    case 'info': return 'InfoIcon';
    default: return null;
  }
});

function close() {
  emit('close');
}

function handleOverlayClick() {
  if (props.showCloseButton) {
    close();
  }
}

function handleButtonClick(button: MessageButton) {
  emit('buttonClick', button);
  if (button.action) {
    button.action();
  } else if (button.text === 'OK' && button.class === 'primary') {
    // デフォルトのOKボタンの場合は閉じる
    close();
  }
}
</script>

<script lang="ts">
// アイコンコンポーネント
const SuccessIcon = {
  template: `
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
      <polyline points="22,4 12,14.01 9,11.01"></polyline>
    </svg>
  `
};

const ErrorIcon = {
  template: `
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <circle cx="12" cy="12" r="10"></circle>
      <line x1="15" y1="9" x2="9" y2="15"></line>
      <line x1="9" y1="9" x2="15" y2="15"></line>
    </svg>
  `
};

const WarningIcon = {
  template: `
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
      <line x1="12" y1="9" x2="12" y2="13"></line>
      <line x1="12" y1="17" x2="12.01" y2="17"></line>
    </svg>
  `
};

const InfoIcon = {
  template: `
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <circle cx="12" cy="12" r="10"></circle>
      <line x1="12" y1="16" x2="12" y2="12"></line>
      <line x1="12" y1="8" x2="12.01" y2="8"></line>
    </svg>
  `
};
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
}

.dialog-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 400px;
  max-height: 90vh;
  overflow-y: auto;
  box-sizing: border-box;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.dialog-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.close-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: #6b7280;
  transition: all 0.2s ease;
}

.close-button:hover {
  background: #f3f4f6;
  color: #374151;
}

.dialog-body {
  padding: 24px;
  box-sizing: border-box;
}

.message-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 16px;
}

.message-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-text {
  width: 100%;
}

.message {
  font-size: 1rem;
  color: #374151;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.details {
  font-size: 0.875rem;
  color: #6b7280;
  background: #f9fafb;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  word-break: break-all;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
}

.dialog-button {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 80px;
}

.dialog-button.primary {
  background: #3b82f6;
  color: white;
}

.dialog-button.primary:hover:not(:disabled) {
  background: #2563eb;
}

.dialog-button.secondary {
  background: #f3f4f6;
  color: #374151;
}

.dialog-button.secondary:hover:not(:disabled) {
  background: #e5e7eb;
}

.dialog-button.danger {
  background: #ef4444;
  color: white;
}

.dialog-button.danger:hover:not(:disabled) {
  background: #dc2626;
}

.dialog-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* レスポンシブデザイン */
@media (max-width: 640px) {
  .dialog-content {
    max-width: 100%;
    margin: 0;
  }
  
  .dialog-header,
  .dialog-body,
  .dialog-footer {
    padding: 16px;
  }
  
  .dialog-footer {
    flex-direction: column;
  }
  
  .dialog-button {
    width: 100%;
  }
}
</style>
