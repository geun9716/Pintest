package com.example.pintest1.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.pintest1.model.ContentDTO;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RoadDTO implements Serializable {
    private String uId;
    private String userId;
    private ArrayList<ContentDTO> pins;
    private String timestamp;

    public RoadDTO(ArrayList<ContentDTO> pins) {
        this.pins = pins;
    }
    public void setuId(String uId){
        this.uId = uId;
    }
    public void setuserId(String userId){
        this.userId = userId;
    }
    public void setTimestamp(){

        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public String getuId(){return this.uId;}
    public String getuserId(){return this.userId;}
    public ArrayList<ContentDTO> getPins(){return this.pins;}
    public String getTimestamp(){return this.timestamp;}

    public ContentDTO getPin(int index){
        return pins.get(index);
    }
}

