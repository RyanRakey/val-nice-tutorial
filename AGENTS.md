# AGENTS.md — val-nice-tutorial

## Purpose

This project is a utility that listens for a configurable hotkey (default: **numpad 1**) and automatically sends a customizable message to Valorant chat by simulating keystrokes. It supports both **All Chat** (`/all`) and **Team Chat** (`/team`).

## Development Workflow

### Running the utility

```
clojure -M:run
```

Requires a desktop environment with a display (uses `java.awt.Robot` and JNativeHook for global keyboard listening).

### nREPL

The project includes `nrepl` as a dependency. Start a REPL with:

```
clojure -M -m clojure.main
```

### Tests

Run the test suite (clojure.test):

```
clojure -M:test
```

### Formatting

Format all source files with cljfmt via the `:format` alias:

```
clojure -M:format
```

### Linting

Lint with clj-kondo (must be installed separately):

```
clj-kondo --lint src test
```

### Git

Only commit changes when explicitly asked by the user.

### Parentheses Repair

When encountering unbalanced parentheses errors in Clojure files, use the `clojure-mcp_paren_repair` MCP tool instead of manually debugging delimiter counts.

## Project Structure

```
src/val_nice_tutorial/
├── core.clj          — state atom, Robot simulation, listener factory, start/stop lifecycle
├── config.clj        — load/save config from ~/.val-nice-tutorial/config.edn
├── tray.clj          — system tray icon (Start/Stop/Settings/Quit)
└── settings.clj      — Swing settings dialog (hotkey, chat mode, message)
test/val_nice_tutorial/
├── core_test.clj     — unit tests (char mapping, key sequences, listener dispatch, lifecycle)
└── test_runner.clj   — test runner entry point
```

## Key Design Decisions

- **Global hotkey** uses `com.github.kwhat/jnativehook` to detect hotkey presses even when Valorant is in focus
- **Numpad detection** uses `KEY_LOCATION_NUMPAD` to distinguish numpad keys from the number row
- **Config persistence** saved to `~/.val-nice-tutorial/config.edn`
- **System tray** icon sits in the notification area; double-click or right-click for controls
- **Settings dialog** lets the user change: trigger key (dropdown), chat mode (All/Team), message text
- **Start/Stop lifecycle** enables/disables the listener cleanly without restarting the app
- **Testability**: `send-message` accepts an optional `Robot` argument; `create-robot`, `add-listener!`, `remove-listener!` are separate vars redefinable in tests
