package com.cranks.dungeons.client.guide;

import java.util.Locale;
import java.util.Optional;

/** Stable reference IDs, independent of the displayed language. */
public record GuideTarget(Kind kind,String key) {
    public enum Kind { ARTICLE, STAT, RUNE, ITEM, EFFECT, ENCHANTMENT, RECIPE, MOB }
    public GuideTarget {
        if(kind==null||key==null||key.isBlank()||key.length()>256)throw new IllegalArgumentException("Invalid guide target");
    }
    public String encode(){return kind.name().toLowerCase(Locale.ROOT)+"/"+key;}
    public static Optional<GuideTarget> parse(String encoded){
        if(encoded==null)return Optional.empty();int slash=encoded.indexOf('/');if(slash<1)return Optional.empty();
        try{return Optional.of(new GuideTarget(Kind.valueOf(encoded.substring(0,slash).toUpperCase(Locale.ROOT)),encoded.substring(slash+1)));}
        catch(IllegalArgumentException e){return Optional.empty();}
    }
    public static GuideTarget article(String id){return new GuideTarget(Kind.ARTICLE,id);}
    public static GuideTarget stat(String id){return new GuideTarget(Kind.STAT,id);}
    public static GuideTarget rune(String id){return new GuideTarget(Kind.RUNE,id);}
    public static GuideTarget item(String id){return new GuideTarget(Kind.ITEM,id);}
}
