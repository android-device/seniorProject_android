package com.mooo.samcat.temperaturemonitor;

/**
 * Created by rodrigo on 4/12/16.
 */
public class sensor {
    private String name;
    private float temperature;
    private float prevTemperature;
    private String UUID;

    public sensor(String newName, float newTemperature, String newUUID) {
        this.name = newName;
        this.temperature = newTemperature;
        this.prevTemperature = 0;
        this.UUID = newUUID;
    }

    public sensor() {
        this.name = "";
        this.temperature = 0;
        this.prevTemperature = 0;
        this.UUID = "";
    }

    public String getName() {
        return this.name;
    }

    public float getTemperature() {
        return this.temperature;
    }

    public float getPrevTemperature() {
        return this.prevTemperature;
    }

    public String getUUID() {
        return this.UUID;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setTemperature(float newTemp) {
        this.prevTemperature = this.temperature;
        this.temperature = newTemp;
    }

    public void setPrevTemperature(float newTemp) { //Can only be set by setting a new temperature
        return;
    }

    public void setUUID(String newUUID) {
        this.UUID = newUUID;
    }
}
