package com.cranks.dungeons;

import com.cranks.dungeons.party.Party;
import com.cranks.dungeons.util.CombatXpContext;
import com.cranks.dungeons.util.TreeUnlockPlanner;

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

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class ExpectedFailure extends RuntimeException {}
}
