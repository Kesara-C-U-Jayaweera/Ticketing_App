package com.ticketsystem.javafx;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class TicketPool {
    private final Vector<Integer> tickets;
    private final int maxCapacity;
    private final AtomicInteger ticketCounter;
    private volatile int totalTicketsAdded;
    private final int totalTicketsLimit;
    private final ReentrantLock lock;
    private final Condition notFull;
    private final Condition notEmpty;
    private volatile boolean isRunning;

    public TicketPool(int maxCapacity, int totalTicketsLimit) {
        this.tickets = new Vector<>();
        this.maxCapacity = maxCapacity;
        this.totalTicketsLimit = totalTicketsLimit;
        this.ticketCounter = new AtomicInteger(0);
        this.totalTicketsAdded = 0;
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
        this.isRunning = true;
    }

    public boolean addTickets(int vendorId, int amount) {
        if (!isRunning) {
            return false;
        }

        lock.lock();
        try {
            while (isRunning && tickets.size() >= maxCapacity) {
                try {
                    notFull.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            if (!isRunning) {
                return false;
            }

            if (totalTicketsAdded >= totalTicketsLimit) {
                return false;
            }

            int ticketsToAdd = Math.min(amount, totalTicketsLimit - totalTicketsAdded);

            for (int i = 0; i < ticketsToAdd && tickets.size() < maxCapacity; i++) {
                int ticketNumber = ticketCounter.incrementAndGet();
                tickets.add(ticketNumber);
                totalTicketsAdded++;

                System.out.printf("Vendor %d added ticket #%d to the pool. Total tickets: %d%n",
                        vendorId, ticketNumber, tickets.size());
            }

            notEmpty.signalAll();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public Integer removeTicket(int customerId) {
        if (!isRunning) {
            return null;
        }

        lock.lock();
        try {
            while (isRunning && tickets.isEmpty() && totalTicketsAdded < totalTicketsLimit) {
                try {
                    System.out.printf("Customer %d waiting for tickets...%n", customerId);
                    notEmpty.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }

            if (!isRunning || (tickets.isEmpty() && totalTicketsAdded >= totalTicketsLimit)) {
                return null;
            }

            if (!tickets.isEmpty()) {
                Integer ticket = tickets.remove(0);
                System.out.printf("Customer %d purchased ticket #%d. Remaining tickets: %d%n",
                        customerId, ticket, tickets.size());

                notFull.signalAll();
                return ticket;
            }

            return null;
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            isRunning = false;
            notEmpty.signalAll();
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean allTicketsRetrieved() {
        lock.lock();
        try {
            return totalTicketsAdded >= totalTicketsLimit && tickets.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public int getAvailableTickets() {
        lock.lock();
        try {
            return tickets.size();
        } finally {
            lock.unlock();
        }
    }

    public int getTotalTicketsAdded() {
        return totalTicketsAdded;
    }

    public int getTotalTicketsLimit() {
        return totalTicketsLimit;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}