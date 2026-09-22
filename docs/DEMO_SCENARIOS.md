# Demo scenarios

The `sample-apps/commerce-platform` tree contains Spring-style controller, service, Kafka, and Flyway source examples. Create a base commit and a head commit around one of the following changes, then submit those commit IDs through the interface or `POST /api/v1/analyses`.

| Change | Expected deterministic signal |
| --- | --- |
| Add or change a Java record used by an HTTP endpoint | API contract change and evidence-backed DTO node |
| Add `@DeleteMapping` | High-severity API contract finding |
| Add or alter `@PreAuthorize`, `@PostAuthorize`, or `@Secured` in the selected head revision | Security semantic change |
| Add `DROP TABLE` or `DROP COLUMN` to a Flyway migration | Critical database finding and blocked policy outcome |
| Add or change `@KafkaListener` | Messaging semantic change |
| Change an `application.yml`, `application.yaml`, or `.properties` key | Configuration semantic change |
| Refactor a production Java file without a matching test file | Low-severity test-gap finding |

Inspect the returned evidence path, revision, line range, symbol, and snippet for every reported result.
