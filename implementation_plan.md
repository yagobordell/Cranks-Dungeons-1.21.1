# Rendering Fixes Implementation Plan

## Goal Description
Fix three major client-side rendering bugs introduced by the downgrade to Minecraft 1.21.1:
1. Missing health/shield bars above entities.
2. The Compendium screen menu background being blurred.
3. The World and Skill tree rendering incorrectly (blank or corrupted).

## Proposed Changes

### Entity Health Bars
The issue is that in 1.21.1, the `VertexConsumer` interface requires a `.next()` call to finalize each vertex. In 1.21.2+, this was removed. The downgrade left out the `.next()` calls, causing the vertices to be discarded.

#### [MODIFY] EntityStatBarRenderer.java
- Append `.next()` to every `buffer.vertex(...)` chain inside the `drawBar` method.

#### [MODIFY] LightningArcRenderer.java
- Append `.next()` to `pos()`'s return value.

#### [MODIFY] AilmentIconRenderer.java
- Append `.next()` to `buffer.vertex(...)` chains.

### Compendium Menu Blur
In 1.21, Minecraft automatically applies a blur post-processing effect to screens unless explicitly bypassed. The `CompendiumScreen` currently overrides `render()` but not `renderBackground()`. By default, the vanilla `render()` might call `renderBackground()`, or opening a screen automatically applies blur. 

#### [MODIFY] CompendiumScreen.java
- Override `renderBackground(DrawContext context, int mouseX, int mouseY, float delta)` and do nothing (or just draw the dimming overlay there), preventing the default Minecraft screen blur logic from applying.

### Skill Tree Rendering (Compendium)
In 1.21.11, `drawGuiTexture` was updated to include a `color` parameter. However, in 1.21.1, the signature with 6 parameters is `(Identifier texture, int x, int y, int z, int width, int height)`. 
The current code in `PassiveTreeTabRenderer` and `WorldTreeTabRenderer` passes the color integer as the last argument, which 1.21.1 interprets as `height`. Since the color is negative (e.g., `0xFFFFFFFF`), the height is negative, and nothing renders.

#### [MODIFY] PassiveTreeTabRenderer.java
- Update `drawPrecisionTexture` to tint using `RenderSystem.setShaderColor` and use the 4-argument `drawGuiTexture`.

#### [MODIFY] WorldTreeTabRenderer.java
- Update `drawPrecisionTexture` identically.

## Verification Plan
- Compile the game with `./gradlew compileJava`.
- The user will launch the game and verify the visual fixes.
