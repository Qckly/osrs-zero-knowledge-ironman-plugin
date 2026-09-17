# UI Guidance Audit — Tutorial Island

This document tracks every Tutorial Island step that depends on an OSRS interface,
dialogue choice, tab, production menu, or spell/action widget.

The goal is to avoid one-off fixes. Guide data should describe *what* to select,
while generic overlays resolve and highlight the live RuneLite widget.

## Guidance layers

- **InterfaceGuidanceOverlay** — top-level side tabs/icons.
- **ActionWidgetGuidanceOverlay** — buttons, production choices, spells, mode selectors.
- **DialogueGuidanceOverlay** — exact dialogue choices and "Click here to continue".
- **InventoryGuidanceOverlay** — actionable inventory items.
- **WorldGuidanceOverlay / PathGuidanceOverlay** — NPCs, objects, tiles, routes.

## Machine-readable guide fields

- `UI_TARGET` — exact or stable visible/action name to highlight.
- `DIALOGUE_CHOICE` — stable substring of the desired dialogue option.

## Tutorial Island UI coverage

| Step | UI action | Guidance |
|---|---|---|
| 000.01 | Character Creator | Native tutorial / future explicit character-creator actions |
| 000.02 | New-player familiarity choice | DIALOGUE_CHOICE: brand new |
| 000.03A | Settings tab | UI_TARGET: Settings / top-level tab |
| 000.06 | Inventory tab | UI_TARGET: Inventory / top-level tab |
| 000.08 | Skills tab | UI_TARGET: Skills / top-level tab |
| 000.19 | Quest List tab | UI_TARGET: Quest List / top-level tab |
| 000.26A | Smith Bronze dagger | UI_TARGET: Dagger / generic action widget |
| 000.28 | Worn Equipment tab | UI_TARGET: Worn Equipment / top-level tab |
| 000.28A | View equipment stats | UI_TARGET: View equipment stats / generic action widget |
| 000.31 | Combat Options tab | UI_TARGET: Combat Options / top-level tab |
| 000.41 | Account Management | UI_TARGET: Account Management / generic visible widget fallback |
| 000.43 | Prayer tab | UI_TARGET: Prayer / top-level tab |
| 000.43B | Friends List | UI_TARGET: Friends List / top-level tab |
| 000.45A | Magic tab | UI_TARGET: Magic / top-level tab |
| 000.46 | Wind Strike | UI_TARGET: Wind Strike / generic action widget |
| 000.47 | Ironman dialogue branch | DIALOGUE_CHOICE: Ironman |
| 000.49 | Standard Ironman selection | UI_TARGET: Standard Ironman / generic action widget |
| 000.52 | Lumbridge Home Teleport | UI_TARGET: Lumbridge Home Teleport / generic action widget |

## Resolver rules

1. Prefer explicit `UI_TARGET` / `DIALOGUE_CHOICE` from guide data.
2. For top-level tabs, use stable RuneLite top-level stone widgets.
3. For nested interfaces, recursively inspect visible widgets using:
   - widget text,
   - widget name,
   - widget actions.
4. Expand a matching child to its clickable parent, but stop before highlighting
   the entire modal/window.
5. If an exact dialogue choice is supplied, never highlight unrelated choices.
6. Human-readable `TARGET` and `DO` text is display content, not the primary UI
   resolver contract.

## Live-test checklist

When each interface is encountered in-game, verify:
- highlight is on the actual clickable region,
- only the intended choice is highlighted,
- fixed and resizable layouts both work,
- no unrelated widget with similar text is selected,
- step auto-detection does not advance before the interface action is complete.

Any live mismatch should be fixed in the generic resolver or guide metadata first,
not with screen-coordinate hardcoding.
