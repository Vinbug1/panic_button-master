package com.example.ubfac.panicbutton.model;

import java.util.ArrayList;

/**
 * Created by Emem on 10/31/18.
 */
public class LGAs {
    String state;
    String stateId;
    String name;
    int value;
    ArrayList<PollingUnit> pollingUnits;


    public LGAs() {
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ArrayList<PollingUnit> getPollingUnits() {
        return pollingUnits;
    }

    public void setPollingUnits(ArrayList<PollingUnit> pollingUnits) {
        this.pollingUnits = pollingUnits;
    }

    @Override
    public String toString() {
        return "LGAs{" +
                "state='" + state + '\'' +
                ", stateId='" + stateId + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                ", pollingUnits=" + pollingUnits +
                '}';
    }
}
