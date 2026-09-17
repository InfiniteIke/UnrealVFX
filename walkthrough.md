# UnrealVFX: Master Architecture & Project State

Documento tecnico completo per il passaggio di contesto tra sessioni di sviluppo. Contiene l'architettura dettagliata dell'engine Minecraft (Java/GLSL), del visual editor 3D e lo stato aggiornato di tutti i moduli.

---

## 1. Mappa Architetturale del Codebase (Java & Engine)

### Core & Registries:
* [`com.unrealvfx.UnrealVFX`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/java/com/unrealvfx/UnrealVFX.java): Entrypoint principale del mod (Forge/Fabric).
* [`com.unrealvfx.api.UnrealVFXAPI`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/java/com/unrealvfx/api/UnrealVFXAPI.java): Interfaccia pubblica per invocare VFX da codice o da altri mod.
* [`com.unrealvfx.schematic.VFXSchematicRegistry`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/java/com/unrealvfx/schematic/VFXSchematicRegistry.java): Registrazione e hot-reloading dei file JSON da `config/unrealvfx/effects/*.json`.

### Pipeline di Rendering & Shaders:
* [`com.unrealvfx.client.render.ShaderProgram`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/java/com/unrealvfx/client/render/ShaderProgram.java): Compilazione, binding e gestione uniformi degli shader GLSL.
* [`com.unrealvfx.client.render.BloomRenderer`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/java/com/unrealvfx/client/render/BloomRenderer.java): Pass di post-processing per neon bloom multilivello e HDR glow.
* [`com.unrealvfx.client.render.ImpactFrameManager`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/java/com/unrealvfx/client/render/ImpactFrameManager.java): Gestione dei flash a frame singolo (Manga B&W, Negative Invert, Hakai).
* [`com.unrealvfx.client.render.AnimeSketchManager`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/java/com/unrealvfx/client/render/AnimeSketchManager.java): Rendering delle linee d'azione e sketch sakuga in tempo reale.
* [`com.unrealvfx.client.render.DepthCaptureHelper`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/java/com/unrealvfx/client/render/DepthCaptureHelper.java): Cattura del buffer di profondità del mondo per rifrazione termica e occlusioni ottiche.

### Componenti VFX Modulari ([`com.unrealvfx.client.vfx.component`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/java/com/unrealvfx/client/vfx/component)):
1. **`MeshComponent.java`**: Primitiva geometrica 3D (Sfere, Capsule Ki, Parallelepipedi, Coni, Cupole, Cilindri) associata a uno shader GLSL.
2. **`LightComponent.java`**: Luci dinamiche e volumetriche (Point, Spot con godrays e scattering Henyey-Greenstein, Colonna di luce, Dischi a terra).
3. **`HeatDistortionComponent.java`**: Distorsione termica e rifrazione atmosferica senza macchie di colore (0% tint).
4. **`GravitationalPullComponent.java`**: Attrazione fisica di mob, entità e item caduti nel mondo verso la singolarità.
5. **`GravitationalLensingComponent.java`**: Calcolo ottico della curvatura della luce e dell'anello di Einstein.
6. **`BeamComponent.java`**: Raggi continui con anime windup, guaina di plasma e particelle orbitanti a doppia elica.
7. **`LightningComponent.java`**: Archi voltaici e ribbon procedurali a doppio passaggio.
8. **`FloatingDebrisComponent.java`**: Simulazione fisica a 3 fasi (Levitazione con tremore sismico $\to$ Suzione a vortice $\to$ Dispersione esplosiva).
9. **`ShockwaveComponent.java`**: Onde d'urto piane con curve di easing (`OUT_QUAD`, `IN_QUAD`).
10. **`DistortionComponent.java`**: Bolle di rifrazione gravitazionale screen-space.
11. **`CameraShakeComponent.java`**: Trauma decadente a legge di potenza, FOV kick e tremolio a bassa frequenza.
12. **`VelocityParticleComponent.java`**: Particelle orientate al moto con attrazione/suzione centripeta.

---

## 2. Catalogo Shaders GLSL ([`src/main/resources/assets/unrealvfx/shaders/`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/src/main/resources/assets/unrealvfx/shaders/))

* **Cosmici & Relatività:**
  * `black_hole_gargantua.frag`: Raymarching pinhole 3D su quad orientato alla camera, assorbimento totale dell'orizzonte degli eventi e gradiente termico a 5 stop.
  * `celestial_void.frag`: Pianeta di vuoto nero con corona elettrica e vene di plasma.
  * `gravitational_lens.frag`: Curvatura dello spazio-tempo e deformazione di sfondo.
* **Luci & Atmosfera:**
  * `volumetric_light.frag`: Coni di luce con scattering volumetrico e pulviscolo 3D.
  * `aurora_borealis.frag`: Cortine spettrali ondulatorie multiharmoniche verde-magenta.
  * `cyber_grid_shaft.frag`: Fascio olografico con reticolo esagonale e glitch digitale.
  * `heat_distortion.frag`: Rifrazione atmosferica trasparente con modulazione di rumore.
* **Anime & Sakuga:**
  * `aura_cel.frag`: Fiamme Ki in stile ArcSys con cadenza a scatti (Sakuga FPS) e cel-shading.
  * `celestial_beam.frag`: Pilastro di luce quantizzato a 3 bande con anima incandescente.
  * `energy_disc.frag`: Disco tagliente di plasma (Kienzan) con denti a sega rotanti.
  * `impact_frame.frag`: Inversione cromatica binaria per impatti pesanti.

---

## 3. Studio Editor 3D Web/Desktop ([`editor/index.html`](file:///c:/Users/emanu/IdeaProjects/UnrealVFX/editor/index.html))

* **Motore Grafico:** Three.js + Post-Processing (UnrealBloomPass + CopyShader) con parità shader 1:1.
* **Accelerazione Hardware GPU:**
  * `powerPreference: "high-performance"`, `precision: "highp"`, `preserveDrawingBuffer: false`.
  * Rilevamento automatico hardware tramite `WEBGL_debug_renderer_info` con badge in tempo reale nell'header.
* **Design System Apple HIG & Liquid Glass:**
  * Superfici translucide con `backdrop-filter: blur(24px) saturate(190%)` e bordi speculari ultra-sottili.
  * Segmented Controls macOS, Scrubber progressivo con gradiente e tipografia SF Pro con `tabular-nums`.
  * Sistema di notifiche Toast HUD fluttuanti per esportazione, snapshot e cambio preset.
  * Scorciatoie da tastiera: <kbd>Space</kbd> (Play/Pausa), <kbd>W</kbd>/<kbd>E</kbd>/<kbd>R</kbd> (Gizmo), <kbd>B</kbd> (Bloom), <kbd>G</kbd> (Ground), <kbd>⌘S</kbd> (Esporta JSON).

---

## 4. Comandi di Test Rapidi

```text
/vfx spawn cosmic:gargantua 3.0
/vfx spawn divine:sanctuary 1.0
/vfx spawn anime:beam_cannon 1.0
/vfx spawn cosmic:reality_singularity 2.0
```

