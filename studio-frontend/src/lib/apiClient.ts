export async function apiGet(path: string): Promise<any> {
  const res = await fetch(`/api${path}`, {
    method: "GET",
    headers: { 
      "Accept": "application/json",
    },
  });
  if (!res.ok) {
    const body = await res.text();
    throw new Error(`GET ${path} failed: ${res.status} ${body}`);
  }
  return await res.json();
}