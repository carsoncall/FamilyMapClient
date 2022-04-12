package com.example.familymapclient;

import android.graphics.ColorSpace;
import android.provider.Settings;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;
import Result.EventDataResult;
import Result.LoginResult;
import Result.PersonResult;


public class DataCache {
    //holds all of the data that the app needs, such as authtoken and all of their information
    // including family tree information

    private DataCache(){};
    private static DataCache instance = new DataCache();
    public static DataCache getInstance() {
        return  instance;
    }

    String authToken;
    Model.Person user;
    Map<String, Person> persons = new HashMap<String, Model.Person>();
    Map<String, Model.Event> events = new HashMap<String, Model.Event>();
    Map<String, List<Event>> personEvents = new HashMap<>();

    //personID sets
    Set<String> paternalAncestors = new HashSet<>();
    Set<String> maternalAncestors = new HashSet<>();
    Set<String> malePersonIDs = new HashSet<>();
    Set<String> femalePersonIDs = new HashSet<>();
    Set<String> filteredPersonIDs = new HashSet<>();
    //eventID sets
    Set<String> maleEventIDs = new HashSet<>();
    Set<String> femaleEventIDs = new HashSet<>();
    Set<String> paternalEventIDs = new HashSet<>();
    Set<String> maternalEventIDs = new HashSet<>();
    Set<Model.Event> setOfFilteredEvents;

    Settings settings;
    Map <String, Float> otherTypesColors = new HashMap<>();
    Float counter = 0.0f;

    public void store(LoginResult result) {
        this.authToken = result.getAuthtoken();
    }

    public void store(PersonResult result) {
        Model.Person[] resultData = result.getData();
        for (Model.Person person : resultData) {
            persons.put(person.getPersonID(),person);
        }
    }

    public void store(EventDataResult result) {
        Model.Event[] resultData = result.getData();
        for (Model.Event event : resultData) {
            events.put(event.getEventID(),event);
        }
    }

    public void setUser(PersonResult personResult) {
        this.user = new Person(personResult.getPersonID(), personResult.getAssociatedUsername()
        , personResult.getFirstName(), personResult.getLastName(), personResult.getGender()
        , personResult.getFatherID(), personResult.getMotherID(), personResult.getSpouseID());
    }

    /**
     * this function will be called after all data has been stored, and will perform various
     * functions to populate the sets that we need for our other functions. Those other functions
     * will include taking the intersections of these sets to display only the ones that you
     * want.
     */
    public void sort() {
        //makes the sets of PersonIDs sorted by gender
        for (String key : persons.keySet()) {
            Person person = persons.get(key);
            if (person.getGender().equals("m")) {
                malePersonIDs.add(person.getPersonID());
            } else {
                femalePersonIDs.add(person.getPersonID());
            }
        }

        //uses those sets to make sets of EventIDs that are separated by gender
        for (String key : events.keySet()) {
            Model.Event event = events.get(key);
            if (malePersonIDs.contains(event.getPersonID())) {
                maleEventIDs.add(event.getEventID());
            } else {
                femaleEventIDs.add(event.getEventID());
            }
        }

        //calls recursive function on both sides of family to generate sets of personIDs
        recurseParents(user.getFatherID(), paternalAncestors);
        recurseParents(user.getMotherID(), maternalAncestors);

        // makes sets of EventIDs that are separated by family sides
        for (String key : events.keySet()) {
            Model.Event event = events.get(key);
            assert event != null;
            if (paternalAncestors.contains(event.getPersonID())) {
                paternalEventIDs.add(event.getEventID());
            } else {
                maternalEventIDs.add(event.getEventID());
            }
        }

        //populates map of PersonIDs to Lists of Events, already sorted in chronological order and
        // tiebroken

        for (String key : persons.keySet()) {
            List<Model.Event> thisPersonEvents = new ArrayList<>();
            for (Model.Event event: events.values()) {
                if (event.getPersonID().equals(key)) {
                    thisPersonEvents.add(event);
                }
            }

            thisPersonEvents.sort(new EventComparator());
            personEvents.put(key, thisPersonEvents);
        }
    }

    private void recurseParents(String personID, Set<String> familySideSet) {
        Person person = persons.get(personID);
        if (person.getFatherID() == null) {
            familySideSet.add(personID);
        } else {
            familySideSet.add(personID);
            recurseParents(person.getFatherID(), familySideSet);
            recurseParents(person.getMotherID(), familySideSet);
        }
    }

    public Set<Model.Event> setOfDesiredEvents(Boolean showMale, Boolean showFemale
                                        , Boolean showPaternal, Boolean showMaternal) {
        Set<Model.Event> result = new HashSet<>();
        Set<String> resultEventIDs = new HashSet<>();

        if (showPaternal) {
            resultEventIDs.addAll(paternalEventIDs);
            filteredPersonIDs.addAll(paternalAncestors);
        }
        if (showMaternal) {
            resultEventIDs.addAll(maternalEventIDs);
            filteredPersonIDs.addAll(maternalAncestors);
        }

        if (!showMale) {
            resultEventIDs.removeAll(maleEventIDs);
            filteredPersonIDs.removeAll(malePersonIDs);
        }
        if (!showFemale) {
            resultEventIDs.removeAll(femaleEventIDs);
            filteredPersonIDs.removeAll(femalePersonIDs);
        }

        for (String eventID : resultEventIDs) { result.add(events.get(eventID)); }
        setOfFilteredEvents = result;
        return result;
    }

    public Set<String> setOfDesiredMales(Boolean showPaternal, Boolean showMaternal) {

        if (showPaternal && showMaternal) {
            return malePersonIDs;
        } else if (!showMaternal) {
            Set<String> malePaternalPersonIDs = malePersonIDs;
            malePaternalPersonIDs.removeAll(maternalAncestors);
            return malePaternalPersonIDs;
        } else  {
            Set<String> maleMaternalPersonIDs = malePersonIDs;
            maleMaternalPersonIDs.removeAll(paternalAncestors);
            return maleMaternalPersonIDs;
        }
    }

    public float getColor(String eventType) {
        float birth = 0;
        float marriage = 90;
        float death = 270;

        if (eventType.toUpperCase(Locale.ROOT).equals("BIRTH")) { return birth; }
        if (eventType.toUpperCase(Locale.ROOT).equals("MARRIAGE")) { return marriage; }
        if (eventType.toUpperCase(Locale.ROOT).equals("DEATH")) { return death; }

        if (otherTypesColors.containsKey(eventType) && otherTypesColors.get(eventType) != null) {
            return otherTypesColors.get(eventType);
        } else {
            //counter allows for every arbitrary type to have a color that is reasonably unique
            // and also never too close to the three major types.
            counter = (counter + 69)%360;
            otherTypesColors.put(eventType.toUpperCase(Locale.ROOT), counter);
            return counter;
        }
    }

    public Model.Person getPersonByID(String personID) {
        return persons.get(personID);
    }

    public Model.Event getEventByID(String eventID) {
        return events.get(eventID);
    }

    public List<Model.Event> getPersonEventsByID(String personID) { return personEvents.get(personID);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }
    public Map<String, Model.Event> getEvents() {
        return events;
    }

    public Pair<List<Person>, List<Model.Event>> searchData(String s) {
        List<Person> personResults = new ArrayList<>();
        List<Model.Event> eventResults = new ArrayList<>();

        for (Person person : persons.values()) {
            String firstName = person.getFirstName();
            String lastName = person.getLastName();

            if (firstName.contains(s) || lastName.contains(s)) {
                personResults.add(person);
            }
        }

        for (Model.Event event : setOfFilteredEvents) {
            String country = event.getCountry();
            String city = event.getCity();
            String type = event.getEventType();
            String year = String.valueOf(event.getYear());

            if (country.contains(s) || city.contains(s) || type.contains(s) || year.contains(s)) {
                eventResults.add(event);
            }
        }

        return new Pair<>(personResults, eventResults);
    }

    public Model.Event getEarliestEvent(String personID){
        return personEvents.get(personID).get(0);
    }

    public ArrayList<Model.Event> getEventsList() {
        return new ArrayList<>(events.values());
    }
    public ArrayList<Person> getPersonsList() {
        return new ArrayList<>(persons.values());
    }

    public static class EventComparator implements Comparator<Model.Event> {

        @Override
        public int compare(Model.Event event1, Model.Event event2) {
            if (event1.getYear() < event2.getYear()) {
                return -1;
            } else if (event1.getYear() > event2.getYear()) {
                return 1;
            } else {
                String event1Type = event1.getEventType().toLowerCase(Locale.ROOT);
                String event2Type = event2.getEventType().toLowerCase(Locale.ROOT);
                return Integer.compare(event1Type.compareTo(event2Type), 0);
            }
        }
    }


}
