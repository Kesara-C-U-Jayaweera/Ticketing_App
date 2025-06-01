package com.ticketsystem.javafx;

import java.util.concurrent.atomic.AtomicInteger;

public class Vendor implements Runnable {
    private final int vendorId;
    private final TicketPool ticketPool;
    private final int ticketRetrievalTime;
    private volatile boolean running;
    private final AtomicInteger ticketsAdded;
    private Thread vendorThread;

    public Vendor(int vendorId, TicketPool ticketPool, int ticketRetrievalTime) {
        this.vendorId = vendorId;
        this.ticketPool = ticketPool;
        this.ticketRetrievalTime = ticketRetrievalTime;
        this.running = true;
        this.ticketsAdded = new AtomicInteger(0);
    }

    @Override
    public void run() {
        vendorThread = Thread.currentThread();
        System.out.printf("Vendor %d started providing tickets%n", vendorId);

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                boolean success = ticketPool.addTickets(vendorId, 1);
                if (!success || !running) {
                    break;
                }

                Thread.sleep(ticketRetrievalTime);
                ticketsAdded.incrementAndGet();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.printf("Vendor %d finishing. Total tickets added: %d%n",
                vendorId, ticketsAdded.get());
    }

    public void stop() {
        running = false;
        if (vendorThread != null) {
            vendorThread.interrupt();
        }
    }

    public int getVendorId() {
        return vendorId;
    }

    public int getTicketsAdded() {
        return ticketsAdded.get();
    }

    public int getTicketRetrievalTime() {
        return ticketRetrievalTime;
    }

    public boolean isRunning() {
        return running;
    }

    public String getStatistics() {
        return String.format("Vendor %d Statistics:%n" +
                        "Total Tickets Added: %d%n" +
                        "Status: %s",
                vendorId, ticketsAdded.get(),
                running ? "Running" : "Stopped");
    }
}