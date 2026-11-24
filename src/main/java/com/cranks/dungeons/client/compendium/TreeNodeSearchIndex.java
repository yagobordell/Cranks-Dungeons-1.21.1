package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.stat.passive.PassiveNode;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** A snapshot of searchable names, descriptions, and modifiers from one tree. */
final class TreeNodeSearchIndex {
    private static final Pattern ACCENTS = Pattern.compile("\\p{M}+");
    private static final Pattern SEPARATORS = Pattern.compile("[^\\p{L}\\p{N}]+");
    private record Entry(String id, String text) {}
    private final List<Entry> entries;

    TreeNodeSearchIndex(Collection<PassiveNode> nodes, boolean primaryAttributeChoices) {
        entries = nodes.stream().map(node -> entry(node, primaryAttributeChoices)).toList();
    }

    Set<String> find(String query) {
        String normalized = normalize(query);
        if (normalized.isEmpty()) return Set.of();
        String[] terms = normalized.split(" ");
        Set<String> matches = new HashSet<>();
        for (Entry entry : entries) {
            if (matchesAllTerms(entry.text, terms)) matches.add(entry.id);
        }
        return Set.copyOf(matches);
    }

    private static boolean matchesAllTerms(String text, String[] terms) {
        for (String term : terms) {
            if (!text.contains(term)) return false;
        }
        return true;
    }

    private static Entry entry(PassiveNode node, boolean primaryAttributeChoices) {
        StringBuilder text = new StringBuilder(node.id())
                .append(' ').append(stripFormatting(node.name()))
                .append(' ').append(stripFormatting(com.cranks.dungeons.client.guide.TreeNodeDescriptions.plain(node,!primaryAttributeChoices)));
        if (node.shape() != null) text.append(' ').append(node.shape());
        if (node.modifiers() != null) {
            node.modifiers().forEach((attribute, value) -> {
                String translationKey = attribute.value().getTranslationKey();
                String readable = PassiveStatHelper.getReadableName(attribute);
                String localized = Text.translatable(translationKey).getString();
                text.append(' ').append(readable).append(' ').append(localized).append(' ').append(value);
                attribute.getKey().ifPresent(key -> text.append(' ').append(key.getValue()));
            });
        }
        if (node.isAttributeSelection()) {
            text.append(" attribute selection choice");
            if (primaryAttributeChoices) {
                text.append(" strength dexterity intelligence ").append(node.attributeSelectionAmount());
            }
        }
        return new Entry(node.id(), normalize(text.toString()));
    }

    private static String stripFormatting(String text) {
        return text == null ? "" : Formatting.strip(text);
    }

    private static String normalize(String text) {
        String folded = ACCENTS.matcher(Normalizer.normalize(text, Normalizer.Form.NFKD)).replaceAll("")
                .toLowerCase(Locale.ROOT).replace("armour", "armor");
        return SEPARATORS.matcher(folded).replaceAll(" ").strip();
    }
}
