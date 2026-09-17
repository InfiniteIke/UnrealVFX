const { app, BrowserWindow, ipcMain, dialog, shell } = require('electron');
const path = require('path');
const fs = require('fs');

// -----------------------------------------------------------------------------
// DEDICATED HARDWARE GPU ACCELERATION FLAGS
// -----------------------------------------------------------------------------
app.commandLine.appendSwitch('enable-gpu-rasterization');
app.commandLine.appendSwitch('enable-zero-copy');
app.commandLine.appendSwitch('ignore-gpu-blocklist');
app.commandLine.appendSwitch('force_high_performance_gpu');
app.commandLine.appendSwitch('disable-software-rasterizer');
app.commandLine.appendSwitch('enable-features', 'VaapiVideoDecoder,CanvasOopRasterization');

let mainWindow = null;

function getModEffectsDirs() {
    const projectRoot = path.resolve(__dirname, '..');
    const dirs = [
        path.join(projectRoot, 'config', 'unrealvfx', 'effects'),
        path.join(projectRoot, 'run', 'client', 'config', 'unrealvfx', 'effects')
    ];
    return dirs;
}

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1480,
        height: 940,
        minWidth: 1100,
        minHeight: 720,
        frame: false,
        show: false,
        backgroundColor: '#07070b',
        title: 'UnrealVFX Studio',
        icon: path.join(__dirname, 'icon.png'),
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
            nodeIntegration: false,
            contextIsolation: true,
            backgroundThrottling: true,
            webSecurity: true
        }
    });

    mainWindow.loadFile(path.join(__dirname, 'index.html'));

    mainWindow.once('ready-to-show', () => {
        mainWindow.show();
    });

    mainWindow.on('closed', () => {
        mainWindow = null;
    });
}

app.whenReady().then(() => {
    createWindow();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

// -----------------------------------------------------------------------------
// IPC HANDLERS: WINDOW MANAGEMENT
// -----------------------------------------------------------------------------
ipcMain.handle('window-minimize', () => {
    if (mainWindow) mainWindow.minimize();
    return true;
});

ipcMain.handle('window-maximize', () => {
    if (!mainWindow) return false;
    if (mainWindow.isMaximized()) {
        mainWindow.unmaximize();
        return false;
    } else {
        mainWindow.maximize();
        return true;
    }
});

ipcMain.handle('window-is-maximized', () => {
    return mainWindow ? mainWindow.isMaximized() : false;
});

ipcMain.handle('window-close', () => {
    if (mainWindow) mainWindow.close();
    return true;
});

// -----------------------------------------------------------------------------
// IPC HANDLERS: MINECRAFT MOD FILE SYSTEM BRIDGE
// -----------------------------------------------------------------------------
ipcMain.handle('get-mod-effects-dir', () => {
    const dirs = getModEffectsDirs();
    return dirs[0];
});

ipcMain.handle('open-mod-folder', async () => {
    const dirs = getModEffectsDirs();
    const primaryDir = dirs[0];
    if (!fs.existsSync(primaryDir)) {
        fs.mkdirSync(primaryDir, { recursive: true });
    }
    await shell.openPath(primaryDir);
    return true;
});

ipcMain.handle('quick-save-to-mod', async (event, { effectId, jsonContent }) => {
    try {
        const safeId = (effectId || 'custom_effect').toLowerCase().replace(/[^a-z0-9_-]/g, '_');
        const filename = `${safeId}.json`;
        const dirs = getModEffectsDirs();
        const savedPaths = [];

        dirs.forEach(dir => {
            try {
                if (!fs.existsSync(dir)) {
                    fs.mkdirSync(dir, { recursive: true });
                }
                const targetPath = path.join(dir, filename);
                fs.writeFileSync(targetPath, jsonContent, 'utf8');
                savedPaths.push(targetPath);
            } catch (err) {
                console.warn(`Could not save to ${dir}:`, err);
            }
        });

        if (savedPaths.length === 0) {
            return { success: false, error: 'Failed to write to any config directory' };
        }

        return { success: true, filename, savedPaths };
    } catch (e) {
        return { success: false, error: e.message };
    }
});

ipcMain.handle('save-effect-dialog', async (event, { defaultName, jsonContent }) => {
    try {
        const dirs = getModEffectsDirs();
        const defaultPath = path.join(dirs[0], `${defaultName || 'vfx_effect'}.json`);

        const { canceled, filePath } = await dialog.showSaveDialog(mainWindow, {
            title: 'Salva Effetto UnrealVFX (.json)',
            defaultPath: defaultPath,
            filters: [{ name: 'UnrealVFX Schematic JSON', extensions: ['json'] }]
        });

        if (canceled || !filePath) return { success: false, canceled: true };

        fs.writeFileSync(filePath, jsonContent, 'utf8');
        return { success: true, filePath };
    } catch (e) {
        return { success: false, error: e.message };
    }
});

ipcMain.handle('load-effect-dialog', async () => {
    try {
        const dirs = getModEffectsDirs();
        const defaultDir = fs.existsSync(dirs[0]) ? dirs[0] : path.resolve(__dirname, '..');

        const { canceled, filePaths } = await dialog.showOpenDialog(mainWindow, {
            title: 'Carica Effetto UnrealVFX (.json)',
            defaultPath: defaultDir,
            filters: [{ name: 'UnrealVFX Schematic JSON', extensions: ['json'] }],
            properties: ['openFile']
        });

        if (canceled || !filePaths || filePaths.length === 0) return { success: false, canceled: true };

        const raw = fs.readFileSync(filePaths[0], 'utf8');
        return { success: true, filePath: filePaths[0], content: raw };
    } catch (e) {
        return { success: false, error: e.message };
    }
});
