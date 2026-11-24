package com.cranks.dungeons.progression;

/** Stable knowledge keys deliberately collapse alternate recipes, colours and material variants. */
public final class DiscoveryFamilies {
    private DiscoveryFamilies() {}
    private static final String WOODS="oak|spruce|birch|jungle|acacia|dark_oak|mangrove|cherry|bamboo|crimson|warped";
    private static final String COLORS="white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black";
    public static String recipe(String id) {
        String[] parts=id.split(":",2);
        String path=parts.length==2?parts[1]:parts[0];
        String namespace=parts.length==2?parts[0]:"minecraft";
        if(namespace.equals("minecraft")) {
            path=path.replaceFirst("^("+COLORS+")_", "coloured_");
            path=path.replaceFirst("^(stripped_)?("+WOODS+")_", "wood_");
        }
        return namespace+":"+path;
    }
    public static String resource(String id) {
        if(!id.startsWith("minecraft:")) return null;
        String path=id.substring(10);
        if(path.matches("(deepslate_)?(coal|iron|copper|gold|redstone|lapis|diamond|emerald)_ore"))
            path=path.replace("deepslate_", "").replace("_ore", "");
        path=path.replaceFirst("^raw_", "").replaceFirst("_(ingot|nugget|block)$", "");
        if(path.matches("coal|iron|copper|gold|redstone|lapis_lazuli|lapis|diamond|emerald|quartz|netherite_scrap"))
            return "mineral/"+path.replace("lapis_lazuli","lapis");
        if(path.matches("(stripped_)?("+WOODS+")_(log|wood|stem|hyphae)"))
            return "wood/"+path.replaceFirst("^stripped_", "").replaceFirst("_(log|wood|stem|hyphae)$", "");
        if(path.matches("wheat(_seeds)?|beetroot(_seeds)?|carrot|potato|pumpkin(_seeds)?|melon(_seeds|_slice)?|sweet_berries|glow_berries|cocoa_beans|sugar_cane|cactus|bamboo"))
            return "crop/"+path.replaceFirst("_(seeds|slice)$", "");
        if(path.matches("flint|clay_ball|sand|red_sand|gravel|cobblestone|cobbled_deepslate|leather|string|feather|bone|slime_ball|honeycomb|amethyst_shard"))
            return "resource/"+path.replace("red_sand","sand");
        return null;
    }
}
