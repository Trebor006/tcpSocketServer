package com.fldsmdfr.event;

import org.json.JSONObject;

import java.util.EventObject;

public class EventServer extends EventObject {

    private JSONObject data;

    public EventServer(Object source, JSONObject data) {
        super(source);
        this.data = data;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }
}
