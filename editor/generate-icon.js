const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

// Create a 256x256 PNG with Apple Pro Squircle & Liquid Glass Prism Icon
const width = 256;
const height = 256;
const buffer = Buffer.alloc(width * height * 4);

function crc32(buf) {
    let table = new Uint32Array(256);
    for (let i = 0; i < 256; i++) {
        let c = i;
        for (let j = 0; j < 8; j++) {
            c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1);
        }
        table[i] = c;
    }
    let crc = 0xFFFFFFFF;
    for (let i = 0; i < buf.length; i++) {
        crc = (crc >>> 8) ^ table[(crc ^ buf[i]) & 0xFF];
    }
    return (crc ^ 0xFFFFFFFF) >>> 0;
}

function makeChunk(type, data) {
    const len = Buffer.alloc(4);
    len.writeUInt32BE(data.length, 0);
    const typeBuf = Buffer.from(type, 'ascii');
    const body = Buffer.concat([typeBuf, data]);
    const crcBuf = Buffer.alloc(4);
    crcBuf.writeUInt32BE(crc32(body), 0);
    return Buffer.concat([len, body, crcBuf]);
}

// Generate pixels: Apple dark glass squircle with cyan/purple cosmic gradient & prism diamond
const radius = 54; // Squircle corner radius
for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
        const idx = (y * width + x) * 4;

        // Check squircle bounds (superellipse n=4)
        const nx = Math.abs((x - 128) / 108);
        const ny = Math.abs((y - 128) / 108);
        const squircleDist = Math.pow(nx, 4.2) + Math.pow(ny, 4.2);

        if (squircleDist <= 1.0) {
            // Anti-aliased edge factor
            const edgeAlpha = Math.min(1.0, Math.max(0.0, (1.0 - squircleDist) * 12.0));

            // Background gradient (Deep space dark violet to obsidian)
            const gradY = y / height;
            let r = Math.round(12 + gradY * 18);
            let g = Math.round(16 + gradY * 12);
            let b = Math.round(32 + gradY * 36);

            // Specular border glow
            if (squircleDist > 0.85) {
                const borderFactor = (squircleDist - 0.85) / 0.15;
                r = Math.round(r + borderFactor * (100 - gradY * 40));
                g = Math.round(g + borderFactor * (210 - gradY * 80));
                b = Math.round(b + borderFactor * 255);
            }

            // Central 3D Prism Diamond
            const dx = Math.abs(x - 128);
            const dy = Math.abs(y - 128);
            if (dx + dy < 64) {
                const dFactor = 1.0 - (dx + dy) / 64.0;
                // Cosmic Cyan & Magenta shine
                r = Math.round(r * (1 - dFactor) + (191 * dFactor));
                g = Math.round(g * (1 - dFactor) + (90 * dFactor + (1.0 - gradY) * 120 * dFactor));
                b = Math.round(b * (1 - dFactor) + (255 * dFactor));
            }

            // Top-left specular sheen (Liquid Glass highlight)
            const distTopLeft = Math.hypot(x - 64, y - 64);
            if (distTopLeft < 90) {
                const sheen = Math.pow(1.0 - distTopLeft / 90.0, 2) * 0.45;
                r = Math.min(255, Math.round(r + sheen * 255));
                g = Math.min(255, Math.round(g + sheen * 255));
                b = Math.min(255, Math.round(b + sheen * 255));
            }

            buffer[idx] = r;
            buffer[idx + 1] = g;
            buffer[idx + 2] = b;
            buffer[idx + 3] = Math.round(255 * edgeAlpha);
        } else {
            // Transparent outside squircle
            buffer[idx] = 0;
            buffer[idx + 1] = 0;
            buffer[idx + 2] = 0;
            buffer[idx + 3] = 0;
        }
    }
}

// Convert to raw scanlines with filter byte 0
const scanlines = Buffer.alloc(height * (1 + width * 4));
for (let y = 0; y < height; y++) {
    const rowOffset = y * (1 + width * 4);
    scanlines[rowOffset] = 0; // Filter 0 (None)
    buffer.copy(scanlines, rowOffset + 1, y * width * 4, (y + 1) * width * 4);
}

const compressed = zlib.deflateSync(scanlines);

const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

const ihdrData = Buffer.alloc(13);
ihdrData.writeUInt32BE(width, 0);
ihdrData.writeUInt32BE(height, 4);
ihdrData.writeUInt8(8, 8); // 8-bit depth
ihdrData.writeUInt8(6, 9); // RGBA
ihdrData.writeUInt8(0, 10); // Compression
ihdrData.writeUInt8(0, 11); // Filter
ihdrData.writeUInt8(0, 12); // Interlace

const ihdr = makeChunk('IHDR', ihdrData);
const idat = makeChunk('IDAT', compressed);
const iend = makeChunk('IEND', Buffer.alloc(0));

const png = Buffer.concat([signature, ihdr, idat, iend]);
const outPath = path.join(__dirname, 'icon.png');
fs.writeFileSync(outPath, png);
console.log(`✅ Apple Pro App Icon created at ${outPath} (${png.length} bytes)`);
