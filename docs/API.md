# API

`POST /api/v1/analyses` accepts `{repositoryPath, baseRef, headRef}` and returns `202` with an analysis ID. `GET /api/v1/analyses/{id}` returns the complete result. `GET /api/v1/analyses/{id}/events` returns an SSE event containing the current result.

Example:

```powershell
Invoke-RestMethod -Method Post http://127.0.0.1:8080/api/v1/analyses -ContentType 'application/json' -Body '{"repositoryPath":"C:\\work\\repo","baseRef":"HEAD~1","headRef":"HEAD"}'
```
