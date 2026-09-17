package com.unrealvfx.client.render;

import com.unrealvfx.UnrealVFX;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestore avanzato di Shader GLSL per LWJGL 2 / Minecraft 1.7.10.
 * Supporta caricamento da assets, compilazione, linking e uniform caching.
 */
public class ShaderProgram {
    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;
    private final Map<String, Integer> uniformLocationCache = new HashMap<>();
    private static int activeProgram = 0;
    private boolean valid = true;

    public ShaderProgram(ResourceLocation vertLocation, ResourceLocation fragLocation) {
        String vertSource = loadResource(vertLocation);
        String fragSource = loadResource(fragLocation);

        this.vertexShaderId = compileShader(vertSource, GL20.GL_VERTEX_SHADER, vertLocation.toString());
        this.fragmentShaderId = compileShader(fragSource, GL20.GL_FRAGMENT_SHADER, fragLocation.toString());

        if (this.vertexShaderId == -1 || this.fragmentShaderId == -1) {
            this.valid = false;
            this.programId = 0;
            return;
        }

        this.programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);
        GL20.glLinkProgram(programId);

        int linkStatus = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS);
        if (linkStatus == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(programId, 1024);
            UnrealVFX.LOGGER.error("Errore nel linking dello shader program: " + log);
            this.valid = false;
        }
    }

    private static int compileShader(String source, int type, String path) {
        if (source == null || source.isEmpty()) {
            UnrealVFX.LOGGER.error("Sorgente shader vuota per: " + path);
            return -1;
        }

        int shaderId = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);

        int compileStatus = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
        if (compileStatus == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shaderId, 1024);
            UnrealVFX.LOGGER.error("Errore di compilazione dello shader (" + path + "): " + log);
            GL20.glDeleteShader(shaderId);
            return -1;
        }
        return shaderId;
    }

    private static String loadResource(ResourceLocation location) {
        try {
            InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            String res = sb.toString();
            // Rimozione preventiva del Byte Order Mark (BOM) UTF-8 per evitare syntax error nel compilatore GLSL
            if (res.startsWith("\uFEFF")) {
                res = res.substring(1);
            }
            return res.trim();
        } catch (Exception e) {
            UnrealVFX.LOGGER.error("Impossibile caricare la risorsa dello shader: " + location, e);
            return "";
        }
    }

    public void use() {
        if (!valid) {
            GL20.glUseProgram(0);
            activeProgram = 0;
            return;
        }
        if (activeProgram != programId) {
            GL20.glUseProgram(programId);
            activeProgram = programId;
        }
    }

    public boolean isValid() {
        return valid;
    }

    public static void release() {
        if (activeProgram != 0) {
            GL20.glUseProgram(0);
            activeProgram = 0;
        }
    }

    private int getUniformLocation(String name) {
        if (uniformLocationCache.containsKey(name)) {
            return uniformLocationCache.get(name);
        }
        int location = GL20.glGetUniformLocation(programId, name);
        uniformLocationCache.put(name, location);
        return location;
    }

    public void setFloat(String name, float value) {
        int loc = getUniformLocation(name);
        if (loc != -1) GL20.glUniform1f(loc, value);
    }

    public void setVec2(String name, float x, float y) {
        int loc = getUniformLocation(name);
        if (loc != -1) GL20.glUniform2f(loc, x, y);
    }

    public void setVec3(String name, float x, float y, float z) {
        int loc = getUniformLocation(name);
        if (loc != -1) GL20.glUniform3f(loc, x, y, z);
    }

    public void setVec4(String name, float x, float y, float z, float w) {
        int loc = getUniformLocation(name);
        if (loc != -1) GL20.glUniform4f(loc, x, y, z, w);
    }

    public void setInt(String name, int value) {
        int loc = getUniformLocation(name);
        if (loc != -1) GL20.glUniform1i(loc, value);
    }

    public void delete() {
        GL20.glDetachShader(programId, vertexShaderId);
        GL20.glDetachShader(programId, fragmentShaderId);
        GL20.glDeleteShader(vertexShaderId);
        GL20.glDeleteShader(fragmentShaderId);
        GL20.glDeleteProgram(programId);
    }
}
