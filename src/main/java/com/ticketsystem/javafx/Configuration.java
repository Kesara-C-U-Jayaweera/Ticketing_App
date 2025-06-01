package com.ticketsystem.javafx;

import java.io.*;
import java.util.Properties;

public class Configuration implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int totalTickets;
    private final int maxTicketCapacity;
    private final int ticketRetrievalTime;
    private final int ticketBuyingTime;

    public Configuration(int totalTickets, int maxTicketCapacity, int ticketRetrievalTime, int ticketBuyingTime) {
        this.totalTickets = validateTotalTickets(totalTickets);
        this.maxTicketCapacity = validateMaxCapacity(maxTicketCapacity, totalTickets);
        this.ticketRetrievalTime = validateTime(ticketRetrievalTime, "Ticket retrieval time");
        this.ticketBuyingTime = validateTime(ticketBuyingTime, "Ticket buying time");
    }

    private int validateTotalTickets(int tickets) {
        if (tickets < 10 || tickets > 1000) {
            throw new IllegalArgumentException("Total tickets must be between 10 and 1000");
        }
        return tickets;
    }

    private int validateMaxCapacity(int capacity, int totalTickets) {
        if (capacity < totalTickets || capacity > 1000) {
            throw new IllegalArgumentException("Max capacity must be between total tickets and 1000");
        }
        return capacity;
    }

    private int validateTime(int time, String fieldName) {
        if (time < 500 || time > 10000) {
            throw new IllegalArgumentException(fieldName + " must be between 500 and 10000ms");
        }
        return time;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }

    public int getTicketRetrievalTime() {
        return ticketRetrievalTime;
    }

    public int getTicketBuyingTime() {
        return ticketBuyingTime;
    }

    public void saveToFile(String filename) {
        File directory = new File(new File(filename).getParent());
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + directory);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            Properties props = new Properties();
            props.setProperty("TotalTickets", String.valueOf(totalTickets));
            props.setProperty("MaxTicketCapacity", String.valueOf(maxTicketCapacity));
            props.setProperty("TicketRetrievalTime", String.valueOf(ticketRetrievalTime));
            props.setProperty("TicketBuyingTime", String.valueOf(ticketBuyingTime));

            props.store(writer, "Ticket System Configuration");
        } catch (IOException e) {
            throw new RuntimeException("Error saving configuration: " + e.getMessage(), e);
        }
    }

    public static Configuration loadFromFile(String filename) {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(filename)) {
            props.load(reader);

            return new Configuration(
                    Integer.parseInt(props.getProperty("TotalTickets")),
                    Integer.parseInt(props.getProperty("MaxTicketCapacity")),
                    Integer.parseInt(props.getProperty("TicketRetrievalTime")),
                    Integer.parseInt(props.getProperty("TicketBuyingTime"))
            );
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Error loading configuration: " + e.getMessage(), e);
        }
    }

    public boolean isValid() {
        try {
            validateTotalTickets(totalTickets);
            validateMaxCapacity(maxTicketCapacity, totalTickets);
            validateTime(ticketRetrievalTime, "Ticket retrieval time");
            validateTime(ticketBuyingTime, "Ticket buying time");
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Configuration{totalTickets=%d, maxCapacity=%d, retrievalTime=%d, buyingTime=%d}",
                totalTickets, maxTicketCapacity, ticketRetrievalTime, ticketBuyingTime);
    }
}