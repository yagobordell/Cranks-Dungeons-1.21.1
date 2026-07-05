public class MethodDump {
    public static void main(String[] args) {
        for (java.lang.reflect.Method m : net.minecraft.server.command.ServerCommandSource.class.getMethods()) {
            if (m.getName().toLowerCase().contains("permission") || m.getName().toLowerCase().contains("level") || m.getName().toLowerCase().contains("has")) {
                System.out.println(m.getName() + " " + java.util.Arrays.toString(m.getParameterTypes()));
            }
        }
    }
}
