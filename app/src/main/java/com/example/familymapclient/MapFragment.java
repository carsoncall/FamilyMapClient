package com.example.familymapclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;

public class MapFragment extends Fragment {

    private DataCache dataCache;
    private Map<String, Event> events;
    private GoogleMap.OnMarkerClickListener listener;
    private ImageView imageView;
    private TextView textView;
    private GoogleMap map;
    private SharedPreferences preferences;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
//            LatLng sydney = new LatLng(-34, 151);
//            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            dataCache = DataCache.getInstance();

            events = dataCache.getEvents();
            map = googleMap;
            drawMarkers();

            //set default values
            Drawable drawable = new IconDrawable(getActivity(), FontAwesomeIcons.fa_android)
                    .colorRes(R.color.black)
                    .sizeDp(50);
            imageView.setImageDrawable(drawable);
            textView.setText(R.string.click_on_an_icon_to_see_details);


            listener = new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    Event event = (Event)marker.getTag();

                    Person eventPerson = dataCache.getPersonByID(event.getPersonID());
                    String personName = eventPerson.getFirstName() + " " + eventPerson.getLastName();
                    StringBuilder text = new StringBuilder(personName).append("\n")
                            .append(event.getEventType().toUpperCase(Locale.ROOT))
                            .append(": ").append(event.getCity())
                            .append(", ").append(event.getCountry())
                            .append(" (").append(event.getYear()).append(")");
                    textView.setText(text);

                    if (eventPerson.getGender().equals("f")) {
                        Drawable drawable = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female)
                                .colorRes(R.color.purple_200).sizeDp(50);
                        imageView.setImageDrawable(drawable);
                    } else {
                        Drawable drawable = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male)
                                .colorRes(R.color.teal_700).sizeDp(50);
                        imageView.setImageDrawable(drawable);
                    }
                    return false;
                }
            };
            googleMap.setOnMarkerClickListener(listener);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem settingsItem = menu.findItem(R.id.settings_icon);
        MenuItem searchItem = menu.findItem(R.id.search_icon);

        Drawable gearIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_gear)
                .colorRes(R.color.white)
                .sizeDp(20);
        settingsItem.setIcon(gearIcon);

        Drawable searchIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_search)
                .colorRes(R.color.white)
                .sizeDp(20);
        searchItem.setIcon(searchIcon);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_icon:
                Intent i =  new Intent(getActivity(), SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.search_icon:
                //TODO: search functionality
                Intent j = new Intent(getActivity(), SearchActivity.class);
                startActivity(j);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        imageView = view.findViewById(R.id.bottom_icon);
        textView = view.findViewById(R.id.bottom_text);

        Iconify.with(new FontAwesomeModule());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onResume() {
        if (map != null) {
            map.clear();
            drawMarkers();
            Drawable drawable = new IconDrawable(getActivity(), FontAwesomeIcons.fa_android)
                    .colorRes(R.color.black)
                    .sizeDp(50);
            imageView.setImageDrawable(drawable);
            textView.setText(R.string.click_on_an_icon_to_see_details);
        }
        super.onResume();
    }

    private void drawMarkers() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        Boolean showMaleEvents = preferences.getBoolean("male_events", true);
        Boolean showFemaleEvents = preferences.getBoolean("female_events", true);
        Boolean showPaternalEvents = preferences.getBoolean("father_side_events", true);
        Boolean showMaternalEvents = preferences.getBoolean("mother_side_events", true);

        Set<Model.Event> setOfDesiredEvents = dataCache.setOfDesiredEvents(showMaleEvents,showFemaleEvents
                                                            , showPaternalEvents, showMaternalEvents);

        for (Model.Event event : setOfDesiredEvents) {
            LatLng pos = new LatLng(event.getLatitude(), event.getLongitude());
            float hue = dataCache.getColor(event.getEventType());
            Marker marker = map.addMarker(new MarkerOptions().position(pos)
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
            assert marker != null;
            marker.setTag(event);
        }
    }

    private void drawLines() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        Boolean showLifeStoryLines = preferences.getBoolean("life_story_lines", true);
        Boolean showFamilyTreeLines = preferences.getBoolean("family_tree_lines", true);
        Boolean showSpouseLines = preferences.getBoolean("show_spouse_lines", true);

        /*
        LatLng startPoint = new LatLng(startEvent.getLatitude(), startEvent.getLongitude());
        LatLng endPoint = new LatLng(endEvent.getLatitude(), endEvent.getLongitude());
        PolylineOptions options = new PolylineOptions()
            .add(startPoint)
            .add(endPoint)
            .color(color)
            .width(width);
        Polyline line = googleMap.addPolyline(options);


function that returns birth or earliest recorded event w tiebreakers
markerExists
drawLines
set of Markers
         */
    }



}