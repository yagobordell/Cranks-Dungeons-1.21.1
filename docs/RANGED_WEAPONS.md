# Bow and crossbow controls

Controls use the player's configured Use and Attack bindings, including rebound keys.

| Weapon | Use held | Attack | Use released |
|---|---|---|---|
| Bow | Draw an arrow | Shoot while drawing; otherwise melee | Lower the bow without consuming ammunition |
| Crossbow, empty | Reload | Melee; cancel the unfinished reload | Cancel the unfinished reload |
| Crossbow, loaded | Aim with a small zoom | Fire the loaded projectile | Stop aiming; keep the projectile loaded |

After firing, keeping Use held starts the next draw or reload. Each Attack press fires once; holding Attack does not automatically fire again. Bow shots require at least six virtual draw ticks. Cancelled crossbow reloads start over. A weapon or selected-slot change invalidates an ongoing server-side draw/reload.

Finishing a crossbow reload does not zoom while the reload's Use press remains held. Release Use and hold it again to aim, reducing field of view by 20%. This also applies after an automatic reload following a shot.

The configured attack indicator shows draw/reload progress, remaining full when ready. Weapon tooltips show the controls and draw/reload time. Attack-speed and equipment requirement modifiers affect the shared charge clock, including model pull, indicator progress and Punchy animation timing.

## Damage

Player bow/crossbow arrows snapshot the weapon's baked flat physical damage and launch draw strength. A full bow draw or loaded crossbow uses full weapon damage; partial bow draws scale the physical and elemental breakdown once before mitigation. Projectile flight speed no longer multiplies that damage, and the vanilla arrow's random critical bonus is disabled for these arrows. RPG critical strikes, conversions, global bonuses, penetration and defenses still use the combat pipeline.

The fixed damage and draw strength persist with the projectile's NBT. Mob arrows, unmarked arrows, tridents and crossbow fireworks retain their existing damage paths. This change does not snapshot all player attributes at launch.

## Networking and compatibility

The client sends only the firing hand and selected slot. The server checks player state, active weapon, original charge weapon/slot, minimum bow draw, ammunition and cooldown. It creates projectiles and consumes ammunition. Failed bow fire requests cancel the draw and synchronize the player inventory.

Punchy 3.2 uses a single non-looping crossbow charge clip, timed against the same gameplay duration. Its shot detector consumes actual client firing events instead of treating loaded Use presses as shots. Automatic reload waits for the firing animation to finish. Optional Punchy hooks are gated to the supported 3.2 version family; Punchy is not required or bundled. The compile-only jar matches the test modpack's 3.2b version.

## Validation

- Gradle build, regression suite and access-widener validation.
- Isolated copy of the supplied modpack, including Punchy 3.2b, Better Combat and its installed resource packs.
- Actual Attack/Use key events: firing once, held-use redraw/reload, cancellation, retained loaded ammunition and aiming zoom.
- Server damage fixtures: impact-speed independence, partial draw, elemental scaling, projectile-speed bonuses, weapon upgrades, RPG criticals, NBT persistence and unchanged unmarked arrows.
- Server rejection fixtures: insufficient draw, wrong selected slot, missing ammunition and transferring a draw to a different bow.

Native probe sources and logs are retained under `build/ranged-*`; they are development fixtures and are not packaged in the mod.
