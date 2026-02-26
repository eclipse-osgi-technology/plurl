<h1>Plurl API for registering URL handlers</h1>

This workspace repository contains the Plurl library.

**Plurl** is a small Java library that solves a long‑standing Java/OSGi problem: **the JDK only lets you set certain URL “factory” hooks once per JVM**, but modular systems (like OSGi) often need **multiple independent parties** to contribute URL handlers.

In plurl’s own words, it “multiplex[es] the URL factory singletons” for:

- `URL#setURLStreamHandlerFactory(URLStreamHandlerFactory)`
- `URLConnection#setContentHandlerFactory(ContentHandlerFactory)`

So instead of *one* global factory, plurl provides **one global “router” factory** that can **delegate to many registered factories**.

---

## What problem does it address?

In standard Java:

- A `URLStreamHandlerFactory` is how you teach the JVM new URL protocols (e.g. `bundleresource:`, `foo:`).
- A `ContentHandlerFactory` is how you teach the JVM how to turn a URLConnection into typed content for a MIME type.

But the JVM treats these as **singletons**: once someone sets them, *nobody else can* (or you get errors). That’s a big mismatch for OSGi / plugin ecosystems where:
- multiple bundles may want to register their own URL protocols/content handlers
- bundles come and go at runtime (so handlers need to be addable/removable)

Plurl was created to make those “singleton hooks” behave like **a registry** instead.

(See Javadoc in `Plurl.java` where it explains multiplexing and add/remove behavior, and that it may need deep reflection in some cases.)

---

## How does plurl work (simple mental model)?

1. **Install plurl** once into the JVM (it becomes the one factory the JVM knows about).
2. Other components **register** their `URLStreamHandlerFactory` and/or `ContentHandlerFactory` with plurl.
3. When Java needs a handler for protocol `X` (or MIME type `Y`), plurl **chooses which registered factory should handle it** and delegates.

### How does it choose which factory?
Plurl uses a callback `shouldHandle(Class<?> clazz)` (from `PlurlFactory`) and walks the **call stack** to decide which factory “owns” the request. If there’s only one factory registered, it uses that. If multiple are registered, it tries to find one whose `shouldHandle` matches the calling code.

That selection logic is described in the `Plurl` Javadoc.

---

## Why is there a `plurl:` protocol?

One particularly interesting design choice: plurl defines a **special protocol** `plurl://...` that acts like an *operations channel* to add/remove factories.

From `Plurl.java`, you can do operations like:

- `plurl://op/addURLStreamHandlerFactory`
- `plurl://op/removeURLStreamHandlerFactory`
- `plurl://op/addContentHandlerFactory`
- `plurl://op/removeContentHandlerFactory`

Those URLs return a `Consumer<...>` from `getContent()` that you call to perform the operation.

This exists so that factories can be added **even if the code adding them was compiled against a different copy/version of the plurl API** (common in modular/plugin worlds). In that case plurl may use reflection/proxying to interoperate.

Note: It largely should be thought of as an internal implementation detail that allows various copies of the plurl library to live and coordinate in the same JVM instance.  Most users should just use the methods directly on the Plurl interface to add/remove their factories.Under the covers it uses the `plurl:` protocol to communicate with the multiple copies of the library that may be present.

---

## Typical use cases

1. **OSGi / Eclipse / plugin-based apps**
   - Multiple bundles want to contribute URL protocols (common in OSGi: `bundle:`, `bundleresource:`, etc.).
   - You don’t want whichever bundle starts first to “win” the singleton forever.

2. **Application servers / large platforms embedding many libraries**
   - Different subsystems want URL handlers without coordinating global JVM initialization order.

3. **Dynamic add/remove of handlers**
   - Plurl’s API and tests indicate factories should not be strongly referenced and should behave as removed if GC’d (useful for dynamic module lifecycles).

4. **Avoiding (or controlling) overriding built-in JDK protocols**
   - The implementation tracks “forbidden protocols” by default (`jar`, `jmod`, `file`, `jrt`) to reduce the risk of accidentally hijacking core behavior.

---

## Notes / limitations you should be aware of

- Some features rely on **deep reflection into `java.net` internals** (the code mentions needing `--add-opens java.base/java.net=ALL-UNNAMED` in some situations).
- For more details see **Javadoc of `Plurl.java`** and the tests.


## Contributing

Want to hack on plurl? See [CONTRIBUTING.md](CONTRIBUTING.md) for information on building, testing and contributing changes.

They are probably not perfect, please let us know if anything feels
wrong or incomplete.

## Building

We use Maven to build and the repo includes `mvnw`.
You can use your system `mvn` but we require a recent version.

- `./mvnw clean install` - Assembles and tests the project

[![Build Status](https://github.com/eclipse-osgi-technology/plurl-/workflows/CI%20Build/badge.svg)](https://github.com/eclipse-osgi-technology/plurl/actions?query=workflow%3A%22CI%20Build%22)

## Repository

Snapshot plurl artifacts are available from the Sonatype OSS snapshot repository:

[https://oss.sonatype.org/content/repositories/snapshots/](https://oss.sonatype.org/content/repositories/snapshots/)

## License

The contents of this repository are made available to the public under the terms of the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
Bundles may depend on non Apache Licensed code.

