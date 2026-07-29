import os
import re
import glob

files = glob.glob("src/main/java/com/cranks/dungeons/mixin/**/*.java", recursive=True)
for filepath in files:
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    original = content

    if "CriticalStrikeMixin.java" in filepath:
        content = re.sub(
            r'ServerWorld world = \(ServerWorld\) target\.getWorld\(\);\s*',
            r'',
            content
        )
        content = re.sub(
            r'spawnCriticalEffects\(world, target, attacker\);',
            r'if (!target.getWorld().isClient() && target.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {\n                    spawnCriticalEffects(serverWorld, target, attacker);\n                }',
            content
        )
    
    elif "DamageReductionMixin.java" in filepath:
        content = content.replace(
            "ServerWorld world = (ServerWorld) entity.getWorld();",
            "net.minecraft.world.World world = entity.getWorld();"
        )

    elif "DrowningDamageMixin.java" in filepath:
        content = content.replace(
            "broadcastDrownNumbersToNearbyPlayers((ServerWorld) entity.getEntityWorld(), entity, cranks_dungeons$pendingDrownDamage);",
            "if (!entity.getWorld().isClient() && entity.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {\n                broadcastDrownNumbersToNearbyPlayers(serverWorld, entity, cranks_dungeons$pendingDrownDamage);\n            }"
        )

    elif "ElementalDamageMixin.java" in filepath:
        content = content.replace(
            "ServerWorld world = (ServerWorld) target.getWorld();",
            ""
        )
        content = content.replace(
            "handleLivingAttacker(world, source, actualAmount, attacker, target);",
            "if (!target.getWorld().isClient() && target.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {\n                handleLivingAttacker(serverWorld, source, actualAmount, attacker, target);\n            }"
        )
        content = content.replace(
            "handleNonPlayerDamage(world, source, actualAmount, target);",
            "if (!target.getWorld().isClient() && target.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {\n                handleNonPlayerDamage(serverWorld, source, actualAmount, target);\n            }"
        )

    elif "EnergyShieldDamageMixin.java" in filepath:
        content = content.replace(
            "ServerWorld world = (ServerWorld) self.getEntityWorld();",
            "net.minecraft.world.World world = self.getEntityWorld();"
        )

    elif "EnergyShieldRegenMixin.java" in filepath:
        content = content.replace(
            "ServerWorld world = (ServerWorld) self.getEntityWorld();",
            "net.minecraft.world.World world = self.getEntityWorld();"
        )

    elif "EvasionMixin.java" in filepath:
        content = content.replace(
            "ServerWorld world = (ServerWorld) entity.getWorld();",
            "net.minecraft.world.World world = entity.getWorld();"
        )

    elif "LivingEntityMixin.java" in filepath:
        content = content.replace(
            "DamageNumberSender.broadcastHeal((ServerWorld) entity.getEntityWorld(), entity,",
            "if (!entity.getWorld().isClient() && entity.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {\n                    DamageNumberSender.broadcastHeal(serverWorld, entity,"
        )
        content = content.replace(
            "healAmount);",
            "healAmount);\n                }"
        )

    elif "ShieldBlockMixin.java" in filepath:
        content = content.replace(
            "ServerWorld world = (ServerWorld) entity.getEntityWorld();",
            "net.minecraft.world.World world = entity.getWorld();"
        )

    if content != original:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Updated {filepath}")

print("Done")
