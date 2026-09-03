package com.ebsee.emu;

import java.io.ByteArrayInputStream;
import java.util.Map;

public final class CompatibilityProfileReaderTest {
    public static void main(String[] args) throws Exception {
        check(CompatibilityProfileReader.PROFILE_PATH.startsWith("/appdata/freej2meonminijvm.jar/"),
                "profile remains inside the miniJVM application sandbox");
        Map<String, String> profile = CompatibilityProfileReader.read(new ByteArrayInputStream(
                ("schema=1\nwidth=128\nheight=144\nphone=Nokia\nmidlet.launch=thread\nunknown=ignored\n")
                        .getBytes("ISO-8859-1")));
        check("128".equals(profile.get("width")), "width");
        check("144".equals(profile.get("height")), "height");
        check("Nokia".equals(profile.get("phone")), "phone");
        check("thread".equals(profile.get("midlet.launch")), "MIDlet launch mode");
        check(!profile.containsKey("unknown"), "unknown key");

        Map<String, String> incompatible = CompatibilityProfileReader.read(new ByteArrayInputStream(
                "schema=2\nwidth=128\n".getBytes("ISO-8859-1")));
        check(incompatible.isEmpty(), "schema rejection");
    }

    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }
}
