# Zero Knowledge Ironman Guide

A RuneLite companion for a zero-knowledge OSRS Ironman progression route.

## Goal

Turn the Zero Knowledge Ironman Guide into an in-client, step-by-step progression system with automatic completion where RuneLite can safely observe game state.

## Planned features

- RuneLite side-panel guide
- Chapters and ordered progression steps
- Previous / next navigation
- Automatic completion for supported skill, quest and state-based steps
- Manual completion fallback
- NPC, object and tile guidance overlays
- "While you're here" reminders
- Gear, item, diary, minigame and unlock progression
- Persistent per-account progress

The plugin is guidance-only. It will not automate gameplay or perform player actions.

## Development

This repository follows the official RuneLite external plugin structure and targets `latest.release`.

Open the repository in IntelliJ IDEA as a Gradle project, or run with a locally installed Gradle:

```bash
gradle run
```

A Gradle wrapper will be added before the first packaged development release.

## Status

Early development / V1 scaffold.
