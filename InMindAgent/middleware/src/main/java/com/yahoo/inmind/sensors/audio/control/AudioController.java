package com.yahoo.inmind.sensors.audio.control;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.FileUploader;
import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.comm.streaming.model.AudioRecordEvent;
import com.yahoo.inmind.comm.generic.model.MBRequest;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by oscarr on 2/26/15.
 */
public class AudioController extends Thread{
    private static final String TAG = "AudioController";
    private boolean recording;
    private boolean stopped = false;

    private static AudioConfig currentRecorder = null;
    private static MessageBroker mb;
    private static AudioController instance;
    private static Map<Object, AudioConfig> configurations;

    private AudioController( MessageBroker mb){
        this();
        this.mb = mb;
    }

    /**
     * Give the thread high priority so that it's not canceled unexpectedly, and start it
     */
    private AudioController(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        if( configurations == null ){
            configurations = Collections.synchronizedMap(new LinkedHashMap());
        }
    }

    /**
     * Singleton
     * @param sampleRateInHz
     * @param channelConfig
     * @param audioEncoding
     * @param mb
     * @param subscriber
     * @return
     */
    public static AudioController getInstance( Integer sampleRateInHz, Integer channelConfig,
                                               Integer audioEncoding, Integer bufferElements2Rec,
                                               Integer bytesPerElement, MessageBroker mb,
                                               Object subscriber, String fileExtension, String mimeType ) {
        if (instance == null) {
            instance = new AudioController( mb );
        }
        // add the subscriber to the waiting list
        if( configurations.containsKey( subscriber ) == false ){
            AudioConfig config = new AudioConfig( sampleRateInHz, channelConfig, audioEncoding,
                    bufferElements2Rec, bytesPerElement, subscriber, fileExtension, mimeType );
            configurations.put( subscriber, config );
            if( currentRecorder == null ){
                currentRecorder = config;
            }
        }
        if( instance.isAlive() == false ){
            instance.start();
        }
        return instance;
    }

    /**
     * Singleton
     * @param mb
     * @return
     */
    public static AudioController getInstance( MessageBroker mb ) {
        if (instance == null ) { //ojrl  || instance.isAlive() == false
            instance = new AudioController( mb );
        }
        if( instance.isAlive() == false ){
            instance.start();
        }
        return instance;
    }


    @Override
    public void run(){

        try{
            /*
             * Loops until something outside of this thread stops it.
             * Reads the data from the currentRecorder and writes it to the audio track for playback.
             */
            while ( !stopped ) {
                initializeRecordObj();
                while( recording && currentRecorder != null ) {
                    currentRecorder.recordAndSend();

//                    recording = false;
//                    stopped = true;
                }
            }
        } catch(Throwable x){
            Log.w("Audio", "Error reading voice audio", x);
        }
    }


    /**
     * Frees the thread's resources after the loop completes so that it can be run again
     */
    public void releaseController(){
        stopped = true;
        for( AudioConfig ac : configurations.values() ){
            ac.release();
        }
        configurations.clear();
        configurations = null;
        instance.interrupt();
        currentRecorder = null;
        instance = null;
    }

    /**
     * Initialize buffer to hold continuously recorded audio data, start recording, and start
     * playback.
     */
    private void initializeRecordObj() {
        try {
            currentRecorder.getRecorder().startRecording();
            recording = true;
        }catch(Exception e){
            currentRecorder.sendErrorMessage();
            unsubscribe( currentRecorder.getSubscriber() );
        }
    }


    //convert short to byte
    private byte[] short2byte( short[] sData ) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for ( int i = 0; i < shortArrsize; i++ ) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    /**
     * Called from outside of the thread in order to stop the recording/playback loop
     */
    public void unsubscribe( Object subscriber ){
        for( Object subs : configurations.keySet() ){
            if( subs == subscriber ){
                AudioConfig config = configurations.remove( subs );
                config.release();
                break;
            }
        }
        if( configurations != null && configurations.isEmpty() ) {
            releaseController();
        } else if( configurations != null && configurations.values().size() > 0 ){
            Object key = configurations.keySet().iterator().next(); //the next config in the
            currentRecorder = configurations.get(key);
            recording = false;
        }
    }


    /**
     * Helper class that contains each particular audio record configuration and the corresponding subscriber
     */
    static class AudioConfig{
        private AudioRecord recorder;
        private int bufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
        private int bytesPerElement = 2; // 2 bytes in 16bit format
        private int sampleRateInHz = 44100;
        private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        private String fileExtension = "acc"; //acc
        private String mimeType = MediaFormat.MIMETYPE_AUDIO_AAC; //"audio/mp4a-latm";
        private int sizeInBytes;
        private short[] buffer;
        private boolean flagRecord = true;
        private boolean isNewConfiguration = true;
        private Object subscriber;
        private MediaFormat audioFormat;
        private MediaCodec codec;
        private final long timeout = 10000;
        private int bufferSize = bufferElements2Rec * bytesPerElement;
        private boolean firstTime = true;


        AudioConfig(Integer sampleRate, Integer channelConfig, Integer audioEncoding,
                    Integer bufferElements2Rec, Integer bytesPerElement, Object subscriber,
                    String fileExtension, String mimeType) {
            this.subscriber = subscriber;
            if( sampleRate != null ) {
                this.sampleRateInHz = sampleRate;
            }
            if( channelConfig != null && (channelConfig == AudioFormat.CHANNEL_IN_STEREO
                    || channelConfig == AudioFormat.CHANNEL_IN_MONO ) ){
                this.channelConfig = channelConfig;
            }
            if( audioEncoding != null && audioEncoding >= AudioFormat.ENCODING_DEFAULT
                    && audioEncoding <= AudioFormat. ENCODING_E_AC3 ) {
                this.audioEncoding = audioEncoding;
            }
            if( bufferElements2Rec != null && bufferElements2Rec >
                    AudioRecord.getMinBufferSize( this.sampleRateInHz
                            , this.channelConfig, this.audioEncoding)  ){
                this.bufferElements2Rec = bufferElements2Rec;
            } else{
                this.bufferElements2Rec = AudioRecord.getMinBufferSize( this.sampleRateInHz
                        , this.channelConfig, this.audioEncoding);
            }
            if( bytesPerElement != null && bytesPerElement > 1){
                this.bytesPerElement = bytesPerElement;
            }
            if( fileExtension != null && !fileExtension.equals("") ){
                this.fileExtension = fileExtension;
            }
            if( mimeType != null && !mimeType.equals("") ){
                this.mimeType = mimeType;
            }

            bufferSize = this.bufferElements2Rec * this.bytesPerElement;
            recorder = new AudioRecord( MediaRecorder.AudioSource.MIC,
                this.sampleRateInHz,
                this.channelConfig,
                this.audioEncoding,
                this.bufferElements2Rec * this.bytesPerElement);
            buffer =  new short[bufferElements2Rec];


            try {
                audioFormat = new MediaFormat();
                audioFormat.setString(MediaFormat.KEY_MIME, this.mimeType);
                audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                        MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, this.sampleRateInHz);
                audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT,
                        channelConfig == AudioFormat.CHANNEL_IN_MONO? 1 : 2);
                int bitRate =  64 * 1024; //64000;
                audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate );
                //audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, sampleRate); //bufferSize
                codec = MediaCodec.createByCodecName( getEncoderNamesForType( this.mimeType ).get(0) ); //"OMX.google.aac.encoder");
                codec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                codec.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public AudioRecord getRecorder() {
            return recorder;
        }

        public synchronized void release(){
            flagRecord = false;
            currentRecorder.getRecorder().release();
            if( recorder != null ) {
                recorder.release();
                recorder = null;
            }
            audioFormat = null;
            if( codec != null ) {
                codec.stop();
                codec.release();
                codec = null;
            }
        }

        /**
         * Builds an AudioRecordEvent containing the read buffer and send it to all subscribers
         */
        public synchronized void recordAndSend() {
            if( flagRecord ) {
                sizeInBytes =  recorder.read(buffer, 0, bufferElements2Rec); //bufferSize);
                if( !firstTime ) {
                    byte[] bData = AudioController.instance.short2byte(buffer); //Arrays.copyOf( buffer, buffer.length );
                    if( subscriber instanceof String && ((String) subscriber).startsWith("http") ){
                        try {

    //                            EncoderTest.dataInputAudio = bData;
    //                            EncoderTest test = new EncoderTest();
    //                            test.testAACEncoders();
    //                            bData = EncoderTest.dataOutputAudio;
                            bData = compressAndEncodeBytes( bData );
                            MBRequest request = MBRequest.build(Constants.MSG_UPLOAD_TO_SERVER)
                                    .put(Constants.HTTP_REQUEST_SERVER_URL, subscriber)
                                    .put(Constants.HTTP_REQUEST_BODY, bData)
                                    .put(Constants.HTTP_RESOURCE_NAME, "voice_" + System.currentTimeMillis())
                                    .put(Constants.HTTP_MIME_TYPE, mimeType)
                                    .put(Constants.HTTP_FILE_EXTENSION, fileExtension);
                            FileUploader.upload(request);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        bData = compressAndEncodeBytes( bData );
                        AudioRecordEvent event = new AudioRecordEvent();
                        event.setBuffer(bData);
                        event.setSizeInBytes(sizeInBytes);
                        event.setMinBufferSize(bufferElements2Rec * bytesPerElement );
                        event.setSampleRate(sampleRateInHz);
                        event.setChannelConfig(channelConfig);
                        event.setAudioEncoding(audioEncoding);
                        if (isNewConfiguration) {
                            isNewConfiguration = false;
                            event.setNewConfiguration(true);
                        }
                        AudioController.instance.mb.send(event);

                        /*** check it **/
                        //MediaRecorder.OutputFormat.AAC_ADTS;
                    }
                }else{
                    firstTime = false;
                }
            }
        }

        /**
         * Encode raw PCM audio samples into a raw AAC file.
         * @param bData
         * @return
         */
        private synchronized byte[] compressAndEncodeBytes( byte[] bData  ){
            int numBytesSubmitted = 0;
            boolean doneSubmittingInput = false;
            ArrayList<Byte> bytesEncoded = new ArrayList<>();
            while (true) {
                int index;
                if (!doneSubmittingInput) {
                    index = codec.dequeueInputBuffer(timeout /* timeoutUs */);
                    if (index != MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (numBytesSubmitted >= bufferSize) {
//                            codec.queueInputBuffer(
//                                    index,
//                                    0 /* offset */,
//                                    0 /* size */,
//                                    0 /* timeUs */,
//                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            doneSubmittingInput = true;
                        } else {
                            int size = queueInputBuffer(codec, index, bData);
                            numBytesSubmitted += size;
                        }
                    }
                }
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                bytesEncoded.addAll(dequeueOutputBuffer( codec, info ));
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
            int sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            int inBitrate = sampleRate * channelCount * 16;  // bit/sec
            int outBitrate = audioFormat.getInteger(MediaFormat.KEY_BIT_RATE);
            float desiredRatio = (float)outBitrate / (float)inBitrate;
            float actualRatio = (float)bytesEncoded.size() / (float)numBytesSubmitted;
            if (actualRatio < 0.9 * desiredRatio || actualRatio > 1.1 * desiredRatio) {
//                Log.e(TAG, "desiredRatio = " + desiredRatio
//                        + ", actualRatio = " + actualRatio);
            }
            //addADTStoPacket( bytesEncoded );
            codec.flush(); // we need this to avoid INFO_TRY_AGAIN_LATER
            return getByteTobyte( bytesEncoded );
        }

        private ArrayList<Byte> dequeueOutputBuffer(MediaCodec codec, MediaCodec.BufferInfo info) {
            int index = codec.dequeueOutputBuffer(info, timeout /* timeoutUs */); //timeout
            if (index >= 0) {
                ByteBuffer outBuf = codec.getOutputBuffer( index );
                byte[] data = null;
                if( info.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG ){
                    data = new byte[info.size];
                    outBuf.get(data);
                    data = addADTStoPacket( data );
                }
                outBuf.clear();
                codec.releaseOutputBuffer(index, false /* render */);
                return getbyteToByte(data);
            } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                Log.e(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
            } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                Log.e(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
            } else if(index == MediaCodec.INFO_TRY_AGAIN_LATER ){
                info.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
//                Log.e(TAG, "INFO_TRY_AGAIN_LATER");
            }
            return new ArrayList<>();
        }


        private ArrayList<Byte> getbyteToByte(byte[] data){
            if( data == null ){
                return new ArrayList<>();
            }
            ArrayList<Byte> bytesList = new ArrayList<>();
            for( byte b : data ){
                bytesList.add( Byte.valueOf(b) );
            }
            return bytesList;
        }


        private byte[] getByteTobyte(ArrayList<Byte> data){
            byte[] byteArray = new byte[data.size()];
            for(int i = 0; i < data.size(); i++  ){
                byteArray[i] = data.get(i);
            }
            return byteArray;
        }



        private int queueInputBuffer(MediaCodec codec, int index, byte[] bData) {
            ByteBuffer buffer = codec.getInputBuffer( index );
            buffer.clear();
            //int size = buffer.limit();
            int size = bData.length;
            buffer.put( bData );
            codec.queueInputBuffer(index, 0 /* offset */, size, 0 /* timeUs */, 0);
            return size;
        }


        private List<String> getEncoderNamesForType(String mime) {
            LinkedList<String> names = new LinkedList<>();
            int n = MediaCodecList.getCodecCount();
            for (int i = 0; i < n; ++i) {
                MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
                if (!info.isEncoder()) {
                    continue;
                }
                if (!info.getName().startsWith("OMX.")) {
                    // Unfortunately for legacy reasons, "AACEncoder", a non OMX component had to
                    // be in this list for the video editor code to work... but it cannot actually
                    // be instantiated using MediaCodec.
                    Log.e("AudioController", "skipping '" + info.getName() + "'.");
                    continue;
                }
                String[] supportedTypes = info.getSupportedTypes();
                for (int j = 0; j < supportedTypes.length; ++j) {
                    if (supportedTypes[j].equalsIgnoreCase(mime)) {
                        names.push(info.getName());
                        break;
                    }
                }
            }
            return names;
        }


        /**
         *  Add ADTS header at the beginning of each and every AAC packet.
         *  This is needed as MediaCodec encoder generates a packet of raw
         *  AAC data.
         *
         *  Note the packetLen must count in the ADTS header itself.
         **/
        private void addADTStoPacket(ArrayList<Byte> packet) {
            int profile = audioFormat.getInteger( MediaFormat.KEY_AAC_PROFILE );
            int freqIdx = getFrequency( audioFormat.getInteger( MediaFormat.KEY_SAMPLE_RATE ) );
            int chanCfg = audioFormat.getInteger( MediaFormat.KEY_CHANNEL_COUNT );  //1 CPE
            int packetLen = 7 + packet.size();

            // fill in ADTS data
            packet.add( 0, (byte) 0xFF );
            packet.add( 1, (byte)0xF9 );
            packet.add( 2, (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2)) );
            packet.add( 3, (byte)(((chanCfg&3)<<6) + (packetLen>>11)) );
            packet.add( 4, (byte)((packetLen&0x7FF) >> 3) );
            packet.add( 5, (byte)(((packetLen&7)<<5) + 0x1F) );
            packet.add( 6, (byte)0xFC );
        }

        private byte[] addADTStoPacket(byte[] packet) {
            int profile = audioFormat.getInteger( MediaFormat.KEY_AAC_PROFILE );
            int freqIdx = getFrequency( audioFormat.getInteger( MediaFormat.KEY_SAMPLE_RATE ) );
            int chanCfg = audioFormat.getInteger( MediaFormat.KEY_CHANNEL_COUNT );  //1 CPE
            int packetLen = 7 + packet.length;
            byte[] data = new byte[packetLen];

            // fill in ADTS data
            data[0] = (byte) 0xFF ;
            data[1] = (byte)0xF9 ;
            data[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2)) ;
            data[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11)) ;
            data[4] = (byte)((packetLen&0x7FF) >> 3) ;
            data[5] = (byte)(((packetLen&7)<<5) + 0x1F) ;
            data[6] = (byte)0xFC ;
            return data;
        }


        private int getFrequency( final int sampleRate ){
            switch ( sampleRate ){
                case 96000:
                    return 0;
                case 88200:
                    return 1;
                case 64000:
                    return 2;
                case 48000:
                    return 3;
                case 44100:
                    return 4;
                case 32000:
                    return 5;
                case 24000:
                    return 6;
                case 22050:
                    return 7;
                case 16000:
                    return 8;
                case 12000:
                    return 9;
                case 11025:
                    return 10;
                case 8000:
                    return 11;
                case 7350:
                    return 12;
            }
            return 0;
        }

        private void addADTStoPacket(byte[] packet, int packetLen) {
            int profile = audioFormat.getInteger( MediaFormat.KEY_AAC_PROFILE );
            int freqIdx = audioFormat.getInteger( MediaFormat.KEY_SAMPLE_RATE );  //44.1KHz
            int chanCfg = audioFormat.getInteger( MediaFormat.KEY_CHANNEL_COUNT );  //1 CPE

            // fill in ADTS data
            packet[0] = (byte)0xFF;
            packet[1] = (byte)0xF9;
            packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
            packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
            packet[4] = (byte)((packetLen&0x7FF) >> 3);
            packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
            packet[6] = (byte)0xFC;
        }

        public Object getSubscriber() {
            return subscriber;
        }

        public void sendErrorMessage() {
            AudioRecordEvent event = new AudioRecordEvent();
            event.setErrorMessage( "AudioRecord cannot be initialized with configuration:" +
                                    " Sample Rate: " + sampleRateInHz +
                                    " Channel: " + channelConfig +
                                    " Encoding: " + audioEncoding +
                                    " Buffer Elements: " + bufferElements2Rec +
                                    " Bytes per element: " + bytesPerElement);
            AudioController.instance.mb.send(event);
        }
    }
}
