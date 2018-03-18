package com.suridosa.callpopup;

import java.io.Serializable;

/**
 * Created by Administrator on 2018-02-13.
 */

public class CommDomain implements Serializable {

    private String id;
    private String name;
    private String number;
    private String address;
    private String recvDt;
    private String lat;
    private String lng;


    //내 위치 저장
    String myNumber;
    String myLat;
    String myLng;

    //상대방 위치 정보
    String callNumber;
    String callLat;
    String callLng;
    String callAddress;

    String result;
    public void setResult(String result) {
        this.result = result;
    }
    public String getResult() {
        return result;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getRecvDt() {
        return recvDt;
    }
    public void setRecvDt(String recvDt) {
        this.recvDt = recvDt;
    }


    public String getLat() {
        return lat;
    }
    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }


    public String getMyNumber() {
        return myNumber;
    }
    public void setMyNumber(String myNumber) {
        this.myNumber = myNumber;
    }
    public String getMyLat() {
        return myLat;
    }
    public void setMyLat(String myLat) {
        this.myLat = myLat;
    }
    public String getMyLng() {
        return myLng;
    }
    public void setMyLng(String myLng) {
        this.myLng = myLng;
    }
    public String getCallNumber() {
        return callNumber;
    }
    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }
    public String getCallLat() {
        return callLat;
    }
    public void setCallLat(String callLat) {
        this.callLat = callLat;
    }
    public String getCallLng() {
        return callLng;
    }
    public void setCallLng(String callLng) {
        this.callLng = callLng;
    }
    public String getCallAddress() {
        return callAddress;
    }
    public void setCallAddress(String callAddress) {
        this.callAddress = callAddress;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CommonDomain [id=");
        builder.append(id);
        builder.append(", name=");
        builder.append(name);
        builder.append(", number=");
        builder.append(number);
        builder.append(", address=");
        builder.append(address);
        builder.append(", recvDt=");
        builder.append(recvDt);
        builder.append(", lat=");
        builder.append(lat);
        builder.append(", lng=");
        builder.append(lng);
        builder.append(", myNumber=");
        builder.append(myNumber);
        builder.append(", myLat=");
        builder.append(myLat);
        builder.append(", myLng=");
        builder.append(myLng);
        builder.append(", callNumber=");
        builder.append(callNumber);
        builder.append(", callLat=");
        builder.append(callLat);
        builder.append(", callLng=");
        builder.append(callLng);
        builder.append(", callAddress=");
        builder.append(callAddress);
        builder.append("]");
        return builder.toString();
    }

}
