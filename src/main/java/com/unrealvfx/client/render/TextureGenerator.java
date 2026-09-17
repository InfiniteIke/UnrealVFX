package com.unrealvfx.client.render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Generatore procedurale di texture ad alta definizione per VFX anime:
 * - hakai_corona.png: Corona solare anime a raggiera con lingue di plasma fucsia e nucleo bianco (512x512)
 * - hakai_ring.png: Anello di accrescimento con dentellature di energia ad alta frequenza (512x64)
 * - anime_flame.png: Sprite fiamma stilizzata con punte affilate e cel-shading a 3 toni (256x512)
 * - disintegration_cube.png: Mote di cancellazione della realta (cubo nero con bordo neon) (64x64)
 * - flame_erosion.png: Maschera di dissolvenza con pennellate Toei (256x512)
 * - flame_flow.png: Flow map vettoriale (256x256)
 * - spark_streak.png: Scintilla ad ago ad alta luminanza (128x256)
 * - energy_ramp.png: Gradiente cromatico a fasce cel-shading (256x16)
 */
public class TextureGenerator {

    public static void main(String[] args) {
        generateAll(new File("src/main/resources/assets/unrealvfx/textures/vfx"));
        System.out.println("Texture generation complete!");
    }

    public static void generateAll(File outputDir) {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try {
            generateHakaiCorona(new File(outputDir, "hakai_corona.png"));
            generateHakaiRing(new File(outputDir, "hakai_ring.png"));
            generateAnimeFlame(new File(outputDir, "anime_flame.png"));
            generateDisintegrationCube(new File(outputDir, "disintegration_cube.png"));
            generateFlameErosion(new File(outputDir, "flame_erosion.png"));
            generateFlameFlow(new File(outputDir, "flame_flow.png"));
            generateSparkStreak(new File(outputDir, "spark_streak.png"));
            generateEnergyRamp(new File(outputDir, "energy_ramp.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateHakaiCorona(File file) throws Exception {
        int w = 512, h = 512;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        float cx = w * 0.5f;
        float cy = h * 0.5f;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) Math.atan2(dy, dx);

                if (dist < 55.0f || dist > 250.0f) {
                    img.setRGB(x, y, 0x00000000);
                    continue;
                }

                // Onde radiali a frequenze multiple per creare le creste fiammeggianti anime
                double f1 = Math.sin(angle * 24.0 + Math.cos(angle * 6.0) * 1.8);
                double f2 = Math.sin(angle * 48.0 - Math.sin(angle * 12.0) * 2.2);
                double f3 = Math.cos(angle * 16.0 + angle * 32.0);

                double spikes = (f1 * 0.55 + f2 * 0.30 + f3 * 0.15 + 1.0) * 0.5;

                // Lunghezza estesa delle punte verso l'esterno
                float maxRadius = 140.0f + (float) (spikes * 105.0f);
                if (dist > maxRadius) {
                    img.setRGB(x, y, 0x00000000);
                    continue;
                }

                // Normalizza posizione radiale lungo la punta
                float t = (dist - 55.0f) / (maxRadius - 55.0f);
                t = Math.max(0.0f, Math.min(1.0f, t));

                // 3 Fasce Cel-Shading nette (Sparking! ZERO Palette)
                int r, g, b, a;
                if (t < 0.28f) {
                    // Fascia interna: Bianco/Rosa caldo accecante
                    r = 255; g = 235; b = 255; a = 255;
                } else if (t < 0.65f) {
                    // Fascia mediana: Neon Magenta / Fucsia ultra-saturo
                    r = 255; g = 10; b = 200; a = (int) (245 * (1.0f - (t - 0.28f) * 0.4f));
                } else {
                    // Punte esterne: Viola elettrico / Porpora scuro
                    r = 160; g = 0; b = 230; a = (int) (210 * (1.0f - (t - 0.65f) / 0.35f));
                }

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(x, y, argb);
            }
        }
        ImageIO.write(img, "PNG", file);
    }

    private static void generateHakaiRing(File file) throws Exception {
        int w = 512, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            float ny = (float) y / h;
            float distFromCenter = Math.abs(ny - 0.5f) * 2.0f; // 0 al centro, 1 ai bordi

            for (int x = 0; x < w; x++) {
                float nx = (float) x / w;

                // Striature di plasma ad alta velocita lungo l'anello
                double streak1 = Math.sin(nx * 18.0 * Math.PI + Math.cos(nx * 6.0 * Math.PI));
                double streak2 = Math.sin(nx * 36.0 * Math.PI);
                double energyVal = (streak1 * 0.6 + streak2 * 0.4 + 1.0) * 0.5;

                // Maschera di spessore dell'anello con bordo netto
                float thickness = (float) Math.pow(1.0f - distFromCenter, 1.4);
                float combined = (float) (energyVal * 0.4 + thickness * 0.6);

                if (combined < 0.32f) {
                    img.setRGB(x, y, 0x00000000);
                    continue;
                }

                int r, g, b, a;
                if (combined > 0.75f) {
                    // Nucleo bianco-caldo dell'anello
                    r = 255; g = 245; b = 255; a = 255;
                } else if (combined > 0.48f) {
                    // Neon Magenta brillante
                    r = 255; g = 15; b = 210; a = 240;
                } else {
                    // Viola/Porpora esterno
                    r = 175; g = 0; b = 240; a = 190;
                }

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(x, y, argb);
            }
        }
        ImageIO.write(img, "PNG", file);
    }

    private static void generateAnimeFlame(File file) throws Exception {
        int w = 256, h = 512;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, w, h);
        g2.setComposite(AlphaComposite.SrcOver);

        float cx = w * 0.5f;

        for (int y = 0; y < h; y++) {
            float ny = (float) y / h; // 0 in cima, 1 in basso
            float heightFactor = 1.0f - ny; // 0 in basso, 1 in cima

            float baseWidth = (float) (Math.sin(ny * Math.PI) * (w * 0.42f));

            for (int x = 0; x < w; x++) {
                float dx = Math.abs(x - cx);
                if (dx > baseWidth) continue;

                float nx = (float) x / w;
                double curl = Math.sin(heightFactor * 12.0 + nx * 8.0) * 0.25;
                float normalizedDist = (dx / Math.max(1.0f, baseWidth)) + (float) curl;

                if (normalizedDist > 0.95f) continue;

                int r, g, b, a;
                if (normalizedDist < 0.35f) {
                    r = 255; g = 255; b = 240; a = 255;
                } else if (normalizedDist < 0.70f) {
                    r = 255; g = 205; b = 20; a = 230;
                } else {
                    r = 230; g = 90; b = 10; a = 180;
                }

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(x, y, argb);
            }
        }
        g2.dispose();
        ImageIO.write(img, "PNG", file);
    }

    private static void generateDisintegrationCube(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float distEdgeX = Math.min(x, w - 1 - x);
                float distEdgeY = Math.min(y, h - 1 - y);
                float distEdge = Math.min(distEdgeX, distEdgeY);

                if (distEdge < 2) {
                    img.setRGB(x, y, 0x00000000);
                } else if (distEdge < 7) {
                    int argb = (255 << 24) | (255 << 16) | (10 << 8) | 215;
                    img.setRGB(x, y, argb);
                } else if (distEdge < 10) {
                    int argb = (240 << 24) | (150 << 16) | (0 << 8) | 220;
                    img.setRGB(x, y, argb);
                } else {
                    int argb = (255 << 24) | (10 << 16) | (0 << 8) | 18;
                    img.setRGB(x, y, argb);
                }
            }
        }
        ImageIO.write(img, "PNG", file);
    }

    private static void generateFlameErosion(File file) throws Exception {
        int w = 256, h = 512;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            float ny = (float) y / h;
            for (int x = 0; x < w; x++) {
                float nx = (float) x / w;

                double v1 = Math.sin(nx * 14.0 * Math.PI + Math.cos(ny * 18.0 * Math.PI) * 1.6);
                double v2 = Math.sin(nx * 28.0 * Math.PI - ny * 10.0 * Math.PI);
                double v3 = Math.cos(nx * 8.0 * Math.PI + ny * 24.0 * Math.PI);

                double combined = (v1 * 0.5 + v2 * 0.3 + v3 * 0.2 + 1.0) * 0.5;
                double fade = Math.sin(ny * Math.PI);
                int val = (int) Math.max(0, Math.min(255, combined * fade * 255.0));

                int argb = (val << 24) | (val << 16) | (val << 8) | val;
                img.setRGB(x, y, argb);
            }
        }
        ImageIO.write(img, "PNG", file);
    }

    private static void generateFlameFlow(File file) throws Exception {
        int w = 256, h = 256;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            float ny = (float) y / h;
            for (int x = 0; x < w; x++) {
                float nx = (float) x / w;

                double angle = Math.sin(nx * 8.0 * Math.PI) * Math.cos(ny * 8.0 * Math.PI) * Math.PI;
                int r = (int) ((Math.cos(angle) * 0.5 + 0.5) * 255);
                int g = (int) ((Math.sin(angle) * 0.5 + 0.5) * 255);
                int b = 128;

                int argb = (255 << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(x, y, argb);
            }
        }
        ImageIO.write(img, "PNG", file);
    }

    private static void generateSparkStreak(File file) throws Exception {
        int w = 128, h = 256;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, w, h);
        g2.setComposite(AlphaComposite.SrcOver);

        int cx = w / 2;
        for (int y = 0; y < h; y++) {
            float distY = Math.abs(y - h * 0.5f) / (h * 0.5f);
            float taper = (1.0f - distY) * (1.0f - distY);
            int halfWidth = (int) (taper * (w * 0.45f));

            for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                float distX = Math.abs(dx) / (float) Math.max(1, halfWidth);
                float intensity = (1.0f - distX) * taper;
                int alpha = (int) (Math.pow(intensity, 1.8) * 255);
                int col = (alpha << 24) | 0x00FFFFFF;
                img.setRGB(cx + dx, y, col);
            }
        }
        g2.dispose();
        ImageIO.write(img, "PNG", file);
    }

    private static void generateEnergyRamp(File file) throws Exception {
        int w = 256, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < w; x++) {
            float pos = (float) x / w;
            int r, g, b;
            if (pos < 0.35f) {
                r = 240; g = 110; b = 15;
            } else if (pos < 0.78f) {
                r = 255; g = 215; b = 30;
            } else {
                r = 255; g = 255; b = 230;
            }
            int rgb = (255 << 24) | (r << 16) | (g << 8) | b;
            for (int y = 0; y < h; y++) {
                img.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(img, "PNG", file);
    }
}
