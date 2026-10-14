# KraftPulse

> **Spring Boot observability built for developers.**
>
> Understand your application's behavior, investigate performance problems, and find errors faster—all through one developer-friendly experience.

---

## Vision & Philosophy

KraftPulse is an application observability platform designed specifically for the Spring Boot ecosystem. It focuses on the direct questions developers ask every day:
- Why is my application slow?
- Which database operation caused the slowdown?
- Which endpoint is throwing exceptions?
- What caused an external service to time out?

### Core Principles
* **Spring Boot First**: Connects telemetry directly to Controllers, Repositories, Security, Transactions, and Scheduled Jobs rather than disconnected JVM metrics.
* **Minimal Configuration**: Add KraftPulse, start your app, and immediately open the dashboard.
* **Local First**: Fully functional offline without an account; seamless transition to hosted environments.
* **Developer Experience First**: Simple, unobtrusive, approachable, and actionable.

---

## Core Observability Areas

* **HTTP Requests**: Incoming URLs, methods, status codes, durations, controllers, user context, and trace correlation.
* **Database & Data Access**: Repository operations, SQL/JPQL/MongoDB queries, query durations, slow operations, transactions, and N+1 pattern detection.
* **Outbound Requests**: External HTTP destinations, statuses, durations, timeouts, retries, and failures.
* **Exceptions**: Grouped stack traces, error messages, trace linkage, and occurrence frequencies.
* **Cache**: Hits, misses, hit ratios, evictions, lookup durations, and cache-related failures.
* **Application Lifecycle**: Startup/shutdown durations, active profiles, runtime versions, and readiness states.
* **Audit Activity**: Privacy-conscious visibility into authentication events, entity mutations, and custom security events.

---

## Spring Ecosystem Visibility

- **Spring Security**: Authentication outcomes, login failures, access-denied events, and role context.
- **Transactions**: Duration, completion, and rollback tracking.
- **Scheduled Jobs**: Execution timings, outcomes, and failure inspection for `@Scheduled` tasks.
- **Spring Events**: Processing times, listener activity, and event-driven error diagnostics.

---

## Developer Dashboard

* **Overview Screen**: Concise signals covering latency trends, slow queries, error rates, and lifecycle events.
* **Request Timeline**: Sequential trace breakdown showing time spent in handling, security, controller logic, data access, cache, and external APIs.
* **Search & Investigation**: Rapid querying by controller, repository, endpoint, exception, trace ID, or dependency.

---

## Deployment & Workflows

### Experiences
- **Local Experience**: Zero-account, offline, integrated local dashboard with configurable retention.
- **Hosted Experience**: Team workspaces, long-term retention, alerts, and collaborative diagnostics.

### Integrations & Assisted Diagnostics
- Optional webhooks, email, Slack/Teams alerts, and export capabilities.
- Contextual, evidence-grounded explanations (e.g., explaining latency spikes post-deployment or PostgreSQL waiting bottlenecks).

---

## Non-Goals

KraftPulse is **not**:
- A Kubernetes or cloud infrastructure monitoring platform.
- A replacement for Prometheus, Grafana, or general-purpose log aggregation.
- A SIEM or complete DevOps platform.

---

## Phased Roadmap

1. **Initial Release (MVP)**: Inbound HTTP, Database diagnostics, Outbound requests, Exceptions, Cache, Lifecycle, Timeline view, Search, Local offline mode.
2. **Next**: Scheduled jobs, Spring events, Transaction insights, Audit exploration, Alerts, N+1 detection.
3. **Later & Future**: Assisted diagnostics, Team webhooks, Deployment context, OpenTelemetry/Ecosystem export, Custom instrumentation SDK.

---

## Mission

> **Help Spring Boot developers understand their applications in minutes, not hours.**# kraftpulse
