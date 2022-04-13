package com.example.familymapclient;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import Request.LoginRequest;
import Result.LoginResult;

public class DataCacheTest {
    private static DataCache dataCache;

    //Note: these tests rely on the test data found in the Family Map Client Passoff data file.
    //first, load the data using the website and then the tests will work.
    //https://byu.instructure.com/courses/13127/assignments/532090

    @BeforeAll
    public static void setup() {
        ServerProxy serverProxy = new ServerProxy("localhost", "");
        LoginRequest loginRequest = new LoginRequest("sheila", "parker");
        LoginResult loginResult = serverProxy.login(loginRequest);
        DataCache.clear();
        dataCache = DataCache.getInstance();
        dataCache.store(loginResult);
        String authtoken = loginResult.getAuthtoken();
        dataCache.store(serverProxy.getPersons(authtoken));
        dataCache.store(serverProxy.getEvents(authtoken));
        dataCache.sort();
    }

    @Test
    //make sure that the setup actually worked
    public void setupPass() {
        assertNotNull(dataCache);
        assertEquals(11, dataCache.getPersonsList().size());
        assertEquals(19, dataCache.getEventsList().size());
    }

    @Test
    public void getFamilyPass() {

    }
    @Test
    public void getFamilyFail() {

    }
    @Test
    public void getDesiredEventsPass() {

    }
    @Test
    public void getDesiredEventsFail() {

    }
    @Test
    public void getPersonsOrderedEventsPass() {

    }
    @Test
    public void getPersonsOrderedEventsFail() {

    }
    @Test
    public void searchPass() {

    }
    @Test
    public void searchFail() {

    }
}
