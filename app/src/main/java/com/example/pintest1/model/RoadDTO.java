package com.example.pintest1.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.pintest1.model.ContentDTO;
import java.io.Serializable;
import java.util.ArrayList;

public class RoadDTO implements Serializable {
    private String rId;
    private String uId;
    private String userId;
    private ArrayList<ContentDTO> pins;
    private ArrayList<String> keyWord;
    private String timestamap;

    public RoadDTO(ArrayList<ContentDTO> pins) {
        this.pins = pins;

    }
    public int getCountOfPins(){
        return pins.size();
    }
    public ContentDTO getPin(int index){
        return pins.get(index);
    }
}

