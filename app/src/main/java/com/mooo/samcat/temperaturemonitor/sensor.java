package com.mooo.samcat.temperaturemonitor;

/**
 * Created by rodrigo on 4/12/16.
 */
public class sensor {
    private String name; //human given name
    private int temperature;
    private int prevTemperature;
    private String address; //Bluetooth address
    private String deviceID; //assigned device ID
    private float battery;

    public sensor(String newName, String newID, String newAddress) {
        this.name = newName;
        this.deviceID = newID;
        this.temperature = 0;
        this.prevTemperature = 0;
        this.address = newAddress;
    }

    public sensor() {
        this.name = "";
        this.temperature = 0;
        this.prevTemperature = 0;
        this.address = "";
        deviceID = "";
        battery = 0;
    }

    public String getName() {
        if(this.name.equals(""))
            return this.deviceID;
        else
            return this.name;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public int getPrevTemperature() {
        return this.prevTemperature;
    }

    public float getBattery() {
        return this.battery;
    }

    public String getAddress() {
        return this.address;
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    private void setTemperature(int newTemp) {
        this.prevTemperature = this.temperature;
        this.temperature = newTemp;
    }

    private void setPrevTemperature(float newTemp) {return;} //Cannot be manually set!

    public void setAddress(String newAddress) {
        this.address = newAddress;
    }

    private void setBattery(float newBattery) {this.battery = newBattery;}

    private void setDeviceID(String newID) {this.deviceID = newID;}

    public void setData(String bleReadData) { //update values using data read from ibeacon
        String temperature = bleReadData.substring(50, 52);
        String battery = bleReadData.substring(52,53);

        setTemperature(hex2decimal(temperature));
        float tempBattery = (float)hex2decimal(battery)/(float)hex2decimal("F");
        setBattery(tempBattery);
        setDeviceID(bleReadData.substring(53,58));
    }

    public void setTemp_batt(String bleReadData) {
        String temperature = bleReadData.substring(50, 52);
        String battery = bleReadData.substring(52,53);

        setTemperature(hex2decimal(temperature));
        float tempBattery = (float)hex2decimal(battery)/(float)hex2decimal("F");
        setBattery(tempBattery);
    }

    private int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
         s = s.toUpperCase();
         int val = 0;
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             int d = digits.indexOf(c);
             val = 16*val + d;
         }
         return val;
    }
}
