package com.cranks.dungeons;

import com.cranks.dungeons.party.Party;
import com.cranks.dungeons.util.CombatXpContext;
import com.cranks.dungeons.util.TreeUnlockPlanner;
import com.cranks.dungeons.horse.HorseBreedingFocus;
import com.cranks.dungeons.horse.HorseBreedingManager;
import com.cranks.dungeons.horse.HorseBreedingRules;
import com.cranks.dungeons.horse.HorseGeneticsData;
import com.cranks.dungeons.horse.HorseTrait;
import com.cranks.dungeons.mount.MountGeneticRatings;
import com.cranks.dungeons.mount.MountKind;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class RegressionTestSuite {
    private RegressionTestSuite() {}

    public static void main(String[] args) {
        combatXpContextRestoresNestedState();
        combatXpContextRestoresStateAfterFailure();
        partyLeadershipTransferKeepsMembershipConsistent();
        partyRejectsInvalidMembers();
        treeDraftPlansSelectionNodesAcrossNewConnections();
        treeDraftRejectsDisconnectedOrUnaffordableChanges();
        horseTrainingMilestonesChangeMobilityCosts();
        hardyTraitImprovesStaminaWithoutChangingOtherCosts();
        horseBreedingFocusParsingIsSafe();
        horseBreedingCatchUpRewardsWeakGenes();
        horseTrainingCurveAndActivityRewardsAreBalanced();
        mountGeneGradesReflectIndividualPotential();
        horsePersonalMutationRecordPersists();
    }

    private static void combatXpContextRestoresNestedState() {
        CombatXpContext.setCombatXp(false);
        CombatXpContext.runAsCombatXp(() -> {
            require(CombatXpContext.isCombatXp(), "combat XP context was not enabled");
            CombatXpContext.runAsCombatXp(() ->
                    require(CombatXpContext.isCombatXp(), "nested combat XP context was not enabled"));
            require(CombatXpContext.isCombatXp(), "nested context did not restore its parent state");
        });
        require(!CombatXpContext.isCombatXp(), "combat XP context leaked after completion");
    }

    private static void combatXpContextRestoresStateAfterFailure() {
        CombatXpContext.setCombatXp(false);
        try {
            CombatXpContext.runAsCombatXp(() -> {
                throw new ExpectedFailure();
            });
            throw new AssertionError("combat XP action did not propagate its failure");
        } catch (ExpectedFailure ignored) {
            require(!CombatXpContext.isCombatXp(), "combat XP context leaked after a failure");
        }
    }

    private static void partyLeadershipTransferKeepsMembershipConsistent() {
        UUID originalLeader = UUID.randomUUID();
        UUID newLeader = UUID.randomUUID();
        Party party = new Party(originalLeader);
        require(party.addMember(newLeader), "could not add the future leader");

        party.transferLeadership(newLeader);

        require(party.isLeader(newLeader), "leadership was not transferred");
        require(party.getMembers().equals(List.of(newLeader, originalLeader)),
                "new leader was not moved to the first membership slot");
        require(party.removeMember(originalLeader), "old leader could not leave after transfer");
        require(!party.isMember(originalLeader), "old leader remained in the party after leaving");
    }

    private static void partyRejectsInvalidMembers() {
        Party party = new Party(UUID.randomUUID());
        require(!party.addMember(null), "party accepted a null member");

        try {
            party.transferLeadership(UUID.randomUUID());
            throw new AssertionError("party accepted a non-member as leader");
        } catch (IllegalArgumentException ignored) {
            // Expected.
        }
    }

    private static void treeDraftPlansSelectionNodesAcrossNewConnections() {
        Map<String, Collection<String>> graph = Map.of(
                "start", List.of("normal"),
                "normal", List.of("start", "selection"),
                "selection", List.of("normal", "next"),
                "next", List.of("selection")
        );

        TreeUnlockPlanner.Result result = TreeUnlockPlanner.plan(
                Set.of("start"), List.of("selection", "normal", "next"), 3,
                id -> graph.getOrDefault(id, List.of()));

        require(result.successful(), "reachable draft containing a selection node was rejected");
        require(result.unlockOrder().indexOf("normal") < result.unlockOrder().indexOf("selection"),
                "selection node was planned before its connecting node");
        require(result.unlockOrder().indexOf("selection") < result.unlockOrder().indexOf("next"),
                "node beyond selection was planned before the selection node");
    }

    private static void treeDraftRejectsDisconnectedOrUnaffordableChanges() {
        TreeUnlockPlanner.Result disconnected = TreeUnlockPlanner.plan(
                Set.of("start"), List.of("isolated"), 1, id -> List.of());
        require(disconnected.failure() == TreeUnlockPlanner.Failure.DISCONNECTED,
                "disconnected draft was accepted");

        TreeUnlockPlanner.Result unaffordable = TreeUnlockPlanner.plan(
                Set.of("start"), List.of("one", "two"), 1, id -> List.of("start"));
        require(unaffordable.failure() == TreeUnlockPlanner.Failure.NOT_ENOUGH_POINTS,
                "unaffordable draft was accepted");
    }

    private static void horseTrainingMilestonesChangeMobilityCosts() {
        HorseGeneticsData data = new HorseGeneticsData();
        data.geneticEndurance = 100.0;
        data.level = 1;
        require(close(HorseBreedingManager.getMaxStamina(data), 100.0),
                "level-one horse stamina was incorrect");
        require(close(HorseBreedingManager.jumpStaminaCost(data), 8.0),
                "level-one jump cost was incorrect");
        require(close(HorseBreedingManager.gallopDrainPerSecond(data), 4.0),
                "level-one gallop drain was incorrect");

        data.level = 30;
        require(close(HorseBreedingManager.getMaxStamina(data), 143.5),
                "training stamina did not include all 29 trained levels");
        require(close(HorseBreedingManager.jumpStaminaCost(data), 6.0),
                "efficient-jumper milestone did not reduce jump cost");
        require(close(HorseBreedingManager.gallopDrainPerSecond(data), 3.0),
                "endurance-training milestone did not reduce gallop drain");
        require(close(HorseBreedingManager.gallopSpeedBonus(data), 0.20),
                "mastered-gallop milestone did not increase gallop speed");
    }

    private static void hardyTraitImprovesStaminaWithoutChangingOtherCosts() {
        HorseGeneticsData data = new HorseGeneticsData();
        data.geneticEndurance = 100.0;
        data.level = 1;
        data.trait = HorseTrait.HARDY;
        require(close(HorseBreedingManager.getMaxStamina(data), 115.0),
                "Hardy did not increase maximum stamina by 15 percent");
        require(close(HorseBreedingManager.mountedStaminaRegenPerSecond(data), 2.5),
                "Hardy did not increase mounted recovery by 25 percent");
        require(close(HorseBreedingManager.jumpStaminaCost(data), 8.0),
                "Hardy unexpectedly changed jump cost");

        data.trait = HorseTrait.SURE_FOOTED;
        require(close(HorseBreedingManager.jumpStaminaCost(data), 6.0),
                "Sure-Footed did not reduce jump cost");
    }

    private static void horseBreedingFocusParsingIsSafe() {
        require(HorseBreedingFocus.fromId("speed") == HorseBreedingFocus.SPEED,
                "lower-case breeding focus was not parsed");
        require(HorseBreedingFocus.fromId("not_a_focus") == HorseBreedingFocus.NONE,
                "invalid breeding focus did not fall back to None");
        require(HorseBreedingFocus.fromId(null) == HorseBreedingFocus.NONE,
                "null breeding focus did not fall back to None");
    }

    private static void horseBreedingCatchUpRewardsWeakGenes() {
        require(close(HorseBreedingRules.lineageImprovement(
                        HorseBreedingFocus.SPEED, HorseBreedingFocus.SPEED, 0.10), 0.03),
                "matching focus did not give a three-percent low-gene catch-up");
        require(close(HorseBreedingRules.lineageImprovement(
                        HorseBreedingFocus.SPEED, HorseBreedingFocus.NONE, 0.10), 0.0225),
                "single focus did not retain its proportional catch-up bonus");
        require(close(HorseBreedingRules.lineageImprovement(
                        HorseBreedingFocus.NONE, HorseBreedingFocus.NONE, 0.10), 0.015),
                "unfocused breeding did not retain its proportional catch-up bonus");
        require(close(HorseBreedingRules.lineageImprovement(
                        HorseBreedingFocus.SPEED, HorseBreedingFocus.SPEED, 0.90), 0.01),
                "near-maximum gene did not use the intended one-percent final push");
    }

    private static void horseTrainingCurveAndActivityRewardsAreBalanced() {
        require(HorseBreedingManager.experienceForNextLevel(1) == 90,
                "level-one horse training requirement was not reduced");
        require(HorseBreedingManager.experienceForNextLevel(10) == 470,
                "middle horse training requirement did not follow the gradual curve");
        require(HorseBreedingManager.experienceForNextLevel(20) == 1100,
                "horse training curve did not return to the original requirement at level 20");
        require(HorseBreedingRules.travelExperience(false) == 2
                        && HorseBreedingRules.travelExperience(true) == 4,
                "horse travel XP rewards were incorrect");
        require(HorseBreedingRules.longJumpExperience(3.99) == 0
                        && HorseBreedingRules.longJumpExperience(4.0) == 2
                        && HorseBreedingRules.longJumpExperience(8.0) == 6
                        && HorseBreedingRules.longJumpExperience(12.0) == 10,
                "long-jump XP thresholds were incorrect");
    }

    private static void mountGeneGradesReflectIndividualPotential() {
        HorseGeneticsData data = new HorseGeneticsData();
        data.geneticHealth = MountGeneticRatings.naturalMaximum(MountKind.HORSE, 0);
        data.geneticSpeed = HorseBreedingRules.MIN_SPEED;
        data.geneticJump = HorseBreedingRules.MIN_JUMP;
        data.geneticEvasion = HorseBreedingRules.MIN_EVASION;
        data.geneticEndurance = HorseBreedingRules.MIN_ENDURANCE;
        data.geneticArmor = HorseBreedingRules.MIN_ARMOR;
        require("S+".equals(MountGeneticRatings.grade(MountKind.HORSE, 0, data.geneticHealth, 0)),
                "a top natural health gene was graded against the later-generation cap");
        require("D-".equals(MountGeneticRatings.grade(MountKind.HORSE, 1, data.geneticSpeed, 0)),
                "a minimum natural speed gene did not receive the bottom individual grade");
        require(MountGeneticRatings.quality(MountKind.HORSE, 0, data.geneticHealth, 10) >= 0.60,
                "a top wild gene lost its strong grade merely because breeding can improve it");
        require("D+".equals(HorseBreedingManager.grade(data)),
                "the overall grade did not summarize the six individual gene qualities");
        require("S+".equals(MountGeneticRatings.grade(MountKind.DONKEY, 4, 21.0, 0)),
                "a top natural utility gene did not receive its own high grade");
        require("S+".equals(MountGeneticRatings.grade(MountKind.MULE, 4, 24.0, 1)),
                "a mule's hybrid-only gene range was not used for its grade");
    }

    private static void horsePersonalMutationRecordPersists() {
        HorseGeneticsData data = new HorseGeneticsData();
        data.initialized = true;
        data.personalGeneMutationMask = HorseBreedingRules.SPEED_MUTATION
                | HorseBreedingRules.ENDURANCE_MUTATION;
        data.personalPositiveMutationMask = HorseBreedingRules.SPEED_MUTATION;
        data.speedMutation = 0.0125;
        data.enduranceMutation = -4.5;
        data.traitMutated = true;
        data.traitMutationFrom = HorseTrait.HARDY;
        data.traitMutationTo = HorseTrait.SWIFT;
        data.coatMutated = true;

        HorseGeneticsData restored = new HorseGeneticsData();
        restored.readNbt(data.toNbt());
        require(restored.personalGeneMutationMask == data.personalGeneMutationMask,
                "personal gene mutation mask was not persisted");
        require(restored.personalPositiveMutationMask == data.personalPositiveMutationMask,
                "positive mutation directions were not persisted");
        require(close(restored.speedMutation, 0.0125)
                        && close(restored.enduranceMutation, -4.5),
                "personal mutation magnitudes were not persisted");
        require(restored.traitMutated
                        && restored.traitMutationFrom == HorseTrait.HARDY
                        && restored.traitMutationTo == HorseTrait.SWIFT,
                "trait mutation history was not persisted");
        require(restored.coatMutated, "coat mutation history was not persisted");
    }

    private static boolean close(double first, double second) {
        return Math.abs(first - second) < 0.000001;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class ExpectedFailure extends RuntimeException {}
}
