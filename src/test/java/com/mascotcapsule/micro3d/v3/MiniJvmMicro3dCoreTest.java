package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.base.FrameState;
import com.mascotcapsule.micro3d.v3.base.Micro3dBackend;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.recompile.mobile.PlatformGraphics;
import com.mascotcapsule.micro3d.v3.base.MiniJvmMicro3dGlBackend;

public final class MiniJvmMicro3dCoreTest {
    private static final String FACTORY_PROPERTY = "freej2me.micro3d.backend.factory";

    private MiniJvmMicro3dCoreTest() {
    }

    public static void main(String[] args) throws Exception {
        assertMethod(ActionTable.class, "getMaxFrame", Integer.TYPE);
        assertConstructor(ActionTable.class, InputStream.class);
        assertMethod(Figure.class, "getNumPattern");
        assertMethod(Graphics3D.class, "bind", PlatformGraphics.class);
        assertMethod(Graphics3D.class, "release");
        assertConstructor(Texture.class);
        assertConstructor(Texture.class, InputStream.class, Boolean.TYPE);
        assertMethod(Vector3D.class, "normalize");

        if (!(Graphics3D.createBackendFactory(null) instanceof MiniJvmMicro3dFactory)) {
            throw new AssertionError("default MascotCapsule backend must not depend on reflective class loading");
        }
        Method availability = MiniJvmMicro3dGlBackend.class.getDeclaredMethod(
                "isApiPresenceSufficientForAvailability", Boolean.TYPE);
        availability.setAccessible(true);
        if (!((Boolean)availability.invoke(null, Boolean.TRUE)).booleanValue()) {
            throw new AssertionError("MascotCapsule GL availability must not depend on callback thread publication");
        }

        System.setProperty(FACTORY_PROPERTY, RecordingFactory.class.getName());
        try {
            Graphics3D graphics = new Graphics3D();
            Field field = Graphics3D.class.getDeclaredField("backend");
            field.setAccessible(true);
            Object backend = field.get(graphics);
            if (!(backend instanceof RecordingBackend)) {
                throw new AssertionError("configured MascotCapsule backend was not selected: " + backend);
            }
        } finally {
            System.clearProperty(FACTORY_PROPERTY);
        }
        System.out.println("MascotCapsule backend dispatch and compatibility surface verified.");
    }

    private static void assertMethod(Class<?> owner, String name, Class<?>... parameters) throws Exception {
        owner.getMethod(name, parameters);
    }

    private static void assertConstructor(Class<?> owner, Class<?>... parameters) throws Exception {
        owner.getConstructor(parameters);
    }

    public static final class RecordingFactory implements Graphics3D.BackendFactory {
        public Micro3dBackend create() {
            return new RecordingBackend();
        }
    }

    private static final class RecordingBackend implements Micro3dBackend {
        public void bind(Object target, boolean doClip) { }
        public void flushFrame(FrameState frame) { }
        public void flushItems(FrameState frame) { }
        public void release(Object target) { }
        public boolean isAvailable() { return true; }
        public int getTargetWidth() { return 240; }
        public int getTargetHeight() { return 320; }
    }
}
