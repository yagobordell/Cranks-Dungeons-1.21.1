package com.cranks.dungeons.client.guide;

public final class GuideWritingRegression {
    public static void run(){
        String text=GuideWriting.playerText("Improves mining. 0.1 means 10%; maximum bonus is 300%. A bonus of 20% is stored as 0.2.");
        check(!text.contains("0.1")&&!text.contains("0.2")&&!text.contains("stored"),"storage prose leaked into guide");
        check(text.contains("300%")&&text.contains("mining"),"useful limits or effect removed");
        String example="100 Armour reduces a 50-damage hit by about 16.7%.";
        check(GuideWriting.playerText(example).equals(example),"worked example was stripped");
        check(!GuideWriting.calculations("cranks-dungeons:crit_chance").isEmpty(),"critical chance needs a worked calculation");
        check(GuideWriting.relatedStats("cranks-dungeons:poison_magnitude_percent").contains("poison_max_stacks"),"poison build relationship missing");
        for(String id:java.util.List.of("derived/evasion_rating","cranks-dungeons:physical_as_extra_fire_percent",
                "cranks-dungeons:physical_to_void_conversion_percent","minecraft:generic.oxygen_bonus","minecraft:player.submerged_mining_speed")){
            String description=GuideStatText.description(id);
            check(!description.isBlank()&&!description.startsWith("This statistic contributes"),"missing explanation for "+id);
        }
        check(GuideStatText.description("cranks-dungeons:physical_to_void_conversion_percent").contains("into void damage"),"conversion names wrong damage type");
        check(GuideWriting.displayValue((double)0.01f)==0.01,"float noise in guide bonus");
        check(GuideWriting.displayValue(0.0015)==0.0015,"small useful bonus rounded away");
        System.out.println("Player-facing guide writing regression checks passed");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
