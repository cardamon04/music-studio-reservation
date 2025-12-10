<template>
    <ModalSheet 
      :model-value="modelValue" 
      @update:model-value="emit('update:modelValue', $event)"
      title="予約内容の確認"
    >
      <!-- スタジオ / 限 / 日付のサマリ -->
      <section class="space-y-2 rounded-xl border border-gray-200 p-4">
        <Row label="日付">{{ dateLabel }}</Row>
        <Row label="スタジオ">{{ studioName }}</Row>
        <Row label="限">{{ periodLabel }}（{{ timeRange }}）</Row>
      </section>

      <!-- 利用目的 -->
      <section class="mt-4">
        <h4 class="mb-2 text-sm font-semibold text-gray-700">利用目的（任意）</h4>
        <textarea
          v-model="purpose"
          class="w-full resize-y rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
          rows="3"
          placeholder="例）バンド練習、発表会前の個人練習 など"
        />
      </section>

      <!-- 注意事項 -->
      <section class="mt-4 rounded-xl bg-gray-50 p-3 text-xs text-gray-600">
        ・開始5分前に入室してください。<br />
        ・無断キャンセルは次回以降の予約制限の対象になります。
      </section>

      <!-- アクション -->
      <div class="mt-5 grid grid-cols-2 gap-3">
        <button
          type="button"
          class="h-12 rounded-xl border border-gray-300 bg-white font-semibold text-gray-700"
          @click="close"
        >
          キャンセル
        </button>
        <button
          type="button"
          class="h-12 rounded-xl bg-blue-500 font-semibold text-white enabled:hover:bg-blue-600 disabled:opacity-50"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? '送信中…' : 'この内容で予約' }}
        </button>
      </div>
    </ModalSheet>
  </template>

  <script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import ModalSheet from '@/components/molecules/ModalSheet.vue';
  import Row from '@/components/atoms/Row.vue';

  const props = defineProps<{
    /** 表示/非表示（v-model） */
    modelValue: boolean;
    /** YYYY-MM-DD */
    date: string;
    dateLabel: string;         // 例: "2025/10/07 (火)"
    studioName: string;        // 例: "Studio A"
    periodLabel: string;       // 例: "P1"
    timeRange: string;         // 例: "09:00–10:30"
  }>();

  const emit = defineEmits<{
    (e: 'update:modelValue', v: boolean): void;
    (e: 'confirm', payload: {
      date: string;
      studioName: string;
      periodLabel: string;
      timeRange: string;
      purpose: string;
    }): void;
  }>();

  // デバッグ用ログ
  console.log('[ReservationDialog] コンポーネント初期化');
  console.log('[ReservationDialog] props:', props);

  const dateLabel = computed(() => props.dateLabel);
  const studioName = computed(() => props.studioName);
  const periodLabel = computed(() => props.periodLabel);
  const timeRange = computed(() => props.timeRange);

  const purpose = ref('');
  const submitting = ref(false);

  function close() {
    console.log('[ReservationDialog] close() called');
    emit('update:modelValue', false);
  }

  async function submit() {
    submitting.value = true;
    try {
      emit('confirm', {
        date: props.date,
        studioName: studioName.value,
        periodLabel: periodLabel.value,
        timeRange: timeRange.value,
        purpose: purpose.value.trim(),
      });
      close();
    } finally {
      submitting.value = false;
    }
  }
  </script>

