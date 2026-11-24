package com.cranks.dungeons.client.guide;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.regex.Pattern;

public final class GuideText {
    private static final String PREFIX="cranks-guide/";
    private static final Pattern REFERENCE=Pattern.compile("\\[\\[([^|\\]]+)\\|([^\\]]+)\\]\\]");
    private GuideText(){}
    public static MutableText link(Text text,GuideTarget target){
        return text.copy().styled(s->s.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE,PREFIX+target.encode())));
    }
    public static MutableText link(String text,GuideTarget target){return link(Text.literal(text),target);}
    public static GuideTarget itemTarget(ItemStack stack){
        String id=Registries.ITEM.getId(stack.getItem()).toString();
        var contents=stack.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);
        if(contents!=null&&contents.potion().isPresent())id+="~"+contents.potion().get().getKey().orElseThrow().getValue();
        return GuideTarget.item(id);
    }
    public static MutableText itemName(ItemStack stack){return link(stack.getName(),itemTarget(stack));}
    public static Optional<GuideTarget> target(Style style){
        var event=style.getClickEvent();return event!=null&&event.getAction()==ClickEvent.Action.CHANGE_PAGE&&event.getValue().startsWith(PREFIX)
                ?GuideTarget.parse(event.getValue().substring(PREFIX.length())):Optional.empty();
    }
    public static String reference(String label,GuideTarget target){return "[["+target.encode()+"|"+label.replace("]]","] ]").replace('|','/')+"]]";}
    /** Explicit rich references; ordinary words never acquire guessed links at runtime. */
    public static Text parse(String markup){
        var result=Text.empty();var matcher=REFERENCE.matcher(markup==null?"":markup);int from=0;
        while(matcher.find()){
            result.append(Text.literal(markup.substring(from,matcher.start())));
            var target=GuideTarget.parse(matcher.group(1));
            result.append(target.isPresent()?link(matcher.group(2),target.get()):Text.literal(matcher.group(2)));
            from=matcher.end();
        }
        return result.append(Text.literal(markup==null?"":markup.substring(from)));
    }
    public static String plain(String markup){return parse(markup).getString();}
}
