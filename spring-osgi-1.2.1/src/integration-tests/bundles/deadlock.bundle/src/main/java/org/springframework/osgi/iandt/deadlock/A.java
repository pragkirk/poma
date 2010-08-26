package org.springframework.osgi.iandt.deadlock;

/**
 * @author Hal Hildebrand
 *         Date: Jun 5, 2007
 *         Time: 8:48:43 PM
 */
public class A implements Test {
    public static final Object lockA = new Object();


    static {
        Runnable runnable = new Runnable() {

            public void run() {
                while (true) {
                    synchronized (lockA) {
                        synchronized (B.lockB) {
                            try {
                                Thread.sleep(300 * 1000); // five minutes
                            } catch (InterruptedException e) {
                                // We're mean and ignoring the InterruptedException
                            }
                        }
                    }
                }
            }
        };
        Thread t = new Thread(runnable, "deadlock A");
        t.setDaemon(true);
        t.start();
        Thread.yield();
    }

    private B b;


    public void setB(B b) {
        synchronized(lockA) {
            synchronized(B.lockB) {
                this.b = b;    
            }
        }
    }

    public String someMethod() {
        return b.toString();
    }

}
