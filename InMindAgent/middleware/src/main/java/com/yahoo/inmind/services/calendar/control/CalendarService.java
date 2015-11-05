package com.yahoo.inmind.services.calendar.control;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.yahoo.inmind.comm.calendar.model.CalendarNotificationEvent;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.commons.control.UtilServiceAPIs;
import com.yahoo.inmind.services.calendar.model.CalendarEventVO;
import com.yahoo.inmind.services.generic.control.GenericService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class CalendarService extends GenericService {
    /**
     * A Google Calendar API service object used to access the API.
     * Note: Do not confuse this class with API library's model classes, which
     * represent specific data structures.
     */
    private  Calendar calendar;
    private TreeMap<String, Event> events;
    private HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static final String CALENDAR_ID = "primary";

    public CalendarService(){
        super(null);
        if( actions.isEmpty() ) {
            this.actions.add(Constants.ACTION_CALENDAR);
        }
    }

    @Override
    public void doAfterBind(){
        super.doAfterBind();
        processEvents( Constants.CALENDAR_GET_EVENTS, null );
    }

    public synchronized void initializeCalendar(){
        if( UtilServiceAPIs.credential == null ) {
            UtilServiceAPIs.initializeCredentials(mContext, null);
        }
        calendar = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, UtilServiceAPIs.credential)
                .setApplicationName("InMInd Calendar API")
                .build();
    }

    /********  CRUD OPERATIONS   ******************************************************************/

    /**
     * Creates a new calendar event
     * @param event
     * @throws IOException
     */
    private synchronized Event createAndInsertEvent( CalendarEventVO event ) throws IOException {
        if( event != null ){
            Event eventData = convertToEventData( event );
            if( checkDates( eventData ) ) {
                return calendar.events().insert(CALENDAR_ID, eventData).execute();
            }
        }else {
            mb.send(CalendarService.this,
                    CalendarNotificationEvent.build()
                    .setNotification("Calendar Event is null")
                    .setIsError(true));
        }
        return null;
    }

    /**
     * Queries all calendar events from fromDate to a certain amount of months ahead
     * @param fromDate
     * @param numberOfMonths
     * @return
     * @throws IOException
     */
    private synchronized TreeMap<String, Event> getDataFromApi(Date fromDate, int numberOfMonths)
            throws IOException {
        DateTime from = new DateTime( fromDate == null? new Date(System.currentTimeMillis()) : fromDate );
        return getDataFromApi(from, numberOfMonths);
    }

    public List<CalendarEventVO> getEvents() {
        if (events != null){
            return convertEvents(new ArrayList<>(events.values()));
        }
        return null;
    }

    /**
     * Queries all calendar events from fromDate to a certain amount of months ahead
     * @param fromDate
     * @param numberOfMonths
     * @return
     * @throws IOException
     */
    private synchronized TreeMap<String, Event> getDataFromApi(DateTime fromDate, int numberOfMonths)
            throws IOException {
        Events searchEvents = calendar.events().list( CALENDAR_ID )
                .setTimeMin( fromDate )
                .setTimeMax(new DateTime(
                        Util.getRelativeDate(java.util.Calendar.MONTH, numberOfMonths)))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        createHashMapEvents(searchEvents.getItems());
        return events;
    }

    private void createHashMapEvents( List<Event> listEvents){
        if( events == null ){
            events = new TreeMap<>();
        }
        for(Event event : listEvents){
            events.put(getEventKey(event), event);
        }
    }

    private String getEventKey(Event event) {
        return event.getCreated().toStringRfc3339() + event.getId();
    }

    private String getEventKey(CalendarEventVO event) {
        return event.getCreatedTime() + event.getId();
    }


    /**
     * Deletes all calendar events from Jan 1 1900 to 1 year ahead of current date (-1)
     * @throws Exception
     */
    private void deleteAllEvents() throws Exception{
        getDataFromApi( new DateTime("1900-01-01T01:00:00.000-04:00"), -1 );
        for(Event event : events.values()){
            deleteEvent( event.getId() );
        }
    }

    /**
     * Updates the specified calendar event
     * @param eventData
     * @throws Exception
     */
    private Event updateEvent(CalendarEventVO eventData) throws Exception{
        return calendar.events().update(CALENDAR_ID, eventData.getId(),
                convertToEventData( eventData ) ).execute();
    }

    /**
     * Deletes a specified calendar event
     * @param eventId
     * @throws Exception
     */
    private void deleteEvent(String eventId) throws Exception{
        calendar.events().delete(CALENDAR_ID, eventId).execute();
    }

    /********  END CRUD OPERATIONS   **************************************************************/



    private synchronized List<CalendarEventVO> convertEvents( List<Event> events ){
        List<CalendarEventVO> eventsVO = new ArrayList<>();
        for( Event event : events ){
            eventsVO.add( convertToEventVO(event));
        }
        return eventsVO;
    }


    private synchronized Event convertToEventData( CalendarEventVO eventData ){
        Event event = new Event()
                .setSummary( eventData.getSummary() )
                .setLocation( eventData.getLocation() )
                .setDescription( eventData.getDescription() )
                .setId( eventData.getId() );

        DateTime startDateTime = new DateTime( eventData.getStartDateTime() ); //"2015-08-30T10:00:00-04:00");
        EventDateTime start = new EventDateTime().setDateTime(startDateTime);
        event.setStart(start);

        DateTime endDateTime = new DateTime( eventData.getEndDateTime() ); //"2015-08-30T11:00:00-04:00");
        EventDateTime end = new EventDateTime().setDateTime(endDateTime);
        event.setEnd(end);


//        event.setRecurrence(Arrays.asList( eventData.getRecurrence() ));
//        ArrayList<EventAttendee> attendees = new ArrayList<>();
//        for( String attendee : eventData.getAttendees() ){
//                attendees.add( new EventAttendee().setEmail( attendee ) );
//        }
//        event.setAttendees(attendees);


        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("email").setMinutes( eventData.getEmailReminder() ),
                new EventReminder().setMethod("sms").setMinutes( eventData.getSmsReminder() ),
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        return event;
    }


    private synchronized CalendarEventVO convertToEventVO( Event eventData ){
        CalendarEventVO event = new CalendarEventVO()
                .setSummary( eventData.getSummary() )
                .setLocation(eventData.getLocation())
                .setDescription(eventData.getDescription())
                .setId(eventData.getId());
        event.setStartDate( new Date( getDateTimeFromEvent( eventData.getStart() ).getValue() ) );
        event.setEndDate( new Date( getDateTimeFromEvent( eventData.getEnd() ).getValue() ) );

        event.setRecurrence( eventData.getRecurrence() );
        if( eventData.getAttendees() != null ) {
            ArrayList<String> attendees = new ArrayList<>();
            for (EventAttendee attendee : eventData.getAttendees()) {
                attendees.add(attendee.getEmail());
            }
            event.setAttendees(attendees);
        }

        if(eventData.getReminders() != null && eventData.getReminders().getOverrides() != null) {
            for(EventReminder reminder : eventData.getReminders().getOverrides() ){
                if(reminder.getMethod().equals("email")){
                    event.setEmailReminder( reminder.getMinutes());
                }else if(reminder.getMethod().equals("sms")){
                    event.setSmsReminder( reminder.getMinutes() );
                }
            }
        }

        event.setCreatedTime(eventData.getCreated().toStringRfc3339());
        return event;
    }


    private synchronized boolean checkDates( Event eventData ) throws IOException {
        Date newEventStartDate = new Date( getDateTimeFromEvent(eventData.getStart()).getValue() );
        Date newEventEndDate = new Date( getDateTimeFromEvent( eventData.getEnd() ).getValue() );

        for(Event event : events.values()){
            Date eventStartDate = new Date( getDateTimeFromEvent(event.getStart()).getValue() );
            Date eventEndDate = new Date( getDateTimeFromEvent(event.getEnd()).getValue() );
            if( ( newEventStartDate.compareTo(eventEndDate) >= 0
                    && newEventEndDate.compareTo( eventEndDate ) > 0
                    && newEventStartDate.compareTo( newEventEndDate) < 0 )
                    || ( newEventEndDate.compareTo(eventStartDate) <= 0
                    && newEventStartDate.compareTo( eventEndDate ) < 0
                    && newEventStartDate.compareTo( newEventEndDate) < 0 ) ) {
                continue;
            }else{
                String message = event.getDescription() + "\nSummary: " + event.getSummary()
                        + "\nFrom: " + event.getStart().getDateTime().toString() + " to "
                        + event.getEnd().getDateTime().toString();
                mb.send(CalendarService.this,
                        CalendarNotificationEvent.build()
                        .setNotification("Conflict with the event:\n" + message)
                        .setIsError(true));

                return false;
            }
        }
        return true;
    }

    private synchronized DateTime getDateTimeFromEvent(EventDateTime dateTime){
        if( dateTime.getDateTime() != null ){
            return dateTime.getDateTime();
        }
        if( dateTime.getDate() != null ) {
            return dateTime.getDate();
        }
        return new DateTime( new Date( System.currentTimeMillis() ) );
    }


    /**
     * task to call Google Calendar API.
     */
    public synchronized Thread processEvents(final int mode, final Object eventObject) {
        try {
            Thread t = new Thread() {
                public void run() {
                    if( calendar == null ){
                        initializeCalendar();
                    }
                    boolean queryEvents = true;
                    CalendarEventVO eventData = eventObject instanceof CalendarEventVO ?
                            (CalendarEventVO) eventObject : null;
                    List<CalendarEventVO> eventVOs = eventObject instanceof List ?
                            (List) eventObject : null;
                    Date fromDate = eventData != null ? eventData.getFromDate() : null;
                    int numberOfMonths = eventData != null ? eventData.getNumberOfMonths() : -1;
                    Event resultEvent;
                    try {
                        // we need to make sure if that there are events to
                        // manipulate (insert, update, delete...)
                        if (events == null) {
                            events = getDataFromApi(fromDate, numberOfMonths);
                            queryEvents = false;
                        }
                        switch (mode) {
                            case Constants.CALENDAR_GET_EVENTS:
                                if (queryEvents) {
                                    events = getDataFromApi(fromDate, numberOfMonths);
                                }
                                mb.send(CalendarService.this,
                                        CalendarNotificationEvent.build()
                                        .setNotification(Constants.CALENDAR_AFTER_QUERY_EVENTS)
                                        .setEvents(convertEvents(new ArrayList<>(events.values()))));
                                break;
                            case Constants.CALENDAR_INSERT_EVENT:
                                resultEvent = createAndInsertEvent(eventData);
                                if (resultEvent != null) {
                                    events.put(getEventKey(resultEvent), resultEvent);
                                    mb.send(CalendarService.this,
                                            CalendarNotificationEvent.build()
                                            .setNotification(Constants.CALENDAR_AFTER_CREATE_EVENT)
                                            .setEvents(convertEvents(new ArrayList<>(events.values())))
                                            .setNewOrModifiedEvent( convertToEventVO( resultEvent )));
                                }
                                break;
                            case Constants.CALENDAR_DELETE_EVENTS:
                                if (eventVOs != null) {
                                    for (CalendarEventVO eventVO : eventVOs) {
                                        deleteEvent(eventVO.getId());
                                        events.remove(getEventKey(eventVO));
                                    }
                                }
                                mb.send(CalendarService.this,
                                        CalendarNotificationEvent.build()
                                        .setNotification(Constants.CALENDAR_AFTER_DELETE_EVENTS)
                                        .setEvents(convertEvents(new ArrayList<>(events.values()))));
                                break;

                            case Constants.CALENDAR_UPDATE_EVENT:
                                resultEvent = updateEvent(eventData);
                                if (resultEvent != null) {
                                    events.remove(getEventKey(resultEvent));
                                    events.put(getEventKey(resultEvent), resultEvent);
                                    mb.send(CalendarService.this,
                                            CalendarNotificationEvent.build()
                                            .setNotification(Constants.CALENDAR_AFTER_UPDATE_EVENT)
                                            .setEvents(convertEvents(new ArrayList<>(events.values())))
                                            .setNewOrModifiedEvent(convertToEventVO(resultEvent )));
                                } else {
                                    mb.send(CalendarService.this,
                                            CalendarNotificationEvent.build()
                                            .setNotification("Calendar Event is null")
                                            .setIsError(true));
                                }
                                break;

                            case Constants.CALENDAR_DELETE_ALL_EVENTS:
                                deleteAllEvents();
                                events.clear();
                                mb.send(CalendarService.this,
                                        CalendarNotificationEvent.build()
                                        .setNotification(Constants.CALENDAR_AFTER_DELETE_ALL_EVENTS));
                                break;
                        }
                    } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
                        mb.send(CalendarService.this,
                                CalendarNotificationEvent.build()
                                .setNotification(Constants.CALENDAR_AVAILABILITY_EXCEPTION)
                                .setParams(availabilityException.getConnectionStatusCode()));
                    } catch (UserRecoverableAuthIOException userRecoverableException) {
                        mb.send(CalendarService.this,
                                CalendarNotificationEvent.build()
                                .setNotification(Constants.CALENDAR_USER_RECOVERABLE_EXCEPTION)
                                .setParams(userRecoverableException.getIntent()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        mb.send(CalendarService.this,
                                CalendarNotificationEvent.build()
                                .setNotification("The following error occurred: " + e.getMessage()));
                    }
                }
            };
            t.start();
            return t;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public synchronized void getCalendarEvents() {

        if (UtilServiceAPIs.credential != null) {
            if (UtilServiceAPIs.credential.getSelectedAccountName() == null
                    && !UtilServiceAPIs.chooseAccountInProcess) {
                UtilServiceAPIs.chooseAccount( null );
                getCalendarEvents();
            } else {
                if (UtilServiceAPIs.isAccountSelected) {
                    if (UtilServiceAPIs.isDeviceOnline( null )) {
                        processEvents((Constants.CALENDAR_GET_EVENTS), null);
                    } else {
                        mb.send(CalendarService.this,
                                CalendarNotificationEvent.build()
                                .setNotification("No network connection available."));
                    }
                } else {
                    Util.sleep(1000);
                    getCalendarEvents();
                }
            }
        }
    }

    /**
     * EVENTS HANDLER
     * @param event
     */
    public void onEvent( CalendarNotificationEvent event ){
        if( event.getNotification().equals(Constants.CALENDAR_CHECK_REFRESH_CALENDAR) ){
            getCalendarEvents();
        }
    }

    @Override
    public void onDestroy(){
        calendar = null;
        events = null;
        transport = null;
        jsonFactory = null;
        super.onDestroy();
    }

    public void pruebita(final int a, final int b){

    }
}
