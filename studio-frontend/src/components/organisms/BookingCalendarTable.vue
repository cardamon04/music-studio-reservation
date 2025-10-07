<!-- src/components/organisms/BookingCalendarTable.vue -->
<template>
  <div class="w-full overflow-auto">
    <table class="min-w-full border-collapse">
      <thead>
        <tr>
          <th class="p-2 text-left border-b w-32">スタジオ</th>
          <th v-for="p in periodOrder" :key="p" class="p-2 text-left border-b min-w-48">
            {{ p }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.studioId">
          <td class="p-2 align-top border-b">{{ row.studioName }}</td>
          <td v-for="s in row.slots" :key="row.studioId + s.periodId" class="p-2 align-top border-b">
            <SlotCell :slot="s" />
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import SlotCell from "@/components/molecules/SlotCell.vue";
import type { StudioRow } from "@/types/bookingCalendar";

export default defineComponent({
  name: "BookingCalendarTable",
  components: { SlotCell },
  props: {
    periodOrder: { type: Array as PropType<string[]>, required: true },
    rows: { type: Array as PropType<StudioRow[]>, required: true }
  }
});
</script>
