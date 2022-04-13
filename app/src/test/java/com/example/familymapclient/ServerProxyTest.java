package com.example.familymapclient;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Request.LoginRequest;
import Request.RegisterRequest;
import Result.EventDataResult;
import Result.LoginResult;
import Result.PersonResult;
import Result.RegisterResult;

public class ServerProxyTest {
    ServerProxy serverProxy;
    LoginRequest loginRequest;
    LoginRequest loginRequest2;
    LoginRequest loginRequest3;
    RegisterRequest registerRequest;
    RegisterRequest registerRequest2;
    RegisterRequest registerRequest3;

    LoginResult loginResultPass;
    LoginResult loginResultFail;

    RegisterResult registerResultPass;
    RegisterResult registerResultFail;

    String authtoken;


/*
note: for these tests to work, you must clear the database after every run
 */

    @BeforeEach
    public void setup() {
        serverProxy = new ServerProxy("localhost", "8080");
        registerRequest = new RegisterRequest("carson", "call", "email.com"
            ,"cars", "ca", "m");
        loginRequest = new LoginRequest("carson", "call");

        loginRequest2 = new LoginRequest("kaitlyn", "dupuis");
        registerRequest2 = new RegisterRequest("kaitlyn", "dupuis","email"
            ,"kate", "dupe", "f");

        loginRequest3 = new LoginRequest("jaxson", "ruff");
        registerRequest3 = new RegisterRequest("jaxson", "ruff", "emale"
            ,"jax", "ru", "m");

        loginResultPass = new LoginResult("good", "carson", "holder");
        registerResultPass = new RegisterResult();

        loginResultFail = new LoginResult(null, null, null);
        loginResultFail.setSuccess(false);

        registerResultFail = new RegisterResult();
        registerResultFail.setSuccess(false);
    }

    @Test
    //Test if making a valid registration request returns a successful result.
    //note that we are grabbing a personID to compare other stuff to later
    public void registerTestPass() {
        RegisterResult result = serverProxy.register(registerRequest);
        assertTrue(result.isSuccess());
    }

    @Test
    //Test if registering someone twice fails
    public void registerTestFail() {
        serverProxy.register(registerRequest2);
        RegisterResult result2 = serverProxy.register(registerRequest2);
        assertFalse(result2.isSuccess());
    }

    @Test
    //Test if logging in someone who successfully registered before works. redundant register
    //in case the above tests have not run completely
    public void loginTestPass() {
        serverProxy.register(registerRequest2);
        LoginResult result = serverProxy.login(loginRequest2);
        authtoken = result.getAuthtoken();
        assertTrue(result.isSuccess());
    }

    @Test
    //Test if logging in someone who has not registered yet works
    public void loginTestFail() {
        LoginResult result = serverProxy.login(loginRequest3);
        assertFalse(result.isSuccess());
    }

    @Test
    //Logs the user in and then tests if events can be retrieved
    public void retrieveEventsTestPass() {
        LoginResult result = serverProxy.login(loginRequest);
        EventDataResult eventResult = new EventDataResult();
        assertNull(eventResult.getData());
        eventResult = serverProxy.getEvents(result.getAuthtoken());
        assertNotNull(eventResult.getData());
        assertEquals(92, eventResult.getData().length);
    }

    @Test
    //Tests that the server proxy returns null if we try to grab data with a fake auth token
    public void retrieveEventsTestFail() {
        EventDataResult result = new EventDataResult();
        assertNull(result.getData());
        result = serverProxy.getEvents("fake auth token");
        assertNull(result.getData());
        assertFalse(result.isSuccess());
    }

    @Test
    //tests that the server proxy can successfully return the expected number of events for a
    //person with four random generations of people
    public void retrievePersonsTestPass() {
        LoginResult result = serverProxy.login(loginRequest);
        PersonResult personResult = serverProxy.getPersons(result.getAuthtoken());
        assertNotNull(personResult);
        assertEquals(31, personResult.getData().length);
    }

    @Test
    //tests that the server proxy returns null if we try to grab data with a fake auth token
    public void retrievePersonsTestFail() {
        PersonResult personResult = serverProxy.getPersons("fake auth token");
        assertNull(personResult.getData());
        assertFalse(personResult.isSuccess());
    }

}
