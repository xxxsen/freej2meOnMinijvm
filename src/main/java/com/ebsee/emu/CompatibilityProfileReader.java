package com.ebsee.emu;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Reads the bounded host-generated profile before the MIDlet is loaded. */
public final class CompatibilityProfileReader {
    public static final String PROFILE_PATH = "/j2me-web-profile.properties";

    private CompatibilityProfileReader() {
    }

    public static Map<String, String> read() {
        File file = new File(PROFILE_PATH);
        if (!file.exists() || file.length() <= 0 || file.length() > 4096) {
            return new HashMap<String, String>();
        }
        try {
            FileInputStream input = new FileInputStream(file);
            try {
                return read(input);
            } finally {
                input.close();
            }
        } catch (Exception failure) {
            System.out.println("[j2me-web] PROFILE_READ_FAILED " + failure);
            return new HashMap<String, String>();
        }
    }

    static Map<String, String> read(InputStream input) throws Exception {
        Properties properties = new Properties();
        properties.load(input);
        HashMap<String, String> result = new HashMap<String, String>();
        String schema = properties.getProperty("schema");
        if (!"1".equals(schema)) return result;
        String[] keys = {
            "width", "height", "fps", "phone", "rotation", "sound",
            "m3g.backend", "m3g.halfResolution"
        };
        for (int index = 0; index < keys.length; index++) {
            String value = properties.getProperty(keys[index]);
            if (value != null && value.length() <= 100) result.put(keys[index], value);
        }
        return result;
    }
}
