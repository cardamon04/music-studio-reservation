<!-- src/components/molecules/SlotCell.vue -->
<template>
    <div class="flex flex-col items-start gap-1 p-2 border border-gray-200 rounded">
      <div class="text-xs text-gray-500">{{ slot.periodId }} ({{ slot.startTime }}-{{ slot.endTime }})</div>
      <div class="flex items-center gap-2">
        <StatusBadge :text="slot.status" />
        <span v-if="slot.graceExpired" class="text-xs px-2 py-0.5 rounded border border-red-400 text-red-600">
          10分経過
        </span>
      </div>
      <div v-if="slot.reservationType" class="text-xs text-gray-600">
        {{ slot.reservationType }}
        <span v-if="slot.eventName" class="block text-blue-600 font-medium">
          {{ slot.eventName }}
        </span>
      </div>
    </div>
  </template>

  <script lang="ts">
  import { defineComponent, PropType } from "vue";
  import StatusBadge from "@/components/atoms/StatusBadge.vue";
  
  // SlotView型を直接定義
  type SlotView = {
    periodId: string;
    status: "空" | "予約済み" | "予約確定" | "使用中" | "予約キャンセル";
    bookingId?: string;
    reservationType?: "学生レンタル" | "授業レンタル" | "イベント予約";
    eventName?: string; // イベント予約の場合のイベント名
    graceExpired: boolean;
    startTime: string; // "HH:mm"
    endTime: string;   // "HH:mm"
  };
  
  export default defineComponent({
    name: "SlotCell",
    components: { StatusBadge },
    props: {
      slot: { type: Object as PropType<SlotView>, required: true }
    }
  });
  </script>