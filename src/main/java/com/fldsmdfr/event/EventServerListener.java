package com.fldsmdfr.event;

import java.util.EventListener;

public interface EventServerListener extends EventListener {
    void eventServerOccurred(EventServer evt);
}
