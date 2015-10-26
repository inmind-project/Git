package com.yahoo.inmind.comm.calendar.model;

import com.yahoo.inmind.comm.generic.model.BaseEvent;
import com.yahoo.inmind.services.calendar.model.CalendarEventVO;

import java.util.List;

/**
 * Created by oscarr on 8/4/15.
 */
public class CalendarNotificationEvent extends BaseEvent{
    private String notification;
    private boolean isError = false;
    private List<CalendarEventVO> events;
    private Object params;
    private CalendarEventVO newOrModifiedEvent;

    private CalendarNotificationEvent(){ super(); }
    private CalendarNotificationEvent(int mbRequestId){ super( mbRequestId); }

    public static CalendarNotificationEvent build(){
        return new CalendarNotificationEvent();
    }
    public static CalendarNotificationEvent build(int mbRequestId){
        return new CalendarNotificationEvent(mbRequestId);
    }

    public String getNotification() {
        return notification;
    }

    public CalendarNotificationEvent setNotification(String notification) {
        this.notification = notification;
        return this;
    }

    public boolean isError() {
        return isError;
    }

    public CalendarNotificationEvent setIsError(boolean isError) {
        this.isError = isError;
        return this;
    }

    public List<CalendarEventVO> getEvents() {
        return events;
    }

    public CalendarNotificationEvent setEvents(List<CalendarEventVO> events) {
        this.events = events;
        return this;
    }

    public Object getParams() {
        return params;
    }

    public CalendarNotificationEvent setParams(Object o) {
        this.params = o;
        return this;
    }

    public CalendarEventVO getNewOrModifiedEvent() {
        return newOrModifiedEvent;
    }

    public CalendarNotificationEvent setNewOrModifiedEvent(CalendarEventVO newOrModifiedEvent) {
        this.newOrModifiedEvent = newOrModifiedEvent;
        return this;
    }
}
