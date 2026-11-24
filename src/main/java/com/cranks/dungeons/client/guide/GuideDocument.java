package com.cranks.dungeons.client.guide;

import java.util.List;

public record GuideDocument(String title,List<Block> blocks) {
    public record Block(String type,String value,String style) {
        public Block(String type,String value){this(type,value,"");}
    }
}
