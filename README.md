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

Run the development client with:

```bash
./gradlew run
```

On Windows:

```powershell
.\gradlew.bat run
```

## Status

Early development / V1 scaffold.
