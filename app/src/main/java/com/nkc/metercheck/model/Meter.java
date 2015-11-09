package com.nkc.metercheck.model;

/**
 * Created by Jumpon-pc on 9/11/2558.
 */
public class Meter {
    private String room_id, years, meter_start, meter_end, create_at;
    private Integer months, terms, pay_type;

    public Meter(){}
    public Meter(String room_id, Integer months, Integer terms, String years, String meter_start, String meter_end, Integer pay_type, String create_at){
        this.room_id = room_id;
        this.months = months;
        this.terms = terms;
        this.meter_start = meter_start;
        this.meter_end = meter_end;
        this.pay_type = pay_type;
        this.create_at = create_at;
    }

    public void setRoomId(String room_id){
        this.room_id = room_id;
    }

    public String getRoomId(){
        return room_id;
    }

    public void setMonths(Integer months){
        this.months = months;
    }

    public Integer getMonths(){
        return months;
    }

    public void setTerms(Integer terms){
        this.terms = terms;
    }

    public Integer getTerms(){
        return terms;
    }

    public void setYears(String years){
        this.years = years;
    }

    public String getYears(){
        return years;
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

    public void setPayType(Integer pay_type){
        this.pay_type = pay_type;
    }

    public Integer getPayType(){
        return pay_type;
    }

    public void setCreate(String create_at){
        this.create_at = create_at;
    }

    public String getCreate(){
        return create_at;
    }
}
