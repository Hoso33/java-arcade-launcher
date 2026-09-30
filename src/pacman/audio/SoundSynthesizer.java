package pacman.audio;

import pacman.engine.GameEvent;
import pacman.engine.GameEventListener;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 100% Procedural 8-bit Sound Synthesizer using standard Java SE javax.sound.sampled.
 * Synthesizes retro waveforms directly into memory buffers with zero external assets.
 */
public class SoundSynthesizer implements GameEventListener {

    private static final float SAMPLE_RATE = 22050f;
    private final AudioFormat audioFormat;
    private final Map<SoundType, byte[]> soundCache;
    private final ExecutorService soundPool;
    private volatile boolean muted;
    private boolean wakaToggle;
    private long lastWakaTime;

    public SoundSynthesizer() {
        this.audioFormat = new AudioFormat(SAMPLE_RATE, 8, 1, true, false); // 8-bit signed mono
        this.soundCache = new EnumMap<>(SoundType.class);
        this.soundPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "PacmanAudioThread");
            t.setDaemon(true);
            return t;
        });
        this.muted = false;
        this.wakaToggle = false;
        this.lastWakaTime = 0;

        preGenerateSounds();
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void toggleMute() {
        this.muted = !this.muted;
    }

    private void preGenerateSounds() {
        soundCache.put(SoundType.WAKA, generateWakaSound(true));
        soundCache.put(SoundType.POWER_PELLET, generatePowerPelletSound());
        soundCache.put(SoundType.EAT_GHOST, generateEatGhostSound());
        soundCache.put(SoundType.PACMAN_DEATH, generatePacmanDeathSound());
        soundCache.put(SoundType.EAT_FRUIT, generateEatFruitSound());
        soundCache.put(SoundType.EXTRA_LIFE, generateExtraLifeSound());
        soundCache.put(SoundType.GAME_START, generateGameStartSound());
        soundCache.put(SoundType.LEVEL_CLEAR, generateLevelClearSound());
    }

    public void play(SoundType type) {
        if (muted) return;
        byte[] data = soundCache.get(type);
        if (data == null || data.length == 0) return;

        soundPool.submit(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream ais = new AudioInputStream(
                        new ByteArrayInputStream(data),
                        audioFormat,
                        data.length
                );
                clip.open(ais);
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Throwable ignored) {
                // Gracefully fallback if audio hardware is absent/busy
            }
        });
    }

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.getType()) {
            case GAME_START -> play(SoundType.GAME_START);
            case DOT_EATEN -> {
                long now = System.currentTimeMillis();
                if (now - lastWakaTime >= 120) {
                    lastWakaTime = now;
                    playWaka();
                }
            }
            case POWER_PELLET_EATEN -> play(SoundType.POWER_PELLET);
            case GHOST_EATEN -> play(SoundType.EAT_GHOST);
            case PACMAN_DIED -> play(SoundType.PACMAN_DEATH);
            case FRUIT_EATEN -> play(SoundType.EAT_FRUIT);
            case EXTRA_LIFE_EARNED -> play(SoundType.EXTRA_LIFE);
            case LEVEL_CLEARED -> play(SoundType.LEVEL_CLEAR);
            default -> {}
        }
    }

    private void playWaka() {
        if (muted) return;
        wakaToggle = !wakaToggle;
        byte[] data = generateWakaSound(wakaToggle);
        soundPool.submit(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream ais = new AudioInputStream(
                        new ByteArrayInputStream(data),
                        audioFormat,
                        data.length
                );
                clip.open(ais);
                clip.start();
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
            } catch (Throwable ignored) {}
        });
    }

    // --- Waveform Generators ---

    private byte[] generateWakaSound(boolean high) {
        int durationMs = 110;
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples];

        double startFreq = high ? 320.0 : 480.0;
        double endFreq = high ? 520.0 : 260.0;

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double progress = (double) i / numSamples;
            double freq = startFreq + (endFreq - startFreq) * progress;

            // Square/triangle mixed wave
            double phase = t * freq * 2.0 * Math.PI;
            double wave = Math.sin(phase) + 0.3 * Math.signum(Math.sin(phase * 2));
            double env = Math.sin(progress * Math.PI); // Envelope

            buffer[i] = (byte) (wave * env * 90);
        }
        return buffer;
    }

    private byte[] generatePowerPelletSound() {
        int durationMs = 280;
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double freq = 220.0 + 80.0 * Math.sin(t * 30.0);
            double wave = Math.signum(Math.sin(t * freq * 2.0 * Math.PI));
            double env = 1.0 - (double) i / numSamples;
            buffer[i] = (byte) (wave * env * 80);
        }
        return buffer;
    }

    private byte[] generateEatGhostSound() {
        int durationMs = 380;
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples];

        double[] freqs = {250, 350, 500, 750, 1000};
        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            int step = Math.min(freqs.length - 1, (int) (progress * freqs.length));
            double freq = freqs[step];
            double t = (double) i / SAMPLE_RATE;
            double wave = Math.sin(t * freq * 2.0 * Math.PI) + 0.25 * Math.sin(t * freq * 4.0 * Math.PI);
            buffer[i] = (byte) (wave * 95);
        }
        return buffer;
    }

    private byte[] generatePacmanDeathSound() {
        int durationMs = 900;
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            double freq = 800.0 * Math.pow(0.15, progress) + 40.0 * Math.sin(i * 0.1);
            double t = (double) i / SAMPLE_RATE;
            double wave = Math.signum(Math.sin(t * freq * 2.0 * Math.PI));
            double env = (1.0 - progress);
            buffer[i] = (byte) (wave * env * 85);
        }
        return buffer;
    }

    private byte[] generateEatFruitSound() {
        int durationMs = 250;
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples];

        double[] freqs = {523.25, 659.25, 783.99, 1046.50};
        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            int step = Math.min(freqs.length - 1, (int) (progress * freqs.length));
            double freq = freqs[step];
            double t = (double) i / SAMPLE_RATE;
            double wave = Math.sin(t * freq * 2.0 * Math.PI);
            buffer[i] = (byte) (wave * 90);
        }
        return buffer;
    }

    private byte[] generateExtraLifeSound() {
        int durationMs = 600;
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples];

        double[] notes = {330, 392, 659, 523, 587, 784};
        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            int step = Math.min(notes.length - 1, (int) (progress * notes.length));
            double freq = notes[step];
            double t = (double) i / SAMPLE_RATE;
            double wave = Math.sin(t * freq * 2.0 * Math.PI) + 0.2 * Math.signum(Math.sin(t * freq * 2.0 * Math.PI));
            buffer[i] = (byte) (wave * 95);
        }
        return buffer;
    }

    private byte[] generateGameStartSound() {
        int durationMs = 1200;
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples];

        double[] melody = {493.88, 987.77, 739.99, 622.25, 987.77, 739.99, 622.25,
                           523.25, 1046.50, 783.99, 659.25, 1046.50, 783.99, 659.25};
        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            int step = Math.min(melody.length - 1, (int) (progress * melody.length));
            double freq = melody[step];
            double t = (double) i / SAMPLE_RATE;
            double wave = Math.signum(Math.sin(t * freq * 2.0 * Math.PI));
            buffer[i] = (byte) (wave * 70);
        }
        return buffer;
    }

    private byte[] generateLevelClearSound() {
        int durationMs = 800;
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples];

        double[] arpeggio = {440, 554.37, 659.25, 880, 1108.73, 1318.51};
        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            int step = Math.min(arpeggio.length - 1, (int) (progress * arpeggio.length));
            double freq = arpeggio[step];
            double t = (double) i / SAMPLE_RATE;
            double wave = Math.sin(t * freq * 2.0 * Math.PI) + 0.3 * Math.sin(t * freq * 3.0 * Math.PI);
            buffer[i] = (byte) (wave * 85);
        }
        return buffer;
    }

    public void shutdown() {
        soundPool.shutdownNow();
    }
}
