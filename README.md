# KraftPulse

> **Understand every request. Find every bottleneck.**

KraftPulse is a request-level observability tool for Spring Boot applications.

It helps developers trace a request through their application, understand where execution time is spent, identify slow database operations and external calls, and investigate exceptions in context.

Instead of making developers jump between logs, metrics, and stack traces, KraftPulse brings the events surrounding a request into one clear, searchable timeline.

## The Problem

When a Spring Boot endpoint becomes slow or starts failing, developers often have to piece together what happened from separate sources.

They may know that a request took three seconds, but not whether the time was spent in a repository call, an external API, application logic, or repeated database queries.

They may find an exception without knowing which request triggered it or what happened immediately before it.

KraftPulse aims to close that gap.

## The Core Experience

### 1. Follow a Request

Inspect an incoming HTTP request and the operations associated with it.

Understand its endpoint, response status, execution duration, and the related database operations, external calls, and exceptions that were observed.

### 2. Find the Bottleneck

See where request time was spent.

Identify slow queries, repeated database operations, slow external dependencies, and other expensive parts of request execution.

The goal is not merely to report that a request was slow, but to help explain why.

### 3. Investigate Failures

Inspect exceptions alongside the requests and operations associated with them.

Move from an error to its surrounding execution context without manually correlating unrelated logs.

Recognize recurring errors and determine whether they affect particular endpoints or operations.

### 4. Understand Application Behavior

Provide enough additional context to make request investigations useful, including application lifecycle events, scheduled jobs, cache activity, transaction outcomes, and other Spring-related events as supported.

These capabilities support the request investigation experience rather than compete with it as separate product directions.

## The Dashboard

KraftPulse should revolve around three primary views.

- **Requests:** Find slow, failed, and recent requests. Filter by endpoint, status, duration, and other captured attributes.
- **Request details:** Inspect an individual request's execution timeline, related database operations, external calls, and exceptions.
- **Exceptions:** Find recurring failures, inspect stack traces, and navigate to the requests and operations associated with an error.

The interface should prioritize investigation over dashboards filled with disconnected charts.

## Built for Spring Boot

KraftPulse should understand the Spring Boot application model, including HTTP handling, data access, transactions, caching, scheduled tasks, and application lifecycle events.

Developers should not need to build a custom instrumentation setup just to get useful diagnostic information.

Automatic discovery should be the default wherever practical, with explicit controls for sensitive data, collection, and overhead.

## Local First

KraftPulse should provide a useful local experience without requiring an account, an external service, or a hosted observability stack.

Developers should be able to add KraftPulse to a Spring Boot application, start the application, open the dashboard, and investigate real requests.

Hosted capabilities may extend the experience for teams, but local development should remain a complete and useful use case.

## Product Principles

- **Request-centric:** Related operations should be understandable in the context of the request that triggered them.
- **Actionable:** Help developers find the cause of a problem, not merely observe that one exists.
- **Spring Boot first:** Prioritize the framework and application patterns developers already use.
- **Low overhead:** Remain suitable for sustained application use.
- **Privacy by default:** Redact sensitive information and avoid unnecessary capture of request bodies, credentials, and database parameters.
- **Minimal setup:** Deliver useful diagnostics without extensive manual instrumentation.
- **Evidence before explanation:** Any diagnosis should be traceable to the underlying captured events.

## Initial Scope

The first release should concentrate on making the request investigation experience excellent.

- Incoming HTTP request visibility
- Database and data-access operation timing
- Outbound HTTP call timing and failures
- Exceptions linked to request context
- A searchable request list
- A detailed request execution timeline
- Local, offline-capable diagnostics
- Safe defaults and configurable retention

Other capabilities should be introduced when they strengthen this experience or solve a clearly validated developer need.

## Longer-Term Direction

KraftPulse can grow into a broader Spring Boot diagnostics platform through recurring exception analysis, N+1 detection, cache insights, transaction visibility, scheduled job diagnostics, deployment comparisons, notifications, and assisted investigation.

These are extensions of the core product—not reasons to dilute the initial focus.

## Mission

> Make it easy to understand what happened inside a Spring Boot request, where time was spent, and why it failed.