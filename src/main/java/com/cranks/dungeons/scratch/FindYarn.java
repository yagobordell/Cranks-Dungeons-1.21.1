package com.cranks.dungeons.scratch;

import java.io.File;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FindYarn {
    public static void main(String[] args) throws Exception {
        File dir = new File("C:\\Users\\yagob\\.gradle\\caches\\fabric-loom\\1.21.1");
        findYarn(dir);
    }
    
    private static void findYarn(File dir) throws Exception {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (f.getName().endsWith(".jar") && f.getName().contains("yarn") && !f.getName().contains("sources")) {
                    System.out.println("Checking " + f.getName());
                    try (ZipFile zip = new ZipFile(f)) {
                        ZipEntry entry = zip.getEntry("mappings/mappings.tiny");
                        if (entry != null) {
                            System.out.println("Found mappings!");
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)))) {
                                String line;
                                boolean inAnim = false;
                                while ((line = reader.readLine()) != null) {
                                    if (line.contains("net/minecraft/entity/AnimationState")) {
                                        System.out.println(line);
                                        inAnim = true;
                                    } else if (inAnim && line.startsWith("\t")) {
                                        if (line.contains("m\t") || line.contains("\tmethod\t")) {
                                            System.out.println(line);
                                        }
                                    } else if (inAnim) {
                                        inAnim = false;
                                    }
                                }
                            }
                        }
                    }
                } else if (f.isDirectory()) {
                    findYarn(f);
                }
            }
        }
    }
}
