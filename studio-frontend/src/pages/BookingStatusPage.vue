<!-- src/pages/BookingStatusPage.vue -->
<template>
    <section class="p-4 space-y-4">
      <div class="flex items-center gap-3">
        <div class="date-selection-group">
          <input 
            type="date" 
            v-model="date" 
            class="border px-2 py-1 rounded"
            :min="today"
            :max="maxDate"
          />
          <span class="date-info">予約可能期間: 今日から1週間後まで</span>
        </div>
        <button class="px-3 py-1 rounded bg-blue-600 text-white" @click="reload">再読込</button>
        <h1 class="text-2xl font-bold ml-6">予約状況</h1>
      </div>

      <div v-if="store.loading">読み込み中...</div>
      <div v-else-if="store.error" class="text-red-600">エラー: {{ store.error }}</div>

      <!-- v-else の中に絶対テーブルを出す -->
      <div v-else class="overflow-auto">
        <table class="min-w-full border-collapse">
          <thead>
            <tr>
              <th class="p-2 text-left border-b w-32">スタジオ</th>
              <th v-for="p in store.periods" :key="p" class="p-2 text-left border-b min-w-24">{{ p }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="st in store.studios" :key="st">
              <td class="p-2 border-b font-medium">{{ st }}</td>
              <td v-for="p in store.periods" :key="st + p" class="p-2 border-b">
                <span v-if="store.getCell(st, p)" class="px-2 py-1 rounded bg-green-100 text-green-800">○ 空</span>
                <span v-else class="px-2 py-1 rounded bg-gray-100 text-gray-700">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </template>

  <script setup lang="ts">
  import { onMounted, watch, computed } from "vue";
  import { useScheduleStore } from "@/stores/schedule";

  const store = useScheduleStore();
  
  // 日付制限の計算
  const today = computed(() => {
    const d = new Date();
    const mm = String(d.getMonth()+1).padStart(2,"0");
    const dd = String(d.getDate()).padStart(2,"0");
    return `${d.getFullYear()}-${mm}-${dd}`;
  });
  
  const maxDate = computed(() => {
    const d = new Date();
    d.setDate(d.getDate() + 7); // 1週間後
    const mm = String(d.getMonth()+1).padStart(2,"0");
    const dd = String(d.getDate()).padStart(2,"0");
    return `${d.getFullYear()}-${mm}-${dd}`;
  });
  
  const date = computed({
    get: () => store.usageDate,
    set: (v: string) => store.setDate(v),
  });
  
  async function reload() { await store.load(); }
  onMounted(reload);
  watch(() => store.usageDate, reload); // 日付を変えたら再読込
  </script>

<style scoped>
.date-selection-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.date-info {
  font-size: 12px;
  color: #6b7280;
  text-align: center;
}
</style>
