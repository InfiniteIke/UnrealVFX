const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('unrealAPI', {
    isDesktop: true,
    platform: process.platform,
    minimize: () => ipcRenderer.invoke('window-minimize'),
    maximize: () => ipcRenderer.invoke('window-maximize'),
    close: () => ipcRenderer.invoke('window-close'),
    isMaximized: () => ipcRenderer.invoke('window-is-maximized'),
    saveEffectDialog: (defaultName, jsonContent) => ipcRenderer.invoke('save-effect-dialog', { defaultName, jsonContent }),
    quickSaveToMod: (effectId, jsonContent) => ipcRenderer.invoke('quick-save-to-mod', { effectId, jsonContent }),
    loadEffectDialog: () => ipcRenderer.invoke('load-effect-dialog'),
    openModFolder: () => ipcRenderer.invoke('open-mod-folder'),
    getModEffectsDir: () => ipcRenderer.invoke('get-mod-effects-dir')
});
