# Combat numbers

Damage remains server authoritative. These changes affect reporting and presentation, not combat calculations.

## Events and accounting

- One packet represents one event. Physical, Fire, Cold, Lightning and Void values remain separate and retain that order.
- `CombatNumberKind` distinguishes direct hits, periodic damage, bursts (Bleed), immediate healing and gradual recovery. A Fire hit is distinct from a Burn tick.
- Damage events use the resolved damage pipeline, including damage absorbed by Energy Shield. The display does not apply modifiers again.
- Healing is observed around the complete `heal` operation and reports only the positive health difference. Cancelled healing and overhealing produce no extra recovery.
- Regeneration and gradual life leech use a scoped recovery context. Their actual recovery is accumulated for ten entity ticks before broadcasting. Immediate healing is sent immediately. The old independent regeneration broadcast is removed.
- Recipients are nearby players in the same world plus the damage owner, preserving feedback for distant arrows. Existing personal/other-player visibility options still apply.

## Presentation

- Each hit is a compact group of separate outlined elemental values, using two or three columns to keep five-element hits within two rows. Critical values have darker yellow outlines and bold text, with no extra icon or reserved icon space; normal values retain black outlines. Physical is white, Cold light blue, Lightning dark blue and Void purple.
- Logarithmic magnitude scaling continues beyond 48 damage. Criticals have a larger base size and a short entrance pulse. Values remain at their reading size and fade out at the end.
- Camera projection is shared with entity nameplates. Entity positions use render interpolation; placement is assigned once, with screen bounds, reticle and target-head clearance considered. Existing groups do not reflow when new groups arrive or old ones expire.
- Self damage and recovery occupy opposite sides below the reticle in either perspective.
- The animation clock uses monotonic time and stops while singleplayer is paused. Hidden HUD state and world/connection changes are respected.
- Layouts and formatted text are cached per group revision. Active groups are bounded to eight per target and 128 globally. Periodic ticks combine in a 400 ms window; compatible direct hits combine during overflow. At saturation the oldest groups yield to newer events, with personal feedback prioritized over other players' events.
- Placement reserves the complete upward travel corridor. When no clear location is available, extra groups defer instead of overlapping digits; failed placement retries are throttled until the camera moves or 150 ms elapses.

Client and server must use the same mod build because the combat-number packet now includes the event kind.

## Verification

`CombatNumberRegression` covers scale progression and bounds, readability/fade timing, number formatting, invalid values and nested recovery scopes. The isolated native probe and framebuffer gallery are in `build/combat-numbers-live-probe.java` and `build/mount-modpack-smoke/combat-number-gallery`.

Final verification passed:

- Offline `build`, including the complete regression suite and access-widener validation: `build/combat-numbers-verified-build.log`.
- Native accounting, critical flags, shield-only hits, real packet codec, multiplayer recipient routing, periodic grouping, stable placement, 30/120 FPS caps, first/third person, large GUI scale, hidden HUD, expiration and a 5,000-event burst: `build/combat-numbers-native-results.log`.
- Cancelled drowning healing produces no number; an actual flying arrow produces exactly one damage event matching health loss: `build/combat-numbers-focused-results.log`.
- The synthetic 128-group render benchmark measured 1.44 ms median and 4.98 ms maximum across twenty calls on the test machine. This includes pathological overlapping targets and is not a universal FPS guarantee.
- Eighteen native framebuffer captures are indexed in `build/combat-number-gallery.html`. Gallery packets use controlled values; gameplay accounting is checked separately against actual health/shield changes.

The isolated modpack still logs its existing optional Punchy crossbow compatibility injection warning. No new combat-number errors occurred in the successful runs.

The verified jar was copied into the `Cranks Dungeons Testing` profile and its SHA-256 matched the built jar: `F3E4ABC1668F251CF2E88C3870B59D2D0687F9E6C267FC0F3200703372E87B07`.

The yellow-critical-outline follow-up passed `build/critical-outline-build.log` and its native visual probe (`build/critical-outline-native-results.log`). Updated normal, single-critical, five-element-critical and powerful-critical captures are in `build/mount-modpack-smoke/critical-outline-gallery`.

The final palette/icon follow-up passed `build/combat-palette-build.log` and the native preview (`build/combat-palette-native-results.log`). Critical outlines are darker yellow (`D4AE40`), and the icon plus its reserved space are removed. Updated normal and critical five-element captures are in `build/mount-modpack-smoke/combat-palette-gallery`.

Current color refinement: Fire is red (`F5655B`), and critical outlines are yellow (`DED044`). Build/regression checks passed in `build/combat-fire-yellow-build.log`; the native visual check passed in `build/combat-fire-yellow-native-results.log`. Updated captures are in `build/mount-modpack-smoke/combat-fire-yellow-gallery`.
