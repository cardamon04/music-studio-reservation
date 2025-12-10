<template>
    <main class="min-h-dvh bg-gray-100 text-gray-900">
      <!-- Header -->
      <div class="pt-6 pb-3 text-center">
        <h1 class="text-xl font-bold">予約状況</h1>
  
        <!-- Date Picker Bar（ダミーUI） -->
        <div
          class="mx-auto mt-3 inline-flex items-center gap-2 rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm shadow-sm"
          role="group"
          aria-label="日付選択"
        >
          <span aria-hidden="true">📅</span>
          <span class="font-medium">{{ selectedDateLabel }}</span>
        </div>
      </div>
  
      <!-- Content -->
      <div class="mx-auto w-full max-w-md px-4 space-y-4">
        <StudioCard v-for="s in studios" :key="s.id" :studio="s" />
  
        <div class="pb-6 pt-1">
          <AppButton @click="toReserve">予約する</AppButton>
        </div>
      </div>
    </main>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue';
  import StudioCard from '@/components/organisms/StudioCard.vue';
  import AppButton from '@/components/atoms/AppButton.vue';
  import type { Studio } from '@/lib/types';
  
  const selectedDateLabel = ref('2025/10/07 (火)');
  
  // ダミーデータ（API接続時は置き換え）
  const studios = ref<Studio[]>([
    {
      id: 'A',
      name: 'Studio A',
      periods: [
        { id: 'P1', label: 'P1', timeRange: '09:00–10:30', status: 'available' },
        { id: 'P2', label: 'P2', timeRange: '10:30–12:00', status: 'booked' },
        { id: 'P3', label: 'P3', timeRange: '13:00–14:30', status: 'booked' },
        { id: 'P4', label: 'P4', timeRange: '15:00–16:30', status: 'booked' },
        { id: 'P5', label: 'P5', timeRange: '17:00–18:30', status: 'booked' },
      ],
    },
    {
      id: 'B',
      name: 'Studio B',
      periods: [
        { id: 'P1', label: 'P1', timeRange: '09:00–10:30', status: 'available' },
        { id: 'P2', label: 'P2', timeRange: '10:30–12:00', status: 'booked' },
        { id: 'P3', label: 'P3', timeRange: '13:00–14:30', status: 'in_use' },
        { id: 'P4', label: 'P4', timeRange: '15:00–16:30', status: 'canceled' },
        { id: 'P5', label: 'P5', timeRange: '17:00–18:30', status: 'booked' },
      ],
    },
  ]);
  function toReserve() {
    // ここでルーター遷移やモーダル表示を実装
    // router.push({ name: 'reserve', query: { date: selectedDateLabel.value } })
    alert('予約フローに進みます（実装ポイント）');
  }
  </script>
  
  <style scoped>
  /* iOS風のうっすら影（見た目の再現度アップ用） */
  </style>
  