import java.io.BufferedReader;
import java.io.FileReader;

public class ParseMappings {
    public static void main(String[] args) throws Exception {
        boolean inClass = false;
        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("c\t")) {
                    inClass = line.contains("net/minecraft/class_1297") || line.contains("net/minecraft/entity/Entity\t");
                } else if (inClass && line.startsWith("\tm\t")) {
                    System.out.println(line);
                } else if (inClass && !line.startsWith("\t")) {
                    inClass = false;
                }
            }
        }
    }
}
