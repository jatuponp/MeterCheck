package com.nkc.metercheck.model;

/**
 * Created by Jumpon-pc on 5/11/2558.
 */
public class Room {
    private String room_id, meter_start, meter_end;
    private Integer meterId;

    public Room(){}
    public Room(String room_id, String meter_start, String meter_end, Integer meterId){
        this.room_id = room_id;
        this.meter_start = meter_start;
        this.meter_end = meter_end;
        this.meterId = meterId;
    }

    public void setRoomId(String room_id){
        this.room_id = room_id;
    }

    public String getRoomId(){
        return room_id;
    }

    public void setMeterStart(String meter_start){
        this.meter_start = meter_start;
    }

    public String getMeterStart(){
        return meter_start;
    }

    public void setMeterEnd(String meter_end){
        this.meter_end = meter_end;
    }

    public String getMeterEnd(){
        return meter_end;
    }

    public void setMeterId(Integer meterId){
        this.meterId = meterId;
    }

    public Integer getMeterId(){
        return meterId;
    }

}
