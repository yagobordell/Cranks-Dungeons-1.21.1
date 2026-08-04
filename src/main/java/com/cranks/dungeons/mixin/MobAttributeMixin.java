package com.cranks.dungeons.mixin;

/**
 * Previously injected custom elemental resistances into specific mobs (Blaze, Snow Golem, etc.).
 * These stat overrides have been removed — mobs no longer have elemental resistances baked in
 * by default. Players can still gain resistances via runes/enchantments.
 *
 * The inner @Mixin classes were removed (Item 2 of the To Do list).
 * The cranks-dungeons.mixins.json entries for MobAttributeMixin$* have also been removed.
 */
public class MobAttributeMixin {
    // No inner mixin classes — all mob-specific resistance overrides were intentionally removed.
}
