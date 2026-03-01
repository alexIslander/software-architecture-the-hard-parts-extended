# Overview
This project contains simple implementations of the eight transaction saga patterns described in [Software Architecture: The Hard Parts](/). It is intended to make the trade-offs of each saga style concrete. Each implementation lives on a branch named after the pattern (for example, `phone-tag-saga`).

# Branch Navigation
Use this repository with branch-per-saga intent:

- `main`: shared baseline and cross-cutting docs.
- `epic-saga`: synchronous, atomic, orchestrated.
- `phone-tag-saga`: synchronous, atomic, choreographed.
- `fairy-tale-saga`: synchronous, eventual, orchestrated.
- `time-travel-saga`: synchronous, eventual, choreographed.
- `fantasy-fiction-saga`: asynchronous, atomic, orchestrated.
- `horror-story-saga`: asynchronous, atomic, choreographed.
- `parallel-saga`: asynchronous, eventual, orchestrated.
- `anthology-saga`: asynchronous, eventual, choreographed.
- Need help choosing? See [Pattern Decision Matrix](#pattern-decision-matrix).

| Pattern Name | Communication | Consistency | Coordination | Branch Name |
| --- | --- | --- | --- | --- |
| Epic Saga | Synchronous | Atomic | Orchestrated | `epic-saga` |
| Phone Tag Saga | Synchronous | Atomic | Choreographed | `phone-tag-saga` |
| Fairy Tale Saga | Synchronous | Eventual | Orchestrated | `fairy-tale-saga` |
| Time Travel Saga | Synchronous | Eventual | Choreographed | `time-travel-saga` |
| Fantasy Fiction Saga | Asynchronous | Atomic | Orchestrated | `fantasy-fiction-saga` |
| Horror Story Saga | Asynchronous | Atomic | Choreographed | `horror-story-saga` |
| Parallel Saga | Asynchronous | Eventual | Orchestrated | `parallel-saga` |
| Anthology Saga | Asynchronous | Eventual | Choreographed | `anthology-saga` |

# Pattern Decision Matrix
Use this matrix to select a branch based on real production constraints.

| Decision Driver | Primary Constraint | Recommended Pattern | Branch |
| --- | --- | --- | --- |
| Single request path must fully succeed or fail and latency is low | Tight control and strict rollback semantics | Epic Saga (sync + atomic + orchestrated) | `epic-saga` |
| Atomic behavior is required but you want to avoid a central coordinator | Small number of participants with stable contracts | Phone Tag Saga (sync + atomic + choreographed) | `phone-tag-saga` |
| API response should return quickly, while consistency converges later | User-facing responsiveness with controlled follow-up | Fairy Tale Saga (sync + eventual + orchestrated) | `fairy-tale-saga` |
| Teams are autonomous and can handle replay, reordering, and convergence | Eventual consistency with no central workflow owner | Time Travel Saga (sync + eventual + choreographed) | `time-travel-saga` |
| Broker-first workflow with strict compensation and explicit step control | Reliability under load plus centralized governance | Fantasy Fiction Saga (async + atomic + orchestrated) | `fantasy-fiction-saga` |
| High decoupling across services, but each service must self-manage compensation | Strong autonomy with operationally mature teams | Horror Story Saga (async + atomic + choreographed) | `horror-story-saga` |
| Throughput and parallel step execution are more important than immediate consistency | Scale and resilience for core transaction flows | Parallel Saga (async + eventual + orchestrated) | `parallel-saga` |
| Large ecosystem where domains evolve independently and subscribe to shared events | Maximum extensibility and loose coupling | Anthology Saga (async + eventual + choreographed) | `anthology-saga` |

## Production Default
If your context is not clear yet, start with `parallel-saga` for the core transaction flow, then add choreographed side-effects (notifications, analytics, search indexing) where strict central control is unnecessary.

| Architectural Style | Architectural Family |
| --- | --- |
| Layered | Monolithic |
| Pipeline | Monolithic |
| Microkernel | Monolithic |
| Service-base | Distributed |
| Event-driven | Distributed |
| Space-based | Distributed |
| Service-oriented | Distributed |
| Microservices | Distributed |

# Prerequisites
- JDK 21 or greater

# Building
- `mvn clean verify`
- `mvn clean package -DskipTests`
- `mvn -pl orchestrator spring-boot:run`

# Installation

# Tips and Tricks
- order placement service
- payment service
- fulfillment service
- email service

## Architectural Diagrams

## Guidebook
Details about this project are contained in `guidebook/guidebook.adoc` and should be considered mandatory reading prior to contributing to this project.

## Specification By Example
The Cucumber tests are...

# Troubleshooting

# Contributing

# License and Credits
- This project is licensed under the [Apache License Version 2.0, January 2004](http://www.apache.org/licenses/).
- The guidebook structure was created by [Simon Brown](http://simonbrown.je/) as part of his work on the [C4 Architectural Model](https://c4model.com/). His books can be [purchased from LeanPub](https://leanpub.com/b/software-architecture).
- [Learn how to build Cloud Native applications using Spring Boot and Kubernetes](https://youtu.be/Mw6ZilAl3uU)
- [Kubernetes Native Java by Josh Long @ Spring I/O 2022](https://youtu.be/LGOhejS1Itc)
- [Spring Tips: Spring and Kubernetes, Redux (2022)](https://youtu.be/Xe7K1biKcs0)
- [Creating a Loosely Coupled Monolith](https://youtu.be/48C-RsEu0BQ)
- [Asynchronous Messaging in a Loosely Coupled Monolith](https://youtu.be/Qi6TaIYprqc)
- [ZeroMQ and RabbitMQ Messaging for agility and scalability Allen Holub HD](https://youtu.be/tDlwu_Lmpx4)
- [Modular Monoliths](https://youtu.be/5OjqD-ow8GE)
