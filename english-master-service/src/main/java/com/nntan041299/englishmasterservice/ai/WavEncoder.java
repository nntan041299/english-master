package com.nntan041299.englishmasterservice.ai;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Wraps raw 16-bit PCM audio (as returned by Gemini's audio-generation models) in a standard WAV
 * container, so it can be played directly by browsers via an {@code <audio>} element.
 */
public final class WavEncoder {

    private static final int BITS_PER_SAMPLE = 16;

    private WavEncoder() {}

    public static byte[] pcm16ToWav(byte[] pcm, int sampleRate, int channels) {
        int byteRate = sampleRate * channels * BITS_PER_SAMPLE / 8;
        int blockAlign = channels * BITS_PER_SAMPLE / 8;
        int dataSize = pcm.length;

        ByteArrayOutputStream out = new ByteArrayOutputStream(44 + dataSize);
        ByteBuffer header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);

        header.put("RIFF".getBytes());
        header.putInt(36 + dataSize);
        header.put("WAVE".getBytes());
        header.put("fmt ".getBytes());
        header.putInt(16); // fmt chunk size
        header.putShort((short) 1); // PCM format
        header.putShort((short) channels);
        header.putInt(sampleRate);
        header.putInt(byteRate);
        header.putShort((short) blockAlign);
        header.putShort((short) BITS_PER_SAMPLE);
        header.put("data".getBytes());
        header.putInt(dataSize);

        out.writeBytes(header.array());
        out.writeBytes(pcm);
        return out.toByteArray();
    }
}
