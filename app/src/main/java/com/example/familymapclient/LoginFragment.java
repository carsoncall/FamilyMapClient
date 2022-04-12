package com.example.familymapclient;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Request.LoginRequest;
import Request.RegisterRequest;
import Result.EventDataResult;
import Result.LoginResult;
import Result.PersonResult;
import Result.RegisterResult;

public class LoginFragment extends Fragment {

    private static final String LOG_TAG = "LoginFragment";
    private static final String LOGIN_SUCCESS_KEY = "loginSuccess";
    private static final String AUTHTOKEN_KEY = "authToken";
    private static final String PERSON_ID_KEY = "personID";
    private static final String DATA_SUCCESS_KEY = "dataSuccess";
    private static final String REGISTER_SUCCESS_KEY = "registerSuccess";

    private EditText serverAddress;
    private EditText serverPort;
    private EditText username;
    private EditText password;
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private RadioGroup genderGroup;
    private Button loginButton;
    private Button registerButton;

    protected ExecutorService executorService;

    private Listener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "in onCreate(bundle");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(LOG_TAG, "in onCreateView(bundle");
        View view =  inflater.inflate(R.layout.fragment_login, container, false);

        //Grabs all of the text from the EditText fields
        serverAddress = view.findViewById(R.id.server_IP_address);
        serverPort = view.findViewById(R.id.server_port_number);
        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);
        firstName = view.findViewById(R.id.first_name);
        lastName = view.findViewById(R.id.last_name);
        email = view.findViewById(R.id.email);
        genderGroup = view.findViewById(R.id.radioGender1);
        loginButton = view.findViewById(R.id.login_button);
        registerButton = view.findViewById(R.id.register_button);

        serverAddress.addTextChangedListener(mTextWatcher);
        serverPort.addTextChangedListener(mTextWatcher);
        username.addTextChangedListener(mTextWatcher);
        password.addTextChangedListener(mTextWatcher);
        firstName.addTextChangedListener(mTextWatcher);
        lastName.addTextChangedListener(mTextWatcher);
        email.addTextChangedListener(mTextWatcher);
        checkFieldsForEmptyValues();

        loginButton.setOnClickListener((v1) -> {
            @SuppressLint("HandlerLeak") Handler loginHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    Boolean success = bundle.getBoolean(LOGIN_SUCCESS_KEY, false);
                    if (success) {
                        String authToken = bundle.getString(AUTHTOKEN_KEY);
                        String personID = bundle.getString(PERSON_ID_KEY);

                        //Now we will start the thread that will retrieve the user's data
                        startRetrievalTask(authToken, personID);
                        listener.notifyDone();
                        Toast.makeText(getContext(), "Login successful", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Login Failure", Toast.LENGTH_LONG).show();
                    }
                }
            };
            //Create and execute the login task, but on a separate thread
            LoginRequest request = new LoginRequest(username.getText().toString()
                    , password.getText().toString());
            LoginTask task = new LoginTask(loginHandler, request, serverAddress.getText().toString()
                    , serverPort.getText().toString());
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(task);
        });

        registerButton.setOnClickListener((v2) -> {
            @SuppressLint("HandlerLeak") Handler registerHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    Boolean success =  bundle.getBoolean(REGISTER_SUCCESS_KEY, false);
                    if(success) {
                        String authToken = bundle.getString(AUTHTOKEN_KEY);
                        String personID = bundle.getString(PERSON_ID_KEY);
                        startRetrievalTask(authToken,personID);

                        listener.notifyDone();
                    } else {
                        Toast.makeText(getContext(), "Register Failure", Toast.LENGTH_LONG).show();
                    }
                }
            };
        //Get gender
        RadioButton radioButton;
        int id = genderGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton)view.findViewById(id);
        String gender = radioButton.getText().toString();
        Character genderChar = gender.charAt(0);

        //create register request
        RegisterRequest registerRequest = new RegisterRequest(username.getText().toString()
                , password.getText().toString(), email.getText().toString()
                , firstName.getText().toString(), lastName.getText().toString()
                , genderChar.toString().toLowerCase(Locale.ROOT));
        RegisterTask registerTask = new RegisterTask(registerHandler,registerRequest
                , serverAddress.getText().toString(), serverPort.getText().toString());
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(registerTask);
        });

        return view;
    }

    private void startRetrievalTask(String authToken, String personID) {
        @SuppressLint("HandlerLeak") Handler retrieveDataHandler = new Handler() {
            @Override
            public void handleMessage(Message message){
                Bundle bundle = message.getData();
                Boolean success = bundle.getBoolean(DATA_SUCCESS_KEY, false);
                if (success) {
                    listener.notifyDone();
                } else {
                    Toast.makeText(getContext(),"Failed to download data", Toast.LENGTH_LONG).show();
                }
            }
        };
        RetrievalTask retrievalTask = new RetrievalTask(retrieveDataHandler
                , serverAddress.getText().toString(), serverPort.getText().toString()
                , authToken, personID);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(retrievalTask);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // check Fields For Empty Values
            checkFieldsForEmptyValues();
        }
    };

    void checkFieldsForEmptyValues(){
        String hostField = serverAddress.getText().toString();
        String portField = serverPort.getText().toString();
        String usernameField = username.getText().toString();
        String passwordField = password.getText().toString();
        String firstNameField = firstName.getText().toString();
        String lastNameField = lastName.getText().toString();
        String emailAddressField = email.getText().toString();


        if(hostField.equals("") || portField.equals("") || usernameField.equals("") || passwordField.equals("")){
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
        } else if (firstNameField.equals("") || lastNameField.equals("") || emailAddressField.equals("")){
            loginButton.setEnabled(true);
            registerButton.setEnabled(false);
        }
        else {
            loginButton.setEnabled(true);
            registerButton.setEnabled(true);
        }
    }

    //This class implements the task that logs the user in.
    private static class LoginTask implements Runnable {
        private final String address;
        private final String port;
        private final Handler handler;
        private final LoginRequest loginRequest;

        public LoginTask(Handler handler, LoginRequest loginRequest, String address, String port) {
            this.address = address;
            this.port = port;
            this.handler = handler;
            this.loginRequest = loginRequest;
        }

        @Override
        public void run() {
            ServerProxy proxy = new ServerProxy(address,port);
            LoginResult result = proxy.login(loginRequest);

            if (result.isSuccess()) {
                DataCache cache = DataCache.getInstance();
                cache.store(result);
            }

            //now we send a message back to the main thread
            Boolean success = result.isSuccess();
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();

            messageBundle.putBoolean(LOGIN_SUCCESS_KEY, success);
            messageBundle.putString(AUTHTOKEN_KEY, result.getAuthtoken());
            messageBundle.putString(PERSON_ID_KEY, result.getPersonID());

            message.setData(messageBundle);
            handler.sendMessage(message);
        }
    }

    //This class implements the task that registers the user.
    private static class RegisterTask implements Runnable {
        private final String address;
        private final String port;
        private final Handler handler;
        private final RegisterRequest registerRequest;

        public RegisterTask (Handler handler, RegisterRequest registerRequest, String address, String port) {
            this.address = address;
            this.port = port;
            this.handler = handler;
            this.registerRequest = registerRequest;
        }

        @Override
        public void run() {
            ServerProxy proxy = new ServerProxy(address, port);
            RegisterResult result = proxy.register(registerRequest);
            if (result.isSuccess()) {
                DataCache cache = DataCache.getInstance();
                //cache.store(result);
            }

            //now we send a message back to the main thread
            Boolean success = result.isSuccess();
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();

            messageBundle.putBoolean(REGISTER_SUCCESS_KEY,success);
            messageBundle.putString(AUTHTOKEN_KEY, result.getAuthtoken());
            messageBundle.putString(PERSON_ID_KEY, result.getPersonID());

            message.setData(messageBundle);
            handler.sendMessage(message);
        }

    }

    //This class implements the task that retrieves the data from the server.
    private static class RetrievalTask implements Runnable {
        private final String address;
        private final String port;
        private final String authToken;
        private final String personID;

        private final Handler handler;

        public RetrievalTask(Handler handler, String address, String port, String authToken
                , String personID) {
            this.handler = handler;
            this.address = address;
            this.port = port;
            this.authToken = authToken;
            this.personID = personID;
        }

        @Override
        public void run() {
            ServerProxy proxy = new ServerProxy(address, port);
            EventDataResult eventResult = proxy.getEvents(authToken);
            PersonResult personsResult = proxy.getPersons(authToken);
            PersonResult personResult = proxy.getPerson(authToken, personID);

            if (eventResult.isSuccess() && personResult.isSuccess() && personsResult.isSuccess()) {
                //If all of it went well, we store the data in the cache and send a success message
                DataCache cache = DataCache.getInstance();
                cache.store(eventResult);
                cache.setUser(personResult);
                cache.store(personsResult);
                cache.sort();

                sendMessage(true);
            } else {
                sendMessage(false);
            }
        }

        private void sendMessage(Boolean success){
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            messageBundle.putBoolean(DATA_SUCCESS_KEY, success);
            message.setData(messageBundle);
            handler.sendMessage(message);
        }
    }

    public interface Listener {
        void notifyDone();
    }

    public void registerListener(Listener listener) {
        this.listener = listener;
    }


}