<template>
    <section>
      <h1>API Ping</h1>
      <pre>{{ result }}</pre>
      <button @click="call">Call /api/ping</button>
    </section>
  </template>

  <script setup lang="ts">
  import { ref } from "vue";
  import { apiGet } from "../lib/apiClient";

  const result = ref<string>("(not called)");
  async function call(): Promise<void> {
    try {
      const json = await apiGet("/ping");
      result.value = JSON.stringify(json, null, 2);
    } catch (e: any) {
      result.value = e.message;
    }
  }
  </script>

  <style scoped>
  section { padding: 16px; }
  pre { background: #ffffff; padding: 12px; border-radius: 8px; overflow: auto; }
  </style>
