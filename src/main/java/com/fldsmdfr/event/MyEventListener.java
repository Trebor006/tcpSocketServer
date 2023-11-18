package com.fldsmdfr.event;

import java.util.EventListener;

public interface MyEventListener extends EventListener {
  void myEventOccurred(MyEvent evt);
}
