import { apiGet } from "@/lib/apiClient";

// 学生の型定義
export interface Student {
  studentNumber: string;
  name: string;
  email?: string;
}

// 学生一覧を取得
export async function fetchStudentList(): Promise<Student[]> {
  try {
    const response = await apiGet("/students");
    return response;
  } catch (error) {
    console.error("学生一覧の取得に失敗しました:", error);
    throw error;
  }
}

// 学生名で検索
export async function searchStudentsByName(name: string): Promise<Student[]> {
  try {
    if (!name.trim()) {
      return [];
    }
    const response = await apiGet(`/students/search?name=${encodeURIComponent(name)}`);
    return response;
  } catch (error) {
    console.error("学生検索に失敗しました:", error);
    throw error;
  }
}

// 学生番号で学生を取得
export async function fetchStudentByNumber(studentNumber: string): Promise<Student> {
  try {
    const response = await apiGet(`/students/${studentNumber}`);
    return response;
  } catch (error) {
    console.error(`学生詳細の取得に失敗しました (学生番号: ${studentNumber}):`, error);
    throw error;
  }
}
