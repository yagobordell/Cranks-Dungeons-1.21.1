package com.cranks.dungeons.client.guide;

import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Authored player-facing facts; resource packs can extend or replace the catalogues. */
public final class GuideKnowledge {
    private static final Map<String,JsonObject> CATALOGUES=new HashMap<>();
    private GuideKnowledge() {}
    public static void reset(){CATALOGUES.clear();}
    public static JsonObject catalogue(String name){return CATALOGUES.computeIfAbsent(name,key->{
        try(var reader=new InputStreamReader(MinecraftClient.getInstance().getResourceManager()
                .getResource(Identifier.of("cranks-dungeons","compendium/reference/"+key+".json")).orElseThrow().getInputStream(),StandardCharsets.UTF_8)){
            return new Gson().fromJson(reader,JsonObject.class);
        }catch(Exception exception){com.cranks.dungeons.CranksDungeons.LOGGER.warn("Unable to read guide catalogue {}",key,exception);return new JsonObject();}
    });}
    public static JsonObject entry(String catalogue,String key){var root=catalogue(catalogue);return root.has(key)?root.getAsJsonObject(key):new JsonObject();}
    public static String text(JsonObject object,String field){return object.has(field)&&object.get(field).isJsonPrimitive()?object.get(field).getAsString():"";}
    public static List<String> strings(JsonObject object,String field){
        if(!object.has(field)||!object.get(field).isJsonArray())return List.of();
        var result=new ArrayList<String>();for(var value:object.getAsJsonArray(field))if(value.isJsonPrimitive())result.add(value.getAsString());return List.copyOf(result);
    }
    public static List<String> creaturesFor(String item){return catalogue("creatures").entrySet().stream()
            .filter(entry->entry.getValue().getAsJsonObject().getAsJsonArray("drops").asList().stream()
                    .anyMatch(drop->text(drop.getAsJsonObject(),"item").equals(item))).map(Map.Entry::getKey).toList();}
}
