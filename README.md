# val-nice-tutorial

A utility that listens for a configurable hotkey (default: **numpad 1**) and automatically sends a customizable message to Valorant chat by simulating keystrokes. Supports **All Chat** (`/all`) and **Team Chat** (`/team`).

## Usage

### Run from source

Requires the [Clojure CLI](https://clojure.org/guides/getting_started) and a desktop environment (uses `java.awt.Robot` and JNativeHook).

```
clojure -M:run
```

### Build a library JAR

```
clojure -T:build jar
```

Output: `target/val-nice-tutorial.jar`

### Build a standalone uberjar (double-clickable)

```
clojure -T:build uberjar
```

Output: `target/val-nice-tutorial-standalone.jar`

Run with:

```
java -jar target/val-nice-tutorial-standalone.jar
```

### Clean build artifacts

```
clojure -T:build clean
```

## Development

### Run tests

```
clojure -M:test
```

### Format code

```
clojure -M:format
```

### Lint

Requires [clj-kondo](https://github.com/clj-kondo/clj-kondo) installed separately.

```
clj-kondo --lint src test
```

### nREPL

```
clojure -M -m clojure.main
```

## Project Structure

```
src/val_nice_tutorial/
├── core.clj          — state atom, Robot simulation, listener factory, start/stop lifecycle
├── config.clj        — load/save config from ~/.val-nice-tutorial/config.edn
├── tray.clj          — system tray icon (Start/Stop/Settings/Quit)
└── settings.clj      — Swing settings dialog (hotkey, chat mode, message)
test/val_nice_tutorial/
├── core_test.clj     — unit tests
└── test_runner.clj   — test runner entry point
build.clj             — tools.build script for jar/uberjar
deps.edn              — project dependencies and aliases
```
