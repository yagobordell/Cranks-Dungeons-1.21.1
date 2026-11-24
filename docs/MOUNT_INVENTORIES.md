# Mount inventories

Supported horses, donkeys, mules, camels, llamas, skeleton horses, pigs and
striders now open the shared player loadout. Existing interaction gestures and
the inventory key are retained. Chest boats and minecarts continue to use the
storage-container panel.

The mount panel uses the existing dark pixel frame and ornaments. Equipment
shows a live preview and the applicable saddle, armor/carpet and cargo-chest
slots. Saddle and body slots have monochrome hints; the mount attachment slot
is deliberately empty and has a generic tooltip. Cargo uses only the real
available capacity; large llama packs scroll with the wheel or a draggable
scrollbar. Offscreen cargo slots cannot be clicked. Narrow screens have a
Mount/Inventory toggle.

Stats, Genetics and Breeding tabs keep the existing mount information and
controls. Horse descriptions and pairing calculations are shared with the
legacy fallback screen rather than duplicated. Training milestones, gene
grades, traits, imprinting/cooldowns and nearby horse pairing previews remain
available. Existing focus, access-mode and skeleton-specialization actions use
server validation against the currently open mount. Release ownership and
permanent specialization choices require a second click to confirm.

The handler reuses native horse saddle/body slots, preserving armor eligibility
and equipment behavior, and uses the existing custom cargo and llama-pack
inventories. Player bags, both armor sets, offhand and all ten hotbar slots stay
accessible. Shift-click into the player merges compatible stacks everywhere
first, then fills hotbar, regular inventory and unlocked bags. Shift-click
toward the mount tries valid empty equipment slots first, then available cargo.
Attachment removal requires that mount's physical cargo to be empty. An attempted
removal highlights occupied slots and shows an explanation; other llamas' cargo
does not block removal of an empty attachment. Server checks also cover shift-click,
number-key swaps and direct removal requests. Death still drops cargo normally.

Ownership, party/public access, distance, mount life and pack-membership checks
remain enforced. Riding and storage permissions have independent Owner/Party/Everyone
settings. Existing riding permissions are preserved on old saves; storage starts
owner-only. Shared storage never grants permission to alter equipment, permissions,
breeding focus or specialization. Revoking riding access ejects unauthorized riders.
Shared llama capacity updates as chests are added or removed;
a change to pack membership invalidates the open view.

Pack membership is shared for a world tick, with fresh checks at interaction
boundaries and invalidation on party changes. Slot mappings are reused until
members, attachments, strengths or backing inventories change. Old handlers never
reinterpret their indices as a different pack. The cache holds weak entity references.

Sprint requests reset when a rider leaves or changes. The client resends held
input when switching mounts. Horse combat training accepts monsters and actively
hostile non-passive mobs; passive animals and players do not award combat XP.

Training pages explain XP, level caps, per-level bonuses and species trait effects.
Pigs have Equipment/Training, mules add Genetics and explain sterility, and skeleton
horses have a dedicated Soul Bond page with specialization effects. Breedable species
have nearby-partner selection, compatibility/cooldown/attempt/lineage checks and
offspring gene previews. Horse/donkey hybrids preview their actual mule formulas.
Same-species non-horse ranges are explicitly shown before mutations, not as guarantees.
All previews use synchronized client data and do not alter server breeding rules.

## Breeding, training and ownership updates

Breedable mounts have a third independent Owner/Party/Everyone permission, exposed
on the Breeding page. Existing saves default to Owner. Starting love mode and the
birth both validate the feeding players against each parent's current grant. A
guest can still heal an injured mount with its ordinary food. Riding or storage
access does not grant breeding permission. Pigs retain their vanilla breeding.

Pedigrees retain at most sixteen ancestors, ordered by generation and interleaved
between both parents. Direct parents are always retained. Ancestor depths persist
alongside the existing UUID list; old saves keep their known relatives with a
conservative depth fallback. Relatives already pruned from old saves cannot be
reconstructed. Pairing previews distinguish compatible bloodlines from readiness,
including permission, healing, riding, native cooldown and food requirements.

The Genetics page opens a styled breeding-focus menu with mouse and keyboard
selection. It favors genes using the existing inheritance rules; personality
traits remain chance-based. Foal care shows availability, the required Sugar,
the breeder, progress and actual imprint bonuses. Imprint remains breeder-bound
after transfers; a separate bond for new owners is future work.

Owners can offer a mount to a nearby player using Transfer. The recipient must
accept a short-lived, single-use offer; current menus are not interrupted. Both
players must remain near the living mount in the same world, and the sender must
still own it at acceptance. Genes, equipment, physical cargo, training, breeding
attempts and imprint data are preserved. All three access modes reset to Owner;
unauthorized riders are ejected and llama membership caches invalidated.

Travel XP requires active riding input and a plausible per-tick displacement;
passive transport and large teleport jumps do not count. Llama caravans require
a moving player at the leash head, sampled from actual server positions once per
world tick. Unled wandering does not train llamas. Horse jump XP is awarded on
landing after a rider-requested jump travels at least two horizontal blocks;
stationary hops do not award XP.

The build and pedigree regression passed. An isolated copy of the user's complete
testing modpack exercised breeding authorization/revocation and healing, NBT and
packet round-trips, active/passive/jump/teleport training, and accepted ownership
transfers for all eight species with cargo/progression conservation and replay
protection. Client checks covered gene selection, independent breeding grants,
readiness states, imprint text, transfer menus and deferred recipient consent.
Actual walking input also verified caravan XP. Results are in
`build/mount-breeding-polish-build.log` and
`build/mount-updates-modpack-integration.log`. Transaction tests used a server-side
recipient fixture; this was not a two-client dedicated-server playtest.

## Whistle commands and dismount safety

Hold the rebindable Mount Whistle key (V by default) for the shared-style command
wheel. The server lists up to the nearest 64 living, owned mounts within 48 blocks.
Selection uses UUIDs and highlights the chosen animal locally without changing its
tracked glowing flag. Release the key over a command or click a sector. Escape or
releasing without selecting cancels; mouse movement must arm the wheel so merely
opening and closing it cannot accidentally issue a command. The list scrolls and
arrow keys/Enter also support selection.

The wheel uses a cached, symmetrical pixel circle with constant-width dividers.
Its rendered sectors and mouse hit regions share the same raster mask. Each
accepted command broadcasts a distinct mono whistle from the caller's position
in the Players sound category, audible to players within 48 blocks with distance
attenuation. Each command has its own subtitle. Audio provenance and processing
are documented in `docs/MOUNT_WHISTLE_AUDIO.md`.

The polish probe passed with the enabled modpack/resource packs: all three sound
events reached simulated connected listeners at 10 and 47 blocks and excluded a
listener at 55 blocks. Client sound definitions and Ogg assets loaded; the wheel
displayed and released a Follow command successfully. Geometry regression checks
also cover symmetry, divider dead zones and hit bounds at three sizes. Logs and
an in-game preview are in `build/mount-whistle-polish/`.

Come snapshots the owner's position and becomes Stay on arrival. Follow uses the
owner's current position; it pauses while the owner is offline, in another world,
or beyond 96 blocks. Stay holds a local parking area while retaining higher-priority
swimming, panic and breeding AI. Riding and leashes suspend orders; a Stay parking
anchor follows the animal while ridden or led. Seated camels are asked to stand
for movement commands only when there is enough headroom. Explicit llama orders
take precedence over automatic caravan joining while riders/leashes retain priority.

Orders use native navigation, never animal teleportation. Commands validate current
ownership, life, range, rider/leash/vehicle state and a reachable path on the server.
Sharing riding/storage/breeding grants does not grant command authority. A separate
`CranksMountOrder` NBT compound retains intent, issuer, destination and dimension;
paths and running goals are recreated. Old saves default to free roaming. Changing
owner or dimension clears an obsolete order. Request and command rates are bounded.

Name tags validate owner-only editing both before opening the naming screen and
again on submission. Unowned animals and non-mount naming retain their normal
behavior; creative administrators may rename mounts. Rejected submissions do not
consume name tags, and custom-owned species can still be claimed through naming.

Dismounting retains a valid native landing only when its normal on-foot body box,
footing, hazards and access from the mount are safe. Otherwise a bounded three-block
local search tries standing/crouching dry land, then water with the rider's head
above the surface. Collision checks deliberately use on-foot shapes, so mounted
leaf phasing does not validate a blocked landing. Clearance rays retain solid-wall
obstructions. The search avoids damaging blocks and unsupported cliff edges and
prefers a dry shore. If no safe nearby landing exists it warns and keeps the native
fallback rather than trapping the player or teleporting them across the world.

The focused probe passed in an isolated copy of the user's modpack with its enabled
animation packs: all eight species moved for Come/Follow, held Stay, and passed
ownership, persistence, caravan precedence, wheel submission and dismount checks.
The seated camel also stood and followed. Focus pausing was disabled in the test
copy only. Results are recorded in `build/mount-whistle-build.log` and
`build/mount-whistle-modpack-integration.log`. The test source is
`build/alchemy-smoke/test/AlchemySmoke.java`; it resolves live entities again by UUID
after chunk loading instead of asserting against discarded/reloaded instances.

## Riding and terrain handling

Steerable mounts share a hold-Forward controller with gradual acceleration and
release braking. Back first brakes to zero before entering a bounded reverse pace.
Species profiles set acceleration, braking and speed-sensitive turning. Camera
direction does not steer the animal; air turning is limited. Pigs and striders now
read rider Forward/Back input instead of vanilla's unconditional forward vector.

Horse steering responds faster, with tighter turns at rest and approximately twice
the previous turning rate at speed. Steering uses actual horizontal speed rather
than throttle, allowing a horse pushing against an obstacle to turn freely.

Leaf collision shapes let ridden, tamed or saddled supported mounts and their riders
brush through side/overhead foliage. Leaf blocks below the mount's feet retain
collision for treetop footing. Wild, unsaddled mounts and players on foot keep native
collision; logs and solid ceilings still block movement. This fixes one-block
step-ups under a leaf canopy without increasing step height or bypassing stone.

Normal ground movement is intercepted at both `travel` and `applyMovementInput`.
Collision-resolved movement uses short sections and follows nearby descending
collision shapes up to 1.05 blocks. Forward probes follow successive terrain steps
rather than a flat horizon. Two-block ledges retain forward travel and use native
gravity rather than snapping down. Larger unsupported drops reduce forward input
to a cautious pace; holding Forward can still carry the mount over the edge.
There is no artificial collision wall or fall-damage exemption. Explicit jump/dash
intent bypasses approach caution; actual
falls, fluids, climbing, flight and upward impulses retain native movement.
The old airborne full-speed restoration and ground turning-drift mixins are removed.
Horse takeoff impulses remain capped to prevent repeated-jump speed stacking.

Smooth Mount Camera is an optional setting. It eases only small vertical viewpoint
changes during riding/mount transitions, does not alter player position or yaw, and
checks the offset against visual block collision. The Compendium explains controls,
terrain assistance and the deliberate-jump override.

Mounted third-person camera rays ignore leaf shapes independently of the smooth
camera setting, including the smoothing offset's obstruction check. Both rear and
front views retain their normal distance through foliage, while rays continue past
leaves to detect solid walls or trunks. This only affects camera queries for riders
of supported mounts; on-foot camera clipping and other raycasts remain native.
The isolated client check confirmed four-block distance in open space and inside
leaves in both views, identical solid-wall clipping with/without intervening leaves,
and normal leaf clipping after dismounting. Build and integration results are in
`build/mount-camera-foliage-build.log` and `build/mount-camera-foliage-integration.log`.

Original verification before the slow-only cliff update: build plus input-state
regression checks passed. The isolated collision
course exercised seven steerable species on full blocks, bottom slabs and stairs,
plus cliff stops, deliberate override and wall collision. A real client-input run
with vehicle packets passed downhill ground contact, release braking, cliff stopping
and an actual jump beyond the stop. Results are saved in
`build/mount-locomotion-build.log` and `build/mount-locomotion-integration.log`.

The descent follow-up passed with the user's modpack and animation packs: seven
rideable species crossed two-block and ten-block ledges under gravity, retained
full-block/slab/stair descent handling, slowed without stopping at unsupported
edges, and respected jump intent and solid walls. A horse took damage from the
larger fall. Real client input and vehicle packets also passed a two-block descent
without jumping, downhill ground contact and release braking. Current logs are
`build/mount-descent-build.log` and `build/mount-descent-integration.log`.

A separate baseline reproduced failed canopy step-ups with both vanilla movement
and the terrain helper. The foliage/steering follow-up passed oak, spruce and
azalea canopy step-ups, leaf-wall traversal for seven ridden species, rider/mount
collision agreement, dismount escape, wild/on-foot isolation, leaf-top support,
solid log/stone ceilings and turning response. A real client-input course also
passed canopy climbing and steering with vehicle packets. Results are saved in
`build/mount-leaf-baseline.log`, `build/mount-leaves-steering-build.log` and
`build/mount-leaves-steering-integration.log`.

## Visible mount attachments

Horse, skeleton horse, pig, camel and strider panniers use the live torso model
transform rather than an offset from the whole entity. Pig panniers sit against
the flanks instead of above/inside the back. Each species has its own width, height,
rear position and size. The native donkey/mule/llama chests remain unchanged.

The renderer follows torso position, rotation and scale, including the root parent
on camel/strider models. The pig body's 90-degree rest rotation is removed from the
attachment frame without removing animation. Panniers retain the vanilla donkey
texture. A registered feature and a guarded fallback ensure they draw only once.

Animation packs that modify these existing body transforms can carry the panniers
with the body. Arbitrary replacement rigs or substantially different animal shapes
are not guaranteed to fit and may need a pack-specific adapter. No particular
third-party animation pack was included in the original vanilla-rig check; the
subsequent EMF modpack verification is described below.

The subsequent modpack check reproduced displaced horse panniers, sideways pig
panniers and strider attachments following an empty placeholder. With Entity Model
Features, the optional adapter now locates the visible torso mesh and follows its
full animated parent chain, fits its actual flank width and normalizes the pig
mesh's local orientation. Variant children are inspected live; there is no hard
runtime link to EMF when it is absent. Native donkey/mule/llama chests are preserved.
The tested copy includes Fresh Animations, FA All Extensions and Fluid & Foolish,
with the user's mod/config/resource-pack set. Shaders were disabled in that copy
for these geometry checks; the original profile's settings/worlds were not changed.
The final mesh/frame assertions passed for horse, skeleton horse, pig and strider,
including flank contact, upright orientation and parent-root translation. Camel
body-transform checks also passed, and donkey/mule/llama gained no duplicate custom
chests. Results are saved in `build/mount-chests-modpack-integration.log`.

The attachment probe checked exactly two custom cuboids, torso translation,
90-degree rotation, uniform scaling, parent-root translation and hidden-body
visibility on all five custom-rendered species. Native donkey/mule/llama models
received no duplicate custom chests. Both flanks and live horse rearing/camel
sitting were inspected. Results are in `build/mount-body-attachments-integration.log`.

The old llama mapping skipped inventory index 1, assuming the carpet was stored
there. In 1.21.1 the carpet uses BODY equipment. Cargo now starts at index 1,
including empty checks and chest-removal returns. A strength-five llama exposes
all fifteen intended slots. Saved physical stacks are not moved or migrated.

## Verification

The build and isolated Minecraft checks passed for all eight supported species:
native/extended menu opening, saddle/armor/carpet restrictions, live genetic and
pack capacities, chest removal with cargo conservation, transfer priorities,
occupied-bag protection, cursor return, owner/public access and distance/removal
checks. Actual client/server controls were exercised for focus, access modes,
skeleton specialization, confirmed ownership release, armor-set swapping and the
tenth hotbar slot. UI checks covered all information pages, compact switching
and reaching/transferring the last slot of a sixty-slot llama pack. Rendered
frames were inspected and the large-pack controls checked directly in pixel
readbacks. These integrated-server checks are not a multi-client dedicated-server
playtest.

The mount-polish regression also passed adult/owner attachment restrictions,
independent riding/storage grants and revocation, old-save defaults, permission NBT
and packet round-trips, dismount sprint resets, blocked occupied-attachment removal,
passive-versus-enemy combat XP and pack-cache invalidation. Client checks exercised
the independent storage button, occupied-slot warning, horse/donkey hybrid and
donkey/camel/llama/strider pairing previews, training descriptions, pig/mule tabs,
and the skeleton horse's confirmed Soul Bond choice. The isolated probe read 30,000
cached pack slots in approximately 45 ms; this is a focused probe rather than a
full multiplayer performance benchmark. Build output and the integration log are
saved under `build/mount-system-build.log` and `build/mount-polish-integration.log`.
