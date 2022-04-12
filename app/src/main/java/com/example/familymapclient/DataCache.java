package com.example.familymapclient;

import android.media.metrics.Event;
import android.provider.Settings;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Model.Person;
import Result.EventDataResult;
import Result.LoginResult;
import Result.PersonResult;
import Result.RegisterResult;


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

        if (showPaternal) { resultEventIDs.addAll(paternalEventIDs); }
        if (showMaternal) { resultEventIDs.addAll(maternalEventIDs); }

        if (!showMale) { resultEventIDs.removeAll(maleEventIDs); }
        if (!showFemale) { resultEventIDs.removeAll(femaleEventIDs); }

        for (String eventID : resultEventIDs) { result.add(events.get(eventID)); }
        setOfFilteredEvents = result;
        return result;
    }

    public float getColor(String eventType) {
        // TODO: make function that gives each type of event its own color
        float birth = 0;
        float marriage = 90;
        float death = 270;

        if (eventType.equals("birth")) { return birth; }
        if (eventType.equals("marriage")) { return marriage; }
        if (eventType.equals("death")) { return death; }

        if (otherTypesColors.containsKey(eventType) && otherTypesColors.get(eventType) != null) {
            return otherTypesColors.get(eventType);
        } else {
            //counter allows for every arbitrary type to have a color that is reasonably unique
            // and also never too close to the three major types.
            counter = (counter + 62)%360;
            otherTypesColors.put(eventType, counter);
            return counter;
        }
    }

    public Model.Person getPersonByID(String personID) {
        return persons.get(personID);
    }

    public Model.Event getEventByID(String eventID) {
        return events.get(eventID);
    }

    public List<Event> getPersonEventsByID(String personID) {
        return personEvents.get(personID);
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


    public ArrayList<Model.Event> getEventsList() {
        return new ArrayList<>(events.values());
    }
    public ArrayList<Person> getPersonsList() {
        return new ArrayList<>(persons.values());
    }


}
