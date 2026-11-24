package com.cranks.dungeons.client.guide;

import com.google.gson.*;
import java.util.*;

/** Rich table cells retain the same explicit links as ordinary article text. */
public final class GuideTables {
    private GuideTables() {}
    public static GuideDocument.Block table(List<String> headers,List<List<String>> rows,double... weights){
        var json=new JsonObject();var head=new JsonArray();headers.forEach(head::add);json.add("headers",head);
        var body=new JsonArray();for(var row:rows){var cells=new JsonArray();row.forEach(cells::add);body.add(cells);}json.add("rows",body);
        var sizes=new JsonArray();for(double weight:weights)sizes.add(weight);json.add("weights",sizes);
        return new GuideDocument.Block("table",json.toString());
    }
    public static String searchable(String value){
        var root=JsonParser.parseString(value).getAsJsonObject();var text=new StringBuilder();
        for(String key:List.of("headers","rows"))for(var row:root.getAsJsonArray(key)){
            if(row.isJsonArray())for(var cell:row.getAsJsonArray())text.append(' ').append(GuideText.plain(cell.getAsString()));
            else text.append(' ').append(GuideText.plain(row.getAsString()));
        }return text.toString();
    }
}
