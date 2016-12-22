package ru.fkoban.utils;

import ru.fkoban.gelix.GelixOnePacket;

import java.util.Locale;

public class SimpleJSONer {
    public static String makeJSONFromProcessingObject(GelixOnePacket processingGelixObject){
        return String.format(new Locale("en"),"{\"sdt\":\"%s\",\"ts\":%ts,\"lat\":%.6f,\"lon\":%.6f,\"speed\":%.1f,\"dir\":%.1f,\"sats\":%d,\"in0\":%.3f,\"in1\":%.3f,\"in2\":%.3f,\"in3\":%.3f,\"in4\":%d,\"in5\":%d,\"in6\":%d,\"in7\":%d,\"com\":%d}",
                processingGelixObject.strDateTime,
                processingGelixObject.timeStamp,
                processingGelixObject.lat,
                processingGelixObject.lon,
                processingGelixObject.speed,
                processingGelixObject.dir,
                processingGelixObject.sats,
                processingGelixObject.in0,
                processingGelixObject.in1,
                processingGelixObject.in2,
                processingGelixObject.in3,
                processingGelixObject.in4,
                processingGelixObject.in5,
                processingGelixObject.in6,
                processingGelixObject.in7,
                processingGelixObject.rs232);
    }
}
