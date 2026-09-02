/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import java.awt.image.BufferedImage;

public interface Micro3dSurface {
    public int getWidth();

    public int getHeight();

    public int getPixel(int var1, int var2);

    public void setPixel(int var1, int var2, int var3);

    public static final class BufferedImageSurface
    implements Micro3dSurface {
        private final BufferedImage image;

        public BufferedImageSurface(BufferedImage image) {
            this.image = image;
        }

        public BufferedImage getImage() {
            return this.image;
        }

        @Override
        public int getWidth() {
            return this.image.getWidth();
        }

        @Override
        public int getHeight() {
            return this.image.getHeight();
        }

        @Override
        public int getPixel(int x, int y) {
            return this.image.getRGB(x, y);
        }

        @Override
        public void setPixel(int x, int y, int argb) {
            this.image.setRGB(x, y, argb | 0xFF000000);
        }
    }
}



