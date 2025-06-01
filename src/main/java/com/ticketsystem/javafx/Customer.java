package com.ticketsystem.javafx;

import java.util.concurrent.atomic.AtomicInteger;

public class Customer implements Runnable {
    private final int customerId;
    private final TicketPool ticketPool;
    private final int ticketBuyingTime;
    private volatile boolean running;
    private final AtomicInteger ticketsPurchased;
    private Thread customerThread;

    public Customer(int customerId, TicketPool ticketPool, int ticketBuyingTime) {
        this.customerId = customerId;
        this.ticketPool = ticketPool;
        this.ticketBuyingTime = ticketBuyingTime;
        this.running = true;
        this.ticketsPurchased = new AtomicInteger(0);
    }

    @Override
    public void run() {
        customerThread = Thread.currentThread();
        System.out.printf("Customer %d started shopping for tickets%n", customerId);

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Integer ticket = ticketPool.removeTicket(customerId);
                if (ticket == null || !running) {
                    break;
                }

                Thread.sleep(ticketBuyingTime);
                ticketsPurchased.incrementAndGet();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.printf("Customer %d finishing. Total tickets purchased: %d%n",
                customerId, ticketsPurchased.get());
    }

    public void stop() {
        running = false;
        if (customerThread != null) {
            customerThread.interrupt();
        }
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getTicketsPurchased() {
        return ticketsPurchased.get();
    }

    public int getTicketBuyingTime() {
        return ticketBuyingTime;
    }

    public boolean isRunning() {
        return running;
    }

    public String getStatistics() {
        return String.format("Customer %d Statistics:%n" +
                        "Total Tickets Purchased: %d%n" +
                        "Status: %s",
                customerId, ticketsPurchased.get(),
                running ? "Running" : "Stopped");
    }
}