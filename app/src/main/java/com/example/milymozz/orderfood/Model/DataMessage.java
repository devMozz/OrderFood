package com.example.milymozz.orderfood.Model;

import java.util.Map;

/**
 * Created by milymozz on 2018. 4. 4..
 */

public class DataMessage {
    public String to;
    public Map<String, String> data; // data 에는 2개의 키가 존재한다.

    public DataMessage() {
    }

    public DataMessage(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
