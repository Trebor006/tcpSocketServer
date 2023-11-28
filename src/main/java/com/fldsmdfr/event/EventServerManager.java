package com.fldsmdfr.event;


import javax.swing.event.EventListenerList;

public class EventServerManager {
    protected EventListenerList listenerList = new EventListenerList();

    public void addEventListener(EventServerListener listener) {
        listenerList.add(EventServerListener.class, listener);
    }

    public void removeEventListener(EventServerListener listener) {
        listenerList.remove(EventServerListener.class, listener);
    }

    public void fireEventServer(EventServer evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == EventServerListener.class) {
                ((EventServerListener) listeners[i + 1]).eventServerOccurred(evt);
            }
        }
    }
}
