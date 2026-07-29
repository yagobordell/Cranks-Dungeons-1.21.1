import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InspectPlayer {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.class_1657"); // PlayerEntity
        System.out.println("--- Fields ---");
        for (Field f : clazz.getDeclaredFields()) {
            System.out.println(f.getType().getName() + " " + f.getName());
        }
        System.out.println("--- Methods ---");
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.println(m.getReturnType().getName() + " " + m.getName());
        }
    }
}
