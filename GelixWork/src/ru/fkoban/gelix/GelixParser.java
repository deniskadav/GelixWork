package ru.fkoban.gelix;

import ru.fkoban.utils.SimpleJSONer;
import ru.fkoban.utils.SimpleWialonTCPClient;
import ru.fkoban.utils.WialonIPS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GelixParser {
    private int timeDiff = 3;
    private int startOffset = 8;
    private int len;
    private String packet;
    private String packetInProcess;
    private GelixOnePacket processingGelixObject;
    private List<String> jsonList = new ArrayList<String>();
    private List<String> wiaIPSList = new ArrayList<String>();

    public GelixParser(String inputData,int len){
        this.len = (len + 1) * 2;//1 byte CRC at the end of each packet and one for all
        this.packet = inputData;
    }

    private int get2bytes(int offset) {
        String hexValue = packetInProcess.substring(offset,offset + 4);
        return Integer.parseInt(hexValue,16);
    }

    private int get1b(int offset) {
        String hexValue = packetInProcess.substring(offset,offset + 2);
        return Integer.parseInt(hexValue,16);
    }

    private String getTime(long val) {
        int hh  =  (int) val / 3600 ;
        int mm  = (int) (val - hh * 3600) / 60 ;
        int ss  = (int) (val - hh * 3600 - mm * 60);
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    private String getDate( long val ){
        int dd = (int)val & 0x1F;
        if (dd == 0) dd = 1;
        int mm = (int)((val >> 5) & 0x0F);
        int yy = (int)((val >> 9) & 0x1F);
        if (mm == 2){//check february
            if ((yy == 8)||(yy == 12)||(yy == 16)||(yy == 20)||(yy == 24)){
                if (dd == 30){
                    dd = 1;
                    mm = 3;
                }
            }
            else{
                if (dd == 29){
                    dd = 1;
                    mm = 3;
                }
            }
        }
        else
        if ((mm ==4)||(mm == 6)||(mm ==9)||(mm == 11)){
            if (dd == 31){
                dd = 1;
                mm++;
            }
        }
        return String.format("%02d-%02d-%02d", yy, mm, dd);
    }

    private void calculateDateTime(int offset){
        int ti = get2bytes(offset);//		time
        int da = get2bytes(offset + 4);//	date
        if ( (da & 0x8000) > 0 ) {
            ti = ti + 0x10000;
        }

        if( ti + timeDiff * 3600 >= 24 * 3600 ) {
            da++;
            ti += timeDiff * 3600 - 24 * 3600;
        }
        else ti += timeDiff * 3600;

        this.processingGelixObject.strDateTime = getDate(da & 0x7FFF) + " " + getTime(ti)  ;
        SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        try{
            //Note! this is depends of platform time settings (GMT+x)
            this.processingGelixObject.timeStamp = df.parse(this.processingGelixObject.strDateTime);
        } catch (Exception e) {
            //if error date formatting
            this.processingGelixObject.timeStamp = new Date();
        }
    }

    private double getLatLon(int offset) {
        String dotc = packetInProcess.substring(offset, offset + 1);

        if( !dotc.equals("A")) { return 0.0; }//if coordinate marked as invalid we return 0
        int dot = (Integer.parseInt(dotc,16) >> 1 ) + 1;//dot means digits after dot, dont ask me why

        double multiplier = Math.pow(10, dot);
        long pos = Long.parseLong(packetInProcess.substring(offset, offset + 8),16) & 0x1FFFFFFF;	// GGGMMDDDD

        double mmPart = pos % multiplier;
        int ggPart  = (int)((pos - mmPart) / multiplier);
        double mm  = mmPart / 1000000 * 5 / 3;  //convert to degrees from minutes
        return ggPart + mm;
    }

    private void calculateLatLon(int offset){
        this.processingGelixObject.lat = getLatLon(offset);				//4 bytes of latitude
        this.processingGelixObject.lon = getLatLon(offset + 8);			//4 bytes of longtitude
    }

    private double fractal(int val) {
        double ret = val & 0x3FFF;
        if( (val & 0x4000) > 0) ret = ret / 10;
        if( (val & 0x8000) > 0) ret = ret * (-1);
        return ret;
    }

    private void calculate5bAnalogValues(int offset){
        //4 analog inputs is coded in 5 bytes
        int a0 = (Integer.parseInt(packetInProcess.substring(offset, offset + 3), 16) >> 2) & 0x03FF ;		// xxx0000000
        int a1 = (Integer.parseInt(packetInProcess.substring(offset + 2, offset + 3 + 2), 16)) & 0x03FF ;// 00xxx00000
        int a2 = (Integer.parseInt(packetInProcess.substring(offset + 5, offset + 3 + 5), 16) >> 2 ) & 0x03FF ;// 00000xxx00
        int a3 = (Integer.parseInt(packetInProcess.substring(offset + 7, offset + 3 + 7), 16)) & 0x03FF ;// 0000000xxx

        this.processingGelixObject.in0 = a0 * 50 / 1023;
        this.processingGelixObject.in1 = a1 * 50 / 1023;
        this.processingGelixObject.in2 = a2 * 50 / 1023;
        this.processingGelixObject.in3 = a3 * 50 / 1023;
    }

    /**
     * input 00027F7DA6005264A3410AEEA45D9062850A404095433D01013E80000000743174070010C31B00FFFF04
     * output la=54.99047,lo=73.40112,sp=27.565,dig=1,co=82.9,ma=64,an=12.21,0,0,0,cnt=29745,29703,16,49947,com=65535
     * input 44044C36A6055264A33C4833A32FF268850A80415B4A9D0101420000000046052000028874600001517C
     * output la=54.47048,lo=53.78982,sp=64.195,dig=1,co=271.7,ma=0,an=12.90,0,0,0,cnt=17925,8192,648,29792,com=337
     *
     * @param onePacket - one data packet from device log
     */
    private void parseParams(String onePacket){
        this.packetInProcess = onePacket;
        calculateDateTime(startOffset);
        calculateLatLon(startOffset + 8);
        this.processingGelixObject.sats = get1b(startOffset + 26) & 0x0F;		// satellites used
        //za = get1b(start_offset + 26) >> 4;			//zone alarm status
        //ma = get1b(start_offset + 28)&0x7F;			// macro id
        this.processingGelixObject.speed = fractal(get2bytes(startOffset + 30)) * 1.85;	// speed initially in mph
        this.processingGelixObject.dir  = fractal(get2bytes(startOffset + 34));		// course in degrees

        calculate5bAnalogValues(startOffset + 42);//fill in0-in3

        this.processingGelixObject.in4 = get2bytes(startOffset + 52);
        this.processingGelixObject.in5 = get2bytes(startOffset + 56);
        this.processingGelixObject.in6 = get2bytes(startOffset + 60);
        this.processingGelixObject.in7 = get2bytes(startOffset + 64);

        if (onePacket.length() >= (startOffset + 74))//get2bytes need to read 4 symbols, need to check if exists
            this.processingGelixObject.rs232 = get2bytes(startOffset + 70);
        //TO DO need check is_valid_coords later
    }

    private String getOnePacket(int idx){
        return this.packet.substring(this.len * idx,this.len * idx+this.len);
    }

    public void processData(){
        for(int i = 0;i < (this.packet.length() / this.len);i++) {
            String onePacket = getOnePacket(i);
            System.out.println("onePacket="+onePacket);
            this.processingGelixObject = new GelixOnePacket();
            parseParams(onePacket);
            this.jsonList.add(SimpleJSONer.makeJSONFromProcessingObject(this.processingGelixObject));
            this.wiaIPSList.add(WialonIPS.makeIPSFromProcessingObject(this.processingGelixObject));
        }
    }

    /**
     * try to send data to Wialon server directly (193.193.165.165:20332 for WialonIPS)
     * 1. we need to authorize like #L#imei;password\r\n
     * 2. server answer us #AL#1\r\n or #AL#0\r\n  if first one - we passed and continue
     * #D#date;time;lat1;lat2;lon1;lon2;speed;course;height;sats;hdop;inputs;outputs;adc;ibutton;params\r\n
     * #D#date;time;lat1;lat2;lon1;lon2;speed;course;height;sats;hdop;inputs;outputs;adc;ibutton;params\r\n
     * #D#date;time;lat1;lat2;lon1;lon2;speed;course;height;sats;hdop;inputs;outputs;adc;ibutton;params\r\n
     * 3. server send us confirmation #AD#1\r\n
     *
     * @param imei - unique id of device, most of time it is IMEI of GPRS modem
     */
    public void sendDataToWialonIPSServer(String imei){
        String allIPSStrings = "";
        for(String oneElem : this.wiaIPSList){
            allIPSStrings += oneElem;
            System.out.println(oneElem);
        }

        SimpleWialonTCPClient client  = new SimpleWialonTCPClient("#L#"+imei+";NA\r\n",allIPSStrings);
        new Thread(client).start();
    }
}
