# Menu theme and creative inventory

The dark menu theme is included in the mod's normal client resources. No separate
pack installation or server download is needed. Player-selected resource packs
can override these textures through Minecraft's usual resource-pack priority.

The theme covers vanilla buttons, sliders, text fields, tabs, checkboxes, lock
controls, selection frames, world-selection and multiplayer navigation arrows,
and menu backgrounds. It preserves the panorama,
fonts, world textures and immersive book/sign/map artwork, including book arrows.

Run `python scripts/generate_menu_theme.py` to regenerate the original pixel
sprites and the optional standalone `build/distributions/Cranks-Dungeons-Dark-UI-1.21.1.zip`.
The ZIP targets Minecraft 1.21.1 (resource-pack format 34) and contains no gameplay
changes. It is useful for sharing the appearance independently of the mod; players
using the mod already have its textures.

Creative mode uses the same player inventory, stats, equipment, bags and ten-slot
hotbar as Survival. A creative catalog occupies the right station dock, with
paged categories, global item-name/identifier search and `#tag` search. Narrow
windows use the existing inventory/dock toggle. Click takes one item, right-click
reduces an existing cursor stack, middle-click takes a full stack, and Shift-click
inserts a stack into the inventory. Number keys assign the hovered item to a
hotbar slot, including `0` for the tenth slot. The discard control clears only the
held cursor stack. Item creation is validated on the server and requires Creative
mode and the personal inventory handler; station handlers cannot accept it.

The Saved Hotbars category offers nine numbered presets, each with a ten-slot
preview and Save/Load buttons. Counts, item components and empty slots are
preserved. Presets live in `cranks-dungeons-hotbars.dat` in the game profile;
existing vanilla presets are imported without changing their original file.
Minecraft's Save/Load Hotbar modifier shortcuts use the same presets. Loading
replaces only the hotbar, requires an empty cursor, and is validated atomically
against current inventory contents by the server.

Category, search text, scroll position and category page are remembered while
connected to the same world/server session, then cleared on disconnect.

In HUD Layout, click an element to select it. Arrow keys move it one pixel and
Shift+Arrow moves it five pixels, independent of mouse grid snapping. Selection
survives releasing the mouse and resizing the editor. Keyboard placement uses
the same bounds and saved coordinates as dragging.

The default Compendium key is **J**. Existing custom bindings remain under the
player's control in Minecraft's Controls menu.

## Viewport fitting

The shared inventory reserves a small vertical margin at the minimum GUI height. Compact station return controls follow the station panel header, including taller altar panels. Mount previews shrink on short viewports; cargo rows and their scrollbar remain above access and ownership controls. Text entered into the trade coin field survives a resize.

Party cards and their HUD Layout preview share `PartyHudLayout`. The configured scale is retained; the displayed grid shrinks only when needed to fit the current viewport, and its position is clamped consistently. Groups larger than the configured grid gain rows so members remain visible. Resetting the vertical party position supports the same unset value as its horizontal position.

Generic text tooltips wrap to the current GUI width before component conversion. Formatting and contextual guide links are preserved; equipment cards retain their own width and scrolling behavior.

The UI audit helpers and screenshots live under `build/ui-consistency-*` and the isolated modpack's `ui-consistency-gallery`. They cover real window resizing, GUI scale 1–4 and Auto, narrow/tall/ultrawide windows, minimum 320×240 GUI space, compact and docked views, settings/HUD scales, class confirmation, long names and resize draft preservation. The multiplayer checklist is in `docs/MULTIPLAYER_TEST_CHECKLIST.md`; it remains a separate manual test plan.
