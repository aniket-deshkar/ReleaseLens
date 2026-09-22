# API

## Start an analysis

`POST /api/v1/analyses` accepts a repository path and two Git references.

```json
{
  "repositoryPath": "C:\\work\\commerce-platform",
  "baseRef": "HEAD~1",
  "headRef": "HEAD"
}
```

The endpoint returns `202 Accepted` with an identifier and a `Location` header.

```json
{
  "id": "analysis-id",
  "status": "QUEUED"
}
```

## Retrieve an analysis

`GET /api/v1/analyses/{id}` returns the persisted analysis record. Lifecycle values are `QUEUED`, `RUNNING`, `COMPLETED`, and `FAILED`. A completed response includes changed files, semantic changes, findings, graph data, paths, Jev decisions when enabled, policy output, and an optional Luna summary.

## Receive a snapshot event

`GET /api/v1/analyses/{id}/events` uses server-sent events and returns one `analysis` event containing the analysis record available when the endpoint is called. The connection then completes. Clients that need a newer record should request the analysis endpoint again.

## Failure representation

An analysis failure is persisted with status `FAILED`. Its policy outcome has status `INCOMPLETE`, a risk score of zero, and the failure message in `policy.reasons`.
