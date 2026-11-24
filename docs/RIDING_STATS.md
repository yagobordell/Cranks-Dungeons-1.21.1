# World Tree riding stats

These stats are available in the World Tree editor for ordinary modifiers and
modifier-choice nodes. No existing node or connection is changed by this update.
Values are additive fractional percentages: `0.1` means 10%, `0.375` means 37.5%.
Editor values are not restricted to the earlier suggested balance targets.

| Attribute ID (`cranks-dungeons:` prefix) | Effect |
| --- | --- |
| `riding_speed` | Multiplies controlled riding speed by `1 + value`. |
| `mount_endurance` | Multiplies mounted sprint resource drain by `1 - value`. |
| `mount_recovery` | Multiplies positive mounted resource regeneration by `1 + value`. |
| `riding_agility` | Multiplies acceleration and braking response by `1 + value`. |
| `sure_footing` | Multiplies damage from falling to the ridden mount by `1 - value`. |

Only the living controlling player contributes a bonus. Passengers do not stack
bonuses, and dismounting or changing riders immediately changes the effective
values. The mount's attributes, genes, traits and training are never rewritten.
Player attributes are tracked for client movement prediction.

Speed, regeneration and responsiveness cannot become negative. Cost and damage
reductions stop at 100%; negative reductions can increase costs or damage.
All finite editor values can be saved, including negative values. Existing
Minecraft numeric limits still apply. Node bonuses add before the attribute's
physical bounds are applied, so a negative node can offset a positive node.

Endurance changes sustained sprint/ability drain on horses, donkeys, mules,
camels and striders. It does not discount jumps or camel dashes. Recovery keeps
the original recovery delay, ground/lava restrictions and resource maximum;
it does not amplify a cold strider's environmental heat loss. Animals without
a stamina/resource mechanic gain no new resource pool.

Sure Footing affects the mount's own fall damage. It preserves separate vanilla
passenger damage and combines multiplicatively with existing fall protection.
Agility does not change turning speed, jump height or terrain assistance.

Validation: `build/riding-stats-build.log` records the build and regression suite.
The native fixture is `build/riding-stats-live-probe.java`; final results are in
`build/riding-stats-native-results.log`. Checks passed for fractional, negative
and large normal-node values, modifier-choice stacking and tree JSON round trips;
native saddled speed, acceleration, braking and fall damage on horses, donkeys,
mules, skeleton horses, camels, pigs and striders; horse/donkey/mule/camel resource
ticks; recovery delays; unchanged jumps and camel dash costs; camel passenger
isolation and controlling-rider handoff; and immediate dismount cleanup. The
fixture restores player data and removes its temporary in-memory nodes.
