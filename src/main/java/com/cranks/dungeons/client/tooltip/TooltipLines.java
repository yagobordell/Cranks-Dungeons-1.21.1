package com.cranks.dungeons.client.tooltip;

import com.cranks.dungeons.item.CustomRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import com.cranks.dungeons.client.guide.GuideText;
import com.cranks.dungeons.client.guide.GuideTarget;
import com.cranks.dungeons.client.guide.GuideReferences;

import java.util.List;

/** Invisible presentation metadata; getString() continues to expose the complete item information. */
public final class TooltipLines {
    public static final String MARKER = "cranks.tooltip/";
    private TooltipLines() {}

    public static MutableText mark(Text text, String kind) {
        return text.copy().styled(style -> style.withInsertion(MARKER + kind));
    }

    public static MutableText section(String label, int used, int total) {
        return mark(GuideText.link(Text.literal(label + ": ").withColor(0xAAAAB2)
                .append(Text.literal(used + " / " + total).withColor(0x90909A)),GuideTarget.article(label.equals("Enchantments")?"enchantments":"runes")), "section");
    }

    public static MutableText rune(Text value, Text detail, int component, int count) {
        String kind=count<=1?"rune":component==0?"rune_start":component==count-1?"rune_end":"rune_middle";
        return mark(value, kind).append(mark(detail, "detail"));
    }

    public static void decorate(ItemStack stack, List<Text> lines) {
        CustomRarity rarity = CustomRarity.getRarity(stack);
        lines.set(0, mark(GuideText.link(lines.getFirst().copy().withColor(TooltipTheme.color(rarity)),GuideText.itemTarget(stack)),
                "title|" + rarity.name() + "|" + Registries.ITEM.getId(stack.getItem())));
        lines.add(1, mark(GuideText.link(Text.literal(rarity.getName()).withColor(0xAFAFAB),GuideTarget.article("item_rarity")), "rarity"));
        boolean core = true;
        for (int i = 2; i < lines.size(); i++) {
            Text line = lines.get(i);
            if (line.getStyle().getInsertion() != null && line.getStyle().getInsertion().startsWith(MARKER)) {
                if (line.getStyle().getInsertion().equals(MARKER + "section")) core = false;
                continue;
            }
            String raw = line.getString();
            String kind = core && raw.contains(": ") && !raw.startsWith("Requires:") ? "aligned" : "plain";
            if (raw.startsWith("Durability:")) kind = "aligned";
            int colon=raw.indexOf(':');GuideTarget target=colon>0?GuideReferences.equipmentField(raw.substring(0,colon)):null;
            if(target!=null)line=GuideText.link(line,target);
            lines.set(i, mark(line, kind));
        }
    }
}
