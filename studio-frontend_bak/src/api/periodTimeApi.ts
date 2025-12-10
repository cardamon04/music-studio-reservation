import { apiGet } from "@/lib/apiClient";

export interface PeriodTime {
  periodId: string;
  startTime: string; // HH:mm形式
  endTime: string;   // HH:mm形式
}

export interface PeriodTimeResponse {
  periods: PeriodTime[];
}

export async function getPeriodTimes(): Promise<PeriodTime[]> {
  try {
    console.log("=== API呼び出し開始: /period-times ===");
    const response = await apiGet("/period-times");
    console.log("APIレスポンス受信:", response);
    console.log("レスポンスタイプ:", typeof response);
    console.log("レスポンスのperiodsプロパティ:", response.periods);
    
    const periods = response.periods as PeriodTime[];
    console.log("パース後の期間データ:", periods);
    console.log("パース後のデータ件数:", periods.length);
    console.log("=== API呼び出し終了: /period-times ===");
    return periods;
  } catch (error) {
    console.error("=== API呼び出しエラー: /period-times ===");
    console.error("Failed to fetch period times:", error);
    console.error("エラーの詳細:", error);
    throw error;
  }
}
