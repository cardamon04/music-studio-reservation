<template>
  <dialog ref="dialog" class="modal-overlay" :aria-label="label" @cancel.prevent="requestClose" @click.self="requestClose" @keydown.tab="cycleFocus">
    <slot v-if="isVisible" />
  </dialog>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watchEffect } from 'vue';

const props = withDefaults(defineProps<{
  isVisible: boolean;
  label: string;
  dismissible?: boolean;
  fallbackFocus?: HTMLElement;
}>(), { dismissible: true });
const emit = defineEmits<{ close: [] }>();
const dialog = ref<HTMLDialogElement>();

// Native modal behavior makes the background inert and restores the opener on close.
watchEffect(() => {
  if (!dialog.value) return;
  if (props.isVisible && !dialog.value.open) dialog.value.showModal();
  else if (!props.isVisible && dialog.value.open) {
    dialog.value.close();
    // A calendar refresh can remove the original slot while the result is open.
    if (document.activeElement === document.body) props.fallbackFocus?.focus();
  }
}, { flush: 'post' });

function requestClose() {
  if (props.dismissible) emit('close');
}

function cycleFocus(event: KeyboardEvent) {
  const controls = Array.from(dialog.value!.querySelectorAll<HTMLElement>(
    'button, input, select, textarea, a[href], [tabindex]',
  )).filter(el => el.tabIndex >= 0 && !el.matches(':disabled') && el.getClientRects().length > 0);
  const first = controls[0];
  const last = controls[controls.length - 1];
  if (!first) return;
  if (event.shiftKey && (document.activeElement === first || !controls.includes(document.activeElement as HTMLElement))) {
    event.preventDefault();
    last.focus();
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault();
    first.focus();
  }
}

onBeforeUnmount(() => dialog.value?.close());
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  margin: 0;
  border: 0;
  padding: 1rem;
  width: 100%;
  height: 100dvh;
  max-width: none;
  max-height: none;
  box-sizing: border-box;
  background: transparent;
}
.modal-overlay[open] { display: flex; align-items: center; justify-content: center; }
.modal-overlay::backdrop { background: rgba(0, 0, 0, 0.5); }
</style>
