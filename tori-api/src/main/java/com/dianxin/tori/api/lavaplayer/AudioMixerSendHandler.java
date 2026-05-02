package com.dianxin.tori.api.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Audio mixer handler that combines multiple Lavaplayer audio sources into a single
 * audio stream for Discord transmission.
 * <p>
 * This class allows registering multiple audio players (e.g., music, TTS, sound effects)
 * on different channels and mixes their outputs together. It implements {@link AudioSendHandler}
 * to provide 20ms audio frames to JDA's audio system.
 * <p>
 * The mixing process:
 * <ol>
 *     <li>Collects audio frames from all registered players</li>
 *     <li>Sums the PCM samples (accumulation)</li>
 *     <li>Applies clipping to prevent audio distortion</li>
 *     <li>Converts the mixed signal back to byte format for transmission</li>
 * </ol>
 * <p>
 * Usage example:
 * <pre>{@code
 * AudioMixerSendHandler mixer = new AudioMixerSendHandler();
 * mixer.registerChannel("MUSIC", musicPlayer);
 * mixer.registerChannel("TTS", ttsPlayer);
 * audioConnection.setAudioSendHandler(mixer);
 * }</pre>
 *
 * @see com.sedmelluq.discord.lavaplayer.player.AudioPlayer
 */
@SuppressWarnings("unused")
public class AudioMixerSendHandler implements AudioSendHandler {

    // List of active audio streams (Example: "MUSIC" -> MusicPlayer, "TTS" -> TTSPlayer)
    private final Map<String, AudioPlayer> players = new ConcurrentHashMap<>();

    // Last mixed audio frame ready for transmission
    private byte[] lastMixedFrame;

    /**
     * Registers an AudioPlayer to a specific channel.
     * @param channelId Channel name (Ex: "MUSIC", "TTS")
     * @param player Lavaplayer AudioPlayer instance
     */
    public void registerChannel(String channelId, AudioPlayer player) {
        players.put(channelId, player);
    }

    public void removeChannel(String channelId) {
        players.remove(channelId);
    }

    @Override
    public boolean canProvide() {
        // Variable storing the sum of audio waves before converting to byte format
        // Standard Opus frame is 3840 bytes (1920 samples * 2 channels)
        int[] mixedAudio = new int[1920 * 2];
        boolean hasAudio = false;

        for (AudioPlayer player : players.values()) {
            AudioFrame frame = player.provide();
            if (frame != null) {
                hasAudio = true;
                mixAudio(mixedAudio, frame.getData());
            }
        }

        if (hasAudio) {
            this.lastMixedFrame = convertToBytes(mixedAudio);
            return true;
        }

        return false;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastMixedFrame);
    }

    @Override
    public boolean isOpus() {
        return false; // MUST RETURN FALSE. When mixing audio, we work with raw PCM signal (not compressed).
    }

    /**
     * Audio signal mixing algorithm (PCM 16-bit Big-Endian).
     * Accumulates samples from the provided audio frame into the mixed audio array.
     */
    private void mixAudio(int[] mixedAudio, byte[] frameData) {
        for (int i = 0; i < frameData.length; i += 2) {
            // Convert 2 bytes to 1 short number (16-bit PCM)
            short sample = (short) ((frameData[i] << 8) | (frameData[i + 1] & 0xFF));

            // Accumulate audio waves
            mixedAudio[i / 2] += sample;
        }
    }

    /**
     * Converts the accumulated integer audio array to a byte array for transmission.
     * Includes integrated clipping algorithm to prevent audio distortion if volume is too loud.
     */
    private byte[] convertToBytes(int[] mixedAudio) {
        byte[] result = new byte[mixedAudio.length * 2];
        for (int i = 0; i < mixedAudio.length; i++) {
            int sample = mixedAudio[i];

            // Clipping: Limit audio amplitude within the range of 16-bit short (-32768 to 32767)
            if (sample > 32767) sample = 32767;
            else if (sample < -32768) sample = -32768;

            short shortSample = (short) sample;
            result[i * 2] = (byte) (shortSample >> 8);
            result[i * 2 + 1] = (byte) (shortSample & 0xFF);
        }
        return result;
    }
}