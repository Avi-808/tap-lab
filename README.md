# Tap Lab

**A tiny Android sandbox for learning the observe → decide → act loop.**

Tap Lab contains a toy target game and a visible demo agent. The agent observes the target's in-app coordinates, chooses its center, and scores a hit. Start, stop, and reset the agent from the screen.

> This is a learning demo. It acts only inside its own toy game; it does not inspect or control other apps.

## What you can learn

- How an agent receives an observation from an environment
- How a simple policy chooses an action
- How feedback changes the environment and score
- How to extend a baseline agent with noise, delays, or different policies

## Run the app

1. Open this folder in Android Studio.
2. Let Android Studio install the Android Gradle Plugin and Gradle version required by the project.
3. Run the `app` configuration on an emulator or Android device.
4. Tap **Start agent** to run the policy, **Stop** to pause it, and **Reset** to restart the score.

The project uses Android Gradle Plugin 9.3.0 and requires JDK 17 and Gradle 9.5.0. See the [official Android Gradle Plugin 9.3 release notes](https://developer.android.com/build/releases/agp-9-3-0-release-notes) for compatibility details.

## How it works

- `TargetGameView.observeTarget()` exposes the toy game's current target position.
- `agentStep` reads that observation and selects the target center.
- `TargetGameView.agentTap()` applies the action to the toy game.
- A successful hit increments the score and moves the target.

The agent uses direct in-app state for clarity. A useful next experiment is to add a noisy observation model and compare policies by score and number of actions.

## Project layout

```text
app/src/main/AndroidManifest.xml
app/src/main/java/com/example/taplab/MainActivity.java
```

