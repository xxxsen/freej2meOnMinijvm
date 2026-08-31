package javax.sound.sampled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class AudioSystem {
    private AudioSystem() {
    }

    public static AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding,
                                                       AudioInputStream sourceStream) {
        sourceStream.reset();
        return sourceStream;
    }

    public static AudioInputStream getAudioInputStream(InputStream stream) throws IOException {
        if (stream instanceof AudioInputStream) return (AudioInputStream) stream;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = stream.read(buffer)) >= 0) {
            if (count > 0) output.write(buffer, 0, count);
        }
        return new AudioInputStream(output.toByteArray());
    }

    public static Clip getClip() {
        return new Clip();
    }
}
