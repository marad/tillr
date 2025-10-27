# Tillr

A tiling window manager for Windows that helps you organize and manage your windows efficiently using keyboard shortcuts and customizable layouts.

## Features

- **Multiple Views/Workspaces**: Organize windows into different views and switch between them with keyboard shortcuts
- **Automatic Window Tiling**: Windows are automatically arranged according to configurable layouts
- **Customizable Layouts**: Support for different tiling layouts (e.g., two-column layout with adjustable ratios)
- **Keyboard-Driven**: Control everything with configurable hotkeys
- **Window Rules**: Define rules to manage or ignore specific windows based on title, class name, or executable name
- **System Tray Integration**: Runs quietly in the system tray with easy access to controls
- **Hot Reload**: Reload configuration without restarting the application
- **Window Navigation**: Navigate between windows using keyboard shortcuts
- **Dynamic Layout Adjustment**: Adjust layout ratios on-the-fly

## Installation

### Prerequisites

- Windows operating system
- Java Runtime Environment (JRE) 11 or higher

### From Release

1. Download the latest release from the [releases page](https://github.com/marad/tillr/releases)
2. Extract the archive to your preferred location
3. Run `tillr.exe` or `tillr.bat`

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/marad/tillr.git
   cd tillr
   ```

2. Build the project using Gradle:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew run
   ```

## Configuration

Tillr uses a YAML configuration file to define hotkeys, layouts, and window management rules.

### Configuration File Location

By default, Tillr looks for the configuration at:
```
%USERPROFILE%\.config\tiler\config.yaml
```

You can also specify a custom configuration file path:
```bash
tillr --yaml-config=path/to/your/config.yaml
```

### Example Configuration

See [example-config.yaml](example-config.yaml) for a complete example. Here's a basic configuration:

```yaml
# Editor to use when opening config file
editor: "notepad.exe"

# Layout configuration
layout:
  gap: 0          # Gap between windows in pixels
  ratio: 0.55     # Ratio for two-column layout (left column width)

# Window management rules
rules:
  - exeName: "chrome.exe"
    should: manage
  - exeName: "notepad.exe"
    should: ignore

# Hotkey definitions
hotkeys:
  # Switch between views
  - key: S-C-A-Y
    action: SwitchView
    value: 0
  - key: S-C-A-U
    action: SwitchView
    value: 1

  # Move active window to view
  - key: S-A-Y
    action: MoveActiveWindowToView
    value: 0

  # Window navigation
  - key: S-C-A-H
    action: MoveWindowLeft
  - key: S-C-A-J
    action: MoveWindowDown
  - key: S-C-A-K
    action: MoveWindowUp
  - key: S-C-A-L
    action: MoveWindowRight

  # Adjust layout ratio
  - key: S-A-L
    action: LayoutIncrease
    value: 0.05
  - key: S-A-H
    action: LayoutDecrease
    value: 0.05

  # Reload configuration
  - key: S-C-A-R
    action: ReloadConfig
```

### Hotkey Format

Hotkeys use the following modifiers:
- `S` - Shift
- `C` - Control
- `A` - Alt
- Followed by the key letter

Example: `S-C-A-Y` means Shift + Control + Alt + Y

### Available Actions

- **SwitchView**: Switch to a specific view (value: view number)
- **MoveActiveWindowToView**: Move the active window to a specific view
- **SwitchToPreviousView**: Switch back to the previously active view
- **MoveWindowLeft/Right/Up/Down**: Navigate between windows
- **LayoutIncrease/LayoutDecrease**: Adjust the layout ratio
- **ReloadConfig**: Reload the configuration file

### Window Rules

Rules determine which windows should be managed by Tillr:

- **manage**: Window will be tiled and managed
- **ignore**: Window will be ignored by Tillr

Rules can match windows by:
- `title`: Window title
- `class`: Window class name
- `exeName`: Executable name

## Usage

1. **Start Tillr**: Run the application. It will appear in the system tray.

2. **Open Windows**: Open your applications. They will automatically be added to the current view and tiled.

3. **Switch Views**: Use the configured hotkeys to switch between different views/workspaces.

4. **Move Windows**: Use hotkeys to move windows between views or navigate between them.

5. **Adjust Layout**: Use layout increase/decrease hotkeys to adjust the split ratio.

6. **Reload Config**: After modifying your configuration file, use the reload hotkey to apply changes without restarting.

## System Tray

The Tillr icon in the system tray provides:
- Status indicator
- Quick access to application controls
- Exit option

## Development

### Project Structure

- `src/main/kotlin/gh/marad/tiler/` - Main application code
  - `app/` - Application facade and core logic
  - `config/` - Configuration management
  - `os/` - Operating system integration (Windows API)
  - `tiler/` - Tiling logic and view management
  - `common/` - Shared utilities and data structures
  - `actions/` - Action handlers for hotkeys
  - `help/` - Helper utilities (window inspector)

### Running Tests

```bash
./gradlew test
```

### Creating a Release

This project uses [axion-release-plugin](https://github.com/allegro/axion-release-plugin).

To create a release:
```bash
./gradlew release
```

To bump major or minor version:
```bash
./gradlew tag v0.1.0
```

## License

[Add your license information here]

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Troubleshooting

### Application doesn't start
- Ensure Java 11 or higher is installed
- Check that the configuration file is valid YAML

### Hotkeys not working
- Verify that hotkeys don't conflict with other applications
- Check the configuration file syntax
- Try reloading the configuration with the reload hotkey

### Windows not being tiled
- Check window rules in your configuration
- Some windows (like dialogs) may be automatically excluded
- Verify the window is not minimized

## Acknowledgments

Built with:
- Kotlin and Kotlin Coroutines
- JNA for Windows API integration
- SnakeYAML for configuration parsing
