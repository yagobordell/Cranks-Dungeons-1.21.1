import net.minecraft.entity.LivingEntity;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodDump {
    public static void main(String[] args) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.entity.LivingEntity");
            for (Method m : clazz.getDeclaredMethods()) {
                System.out.println(Modifier.toString(m.getModifiers()) + " " + m.getReturnType().getSimpleName() + " " + m.getName() + "(...)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
