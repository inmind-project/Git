package com.yahoo.inmind.services.calendar.model;

import com.yahoo.inmind.commons.control.Util;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by oscarr on 8/26/15.
 */
public class CalendarEventVO{
    private String id;
    private String summary;
    private String location;
    private String description;
    private Date startDate;
    private Date endDate;
    private String startTime;
    private String endTime;
    private List<String> recurrence;
    private List<String> attendees;
    private int emailReminder;
    private int smsReminder;
    private Date fromDate;
    private int numberOfMonths;
    private String createdTime;

    public CalendarEventVO(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CalendarEventVO() {}

    public String getCreatedTime() {
        return createdTime;
    }

    public CalendarEventVO setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public CalendarEventVO setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public CalendarEventVO setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CalendarEventVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getFormattedStartDate() {
        return Util.formatDate( startDate, "DATE" );
    }

    public String getFormattedEndDate() {
        return Util.formatDate( endDate, "DATE" );
    }

    public List<String> getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(List<String> recurrence) {
        this.recurrence = recurrence;
    }

    public List<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<String> attendees) {
        this.attendees = attendees;
    }

    public int getEmailReminder() {
        return emailReminder;
    }

    public CalendarEventVO setEmailReminder(int minutes) {
        this.emailReminder = minutes;
        return this;
    }

    public int getSmsReminder() {
        return smsReminder;
    }

    public CalendarEventVO setSmsReminder(int minutes) {
        this.smsReminder = minutes;
        return this;
    }


    public String getStartTime() {
        return startTime;
    }

    public String getFormattedStartTime() {
        return Util.formatDate( startDate, "TIME" );
    }

    public String getEndTime() {
        return endTime;
    }

    public String getFormattedEndTime() {
        return Util.formatDate( endDate, "TIME" );
    }

    public String getEndDateTime(){
        return Util.formatDate( endDate, "DATE" ) + "T" + getEndTime();
    }

    public String getStartDateTime(){
        return Util.formatDate( startDate, "DATE" ) + "T" + getStartTime();
    }

    public String getUUID() {
        if( id == null ){
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public String getId() {
        return id;
    }

    public CalendarEventVO setId(String id) {
        this.id = id;
        return this;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public int getNumberOfMonths() {
        return numberOfMonths;
    }

    public void setNumberOfMonths(int numberOfMonths) {
        this.numberOfMonths = numberOfMonths;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        startTime = Util.getTime( startDate );
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
        endTime = Util.getTime( endDate );
    }
}
