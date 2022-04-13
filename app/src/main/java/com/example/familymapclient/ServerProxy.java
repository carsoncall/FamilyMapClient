package com.example.familymapclient;

import Request.*;
import Result.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


public class ServerProxy {
    //background thread will call this class and use it to talk to the server
    String serverHost;
    String serverPort;

    public ServerProxy(String serverHost, String serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public LoginResult login(LoginRequest loginRequest) {
        //This method will send a POST request to the server.
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/login");

            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("username", loginRequest.getUsername());
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            //Now we make the JSON that we will send to the server.
            Gson gson = new Gson();
            String reqData = gson.toJson(loginRequest);

            OutputStream reqBody = http.getOutputStream();
            writeString(reqData, reqBody);
            reqBody.close();

            LoginResult result;
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();

                //Extract input data
                String respData = readString(respBody);
                result = gson.fromJson(respData,LoginResult.class);
                System.out.print("Successfully logged in");
            } else {
                //If we are here, then something went wrong.
                System.out.print("ERROR: " + http.getResponseMessage());
                InputStream respBody = http.getErrorStream();
                String respData = readString(respBody);
                System.out.print(respData);
                result = new LoginResult(null,null,null);
                result.setSuccess(false);
                result.setMessage("Error logging in.");
            }
            return result;
        } catch (MalformedURLException e) {
            System.out.print("There was an error making the URL for the login");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.print("There was an error making the HTTP connection in the Login function");
            e.printStackTrace();
        }

        LoginResult result = new LoginResult(null,null,null);
        result.setSuccess(false);
        result.setMessage("Error logging in.");
        return result;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        //uses a POST request as well
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/register");

            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            Gson gson = new Gson();
            String reqData = gson.toJson(registerRequest);
            OutputStream reqBody = http.getOutputStream();
            writeString(reqData,reqBody);
            reqBody.close();

            RegisterResult result;
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();

                // Extract JSON data from the HTTP response body
                String respData = readString(respBody);
                result = gson.fromJson(respData, RegisterResult.class);

                // The HTTP response status code indicates success,
                // so print a success message
                System.out.println("Register Successful");
                return result;
            }
            else {

                System.out.println("ERROR: " + http.getResponseMessage());
                InputStream respBody = http.getErrorStream();
                String respData = readString(respBody);

                System.out.println(respData);
                result = new RegisterResult();
                result.setMessage("ERROR: Could not register the user");
                result.setSuccess(false);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
            RegisterResult result = new RegisterResult();
            result = new RegisterResult();
            result.setMessage("ERROR: Could not register the user");
            result.setSuccess(false);
            return result;
        }
    }



    public EventDataResult getEvents(String authToken) {
        //gets events from the server and saves them in an EventResult
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/event");

            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("GET");
            http.setDoOutput(false);
            http.addRequestProperty("Authorization", authToken);
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            EventDataResult result;
            Gson gson = new Gson();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();

                //Extract input data
                String respData = readString(respBody);
                result = gson.fromJson(respData,EventDataResult.class);
                System.out.print("Successfully retrieved Events");
            } else {
                //If we are here, then something went wrong.
                System.out.print("ERROR: " + http.getResponseMessage());
                InputStream respBody = http.getErrorStream();
                String respData = readString(respBody);
                System.out.print(respData);
                result = new EventDataResult();
                result.setSuccess(false);
                result.setMessage("Error retrieving events.");
            }
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            EventDataResult result;
            result = new EventDataResult();
            result.setSuccess(false);
            result.setMessage("Error logging in.");
            return result;
        }
    }
    public PersonResult getPerson(String authToken, String personID) {
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/person/" + personID);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("GET");
            http.setDoOutput(false);
            http.addRequestProperty("Authorization", authToken);
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            PersonResult result;
            Gson gson = new Gson();

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);
                result = gson.fromJson(respData, PersonResult.class);
                System.out.print("Successfully retrieved a person");
                return result;
            } else {
                System.out.print("ERROR: Could not retrieve a person");
                InputStream respBody = http.getErrorStream();
                String respData = readString(respBody);
                System.out.print(respData);
                result = new PersonResult();
                result.setMessage("Error retrieving a person");
                result.setSuccess(false);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("There was an error retrieving a single person.\n");
            PersonResult result = new PersonResult();
            result.setMessage("Error retrieving a person");
            result.setSuccess(false);
            return result;
        }
    }

    public PersonResult getPersons(String authTokenVal){

        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/person");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("GET");
            http.setDoOutput(false);	// There is a request body
            http.addRequestProperty("Authorization", authTokenVal);
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            PersonResult result;
            Gson gson = new Gson();

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();

                String respData = readString(respBody);
                result = gson.fromJson(respData, PersonResult.class);

                System.out.println("Successfully retrieved events");
                return result;
            }
            else {

                System.out.println("ERROR: " + http.getResponseMessage());

                InputStream respBody = http.getErrorStream();

                String respData = readString(respBody);

                System.out.println(respData);
                result = new PersonResult();
                result.setMessage("Error retrieving persons");
                result.setSuccess(false);
                return result;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            PersonResult result = new PersonResult();
            result.setMessage("Error retrieving persons");
            result.setSuccess(false);
            return result;
        }
    }

    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    private static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }
}
