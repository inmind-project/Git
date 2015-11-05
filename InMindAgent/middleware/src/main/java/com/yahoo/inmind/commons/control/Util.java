package com.yahoo.inmind.commons.control;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rits.cloning.Cloner;
import com.yahoo.inmind.commons.rules.control.AnnotationExclusionStrategy;
import com.yahoo.inmind.commons.rules.control.DecisionRuleMarshall;
import com.yahoo.inmind.commons.rules.model.DecisionRule;
import com.yahoo.inmind.commons.rules.model.PropositionalStatement;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oscarr on 12/8/14.
 */
public class Util {

    /**********************************************************************************************/
    /************************************** JSON **************************************************/
    /**********************************************************************************************/

    private static Gson gson = new GsonBuilder().setExclusionStrategies(new
            AnnotationExclusionStrategy()).create();

    public static <T> List<T> fromJsonList( String jsonList, Class<T> element ){
        Type type = new TypeToken<List<T>>() {}.getType();
        return gson.fromJson(jsonList, type);
    }

    public static <T> String toJson( T object ){
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz){
        if( clazz == DecisionRule.class ){
            return (T) fromJsonDR(json);
        }
        return gson.fromJson(json, clazz);
    }

    private static DecisionRule fromJsonDR(String json){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(PropositionalStatement.class, new DecisionRuleMarshall())
                .create();
        DecisionRule decisionRule = gson.fromJson( json, DecisionRule.class );
        for( DecisionRule.ConditionElement conditionElement : decisionRule.getConditions() ){
            conditionElement.getProposition().addRule( decisionRule );
        }
        return decisionRule;
    }

    public static <T> T fromJson(String json, Type type){
        return gson.fromJson(json, type);
    }

    public static String toJsonList( List list ){
        StringBuilder sb = new StringBuilder("[");
        Field[] fields = null;
        boolean firstObject = true;
        for (Object obj : list){
            if (firstObject){
                sb.append("{");
                firstObject = false;
            }else{
                sb.append(", {");
            }
            if (fields == null){
                fields = obj.getClass().getFields();
            }
            //do sth to retrieve each field value -> json property of json object
            //add to json array
            for (int i = 0 ; i < fields.length ; i++){
                Field f = fields[i];
                //jsonFromField(sb, obj, i, f);
            }
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * This method reads the content of a json reader and creates a corresponding object by
     * using reflection over a set of mapped values. If the json content contains multiple objects
     * then a container (List) should be provided.
     * Use .childString for raw arrays (i.e., String arrays instead of object arrays).
     * For instance, [0] = "a", [1] = "b"... instead of [name1] = "a", [name2] = "b"...
     * Use break for breaking the flow when an error ocurrs.
     * @param reader
     * @param parentTag
     * @param mappings
     * @param result
     * @param container
     * @return it returns whether there was an error message during the process
     * @throws Exception
     */
    public static String readJsonToObject(JsonReader reader, String parentTag, HashMap<String,
            String> mappings, Object result, List container, List<Object> errors) throws Exception {
        JsonToken type = reader.peek();
        String error = null;

        if( type.equals(JsonToken.BEGIN_ARRAY) ){
            reader.beginArray();
        } else if(type.equals(JsonToken.BEGIN_OBJECT)){
            reader.beginObject();
        }
        if( reader.peek().equals(JsonToken.BEGIN_OBJECT) ){
            do{
                boolean isError = validateIsJsonError(readJsonToObject(reader, parentTag, mappings,
                        result, container, errors), errors);
                if( !isError && type.equals(JsonToken.BEGIN_ARRAY) ){
                    container.add(Util.clone( result ) );
                }
            }while( type.equals(JsonToken.BEGIN_ARRAY) && reader.hasNext() );
        } else {
            while (reader.hasNext()) {
                String name = reader.peek().equals(JsonToken.NAME)? reader.nextName()
                        : parentTag + ".childString";
                String smethod = mappings.get(name);
                if( smethod == null){
                    smethod = mappings.get(parentTag+"."+name);
                }
                if (smethod == null) {
                    reader.skipValue();
                } else if(smethod.equals("break")){
                    error = reader.nextString();
                    break;
                } else if (smethod.equals("")) {
                    validateIsJsonError(readJsonToObject(reader, name, mappings, result, container, errors), errors);
                } else {
                    Method method = result.getClass().getMethod(smethod, String.class);
                    method.invoke(result, reader.nextString());
                }
            }
        }
        type = reader.peek();
        if( type.equals(JsonToken.END_ARRAY) ){
            reader.endArray();
        }else if(type.equals(JsonToken.END_OBJECT)){
            reader.endObject();
        }
        return error;
    }

    private static boolean validateIsJsonError(Object error, List errors){
        if( error != null ){
            if( errors == null ){
                errors =  new ArrayList();
            }
            errors.add( error );
            return true;
        }
        return false;
    }

    public static <T> T readObjectFromJsonFile(String typeStorageDirectory, String fileName,
                                               Class<T> clazz) {
        try {
            File file = new File( Environment.getExternalStoragePublicDirectory(
                    typeStorageDirectory), fileName + ".json");
            if( file.exists() ) {
                String text = new Scanner(file, "UTF-8").useDelimiter("\\A").next();
                return fromJson(text, clazz);
            }else{
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T readObjectFromJsonFile(String typeStorageDirectory, String fileName, Type type) {
        try {
            File file = new File( Environment.getExternalStoragePublicDirectory(
                    typeStorageDirectory), fileName + ".json");
            if( file.exists() ) {
                String text = new Scanner(file, "UTF-8").useDelimiter("\\A").next();
                return fromJson(text, type);
            }else{
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeObjectToJsonFile(Object obj, String typeStorageDirectory, String fileName) {
        if( obj != null ) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                try {
                    File directory = Environment.getExternalStoragePublicDirectory(typeStorageDirectory);
                    if( !directory.isDirectory() ){
                        directory.mkdir();
                    }
                    File file = new File( directory, fileName + ".json");
                    PrintWriter writer = new PrintWriter(file, "UTF-8");
                    writer.print( gson.toJson( obj ) );
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    public static <T> String toJsonList( List<T> list ){
//        Type listType = new TypeToken<List<T>>(){}.getComponentName();
//        return gson.toJson( list, listType );
//    }

    //    public static JSONArray fromJsonList(String json){
//        JSONArray obj = null;
//        try {
//            obj = (JSONArray) parser.parse(json);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return obj;
//    }


    /**********************************************************************************************/
    /************************************* DATES **************************************************/
    /**********************************************************************************************/

    private static final int DEFAULT_TIME_SPAN = 1;  //1 year
    private static String TIME_OFFSET = "-04:00"; //Since Pittsburgh is GMT-04:00

    public static Date getRelativeDate(int field, int amount) {
        return getRelativeDate(new Date(), field, amount);
    }

    public static Date getRelativeDate(Date date, int field, int amount){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if( amount > 0 ) {
            cal.add(field, amount);
        }else{
            if( field == Calendar.DAY_OF_YEAR ) {
                cal.add(field, DEFAULT_TIME_SPAN * 365); //1 year
            }else if( field == Calendar.MONTH ) {
                cal.add(field, DEFAULT_TIME_SPAN * 12); //1 years
            }else if( field == Calendar.YEAR ) {
                cal.add(field, DEFAULT_TIME_SPAN); //1 years
            }
        }
        return cal.getTime();
    }

    public static String getTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date) + TIME_OFFSET;
    }

    /**
     * If format is null then "yyyy/MM/dd" will be the default format
     * @param format
     * @return
     */
    public static String getDate(Date date, String format) {
        if( format == null ) format = "yyyy/MM/dd";
        return new SimpleDateFormat( format ).format(date);
    }

    public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.YEAR, year );
        cal.set( Calendar.MONTH, month );
        cal.set( Calendar.DAY_OF_MONTH, day );
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set( Calendar.MINUTE, 0);
        cal.set( Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getDate(int year, int month, int day, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.YEAR, year );
        cal.set( Calendar.MONTH, month );
        cal.set( Calendar.DAY_OF_MONTH, day );
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set( Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getTime(Date date, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set( Calendar.MINUTE, minute );
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    /**
     * It returns a full date (date + time)
     * @param date
     * @param time in format HH:MM
     * @return
     */
    public static Date getDateTime(Date date, String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY,
                Integer.valueOf(time.substring(0, time.indexOf(":"))));
        calendar.set(Calendar.MINUTE,
                Integer.valueOf(time.substring(time.indexOf(":") + 1)));
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * It returns a Date in yyyy/MM/dd format
     * @param miliseconds
     * @return
     */
    public static String formatDate( long miliseconds ){
        return new SimpleDateFormat("yyyy/MM/dd").format(new Date(miliseconds));
    }

    /**
     * It returns a String (yyyy-MM-dd) or Time (HH:mm) or StringRfc3339 according to "type"
     * @param date
     * @param type
     * @return
     */
    public static String formatDate(Date date, String type){
        if( type.equals("DATE") ) {
            return new SimpleDateFormat("yyy-MM-dd").format( date );
        }else if( type.equals("TIME") ){
            return new SimpleDateFormat("HH:mm").format( date );
        }
        return new DateTime( date.getTime() ).toStringRfc3339();
    }

    public static int getDateField( Date date, int field ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );
        return calendar.get(field);
    }


    public static boolean isDateInRange( long timeToEvaluate, long threshold, long timeReference ){
        long minRangeTime = timeToEvaluate - threshold/2;
        long maxRangeTime = timeToEvaluate + threshold/2;
        return timeReference >= minRangeTime && timeReference <= maxRangeTime;
    }

    /**********************************************************************************************/
    /************************************** CLONE *************************************************/
    /**********************************************************************************************/

    private static Cloner cloner = new Cloner();

    public static <T> T clone( T object ){
        return cloner.deepClone(object);
    }

    public static <T extends List> T cloneList( T list ){
        return cloner.deepClone(list);
    }

    public static ArrayList cloneArray( ArrayList list ){
        ArrayList result = new ArrayList(list.size());
        for( Object element : list ){
            result.add( cloner.deepClone(element) );
        }
        return result;
    }


    /**********************************************************************************************/
    /************************************** XML ***************************************************/
    /**********************************************************************************************/

    /**
     * This method reads the parser's XML content and set the values (specified in the mappings
     * parameter) on the result object by using reflection
     * @param parser
     * @param tag
     * @param mappings
     * @param result
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T readXMLToObject(XmlPullParser parser, String tag, HashMap<String,
            String> mappings, T result) throws Exception {
        parser.require(XmlPullParser.START_TAG, null, tag);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            String smethod = mappings.get( name );

            //check if there are any attribute to be extracted:
            for( int count = 0; count < parser.getAttributeCount(); count++ ){
                String attribute = parser.getAttributeName( count );
                String methodAtt = mappings.get( name + "." + attribute );
                if( methodAtt != null ){
                    Method method = result.getClass().getMethod(methodAtt, String.class);
                    method.invoke(result, parser.getAttributeValue( null, attribute) );
                }
            }

            if( smethod == null ){
                skipXMLTag(parser);
            }else if( smethod.equals("") ){
                result = readXMLToObject(parser, name, mappings, result);
            } else {
                Method method = result.getClass().getMethod(smethod, String.class);
                method.invoke(result, parser.nextText());
            }
        }
        return result;
    }

    private static void skipXMLTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


    /**********************************************************************************************/
    /************************************** I/O ***************************************************/
    /**********************************************************************************************/

    public static void removeFile(String typeStorageDirectory, String fileName) {
        try{
            File file = new File( Environment.getExternalStoragePublicDirectory(
                    typeStorageDirectory), fileName + ".json");
            if( file.exists() ){
                file.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ByteBuffer getOutputBuffer(MediaCodec codec, int index ){
        ByteBuffer outBuf;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            outBuf = codec.getOutputBuffer(index);
        }else{
            outBuf = codec.getOutputBuffers()[index];
        }
        return outBuf;
    }

    public static ByteBuffer getInputBuffer(MediaCodec codec, int index ){
        ByteBuffer inBuf;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            inBuf = codec.getInputBuffer(index);
        }else{
            inBuf = codec.getInputBuffers()[index];
        }
        return inBuf;
    }

    public static Properties loadConfigAssets( Context app, String propName ) {
        Properties properties = new Properties();
        AssetManager am = app.getAssets();

        InputStream inputStream;
        try {
            inputStream = am.open( propName );
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propName + "' not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }


    /**********************************************************************************************/
    /************************************** IMAGES ************************************************/
    /**********************************************************************************************/

    public static int[] convertYUVtoRGB(byte[] yuv, int width, int height)
            throws NullPointerException, IllegalArgumentException {
        int[] out = new int[width * height];
        int sz = width * height;

        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = yuv[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = yuv[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = yuv[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
                        + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;
                out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
            }
        }

        return out;
    }


    /**********************************************************************************************/
    /************************************** STRINGS ***********************************************/
    /**********************************************************************************************/


    public static String replaceAll(String str, String pat, String rep){
        if (str == null)
            return null;
        return str.replaceAll(pat, rep);
    }


    @TargetApi(19)
    public static String listToString( List list ){
        StringBuilder builder = new StringBuilder();
        for( Object obj : list ){
            builder.append( obj.toString() + System.lineSeparator() );
        }
        return builder.toString();
    }

    public static String[] extractIpAddress(String destination){
        String[] destinationArray = new String[3];
        Pattern uri = Pattern.compile("rtsp://(.+):(\\d*)/(.+)");
        Matcher m = uri.matcher( destination );
        m.find();
        destinationArray[0] = m.group(1); //ip
        destinationArray[1] = m.group(2); //port
        destinationArray[2] = m.group(3); //path
        return destinationArray;
    }


    /**********************************************************************************************/
    /************************************** REFLECTION ********************************************/
    /**********************************************************************************************/

    public static <T> T createInstance(Class<T> clazz){
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**********************************************************************************************/
    /************************************** CONTROL ***********************************************/
    /**********************************************************************************************/

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final ListeningExecutorService pool = MoreExecutors.listeningDecorator(executorService);

    public static void sleep(long milis) {
        try{
            Thread.sleep( milis );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * This method executes a functionality by enclosing it into a different thread (worker thread)
     * rather than in the main thread (UI thread). You can use lambda expressions, e.g:
     * Util.execute( () -> System.out.println("test") );
     *
     * @param command
     * @param blocksForResult it blocks the thread waiting for the task result
     */
    public static <T> T execute(ExecutableTask<T> command, boolean blocksForResult) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                if (blocksForResult) {
                    return pool.submit( (Callable<T>) command ).get();
                }else{
                    pool.submit( (Runnable) command);
                }
            } else {
                if(blocksForResult) {
                    return command.call();
                }else{
                    command.run();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method executes the runnable and does not wait until the end of execution nor returns
     * a result
     * @param command
     */
    public static void execute( ExecutableTask command ){
        execute( command, false );
    }


    public static void printThread(String name){
        if( Thread.currentThread().getName().equals("pool-2-thread-2")) {
            System.err.println("Name: " + name + "  Thread: " + Thread.currentThread().getName());
        }
    }

    public static void release(){
        executorService.shutdown();
        pool.shutdown();
    }
}
