package InMind.Server.SignalInfo;


import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import edu.cmu.sphinx.frontend.*;
import edu.cmu.sphinx.frontend.util.AudioFileProcessListener;
import edu.cmu.sphinx.frontend.util.DataUtil;
import edu.cmu.sphinx.util.props.*;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class InMindByteDataSource extends BaseDataProcessor
{


    public interface NotificationsFromByteSource
    {
        void waitingForMoreData();
    }

    @S4Integer(
            defaultValue = 3200
    )
    public static final String PROP_BYTES_PER_READ = "bytesPerRead";
    @S4ComponentList(
            type = Configurable.class
    )
    public static final String AUDIO_FILE_LISTENERS = "audioFileListners";
    //protected final List<AudioFileProcessListener> fileListeners = new ArrayList();
    //protected InputStream dataStream;
    protected int sampleRate;
    protected int bytesPerRead;
    protected int bytesPerValue;
    private long totalValuesRead;
    protected boolean bigEndian;
    protected boolean signedData;
    //private boolean streamEndReached;
    private boolean utteranceEndSent;
    private boolean utteranceStarted;
    //private File curAudioFile;
    private ByteArrayInputStream byteArrayInputStream;
    private ByteArrayBuffer byteArrayBuffer;// = new ByteArrayBuffer();
    private Object waitingForMoreData = new Object();
    private boolean sendEndStream = false;

    private NotificationsFromByteSource notificationsFromByteSource;

    public void giveInterface(NotificationsFromByteSource notificationsFromByteSource)
    {
        synchronized (this)
        {
            this.notificationsFromByteSource = notificationsFromByteSource;
        }
    }

//    public InMindByteDataSource(int bytesPerRead, List<AudioFileProcessListener> listeners) {
//        this.logger = Logger.getLogger(this.getClass().getName());
//        this.create(bytesPerRead, listeners);
//    }

//    public InMindByteDataSource() {
//    }

    public void newProperties(PropertySheet ps) throws PropertyException
    {
        super.newProperties(ps);
        this.create(ps.getInt("bytesPerRead"), ps.getComponentList("audioFileListners", AudioFileProcessListener.class));
    }

    private void create(int bytesPerRead, List<AudioFileProcessListener> listeners) {
        this.bytesPerRead = bytesPerRead;
//        if(listeners != null) {
//            Iterator i$ = listeners.iterator();
//
//            while(i$.hasNext()) {
//                AudioFileProcessListener configurable = (AudioFileProcessListener)i$.next();
//                this.addNewFileListener(configurable);
//            }
//        }

        this.initialize();
    }

    public void initialize() {
        super.initialize();
        //this.streamEndReached = false;
        this.utteranceEndSent = false;
        this.utteranceStarted = false;
        if(this.bytesPerRead % 2 == 1) {
            ++this.bytesPerRead;
        }

    }

//    public void setAudioFile(File audioFile, String streamName) {
//        try {
//            this.setAudioFile((URL)audioFile.toURI().toURL(), streamName);
//        } catch (MalformedURLException var4) {
//            var4.printStackTrace();
//        }
//    }

//    public void setAudioFile(URL audioFileURL, String streamName) {
//        if(this.dataStream != null) {
//            try {
//                this.dataStream.close();
//            } catch (IOException var8) {
//                var8.printStackTrace();
//            }
//
//            this.dataStream = null;
//        }
//
//        assert audioFileURL != null;
//
//        if(streamName != null) {
//            streamName = audioFileURL.getPath();
//        }
//
//        AudioInputStream audioStream = null;
//
//        try {
//            audioStream = AudioSystem.getAudioInputStream(audioFileURL);
//        } catch (UnsupportedAudioFileException var6) {
//            System.err.println("Audio file format not supported: " + var6);
//            var6.printStackTrace();
//        } catch (IOException var7) {
//            var7.printStackTrace();
//        }
//
//        this.curAudioFile = new File(audioFileURL.getFile());
//        Iterator i$ = this.fileListeners.iterator();
//
//        while(i$.hasNext()) {
//            AudioFileProcessListener fileListener = (AudioFileProcessListener)i$.next();
//            fileListener.audioFileProcStarted(this.curAudioFile);
//        }
//
//        this.setInputStream(audioStream, streamName);
//    }

    public synchronized void startNewStream(AudioFormat format) {
        //this.dataStream = inputStream;
        //this.streamEndReached = false;
        this.utteranceEndSent = false;
        this.utteranceStarted = false;
        //AudioFormat format = inputStream.getFormat();
        this.sampleRate = (int)format.getSampleRate();
        this.bigEndian = format.isBigEndian();
        String s = format.toString();
        this.logger.finer("input format is " + s);
        if(format.getSampleSizeInBits() % 8 != 0) {
            throw new Error("StreamDataSource: bits per sample must be a multiple of 8.");
        } else {
            this.bytesPerValue = format.getSampleSizeInBits() / 8;
            AudioFormat.Encoding encoding = format.getEncoding();
            if(encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
                this.signedData = true;
            } else {
                if(!encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
                    throw new RuntimeException("used file encoding is not supported");
                }

                this.signedData = false;
            }

            this.totalValuesRead = 0L;
        }

        byteArrayBuffer = new ByteArrayBuffer();
        synchronized (waitingForMoreData)
        {
            waitingForMoreData.notify();
        }
        byteArrayInputStream = null;
    }

    public void endStream()
    {
        synchronized (waitingForMoreData)
        {
            sendEndStream = true;
            waitingForMoreData.notify();
        }
    }

    public Data getData() throws DataProcessingException
    {
        this.getTimer().start();
        fetchNewDataIfNeeded();
        Object output = null;
        if(sendEndStream) {
//            if(!this.utteranceEndSent) {
                output = this.createDataEndSignal();
                sendEndStream=false;// = true;
//            }
        } else if(!this.utteranceStarted) {
            this.utteranceStarted = true;
            output = new DataStartSignal(this.sampleRate);
        } else if(this.byteArrayInputStream != null) {
            output = this.readNextFrame();
            if(output == null && !this.utteranceEndSent) {
                output = this.createDataEndSignal();
                this.utteranceEndSent = true;
            }
        }

        this.getTimer().stop();
        return (Data)output;
    }

    private void fetchNewDataIfNeeded()
    {
        if (byteArrayInputStream == null)
        {
            if (byteArrayBuffer != null && byteArrayBuffer.size() > 0)
            {
                synchronized (byteArrayBuffer)
                {
                    //these two lines must be done together.
                    byteArrayInputStream = new ByteArrayInputStream(byteArrayBuffer.getRawData());
                    byteArrayBuffer.reset();// = new ByteArrayBuffer();
                }
            }
            else
            {
                try
                {
                    synchronized (this)
                    {
                        if (notificationsFromByteSource != null)
                            notificationsFromByteSource.waitingForMoreData();
                    }
                    synchronized (waitingForMoreData)
                    {
                        waitingForMoreData.wait();
                    }
                } catch (InterruptedException e)
                {
                }
                fetchNewDataIfNeeded();
            }
        }
    }

    private DataEndSignal createDataEndSignal() {
//        if(!(this instanceof ConcatAudioFileDataSource)) {
//            Iterator i$ = this.fileListeners.iterator();
//
//            while(i$.hasNext()) {
//                AudioFileProcessListener fileListener = (AudioFileProcessListener)i$.next();
//                fileListener.audioFileProcFinished(this.curAudioFile);
//            }
//        }

        return new DataEndSignal(this.getDuration());
    }

    private Data readNextFrame() throws DataProcessingException {
        int totalRead = 0;
        int bytesToRead = this.bytesPerRead;
        byte[] samplesBuffer = new byte[this.bytesPerRead];
        long collectTime = System.currentTimeMillis();
        long firstSample = this.totalValuesRead;

        try {
            int read;
            do {
                if (byteArrayInputStream == null)
                    totalRead = 0;
                read = this.byteArrayInputStream.read(samplesBuffer, totalRead, bytesToRead - totalRead);
                if(read > 0) {
                    totalRead += read;
                }
            } while(read != -1 && totalRead < bytesToRead);

            if(totalRead <= 0) {
                this.closeDataStream();
                fetchNewDataIfNeeded();
                return readNextFrame();
            }

            this.totalValuesRead += (long)(totalRead / this.bytesPerValue);
            if(totalRead < bytesToRead) {
                totalRead = totalRead % 2 == 0?totalRead + 2:totalRead + 3;
                byte[] doubleData = new byte[totalRead];
                System.arraycopy(samplesBuffer, 0, doubleData, 0, totalRead);
                samplesBuffer = doubleData;
                //this.closeDataStream();
            }
        } catch (IOException var10) {
            throw new DataProcessingException("Error reading data", var10);
        }

        double[] doubleData1;
        if(this.bigEndian) {
            doubleData1 = DataUtil.bytesToValues(samplesBuffer, 0, totalRead, this.bytesPerValue, this.signedData);
        } else {
            doubleData1 = DataUtil.littleEndianBytesToValues(samplesBuffer, 0, totalRead, this.bytesPerValue, this.signedData);
        }

        return new DoubleData(doubleData1, this.sampleRate, collectTime, firstSample);
    }

    private void closeDataStream() throws IOException {
        //this.streamEndReached = true;
        if(this.byteArrayInputStream != null) {
            this.byteArrayInputStream.close();
        }
        byteArrayInputStream = null;
    }

    private long getDuration() {
        return (long)((double)this.totalValuesRead / (double)this.sampleRate * 1000.0D);
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public boolean isBigEndian() {
        return this.bigEndian;
    }

//    public void addNewFileListener(AudioFileProcessListener l) {
//        if(l != null) {
//            this.fileListeners.add(l);
//        }
//    }
//
//    public void removeNewFileListener(AudioFileProcessListener l) {
//        if(l != null) {
//            this.fileListeners.remove(l);
//        }
//    }

    public void appendNewData(byte[] onlySample, int length)
    {
        synchronized (this)
        {
            byteArrayBuffer.write(onlySample, 0, length);
        }
        synchronized (waitingForMoreData)
        {
            waitingForMoreData.notify();
        }
    }

}
