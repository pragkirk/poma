package org.springframework.osgi.iandt.deadlock;

/**
 * @author Hal Hildebrand
 *         Date: Jun 5, 2007
 *         Time: 8:48:51 PM
 */
public class B {
    public static final Object lockB = new Object();


    static {
        Runnable runnable = new Runnable() {

            public void run() {
                while (true) {
                    synchronized (lockB) {
                        synchronized (A.lockA) {
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
        Thread t = new Thread(runnable, "deadlock B");
        t.setDaemon(true);
        t.start();
        Thread.yield();
    }


    private A a;


    public void setA(A a) {
        synchronized (lockB) {
            synchronized (A.lockA) {
                this.a = a;
            }
        }
    }


    public String toString() {
        return a.toString();
    }
}
