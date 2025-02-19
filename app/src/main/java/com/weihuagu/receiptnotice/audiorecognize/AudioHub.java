package com.weihuagu.receiptnotice.audiorecognize;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

import java.io.IOException;

public class AudioHub {
    private final static int sampleRate = 8000;
    private static final int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private final AudioRecord recorder;
    private final int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate,
            channelConfig, AudioFormat.ENCODING_PCM_16BIT);
    private boolean isRecord = false;
    private int bufferSize = 0;

    public AudioHub() throws IOException {
        bufferSize = bufferSizeInBytes;
        recorder = new AudioRecord(
                AudioSource.VOICE_RECOGNITION, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
        if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
            recorder.release();
            throw new IOException(
                    "Failed to initialize recorder. Microphone might be already in use.");
        }
    }

    private void startRecord() {
        recorder.startRecording();
        // 让录制状态为true
        isRecord = true;
        // 开启音频文件写入线程
        new Thread(new AudioRecordThread()).start();
    }

    private void stopRecord() {
        close();
    }

    private void close() {
        if (recorder != null) {
            System.out.println("stopRecord");
            isRecord = false;
            recorder.stop();
            recorder.release();//释放资源
        }
    }

    private void getRecordData() {
        short[] buffer = new short[bufferSize];
        recorder.read(buffer, 0, buffer.length); // Skip the first buffer, usually zeroes

    }

    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            getRecordData();//往文件中写入裸数据

        }
    }


}
