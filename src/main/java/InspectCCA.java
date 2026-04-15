import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import java.lang.reflect.Method;

public class InspectCCA {
    public static void main(String[] args) {
        System.out.println("Methods in AutoSyncedComponent:");
        for (Method m : AutoSyncedComponent.class.getMethods()) {
            System.out.println(m.getName() + " with parameters:");
            for (Class<?> p : m.getParameterTypes()) {
                System.out.println("  " + p.getName());
            }
        }
    }
}
