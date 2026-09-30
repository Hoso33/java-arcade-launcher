package snake.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Monophonic Piezo Beeper recreating authentic Nokia 3310 square-wave audio using standard Java SE.
 */
public class NokiaBeeper {

    private static final float SAMPLE_RATE = 22050f;
    private final AudioFormat format;
    private final ExecutorService audioPool;
    private volatile boolean muted;

    public NokiaBeeper() {
        this.format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false); // 8-bit mono PCM
        this.audioPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "NokiaBeeperThread");
            t.setDaemon(true);
            return t;
        });
        this.muted = false;
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

    public void playEatTone() {
        if (muted) return;
        playToneSequence(new int[]{2400}, new int[]{28});
    }

    public void playCrashTone() {
        if (muted) return;
        playToneSequence(new int[]{280, 220, 160, 110}, new int[]{70, 70, 70, 120});
    }

    public void playStartTone() {
        if (muted) return;
        playToneSequence(new int[]{1318, 1174, 739, 830}, new int[]{90, 90, 120, 180});
    }

    public void playHighScoreTone() {
        if (muted) return;
        playToneSequence(new int[]{1046, 1318, 1567, 2093}, new int[]{60, 60, 60, 150});
    }

    private void playToneSequence(int[] frequencies, int[] durationsMs) {
        audioPool.submit(() -> {
            try {
                int totalMs = 0;
                for (int d : durationsMs) totalMs += d;

                int totalSamples = (int) (SAMPLE_RATE * (totalMs / 1000.0));
                byte[] buffer = new byte[totalSamples];

                int sampleIndex = 0;
                for (int i = 0; i < frequencies.length; i++) {
                    int freq = frequencies[i];
                    int durMs = durationsMs[i];
                    int noteSamples = (int) (SAMPLE_RATE * (durMs / 1000.0));

                    for (int s = 0; s < noteSamples && sampleIndex < totalSamples; s++, sampleIndex++) {
                        double t = (double) s / SAMPLE_RATE;
                        // Crisp pure square wave
                        double wave = Math.signum(Math.sin(2.0 * Math.PI * freq * t));
                        buffer[sampleIndex] = (byte) (wave * 80);
                    }
                }

                Clip clip = AudioSystem.getClip();
                AudioInputStream ais = new AudioInputStream(
                        new ByteArrayInputStream(buffer),
                        format,
                        buffer.length
                );
                clip.open(ais);
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Throwable ignored) {
                // Graceful fallback if audio device is unavailable
            }
        });
    }

    public void shutdown() {
        audioPool.shutdownNow();
    }
}
