package com.nuance.cobra.utils.audio;

import com.nuance.cobra.api.response.InternalGenericResponseCode;
import com.nuance.cobra.api.response.InternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.logging.EdfFluentbitListener;
import com.nuance.cobra.logging.EdfLogForwarder;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.encryption.Encryptor;
import com.nuance.cobra.utils.response.EDF.AudioLog;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.response.error.listener.LoggerErrorListener;
import com.nuance.cobra.utils.response.error.listener.SnmpErrorListener;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.nuance.cobra.logging.edf.EdfMessageQueueableConstants.*;

public class WavDumperNode
        extends AudioNode
{
    private static final Logger logger = LogManager.getLogger(WavDumperNode.class);

    // This folder is created in package.sh, changes here must be mirrored there.
    //always point to webapi/ folder, not instance folder
    private static final String FOLDER = "data/eventLogs";

    private static final String EXTENSION = "wav";
    private static final String SEPARATOR = ".";

    private static final String RIFF = "RIFF";
    private static final String WAVE = "WAVE";
    private static final String FMT = "fmt ";
    private static final String DATA = "data";

    private static final short AUDIO_FORMAT = 1;
    private static final short NUM_CHANNELS = 1;
    private static final short BLOCK_ALIGN = 2;
    private static final short BITS_PER_SAMPLE = 16;

    private final ByteArrayOutputStream dataByteArray = new ByteArrayOutputStream();
    private ByteArrayOutputStream endPointedData = null;

    private final int sampleRate;
    private String fileName;
    private String fileNameNoPath = null;
    private String folderPath;
    private String filePathForEdf;
    private String sessionId;
    private Calendar sessionStartTime;
    private String tenantPublicKey = null;
    private Boolean encryptionEnabled = false;
    private Boolean debugEnabled = false;
    private Boolean edfAudioDump = false;
    private EdfMessageQueueable.EdfMessageFormat logFormat;
    private Boolean debugAudioCapture = false;
    private Session currentSession = null;
    private Boolean speechStarted = false;
    private Boolean endPointingEnabled = false;
    private Boolean dumpEndpointedUtterance = false;
    private boolean diskFilled = false;
    private boolean onlyDumpOnce = false;
    private int speechStart = 0;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private Encryptor encryptor;
    private final ResponseHandler responseHandler;
    private SnmpResponseTrapSender snmpResponseTrapSender;

    private ResponseErrorHandler errorHandler;

    private static Map<String, String> encryptedFiles;

    public WavDumperNode(int sampleRate, String fileName, Session session, boolean edfAudioDump, String logFormat,
            boolean isDebugAudioCapture, boolean dumpEndpointedUtterance, ResponseHandler responseHandler, List<AudioNodeObserver> observer)
    {
        this.currentSession = session;
        this.sessionId = currentSession.getSessionId();
        this.sessionStartTime = currentSession.getStartSessionTime();
        this.edfAudioDump = edfAudioDump;
        if(logFormat.equalsIgnoreCase(NEAP_V1_LOG_FORMAT_NAME)){
            this.logFormat = EdfMessageQueueable.EdfMessageFormat.NEAP_V1_FORMAT;
        }
        else if(logFormat.equalsIgnoreCase(EDF_V1_LOG_FORMAT_NAME)){
            this.logFormat = EdfMessageQueueable.EdfMessageFormat.EDF_V1_FORMAT;
        }
        this.debugAudioCapture = isDebugAudioCapture;
        this.dumpEndpointedUtterance = dumpEndpointedUtterance;
        this.responseHandler = responseHandler;
        this.snmpResponseTrapSender = new SnmpResponseTrapSender(currentSession.getSessionConfig());
        addObserver(observer);

        makePath();

        logger.debug("Creating dumper -- sampleRate: {}, fileName: {}", sampleRate, fileName);
        this.sampleRate = sampleRate;

        if (!fileName.endsWith(SEPARATOR + EXTENSION))
        {
            if (!fileName.endsWith(SEPARATOR))
            {
                fileName += ".";
            }

            fileName += EXTENSION;
        }

        this.fileNameNoPath = fileName;

        this.fileName = folderPath + "/" + fileName;

        this.filePathForEdf = folderPath + "/edf/" + fileName;

        try
        {
            if (currentSession == null){
                currentSession = sessionManager.getSession(this.sessionId, false);
            }
            encryptionEnabled = currentSession.getSessionConfig()
                                              .getBoolean(WebApiConfig.Property.AUDIO_ENCRYPTION.key);
            debugEnabled = currentSession.getSessionConfig()
                                         .getBoolean(WebApiConfig.Property.DEBUG_ENABLED.key);
            tenantPublicKey = currentSession.getSessionConfig()
                                            .getString(WebApiConfig.Property.PUBLIC_KEY.key);
            String clientPublicKey = currentSession.getSessionConfig()
                                            .getString(WebApiConfig.Property.CLIENT_PUBLIC_KEY.key);
            if(!clientPublicKey.isEmpty()){
                tenantPublicKey = clientPublicKey;
            }

             logger.debug("Set encryption properties -- tenantPublicKey: {}", tenantPublicKey);
        }
        catch (NullPointerException e)
        {
            logger.debug("One or more encryption properties not found in tenant config, cannot encrypt");
        }

    }

    private ResponseErrorHandler getErrorHandler()
    {
        if (errorHandler == null)
        {
            errorHandler = new ResponseErrorHandler();
            errorHandler.addListener(new LoggerErrorListener(logger));
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());
        }

        return errorHandler;
    }

    @Override
    public void onAudio(AudioChunk chunk)
    {
        final byte[] bytes = chunk.getBytes();
        dataByteArray.write(bytes, 0, bytes.length);
        dispatchChunk(chunk);
    }

    private byte[] packShort(short s)
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Short.SIZE / Byte.SIZE);
        final ByteBuffer orderedBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer resultBuffer = orderedBuffer.putShort(s);
        return resultBuffer.array();
    }


    private byte[] packInt(int i)
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        final ByteBuffer orderedBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer resultBuffer = orderedBuffer.putInt(i);
        return resultBuffer.array();
    }

    private byte[] packString(String s)
    {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    static class MyByteArrayOutputStream
            extends ByteArrayOutputStream
    {
        @Override
        public void write(byte[] bytes)
        {
            write(bytes, 0, bytes.length);
        }
    }

    private byte[] makeFmt()
    {
        final MyByteArrayOutputStream fmt = new MyByteArrayOutputStream();

        fmt.write(packShort(AUDIO_FORMAT));
        fmt.write(packShort(NUM_CHANNELS));
        fmt.write(packInt(sampleRate));
        fmt.write(packInt(sampleRate << 1)); // ByteRate = NUM_CHANNELS * sampleRate * BITS_PER_SAMPLE / 8
        fmt.write(packShort(BLOCK_ALIGN));
        fmt.write(packShort(BITS_PER_SAMPLE));

        return fmt.toByteArray();
    }

    public byte[] makeHeader(int dataLength)
    {
        final MyByteArrayOutputStream header = new MyByteArrayOutputStream();

        header.write(packString(WAVE));
        header.write(packString(FMT));

        final byte[] fmtBytes = makeFmt();
        header.write(packInt(fmtBytes.length));
        header.write(fmtBytes);

        header.write(packString(DATA));
        header.write(packInt(dataLength));

        return header.toByteArray();
    }

    public static Map<String, String> getEncryptedFiles()
    {
        if (encryptedFiles == null)
        {
            encryptedFiles = new ConcurrentHashMap<>();
        }

        return encryptedFiles;
    }

    public void makePath()
    {
        try
        {

            if (currentSession == null)
            {
                currentSession = sessionManager.getSession(this.sessionId, false);
            }
            String companyName = currentSession.getSessionConfig()
                                               .getString(WebApiConfig.Property.DIALOG_MODEL_COMPANY_NAME.key);
            String applicationName = currentSession.getSessionConfig()
                                                   .getString(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key);
            String basePath = getBasePath(currentSession.getSessionConfig());

            LocalDate localDate = LocalDate.now();
            int year = localDate.getYear();
            String monthValue = String.format("%02d", localDate.getMonthValue());
            String month = localDate.getMonth()
                                    .getDisplayName(TextStyle.FULL, Locale.US);
            String day = String.format("%02d", localDate.getDayOfMonth());
            String hourOfDay = String.format("%02d", GregorianCalendar.getInstance()
                                                                      .get(Calendar.HOUR_OF_DAY));
            folderPath = basePath + FOLDER + "/" + companyName + "/" + applicationName + "/" +
                         year + "/" + monthValue + month + "/" + day + "/" + hourOfDay;

            Files.createDirectories(Paths.get(folderPath));

        }
        catch (IOException e)
        {
            if (Files.exists(Paths.get(folderPath)))
            {
                logger.info("Failed to create audio file directory : {} already exists", e.getMessage());
            }
            else
            {
                logger.info("Failed to create audio file directory : {}", e.getMessage());
            }
        }
    }

    public static String getBasePath(CombinedConfiguration configuration) {
        String basePath = configuration.getString(WebApiConfig.Property.DEBUG_PATH.key);

        //handle absolute input or not...
        if (Files.isDirectory(Paths.get(basePath)) && basePath.charAt(basePath.length() - 1) != '/')
        {
            basePath += '/';
        }
        else if (!Files.isDirectory(Paths.get(basePath)))
        {
            basePath = WebApiConfig.Property.DEBUG_PATH.defaultValue.toString();
        }
        return basePath;
    }

    public String getFolderPath()
    {
        return folderPath;
    }

    public String getFileNameNoPath()
    {
        return fileNameNoPath;
    }

    @Override
    public void finish()
    {
        dumpToFiles();
    }


    private void dumpToFiles()
    {
        if (onlyDumpOnce) {
            return;
        }

        onlyDumpOnce = true;

        dumpToFile();
        dumpEndPointedFile();
    }


    private void dumpToFile()
    {
        //TODO: audio is stored in memory here
        byte[] dataBytes;
        if (fileName.contains("raw"))
        {
            dataBytes = dataByteArray.toByteArray();
        }
        else
        {
            //endpointed file
            dataBytes = endPointedData.toByteArray();
        }
        byte[] encDataBytes = null;
        if (debugEnabled && encryptionEnabled && !tenantPublicKey.isEmpty())
        {
            encDataBytes = encryptAudioBytes(dataBytes, Base64.getDecoder().decode(tenantPublicKey));
            logger.debug("Audio Encrypted! Encrypted Audio Length is : {}", dataBytes.length);
        }
        else if (debugEnabled && encryptionEnabled)
        {
            logger.debug("Audio must still be obfuscated, masking audio");
            encDataBytes = encryptAudioBytes(dataBytes,null);
        }
        else
        {
            logger.debug("Not encrypting or masking audio...");
        }

        logger.debug("finish -- length: {}", dataBytes.length);

        byte[] headerBytes = makeHeader(dataBytes.length);
        byte[] riff = packString(RIFF);
        byte[] packInt = packInt(headerBytes.length + dataBytes.length);

        writeFile(riff,headerBytes,packInt,dataBytes,encDataBytes);
        edfDump(riff,headerBytes,packInt,dataBytes);


        super.finish();
    }

    public void edfDump(byte[] riff, byte[] headerBytes, byte[] packInt, byte[] dataBytes){

        try
        {

            //will trigger regardless of whether or not we want audio dumped if we have edf posting enabled
            if (fileName.contains("raw") && !fileName.contains("endpointed") && this.edfAudioDump)
            {
                Path edfPath = Paths.get(filePathForEdf);
                Files.createDirectories(edfPath.getParent());

                if (diskLimitReached(filePathForEdf)){
                    if (!diskFilled){
                        diskFilled = true;
                    }
                    getErrorHandler().handleError(new ResponseResult(ResponseCode.createResponseError(InternalResponseErrorCode.DISK_USAGE_LIMIT_REACHED)));
                    logger.error("Disk Usage Limit Reached, EDF binary data will be lost");
                    return;
                } else {
                    if (diskFilled){
                        getErrorHandler().handleError(new ResponseResult(ResponseCode.createGenericResponse(
                                InternalGenericResponseCode.LOG_POSTING_RESUMED)));
                        diskFilled = false;
                    }
                }

                Path p;
                try
                {
                    p = Files.createFile(edfPath);
                }
                catch (FileAlreadyExistsException e)
                {
                    p = Paths.get(filePathForEdf);
                }

                //verify edf audio encrypted
                String cleaned = fileNameNoPath.replace(".wav", "");
                boolean encrypt = getEncryptedFiles().containsKey(cleaned) && getEncryptedFiles().get(cleaned)
                                                                                                 .equals("encrypted");
                boolean mask = getEncryptedFiles().containsKey(cleaned) && getEncryptedFiles().get(cleaned)
                                                                                              .equals("masked");
                AudioLog audioLog = new AudioLog();
                audioLog.edfMessageFormat = logFormat;
                audioLog.sessionId = sessionId;
                audioLog.audioBlobPath = currentSession.makeAudioBlobPath();
                audioLog.audioLogKey = currentSession.getAudioLogKey();
                audioLog.stepId = currentSession.getStepId();
                if (encrypt)
                {
                    logger.debug("ENCRYPTING EDF AUDIO");
//                        riff = encryptAudioBytes(riff,Base64.decode(tenantPublicKey));
//                        packInt = encryptAudioBytes(packInt,Base64.decode(tenantPublicKey));
//                        headerBytes = encryptAudioBytes(headerBytes,Base64.decode(tenantPublicKey));
//                        dataBytes = encryptAudioBytes(dataBytes,Base64.decode(tenantPublicKey));
                    audioLog.encrypted = true;
                }
                else if (mask)
                {

                    logger.debug("Masking audio, not sending AudioLog to EDF");

                }


//                    logger.debug("DATA BYTES : {}", new String(dataBytes));

                if (!mask)
                {
                    logger.debug("NOT MASKING, SENDING AUDIO TO EDF");


                    if (encrypt)
                    {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byteArrayOutputStream.write(riff);
                        byteArrayOutputStream.write(packInt);
                        byteArrayOutputStream.write(headerBytes);
                        byteArrayOutputStream.write(dataBytes);

                        byte[] decoded  = null;
                        if (!tenantPublicKey.isEmpty())
                        {
                            decoded = Base64.getDecoder().decode(tenantPublicKey);
                        }
                        byte[] encrypted = encryptAudioBytes(byteArrayOutputStream.toByteArray(),
                                decoded);

                        Files.write(p, encrypted);
                    }
                    else
                    {
                        Files.write(p, riff);
                        Files.write(p, packInt, StandardOpenOption.APPEND);
                        Files.write(p, headerBytes, StandardOpenOption.APPEND);
                        Files.write(p, dataBytes, StandardOpenOption.APPEND);
                    }


                    audioLog.fileName = p.toFile()
                                         .getCanonicalPath();
                    audioLog.enabledPosting = currentSession
                            .getSessionConfig()
                            .getBoolean(WebApiConfig.Property.EDF_POSTING_ENABLED_TENANT.key);

                    audioLog.basePath = currentSession
                            .getSessionConfig()
                            .getString(WebApiConfig.Property.EDF_BINARY_ENDPOINT.key);

                    audioLog.tenantKey = Session.getTenantKey(currentSession);

                    //EdfLogPoster deletes file after successful post
                    logger.debug("Adding audioLog to queue");

                    EdfLogForwarder edfLogForwarder = new EdfLogForwarder();
                    if(this.currentSession.getSessionConfig().getBoolean(WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.key))
                    {
                        edfLogForwarder.addListener(new EdfFluentbitListener());
                    }
                    edfLogForwarder.forwardLog(audioLog, this.currentSession.getSessionConfig());
                }


            }


        }
        catch (IOException e)
        {
            logger.error("WavDumperNode Exception while creating temp EDF file {}", e.getMessage(), e);
        }
    }

    public void writeFile(byte[] riff, byte[] headerBytes, byte[] packInt, byte[] dataBytes, byte[] encryptedDataBytes)
    {
        if (! debugEnabled)
        {
            return;
        }

        if (diskLimitReached(fileName))
        {
            if (!diskFilled){
                diskFilled = true;
            }
            getErrorHandler().handleError(new ResponseResult(ResponseCode.createResponseError(InternalResponseErrorCode.DISK_USAGE_LIMIT_REACHED)));
            logger.error("Disk Usage Limit Reached, Not Dumping Audio");
            return;
        } else {
            if (diskFilled){
                getErrorHandler().handleError(new ResponseResult(ResponseCode.createGenericResponse(
                        InternalGenericResponseCode.LOG_POSTING_RESUMED)));
                diskFilled = false;
            }

        }

        OutputStream os = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try
        {
            logger.debug("WavDumperNode::EdfAudioDump ? " + edfAudioDump);

            appendSuffixIfExists();

            if (debugEnabled && debugAudioCapture && encryptionEnabled && (encryptedDataBytes != null) &&
                !tenantPublicKey.isEmpty())
            {
                byte[] decodedKey = Base64.getDecoder().decode(tenantPublicKey);

                byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(riff);
                byteArrayOutputStream.write(packInt);
                byteArrayOutputStream.write(headerBytes);
                byteArrayOutputStream.write(dataBytes);

                byte[] encrypted = encryptAudioBytes(byteArrayOutputStream.toByteArray(), decodedKey);

                logger.debug("Triggered 101 finish");
                Files.write(Paths.get(fileName), encrypted);

            }

            else if (debugEnabled && debugAudioCapture && encryptionEnabled)
            {
                byte[] encRiff = encryptAudioBytes(riff, null);
                byte[] encPackInt = encryptAudioBytes(packInt, null);
                byte[] encHeaderBytes = encryptAudioBytes(headerBytes, null);

                os = new FileOutputStream(fileName);

                logger.debug("Triggered 101 finish");

                os.write(encRiff);
                os.write(encPackInt);
                os.write(encHeaderBytes);
                os.write(encryptedDataBytes);
            }

            //If WavDumperNode was called AND we have debugAudioCapture enabled
            // (i.e we want audio to be dumped regardless of EDF)
            else if (debugEnabled && debugAudioCapture && !encryptionEnabled)
            {
                os = new FileOutputStream(fileName);

                logger.debug("Triggered 101 finish");

                os.write(riff);
                os.write(packInt);
                os.write(headerBytes);
                os.write(dataBytes);
            }
        }
        catch (Exception e)
        {
            logger.error("Exception writing audio data to file : {}", e.getMessage(), e);

        }
        finally
        {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }
    }

    void appendSuffixIfExists()
    {
        if(Files.exists(Paths.get(fileName)))
        {
            fileName = fileName.replace(SEPARATOR + EXTENSION, "-" + UUID.randomUUID().toString().replace("-", "").substring(0,5));
            fileName += SEPARATOR + EXTENSION;
        }
    }

    private byte[] encryptAudioBytes(byte[] inputBytes, byte[] publicKeyBytes)
    {

        if (encryptor == null)
        {
            Encryptor e = new Encryptor();
            encryptor = new Encryptor();
        }

        encryptor.setTenantPublicKey(publicKeyBytes);

        return encryptor.getEncryptedData(inputBytes);
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event)
    {

        if (event == NodeEvent.TIMEOUT)
        {
            logger.debug("WavDumperNode::Timeout Event");

            speechStarted = false;
            dumpToFiles();
        }

        if (event == NodeEvent.START_OF_SPEECH)
        {
            //clear data from before start of speech for endpointed files
            logger.debug("WavDumperNode::Speech Started Event");

            endPointingEnabled = true;

            if (null == endPointedData)
            {
                endPointedData = new ByteArrayOutputStream();
            }
            speechStart = dataByteArray.size();
            logger.debug("WavDumperNode::speechStart : {} " , speechStart);
        }
        if (event == NodeEvent.END_OF_SPEECH)
        {
            logger.debug("WavDumperNode::End Of Speech Event");
            dumpToFiles();
        }

    }

    private void dumpEndPointedFile()
    {
        if (!dumpEndpointedUtterance || !debugEnabled)
        {
            return;
        }

        synchronized (this)
        {
            if (fileName.contains("raw"))
            {
                fileName = fileName.replace("raw","endpointed");
            }
        }

        if (null == endPointedData)
        {
            endPointedData = new ByteArrayOutputStream();
        }

        if (speechStart != 0)
        {
            byte[] theData = dataByteArray.toByteArray();
            theData = Arrays.copyOfRange(theData,speechStart,theData.length);
            endPointedData.reset();
            try
            {
                endPointedData.write(theData);
            }
            catch (IOException e)
            {
                logger.error("IOException:", e);
            }
        }
        else
        {
            endPointedData.reset();
            endPointedData.write(0x01);
        }

        dumpToFile();
        synchronized (this)
        {
            if (fileName.contains("endpointed"))
            {
                fileName = fileName.replace("endpointed","raw");
            }
        }

        endPointedData = null;
    }

    public int getDiskUsage(String path)
    {
        File root = new File(path).getParentFile();
        if (root == null)
        {
            logger.warn("Could not get parent path for {}", path);
            return 100;
        }

        long totalSpace = root.getTotalSpace();
        logger.debug("Total disk space: {}Mb", totalSpace/1024/1024);
        long freeSpace = root.getFreeSpace();
        logger.debug("Free disk space: {}Mb", freeSpace/1024/1024);

        if (totalSpace == 0)
        {
            logger.warn("Total disk space for {} is 0", root.getAbsolutePath());
            return 100;
        }

        long usedSpace = totalSpace - freeSpace;

        double usage = Math.ceil(100.0 * usedSpace / totalSpace);

        int usageInt = (int)usage;
        if (usageInt > 90) {
            logger.debug("Disk usage is critically high at {}%. Consider checking and clearing space.", usageInt);
        } else {
            logger.debug("Disk usage is {}%", usageInt);
        }

        return usageInt;
    }

    public boolean diskLimitReached(String path){
        int limit = WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.DISK_USAGE_LIMIT_PERCENTAGE.key);
        logger.debug("Disk limit percentage: {}", limit);
        return getDiskUsage(path) >= limit;
    }

}

