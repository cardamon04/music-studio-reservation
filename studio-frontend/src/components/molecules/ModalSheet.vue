  <template>
    <div
      v-if="modelValue"
      class="fixed inset-0 z-[1000]"
      @keydown.esc="$emit('update:modelValue', false)"
      aria-modal="true"
      role="dialog"
    >
      <!-- 背景（フェードインアニメーション） -->
      <div
        class="absolute inset-0 bg-black/40 transition-opacity duration-300 ease-out"
        @click="$emit('update:modelValue', false)"
      />

      <!-- ボトムシート本体（スライドアップアニメーション） -->
      <div
        class="absolute inset-x-0 bottom-0 rounded-t-2xl bg-white shadow-2xl transform transition-transform duration-300 ease-out"
        :class="modelValue ? 'translate-y-0' : 'translate-y-full'"
      >
        <!-- グラバー -->
        <div class="flex justify-center pt-2">
          <div class="h-1.5 w-12 rounded-full bg-gray-300" />
        </div>

        <header class="px-5 pt-3 pb-2 flex items-center justify-between">
          <h3 class="text-lg font-semibold">{{ title }}</h3>
          <button
            class="p-2 -mr-2 text-gray-500 hover:text-gray-700"
            @click="$emit('update:modelValue', false)"
            aria-label="閉じる"
          >
            ✕
          </button>
        </header>

        <div class="max-h-[70dvh] overflow-auto px-5 pb-5">
          <slot />
        </div>
      </div>
    </div>
  </template>

  <script setup lang="ts">
  import { watch } from 'vue';
  
  const props = defineProps<{
    modelValue: boolean;
    title?: string;
  }>();
  
  const emit = defineEmits<{
    (e: 'update:modelValue', v: boolean): void;
  }>();
  
  // デバッグ用ログ
  console.log('[ModalSheet] 初期化:', {
    'props.modelValue': props.modelValue,
    'props.title': props.title
  });
  
  watch(() => props.modelValue, v => {
    console.log('[ModalSheet] modelValue changed:', v);
  });
  </script>
