package com.example.familymapclient;

import android.media.metrics.Event;
import android.provider.Settings;

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
    Set<String> paternalAncestors = new HashSet<>();
    Set<String> maternalAncestors = new HashSet<>();

    Settings settings;

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

    public void store(RegisterResult result) {
        //TODO:finish this stub
    }

    public void setUser(PersonResult personResult) {
        this.user = new Person(personResult.getPersonID(), personResult.getAssociatedUsername()
        , personResult.getFirstName(), personResult.getLastName(), personResult.getGender()
        , personResult.getFatherID(), personResult.getMotherID(), personResult.getSpouseID());
    }

    public float getColor(String eventType) {
        // TODO: make function that gives each type of event its own color
        return 0.0f;
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


}
