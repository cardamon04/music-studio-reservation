<!-- 予約ダイアログコンポーネント -->
<template>
  <div v-if="isVisible" class="dialog-overlay" @click="closeDialog">
    <div class="dialog-content" @click.stop>
      <div class="dialog-header">
        <h2 class="dialog-title">スタジオ予約</h2>
        <button class="close-button" @click="closeDialog">×</button>
      </div>

      <div class="dialog-body">
        <!-- 予約情報表示 -->
        <div class="booking-info">
          <div class="info-row">
            <span class="info-label">スタジオ:</span>
            <span class="info-value">{{ selectedStudio }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">時間枠:</span>
            <span class="info-value">{{ selectedPeriod }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">利用日:</span>
            <span class="info-value">{{ usageDate }}</span>
          </div>
          <div class="booking-note">
            <span class="note-text">※ 予約は今日から1週間後まで可能です</span>
          </div>
        </div>

        <!-- 予約種類選択 -->
        <div class="form-section">
          <label class="form-label">予約種類</label>
          <select v-model="reservationType" class="form-select">
            <option value="StudentRental">学生レンタル</option>
            <option value="ClassRental">授業レンタル</option>
            <option value="EventReservation">イベント予約</option>
          </select>
        </div>

        <!-- イベント名入力（イベント予約の場合） -->
        <div v-if="reservationType === 'EventReservation'" class="form-section">
          <label class="form-label">イベント名</label>
          <input
            v-model="eventName"
            type="text"
            placeholder="イベント名を入力してください"
            class="form-input"
            maxlength="100"
          />
          <p v-if="!eventName.trim()" class="error-message">
            イベント予約の場合はイベント名を入力してください
          </p>
        </div>
        <div v-if="reservationType === 'StudentRental'" class="form-section">
          <label class="form-label">利用学生</label>
          <div class="student-selection">
            <!-- 学生検索 -->
            <div class="student-search-group">
              <input
                v-model="studentSearchQuery"
                type="text"
                placeholder="学生名で検索 (例: 田中)"
                class="form-input"
                @input="searchStudents"
                @focus="showSearchResults = true"
              />
              <div v-if="showSearchResults && studentSearchResults.length > 0" class="search-results">
                <div
                  v-for="student in studentSearchResults"
                  :key="student.studentNumber"
                  class="search-result-item"
                  @click="selectStudent(student)"
                >
                  <div class="student-info">
                    <span class="student-name">{{ student.name }}</span>
                    <span class="student-number">{{ student.studentNumber }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 学生番号直接入力 -->
            <div class="student-input-group">
              <input
                v-model="newStudentNumber"
                type="text"
                placeholder="または学生番号を直接入力 (例: AB12345678)"
                class="form-input"
                @keyup.enter="addStudent"
              />
              <button @click="addStudent" class="add-button" :disabled="!newStudentNumber.trim()">
                追加
              </button>
            </div>

            <!-- 選択された学生一覧 -->
            <div class="student-list">
              <div
                v-for="(student, index) in selectedStudents"
                :key="index"
                class="student-item"
              >
                <div class="student-info">
                  <span class="student-name">{{ student.name }}</span>
                  <span class="student-number">{{ student.studentNumber }}</span>
                </div>
                <button @click="removeStudent(index)" class="remove-button">×</button>
              </div>
            </div>
            <p v-if="selectedStudents.length === 0" class="empty-message">
              利用学生を追加してください
            </p>
          </div>
        </div>

        <!-- 備品選択 -->
        <div class="form-section">
          <label class="form-label">レンタル備品</label>
          <div v-if="equipmentLoading" class="loading-message">
            備品一覧を読み込み中...
          </div>
          <div v-else-if="equipmentError" class="error-message">
            {{ equipmentError }}
            <button @click="loadEquipment" class="retry-button">再試行</button>
          </div>
          <div v-else class="equipment-selection">
            <div
              v-for="equipment in availableEquipment"
              :key="equipment.id"
              class="equipment-item"
            >
              <div class="equipment-info">
                <span class="equipment-name">{{ equipment.name }}</span>
                <span class="equipment-stock">在庫: {{ equipment.stock }}個</span>
              </div>
              <div class="equipment-controls">
                <button
                  @click="decreaseQuantity(equipment.id)"
                  :disabled="getQuantity(equipment.id) === 0"
                  class="quantity-button"
                >
                  -
                </button>
                <span class="quantity-display">{{ getQuantity(equipment.id) }}</span>
                <button
                  @click="increaseQuantity(equipment.id)"
                  :disabled="getQuantity(equipment.id) >= equipment.stock"
                  class="quantity-button"
                >
                  +
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="dialog-footer">
        <button @click="closeDialog" class="cancel-button">キャンセル</button>
        <button @click="submitBooking" class="submit-button" :disabled="!canSubmit">
          予約確定
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import type { CreateBookingRequest } from "@/api/bookingApi";
import { fetchEquipmentList, type Equipment } from "@/api/equipmentApi";
import { searchStudentsByName, type Student } from "@/api/studentApi";

// Props
const props = defineProps<{
  isVisible: boolean;
  selectedStudio: string;
  selectedPeriod: string;
  usageDate: string;
}>();

// Emits
const emit = defineEmits<{
  close: [];
  submit: [booking: CreateBookingRequest];
}>();

// リアクティブな状態
const reservationType = ref<string>("StudentRental");
const selectedStudents = ref<Student[]>([]);
const newStudentNumber = ref<string>("");
const equipmentQuantities = ref<Record<string, number>>({});
const eventName = ref<string>("");

// 学生検索関連
const studentSearchQuery = ref<string>("");
const studentSearchResults = ref<Student[]>([]);
const showSearchResults = ref<boolean>(false);

// 仮の備品データ（後でAPIから取得するように変更）
const availableEquipment = ref<Equipment[]>([]);
const equipmentLoading = ref(false);
const equipmentError = ref<string>("");

// 計算プロパティ
const canSubmit = computed(() => {
  // 学生レンタルの場合は学生が1人以上必要
  if (reservationType.value === "StudentRental") {
    return selectedStudents.value.length > 0;
  }
  // イベント予約の場合はイベント名が必要
  if (reservationType.value === "EventReservation") {
    return eventName.value.trim().length > 0;
  }
  return true;
});

// メソッド
function closeDialog() {
  emit("close");
  resetForm();
}

function resetForm() {
  reservationType.value = "StudentRental";
  selectedStudents.value = [];
  newStudentNumber.value = "";
  equipmentQuantities.value = {};
  eventName.value = "";
  studentSearchQuery.value = "";
  studentSearchResults.value = [];
  showSearchResults.value = false;
}

// 学生検索
async function searchStudents() {
  const query = studentSearchQuery.value.trim();
  if (query.length < 1) {
    studentSearchResults.value = [];
    return;
  }

  try {
    const results = await searchStudentsByName(query);
    studentSearchResults.value = results.filter(student => 
      !selectedStudents.value.some(selected => selected.studentNumber === student.studentNumber)
    );
  } catch (error) {
    console.error("学生検索エラー:", error);
    studentSearchResults.value = [];
  }
}

// 学生選択
function selectStudent(student: Student) {
  if (!selectedStudents.value.some(s => s.studentNumber === student.studentNumber)) {
    selectedStudents.value.push(student);
  }
  studentSearchQuery.value = "";
  studentSearchResults.value = [];
  showSearchResults.value = false;
}

function addStudent() {
  const studentNumber = newStudentNumber.value.trim().toUpperCase();
  if (studentNumber && !selectedStudents.value.some(s => s.studentNumber === studentNumber)) {
    // 学生番号のみで学生オブジェクトを作成（名前は後で取得）
    const student: Student = {
      studentNumber: studentNumber,
      name: studentNumber, // 一時的に学生番号を名前として使用
      email: undefined
    };
    selectedStudents.value.push(student);
    newStudentNumber.value = "";
  }
}

function removeStudent(index: number) {
  selectedStudents.value.splice(index, 1);
}

// スタジオ名からスタジオIDを取得する関数
function getStudioIdFromName(studioName: string): string {
  const studioMapping: Record<string, string> = {
    'Aスタ': 'A',
    'Bスタ': 'B', 
    'Cスタ': 'C'
  };
  return studioMapping[studioName] || studioName;
}

function getQuantity(equipmentId: string): number {
  return equipmentQuantities.value[equipmentId] || 0;
}

function increaseQuantity(equipmentId: string) {
  const current = getQuantity(equipmentId);
  const maxStock = availableEquipment.value.find(e => e.id === equipmentId)?.stock || 0;
  if (current < maxStock) {
    equipmentQuantities.value[equipmentId] = current + 1;
  }
}

function decreaseQuantity(equipmentId: string) {
  const current = getQuantity(equipmentId);
  if (current > 0) {
    equipmentQuantities.value[equipmentId] = current - 1;
  }
}

function submitBooking() {
  if (!canSubmit.value) return;

  // 選択された備品のみを送信
  const equipmentItems = Object.entries(equipmentQuantities.value)
    .filter(([_, quantity]) => quantity > 0)
    .map(([equipmentId, quantity]) => ({ equipmentId, quantity }));

  const booking: CreateBookingRequest = {
    studioId: getStudioIdFromName(props.selectedStudio),
    period: props.selectedPeriod,
    usageDate: props.usageDate,
    reservationType: reservationType.value,
    members: selectedStudents.value.map(s => s.studentNumber),
    equipmentItems,
    eventName: reservationType.value === "EventReservation" ? eventName.value.trim() : undefined,
  };

  emit("submit", booking);
  closeDialog();
}

// 備品一覧を取得
async function loadEquipment() {
  equipmentLoading.value = true;
  equipmentError.value = "";
  
  try {
    const equipment = await fetchEquipmentList();
    availableEquipment.value = equipment;
  } catch (error) {
    equipmentError.value = "備品一覧の取得に失敗しました";
    console.error("備品一覧の取得エラー:", error);
  } finally {
    equipmentLoading.value = false;
  }
}

// コンポーネントマウント時に備品を取得
onMounted(() => {
  loadEquipment();
});

// ダイアログが開かれた時にフォームをリセット
watch(() => props.isVisible, (isVisible) => {
  if (isVisible) {
    resetForm();
  }
});
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
  z-index: 1000;
  padding: 1rem;
}

.dialog-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 500px;
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
  color: #111827;
  margin: 0;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
}

.close-button:hover {
  background: #f3f4f6;
  color: #374151;
}

.dialog-body {
  padding: 24px;
  box-sizing: border-box;
}

.booking-info {
  background: #f9fafb;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: 500;
  color: #374151;
  width: 80px;
}

.info-value {
  color: #111827;
}

.form-section {
  margin-bottom: 24px;
  width: 100%;
  box-sizing: border-box;
}

.form-label {
  display: block;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.form-select,
.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  box-sizing: border-box;
  min-width: 0;
}

.form-select:focus,
.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.student-selection {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 16px;
  width: 100%;
  box-sizing: border-box;
}

.student-search-group {
  position: relative;
  margin-bottom: 16px;
  width: 100%;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.search-result-item {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f3f4f6;
}

.search-result-item:hover {
  background: #f9fafb;
}

.search-result-item:last-child {
  border-bottom: none;
}

.student-input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-direction: column;
  width: 100%;
  box-sizing: border-box;
}

.student-input-group .form-input {
  flex: 1;
  min-width: 0;
}

.student-input-group .add-button {
  flex-shrink: 0;
}

.add-button {
  padding: 12px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.add-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.student-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.student-item {
  display: flex;
  align-items: center;
  background: #e0f2fe;
  border: 1px solid #0ea5e9;
  border-radius: 6px;
  padding: 4px 8px;
}

.student-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.student-name {
  font-weight: 500;
  color: #111827;
}

.student-number {
  font-size: 12px;
  color: #6b7280;
}

.remove-button {
  background: none;
  border: none;
  color: #dc2626;
  cursor: pointer;
  font-size: 16px;
  padding: 2px;
}

.empty-message {
  color: #6b7280;
  font-style: italic;
  margin: 0;
}

.equipment-selection {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 16px;
}

.equipment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.equipment-item:last-child {
  border-bottom: none;
}

.equipment-info {
  display: flex;
  flex-direction: column;
}

.equipment-name {
  font-weight: 500;
  color: #111827;
}

.equipment-stock {
  font-size: 12px;
  color: #6b7280;
}

.equipment-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-button {
  width: 32px;
  height: 32px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-button:disabled {
  background: #f9fafb;
  color: #9ca3af;
  cursor: not-allowed;
}

.quantity-display {
  min-width: 24px;
  text-align: center;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
}

.cancel-button,
.submit-button {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.cancel-button {
  background: white;
  border: 1px solid #d1d5db;
  color: #374151;
}

.submit-button {
  background: #10b981;
  border: none;
  color: white;
}

.submit-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.error-message {
  color: #dc2626;
  font-size: 12px;
  margin-top: 4px;
  margin-bottom: 0;
}

.loading-message {
  color: #6b7280;
  font-style: italic;
  text-align: center;
  padding: 20px;
}

.retry-button {
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  margin-left: 8px;
}

.booking-note {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

.note-text {
  font-size: 12px;
  color: #6b7280;
  font-style: italic;
}

/* タブレット以上 */
@media (min-width: 768px) {
  .dialog-content {
    max-width: 600px;
  }
  
  .student-input-group {
    flex-direction: row;
    align-items: stretch;
  }
  
  .student-input-group .form-input {
    flex: 1;
    min-width: 0;
  }
  
  .student-input-group .add-button {
    flex-shrink: 0;
    min-width: 80px;
  }
  
  .dialog-body {
    padding: 2rem;
  }
  
  .dialog-header {
    padding: 1.5rem 2rem;
  }
  
  .dialog-footer {
    padding: 1.5rem 2rem;
  }
}

/* デスクトップ以上 */
@media (min-width: 1024px) {
  .dialog-content {
    max-width: 700px;
  }
}
</style>
