package java.awt.image;

/**
 * Compatibility marker for image producers. miniJVM images are backed by a
 * directly mutable pixel buffer, so producer/consumer streaming is unused.
 */
public interface ImageProducer {
}
