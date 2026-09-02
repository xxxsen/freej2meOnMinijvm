/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.recompile.mobile.Mobile;

public final class Resources {
    private Resources() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] getBytes(String name) {
        InputStream stream = Resources.openResourceStream(name);
        if (stream == null) {
            return null;
        }
        try {
            int count;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            while ((count = stream.read(data)) != -1) {
                buffer.write(data, 0, count);
            }
            byte[] bytes = buffer.toByteArray();
            if (bytes.length == 0) {
                byte[] byArray = null;
                return byArray;
            }
            byte[] byArray = bytes;
            return byArray;
        }
        catch (IOException e) {
            byte[] byArray = null;
            return byArray;
        }
        finally {
            try {
                stream.close();
            }
            catch (IOException iOException) {}
        }
    }

    private static InputStream openResourceStream(String name) {
        try {
            InputStream stream = Mobile.getMIDletResourceAsStream(name);
            if (stream != null) {
                return stream;
            }
        }
        catch (Throwable stream) {
            // empty catch block
        }
        String normalized = name.startsWith("/") ? name : "/" + name;
        InputStream classpathStream = Resources.class.getResourceAsStream(normalized);
        if (classpathStream != null) {
            return classpathStream;
        }
        if (Resources.hasUriScheme(name)) {
            try {
                return new URL(name).openStream();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        try {
            return new FileInputStream(name);
        }
        catch (IOException iOException) {
            return null;
        }
    }

    private static boolean hasUriScheme(String name) {
        int colon = name.indexOf(58);
        if (colon <= 0) {
            return false;
        }
        for (int i = 0; i < colon; ++i) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '+' || c == '-' || c == '.') continue;
            return false;
        }
        return true;
    }
}



