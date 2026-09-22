import type {Analysis} from "./types";
const base=process.env.NEXT_PUBLIC_API_BASE??"http://127.0.0.1:8080";
export async function startAnalysis(repositoryPath:string,baseRef:string,headRef:string){const r=await fetch(`${base}/api/v1/analyses`,{method:"POST",headers:{"content-type":"application/json"},body:JSON.stringify({repositoryPath,baseRef,headRef})});if(!r.ok)throw new Error(await r.text());return (await r.json()) as {id:string};}
export async function getAnalysis(id:string){const r=await fetch(`${base}/api/v1/analyses/${id}`,{cache:"no-store"});if(!r.ok)throw new Error("Analysis unavailable");return (await r.json()) as Analysis;}
