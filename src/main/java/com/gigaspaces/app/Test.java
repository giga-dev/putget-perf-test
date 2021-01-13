package com.gigaspaces.app;

import org.openspaces.core.GigaSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

    private static final Logger logger = LoggerFactory.getLogger(Test.class.getName());
    final static ExecutorService executorService = Executors.newFixedThreadPool(TestConfig.numberOfThreads);

    public static void main(String[] args) {
        try {
            GigaSpace gigaSpace = CacheFactory.getOrCreateSpace(TestConfig.spaceName, TestConfig.embedded);
            logger.info("system properties: \n" + System.getProperties()+"\n");
            logger.info("testConfig " + new TestConfig() +"\n");

            for (int i = 1; i <= TestConfig.testCycles; i++) {
                Test t = new Test(gigaSpace, i);
            }
            logger.info("done");
        } catch (Exception e){
            logger.error("failed to complete test", e);
        } finally {
            System.exit(0);
        }
    }


    public Test(GigaSpace gigaSpace, int cycle) throws InterruptedException {

        logger.info("test cycle {} (out of {})", cycle, TestConfig.testCycles);
        if (cycle == 1) {
            logger.info("clear Space");
            gigaSpace.clear(null);
        }

        final CountDownLatch loadLatch = new CountDownLatch(TestConfig.numberOfEntries);
        final CountDownLatch putLatch = new CountDownLatch(TestConfig.numberOfEntries);
        final CountDownLatch getLatch = new CountDownLatch(TestConfig.numberOfEntries* TestConfig.putGetFactor);
        long start = System.currentTimeMillis();

        if (cycle == 1) {
            logger.info("load Space with {} entries", TestConfig.numberOfEntries);
            for (int i = 0; i < TestConfig.numberOfEntries; i++) {
                executorService.submit(new LoadTask(gigaSpace, loadLatch));
            }
            loadLatch.await();

            long end = System.currentTimeMillis();

            final double duration = Math.max(0.1, (end - start) / 1000);
            logger.info("Duration: " + duration + "s");
            logger.info("Ops/sec: " + String.format("%.2f", TestConfig.numberOfEntries/duration));
            logger.info("---");
        } else {
            int t = 0;
            final GetTask[] getTasks = new GetTask[TestConfig.numberOfEntries * TestConfig.putGetFactor];

            for (int i = 0; i < TestConfig.numberOfEntries; i++) {
                if (TestConfig.doPut) {
                    executorService.submit(new PutTask(gigaSpace, putLatch));
                }
                for (int j = 0; j < TestConfig.putGetFactor; j++) {
                    GetTask getTask = new GetTask(gigaSpace, getLatch);
                    getTasks[t++] = getTask;
                    executorService.submit(getTask);
                }
            }

            if (TestConfig.doPut) {
                putLatch.await();
            }
            getLatch.await();

            long end = System.currentTimeMillis();
            final double duration = Math.max(0.1, (end - start) / 1000);
            final int totalTasks = TestConfig.doPut ? (TestConfig.putGetFactor + 1) * TestConfig.numberOfEntries
                                    : (TestConfig.putGetFactor) * TestConfig.numberOfEntries;
            logger.info("Duration: " + duration + "s");
            logger.info("Ops/sec: " + String.format("%.2f", totalTasks/duration));
            reportLatency(getTasks);
            logger.info("---");
        }
    }

    private void reportLatency(GetTask[] getTasks) {
        if (getTasks == null) return;
        double avgerage = 0 ;
        for (GetTask getTask : getTasks) {
            double latencyMicroSeconds = 0.001 * getTask.getLatency();
            avgerage += latencyMicroSeconds;
        }
        final double avg = (avgerage/getTasks.length);
        int aboveAvg = 0;
        int belowAvg = 0;
        for (GetTask getTask : getTasks) {
            double latency = 0.001 * getTask.getLatency();
            if (latency > avg)
                aboveAvg++;
            else
                belowAvg++;
        }

        logger.info("Latency:  below {}%  | avg: {} us | above {}%",
                String.format("%.2f", 100.0*belowAvg/getTasks.length),
                String.format("%.2f",avg),
                String.format("%.2f", 100.0*aboveAvg/getTasks.length));
    }

    private static class LoadTask implements Runnable {
        private GigaSpace gigaSpace;
        private CountDownLatch latch;
        private static final AtomicInteger key = new AtomicInteger();

        public LoadTask(GigaSpace gigaSpace, CountDownLatch latch) {
            this.gigaSpace = gigaSpace;
            this.latch = latch;
        }

        @Override
        public void run() {
            //adapt to radargun test
            KeyValEntry entry = new KeyValEntry();
            entry.setKey((long) key.incrementAndGet());
            Object val = getVal();
            entry.setVal(val.toString());
            gigaSpace.write(entry);
            latch.countDown();
        }
    }

    private static class PutTask implements Runnable {
        private GigaSpace gigaSpace;
        private CountDownLatch latch;

        public PutTask(GigaSpace gigaSpace, CountDownLatch latch) {
            this.gigaSpace = gigaSpace;
            this.latch = latch;
        }

        @Override
        public void run() {
            long key = getKey();
            KeyValEntry entry = new KeyValEntry();
            entry.setKey(key);
            Object val = getVal();
            entry.setVal(val.toString());
            gigaSpace.write(entry);
            latch.countDown();
        }
    }

    private static class GetTask implements Runnable {
        private GigaSpace gigaSpace;
        private CountDownLatch latch;
        private long latency;

        public GetTask(GigaSpace gigaSpace, CountDownLatch latch) {
            this.gigaSpace = gigaSpace;
            this.latch = latch;
        }

        @Override
        public void run() {
            final long key = getKey();
            final long s = System.nanoTime();
            final Object o = gigaSpace.readById(KeyValEntry.class, key);
            final long e = System.nanoTime();
            if (o == null) logger.error("empty result for key " + key);
            latency = (e - s);
            latch.countDown();
        }

        public long getLatency() {
            return latency;
        }
    }

    final static Random random = new Random();

    public static int getKey() {
        return Math.abs(random.nextInt(TestConfig.numberOfEntries)+1);
    }
    public static byte[] getVal() {
        int size = random.nextInt(TestConfig.entrySizeTo - TestConfig.entrySizeFrom) + TestConfig.entrySizeFrom;
        byte[] array = new byte[size];
        random.nextBytes(array);
        return array;
    }
}
