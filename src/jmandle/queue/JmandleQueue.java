package jmandle.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class JmandleQueue {

    private static BlockingQueue<String> QUEUE;

    public static BlockingQueue<String> getInstance() {
        if (QUEUE == null) {
            QUEUE = new LinkedBlockingQueue<String>();
        }

        return QUEUE;
    }
}
