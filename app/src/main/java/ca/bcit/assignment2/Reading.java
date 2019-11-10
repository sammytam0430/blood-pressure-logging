package ca.bcit.assignment2;

import android.graphics.Color;

public class Reading {
    private String id;
    private String userid;
    private String readingDate;
    private String readingTime;
    private double systolicReading;
    private double diastolicReading;
    private String condition;

    public Reading(String id, String userid, String d, String t, double sr, double dr) {
        this.id = id;
        this.userid = userid;
        readingDate = d;
        readingTime = t;
        systolicReading = sr;
        diastolicReading = dr;
        condition = calculateCondition(systolicReading, diastolicReading);
    }

    public Reading() {
    }

    public String getId() {
        return id;
    }

    public String getUserid() {
        return userid;
    }

    public String getReadingDate() {
        return readingDate;
    }

    public String getReadingTime() {
        return readingTime;
    }

    public double getSystolicReading() {
        return systolicReading;
    }

    public double getDiastolicReading() {
        return diastolicReading;
    }

    public String getCondition() {
        return condition;
    }

    public String getMonth() {
        return readingDate.split("/")[1];
    }

    public String getYear() {
        return readingDate.split("/")[0];
    }

    public static String calculateCondition(double sr, double dr) {
        if (sr < 120 && dr < 80)
            return "Normal";
        else if (sr < 130 && dr < 80)
            return "Elevated";
        else if (sr < 140 || dr < 90)
            return "Stage 1";
        else if (sr > 180 || dr > 120)
            return "Hypertensive Crisis";
        else if (sr >= 140 || dr >= 90)
        return "Stage 2";
        else
            return "Error";
    }

    public static int getColor(String condition) {
        switch (condition) {
            case "Normal":
                return Color.GREEN;
            case "Elevated":
                return Color.YELLOW;
            case "Stage 1":
                return Color.parseColor("#FFCEBA");
            case "Stage 2":
                return Color.parseColor("#FF986E");
            case "Hypertensive Crisis":
                return Color.RED;
            default:
                return Color.WHITE;
        }
    }
}
