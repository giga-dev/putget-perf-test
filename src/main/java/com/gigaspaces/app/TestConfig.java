package com.gigaspaces.app;

public class TestConfig {
    public static final String spaceName = System.getProperty("spaceName", "rgtest");
    public static final int numberOfEntries= Integer.getInteger("numberOfEntries", 250_000);//4_000_000;
    public static final int entrySizeFrom = Integer.getInteger("entrySizeFrom", 100);
    public static final int entrySizeTo = Integer.getInteger("entrySizeTo", 1_000); //10_000;
    public static final int numberOfThreads = Integer.getInteger("numberOfThreads", 4);
    public static final boolean doPut = Boolean.getBoolean("doPut");
    public static final int putGetFactor = Integer.getInteger("putGetFactor", 4);
    public static final int testCycles = Integer.getInteger("testCycles", 1000);
    public static final boolean embedded = Boolean.getBoolean("embedded");

    @Override
    public String toString() {
        return "TestConfig{" +
                "spaceName='" + spaceName + '\'' +
                ", numberOfEntries=" + numberOfEntries +
                ", entrySizeFrom=" + entrySizeFrom +
                ", entrySizeTo=" + entrySizeTo +
                ", numberOfThreads=" + numberOfThreads +
                ", doPut=" + doPut +
                ", putGetFactor=" + putGetFactor +
                ", testCycles=" + testCycles +
                ", embedded=" + embedded +
                '}';
    }
}