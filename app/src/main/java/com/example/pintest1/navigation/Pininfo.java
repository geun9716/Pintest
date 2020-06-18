package com.example.pintest1.navigation;

public class Pininfo {
    double pinlatitude;
    double pinlontitude;
    double distance;
    double degree;
    String pid;
    boolean pinplaced;

    Pininfo(double latitude, double lontitude){
        pinlatitude=latitude;
        pinlontitude=lontitude;
        distance=0;
        pinplaced=false;
        pid=null;
    }
}
