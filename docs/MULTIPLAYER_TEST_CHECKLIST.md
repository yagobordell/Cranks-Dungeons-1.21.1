# Multiplayer test checklist

Run these checks on a dedicated Fabric 1.21.1 server using the same mod and content configuration as the clients. Start with two players, then repeat the shared-combat and station checks with approximately ten players. These boxes record tests still to run; the local UI audit is separate.

## Setup and recording

- [ ] Use two different player accounts. Keep one experienced character and one fresh character so discovery and progression cannot accidentally pass through shared knowledge.
- [ ] Record the mod JAR version or SHA256, server settings, client modpacks, resource packs and relevant gamerules. Use the normal survival death rules for the death tests.
- [ ] Give each player identifiable equipment and exact item and coin totals. Record both players' inventory sections, wallets, mount cargo and station contents before each transfer test.
- [ ] Run once at ordinary latency and repeat transaction tests with noticeable latency. Record steps, expected result, actual result and both client/server logs for any failure.

## Inventory and death

- [ ] Pick up items with partially filled matching stacks in each inventory section. Matching stacks should fill first; new stacks should use hotbar, normal inventory, then bag storage.
- [ ] Equip five Bundles and verify each unlocks only its own six storage slots. The bags themselves must not store contents as vanilla Bundles.
- [ ] Hover an equipped bag and verify all of its unlocked slots highlight. Attempt to remove a filled bag by clicking, Shift-clicking, swapping hotbar slots, dragging and dropping. Removal must remain blocked until its contents are emptied, with the relevant slots highlighted and an explanation shown.
- [ ] Swap armour sets while another player observes. Only the armour swaps; active stats and the visible equipment must agree on both clients. Reconnect and repeat.
- [ ] Die with uniquely identifiable items in every section. Keep the hotbar, normal inventory, equipment and equipped bags; lose the contents of bag storage. Check the death drops and inventories for duplicated or missing items.
- [ ] Repeat death after disconnect/reconnect and a server restart. Verify wallet and both progression tracks persist according to their current rules.

## Coins and player trading

- [ ] Kill an eligible natural hostile mob together. The party must split one fixed coin total. Test different participation amounts and compare the combined award with a solo reward.
- [ ] Test spawner mobs, passive animals and fully automated kills. These must not create combat XP or coin rewards. Recovery-on-kill behavior is a separate mechanic and may still apply to animals.
- [ ] Open a generated treasure chest, close it, reopen it and restart the server. Its coin reward must not repeat for that player. Test both players opening it nearly simultaneously: each may claim their own one-time coin reward, and neither may claim it again. Personal generated-chest item loot is planned for later; do not expect separate item contents for every player yet.
- [ ] Place an ordinary player chest, fill it, break it and place it again. It must not acquire a generated-treasure coin reward.
- [ ] Initiate trading face to face. Check acceptance, refusal, timeout, leaving range, blocked line of sight and changing dimensions.
- [ ] Complete an item-only trade, a coin-only trade and a combined trade. Verify exact before/after totals, item components, durability, runes and enchantments.
- [ ] Mark both offers ready, then change an item or coin amount. The changed offer must require renewed approval before completion.
- [ ] Try insufficient coins, a full destination inventory, a held cursor item, rapidly repeated clicks and changing an offered stack elsewhere. The server must either complete the whole valid transaction once or retain the original ownership; no partial payment or duplication.
- [ ] Disconnect, die or stop the server while a trade is pending or both players are confirming. Reconnect and verify conservation of all offered items and coins.

## Stations and shared containers

- [ ] Have both players use each shared station or container at once. Move or consume a required material from another interface while the first player has a result selected. Stale requests must not produce free output or charge twice.
- [ ] Craft batches larger than one stack with materials split across hotbar, normal inventory and bags. Check exact material consumption, resulting stack sizes and behavior when space runs out.
- [ ] Test recipe search, material search, Craftable, category filters and quantity entry independently on the two clients. One player's selection must not change the other's personal controls.
- [ ] Test recycling and repairing damaged and enchanted equipment, including the confirmation and Shift-click paths. Confirm the displayed return/cost matches the actual transaction and item properties are preserved where appropriate.
- [ ] Use the Upgrade Bench from both clients. Check costs, quality limits, enchantment-slot caps, rejected requests and repeated-click handling.
- [ ] Enchant the same gear in succession. Only applicable enchantments should appear; repeated stat enchantments should stack, utility enchantments should remain single-copy, and removal/replacement should consume exactly the displayed cost.
- [ ] Test Runic Altar additions, removals and replacements, including specialized tomes. Verify the gear's T10/T9/T8/T7 material cap and Prefix/Suffix limits. A hybrid rune must occupy one affix slot.
- [ ] Generate a paid rune comparison, close the screen, reconnect and restart the server. Accept or keep the old result later. The tome must be charged once per attempt, and keeping the old result must not refund that attempt.
- [ ] Test Artificial Runic Altar reservation and reveal with both players watching. Verify the chosen result, item persistence and nearby application sound.
- [ ] Open chests, barrels, shulkers, hoppers and droppers together. Sort while another player moves items or a hopper transfers them. Totals and components must remain unchanged by sorting.
- [ ] Program a Crafter with a known recipe, then let an unfamiliar player operate it after its programmer logs out. Check one craft per redstone pulse, insufficient-material handling and hopper input/output.
- [ ] Verify heat-station exclusivity and cooking: the Smoker and Campfire should support their intended foods, and the Foundry's current salvage role should not acquire unrelated vanilla recipes.

## Personal discoveries and wiki

- [ ] Let player A discover every ingredient of a recipe while player B lacks one ingredient. Only A should see that recipe. Obtain B's missing ingredient and verify B unlocks it independently.
- [ ] Let A learn a potion formula through experimentation. Giving B ingredients or a finished potion must not count as B performing the successful experiment.
- [ ] Open recipe links and potion links in the Compendium on both accounts. Public item/creature information may be shared, but unknown personal recipes must not reveal ingredients or bypass discovery checks.
- [ ] Log out and use the other account on the same client installation. Verify cached search and wiki content do not carry over private recipe knowledge.
- [ ] Verify supported potions, arrows and enchantments remain enforced through loot, trading, containers and restored items. Check drinking/splash/lingering behavior, stacking to 16 and ordinary bottle compatibility.

## Combat and progression

- [ ] Compare solo combat XP with shared combat XP: the party should receive a higher combined total while each individual receives less than a solo kill. Test participation distance, deaths and disconnects before the final hit.
- [ ] Confirm active player combat is required and spawner mobs award no combat XP. Passive animals, crafting and repeatable placed-block breaking must not become combat XP sources.
- [ ] Visit a new location, discover a resource and learn a recipe independently on both players. Check World XP rewards once per intended personal discovery, including after restart.
- [ ] Allocate Passive and World Tree nodes independently. Verify point spending, attribute choices, immediate stat changes, reset behavior and persistence. One player's edits or allocations must not change the other's character.
- [ ] Test melee, bow and crossbow hits with multiple damage types and criticals. Check observer damage numbers and health/shield updates, including shots across chunk boundaries and against moving targets.
- [ ] Apply every ailment and hit an Energy Shield target. Check durations, stack counts, Bleed burst, Poison, Burn buildup, Shock targeting, Freeze and Electrocute control, attribution and visible effects on both clients.
- [ ] Repeat with pets, party members, neutral creatures and PvP enabled/disabled. Targeting and damage must respect the server's applicable friendly-fire rules.

## Mount ownership and riding

- [ ] Tame and name a mount. Only the taming player should receive its free first-name prompt; skipping it must not leave a reusable free naming opportunity. Camels, pigs and striders retain their current name-tag claiming method.
- [ ] Try another player's mount equipment, attachment and cargo. Verify ownership permissions, blocked attachment removal with occupied cargo, and all cargo/gear persistence after restart.
- [ ] Transfer ownership with acceptance, refusal and timeout. Verify gear, cargo, genes and training stay with the mount, permissions update on both clients and breeder imprint retains its intended owner relationship.
- [ ] Use Come, Follow and Stay. Check ownership, command distance, unloaded chunks, reconnect behavior, each whistle sound and whether nearby players hear it.
- [ ] Ride while another player watches: acceleration, braking, turning, leaves, one-block steps, two-block descents and deliberate jumps. Check rider/mount position synchronization and third-person leaf camera behavior.
- [ ] Check chest/attachment placement with the animation resource packs enabled on one client and disabled on the other. Attachments must follow the animal body rather than rotate independently.
- [ ] Breed mounts with different selected breeding focuses. Verify parent attribution, inherited genes, mutations, cooldowns, offspring ownership and lineage after restart.

## UI settings and group load

- [ ] Give the two clients different resolutions, GUI scales and HUD layouts. Keep inventory, station, mount, trade and Compendium screens usable independently; client layout settings must not change another player's UI.
- [ ] Type into search, names and quantity/coin fields with InvMove enabled. Typing must not move the player. Resize the window while a draft is entered and check it is retained without submitting it.
- [ ] Open contextual help while holding an item, using a station or drafting tree changes. Closing help must return to the same transaction and tree state; linked clicks must not move underlying items.
- [ ] Test a ten-player fight with many active effects, party cards, nameplates, damage numbers and mount commands. Check client frame times, server tick times, delayed updates and log errors. Repeat opening different stations simultaneously.

## Failure record

For each failure, record the test, both player names, location/dimension, exact reproduction steps, before/after item and coin totals, and whether reconnect or restart changes the result. Attach client and server logs from the same attempt. Leave failed boxes unchecked until the corrected build passes the same reproduction.
