# QuarterTurn

QuarterTurn is a simple client-side macro mod for **Legacy Fabric 1.8.9** that lets you:

- Record up to **4 view directions** (“corners”)
- Automatically rotate between them in a loop
- **Swing your weapon once** each time you arrive at a corner

It’s handy for things like AFK mob grinders, guarding multiple angles, or just showing off some spinny sword choreography.

> ⚠️ Always respect the rules of any server you play on. Many servers do not allow macros or automation. Use this mod responsibly.

---

## Requirements

- Minecraft **1.8.9**
- Fabric Loader compatible with Legacy Fabric (for example `0.18.0`)
- Legacy Fabric toolchain (Fabric Loom + Legacy Looming), with properties like:

```properties
minecraft_version=1.8.9
yarn_build=576
loader_version=0.18.0
loom_version=1.11-SNAPSHOT
```

The mod itself does not require Legacy Fabric API, but you can include it in your project if you want.

---

## Controls

Default keybinds (hardcoded in `QuarterTurnClient.java`):

- **`R`** – Toggle macro on/off
- **`P`** – Record or update a corner at your current view
- **`O`** – Reset the macro (clear all corners and stop rotating)

You can change these by editing the constants:

```java
private static final int KEY_TOGGLE = Keyboard.KEY_R;
private static final int KEY_RECORD = Keyboard.KEY_P;
private static final int KEY_RESET  = Keyboard.KEY_O;
```

---

## How It Works

### Corners

- You can store up to **4 corners**.
- Each corner is the current `yaw` and `pitch` of your player.
- Press **`P`** while looking in a direction to save:
  - First press → `corner 1/4 saved`
  - Second → `corner 2/4 saved`
  - Third → `corner 3/4 saved`
  - Fourth → `corner 4/4 saved`
- After 4 corners, pressing `P` again overwrites corners in a ring:
  - You’ll see messages like `corner 2/4 updated`.

### Rotation

Once the macro is enabled and at least one corner is set, the mod:

1. Smoothly rotates toward the current corner.
2. When the view is close enough, it snaps exactly to that corner.
3. Swings your weapon once.
4. Advances to the next corner (wrapping around).

The rotation step per tick is controlled by:

```java
private static final float STEP_DEGREES = 6.2F;
```

- Smaller values → slower, smoother rotation  
- Larger values → faster, snappier rotation

### Attacking

When the player reaches a corner:

1. The view is snapped to the saved yaw/pitch.
2. The mod simulates a **left-click attack** using your existing attack keybinding.
3. A small cooldown (`attackCooldown`) prevents multiple swings at the same corner.

Internally, this is implemented by using reflection on `KeyBinding` to call the internal static “onTick”-style method with your attack key code (`mc.options.attackKey.getCode()`), which keeps it compatible with Legacy Fabric 1.8.9 mappings.

---

## Chat Feedback

The mod provides clear chat messages to show what’s happening:

- Toggling:
  - `QuarterTurn enabled, but no corners set. Look somewhere and press P to set 1/4.`
  - `QuarterTurn enabled. Rotating through N/4 corners.`
  - `QuarterTurn disabled.`
- Recording:
  - `QuarterTurn: corner 1/4 saved.`
  - ...
  - `QuarterTurn: corner 4/4 saved.`
  - `QuarterTurn: corner X/4 updated.` (after all 4 are in use)
- Reset:
  - `QuarterTurn reset. All corners cleared.`

Messages are sent with:

```java
mc.player.sendMessage(new LiteralText(message));
```

---

## Usage Walkthrough

1. Launch Minecraft **1.8.9** with Legacy Fabric.
2. Drop the built QuarterTurn jar into your `mods/` folder.
3. Join a world or server (following its rules).
4. Look in your first target direction and press **`P`**.
5. Look in your second direction and press **`P`** again.
6. Repeat for up to 4 total corners.
7. Press **`R`** to enable QuarterTurn:
   - If no corners were set, it will tell you to press `P` to set `1/4`.
   - If corners exist, it will begin rotating and attacking.
8. Press **`O`** to reset and clear all corners at any time.
9. Press **`R`** again to stop the macro.

---

## Building From Source

1. Clone the repository:

```bash
git clone https://github.com/your-user/QuarterTurn.git
cd QuarterTurn
```

2. Build with Gradle:

```bash
./gradlew build
```

3. The compiled jar will be in:

```text
build/libs/quarterturn-<version>.jar
```

Copy that jar into your `.minecraft/mods` folder for your Legacy Fabric 1.8.9 instance.

---

## Configuration / Customization

You can tweak behavior by editing `QuarterTurnClient.java`:

- Change keys:
  - `KEY_TOGGLE`, `KEY_RECORD`, `KEY_RESET`
- Change rotation speed:
  - `STEP_DEGREES`
- Change max number of corners:
  - `MAX_CORNERS` (and adjust messages if you like)
- Adjust attack cooldown:
  - `attackCooldown` (how many ticks to wait between swings)

Rebuild after making changes.

---

## Disclaimer

QuarterTurn is intended for personal, client-side use.

You are responsible for how you use it:

- Make sure macros are allowed on the servers you play on.
- Do not use this mod to break server rules or gain unfair advantages.

Enjoy the spinny sword.