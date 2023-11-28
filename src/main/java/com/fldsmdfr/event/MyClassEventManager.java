package com.fldsmdfr.event;


import javax.swing.event.EventListenerList;

public class MyClassEventManager {
  protected EventListenerList listenerList = new EventListenerList();

  public void addMyEventListener(MyEventListener listener) {
    listenerList.add(MyEventListener.class, listener);
  }

  public void removeMyEventListener(MyEventListener listener) {
    listenerList.remove(MyEventListener.class, listener);
  }

  public void fireMyEvent(MyEvent evt) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i = i + 2) {
      if (listeners[i] == MyEventListener.class) {
        ((MyEventListener) listeners[i + 1]).myEventOccurred(evt);
      }
    }
  }
}
