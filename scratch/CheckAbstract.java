import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CheckAbstract {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.class_1297"); // Entity
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals("method_5652") || m.getName().equals("writeCustomDataToNbt")) {
                System.out.println("Method " + m.getName() + " is abstract: " + Modifier.isAbstract(m.getModifiers()));
            }
            if (m.getName().equals("method_5749") || m.getName().equals("readCustomDataFromNbt")) {
                System.out.println("Method " + m.getName() + " is abstract: " + Modifier.isAbstract(m.getModifiers()));
            }
        }
    }
}
