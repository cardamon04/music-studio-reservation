import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import SchedulePage from "@/pages/SchedulePage.vue";
import PingView from "@/views/PingView.vue";

const routes: RouteRecordRaw[] = [
  { path: "/", redirect: "/schedule" },
  { path: "/schedule", component: SchedulePage },
  { path: "/ping", component: PingView },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;