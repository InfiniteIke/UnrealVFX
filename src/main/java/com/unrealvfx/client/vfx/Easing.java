package com.unrealvfx.client.vfx;

/**
 * Funzioni di interpolazione temporale (Easing) per animare scale, rotazioni, opacità e colori.
 */
public enum Easing {
    LINEAR {
        @Override
        public float ease(float t) {
            return t;
        }
    },
    IN_QUAD {
        @Override
        public float ease(float t) {
            return t * t;
        }
    },
    OUT_QUAD {
        @Override
        public float ease(float t) {
            return t * (2.0f - t);
        }
    },
    IN_OUT_QUAD {
        @Override
        public float ease(float t) {
            return t < 0.5f ? 2.0f * t * t : -1.0f + (4.0f - 2.0f * t) * t;
        }
    },
    OUT_EXPO {
        @Override
        public float ease(float t) {
            return (t == 1.0f) ? 1.0f : 1.0f - (float) Math.pow(2.0, -10.0 * t);
        }
    },
    IN_EXPO {
        @Override
        public float ease(float t) {
            return (t == 0.0f) ? 0.0f : (float) Math.pow(2.0, 10.0 * (t - 1.0));
        }
    },
    OUT_ELASTIC {
        @Override
        public float ease(float t) {
            if (t == 0.0f) return 0.0f;
            if (t == 1.0f) return 1.0f;
            float p = 0.3f;
            return (float) (Math.pow(2.0, -10.0 * t) * Math.sin((t - p / 4.0f) * (2.0f * Math.PI) / p) + 1.0f);
        }
    };

    public abstract float ease(float t);

    public float interpolate(float start, float end, float t) {
        float factor = ease(Math.max(0.0f, Math.min(1.0f, t)));
        return start + (end - start) * factor;
    }
}
