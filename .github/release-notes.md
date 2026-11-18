## What's new

**My Apps** - a new tab in the OpenWebStart settings:

- Import `.jar` and `.jnlp` files (button or drag and drop).
- Choose the Java version each application runs with.
- Create a desktop shortcut that starts the application directly with the chosen Java version
  (macOS `.app`, Windows `.cmd`, Linux `.desktop`).
- Rename imported applications.

**Java downloads** - search the JVM server for a Java version (e.g. `1.8`, `17`) and download it
from the JVM Manager (`Download...`) or directly when choosing a Java version for an application.

**Fixes**

- "Find local" in the JVM Manager now works on Apple Silicon Macs and finds JDKs that are symbolic links.

## Installers

| Platform | File |
|----------|------|
| Windows  | `.exe` |
| macOS (Apple Silicon) | `.dmg` |
| Linux (Debian/Ubuntu) | `.deb` |

These installers include a Java 17 runtime. They are built from source by GitHub Actions and are not
the official OpenWebStart installers from Karakun.
