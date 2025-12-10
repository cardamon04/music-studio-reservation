import { apiGet } from "@/lib/apiClient";

// 備品の型定義
export interface Equipment {
  id: string;
  name: string;
  stock: number;
  isActive: boolean;
}

// 備品一覧を取得
export async function fetchEquipmentList(): Promise<Equipment[]> {
  try {
    const response = await apiGet("/equipment");
    return response;
  } catch (error) {
    console.error("備品一覧の取得に失敗しました:", error);
    throw error;
  }
}

// IDで備品を取得
export async function fetchEquipmentById(id: string): Promise<Equipment> {
  try {
    const response = await apiGet(`/equipment/${id}`);
    return response;
  } catch (error) {
    console.error(`備品詳細の取得に失敗しました (ID: ${id}):`, error);
    throw error;
  }
}
