package com.example.familymapclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.HashSet;
import java.util.List;
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
    private final Set<Marker> markers = new HashSet<>();
    private final Set<Polyline> lines = new HashSet<>();
    Set<Model.Event> setOfDesiredEvents;

    Person eventPerson;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

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
            dataCache = DataCache.getInstance();
            events = dataCache.getEvents();
            map = googleMap;
            drawMarkers();

            if (getArguments() != null) {
                String eventID = getArguments().getString("eventID");
                Event specialEvent = events.get(eventID);
                LatLng specialEventCoord = new LatLng(specialEvent.getLatitude(), specialEvent.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(specialEventCoord));
                populateBottom(specialEvent);
                drawLines(specialEvent);
            } else {
                //set default values
                Drawable drawable = new IconDrawable(getActivity(), FontAwesomeIcons.fa_android)
                        .colorRes(R.color.black)
                        .sizeDp(50);
                imageView.setImageDrawable(drawable);
                textView.setText(R.string.click_on_an_icon_to_see_details);
            }

            listener = new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    Event event = (Event)marker.getTag();
                    populateBottom(event);
                    drawLines(event);

                    return false;
                }
            };
            googleMap.setOnMarkerClickListener(listener);
        }
    };

    private void clearLines() {
        for (Polyline line : lines) {
            line.remove();
        }
        lines.clear();
    }

    private void populateBottom(Model.Event event) {
        assert event != null;
        eventPerson = dataCache.getPersonByID(event.getPersonID());
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
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            setHasOptionsMenu(true);
        }

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

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), PersonActivity.class);
                i.putExtra(PersonActivity.PERSON_KEY, eventPerson.getPersonID());
                startActivity(i);
            }
        });

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
        map.clear();
        preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        Boolean showMaleEvents = preferences.getBoolean("male_events", true);
        Boolean showFemaleEvents = preferences.getBoolean("female_events", true);
        Boolean showPaternalEvents = preferences.getBoolean("father_side_events", true);
        Boolean showMaternalEvents = preferences.getBoolean("mother_side_events", true);

        setOfDesiredEvents = dataCache.setOfDesiredEvents(showMaleEvents,showFemaleEvents
                                                            , showPaternalEvents, showMaternalEvents);

        for (Model.Event event : setOfDesiredEvents) {
            LatLng pos = new LatLng(event.getLatitude(), event.getLongitude());
            float hue = dataCache.getColor(event.getEventType());
            Marker marker = map.addMarker(new MarkerOptions().position(pos)
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
            assert marker != null;
            marker.setTag(event);
            markers.add(marker);
        }
    }

    private void drawLines(Event event) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        Boolean showMaleEvents = preferences.getBoolean("male_events", true);
        Boolean showFemaleEvents = preferences.getBoolean("female_events", true);
        Boolean showPaternalEvents = preferences.getBoolean("father_side_events", true);
        Boolean showMaternalEvents = preferences.getBoolean("mother_side_events", true);

        Boolean showLifeStoryLines = preferences.getBoolean("life_story_lines", true);
        Boolean showFamilyTreeLines = preferences.getBoolean("family_tree_lines", true);
        Boolean showSpouseLines = preferences.getBoolean("spouse_lines", true);

        assert event != null;
        Person eventPerson = dataCache.getPersonByID(event.getPersonID());
        LatLng startPoint = new LatLng(event.getLatitude(), event.getLongitude());

        clearLines();
        if ((!showMaleEvents && !showFemaleEvents)
                || (!showFamilyTreeLines && !showLifeStoryLines && !showSpouseLines)) {
            return;
        }

        if (showMaleEvents && showFemaleEvents && showSpouseLines && eventPerson.getSpouseID() != null) {
            drawSpouseLines(startPoint, eventPerson);
        }
        if (showFamilyTreeLines) {
            drawFamilyTreeLines(startPoint, eventPerson, 10);
        }
        if (showLifeStoryLines) {
            drawLifeStoryLines(eventPerson);
        }
    }

    private void drawSpouseLines(LatLng startPoint, Person eventPerson) {
        Model.Event spouseFirstEvent = dataCache.getEarliestEvent(eventPerson.getSpouseID());
        if (setOfDesiredEvents.contains(spouseFirstEvent)) {
            LatLng endPoint = new LatLng(spouseFirstEvent.getLatitude(), spouseFirstEvent.getLongitude());
            PolylineOptions options = new PolylineOptions()
                    .add(startPoint)
                    .add(endPoint)
                    .color(Color.DKGRAY)
                    .width(5);
            lines.add(map.addPolyline(options));
        }
    }

    private void drawFamilyTreeLines(LatLng startPoint, Person eventPerson, double width) {
        String fatherID = eventPerson.getFatherID();
        String motherID = eventPerson.getMotherID();

        if (fatherID != null) {
            Model.Event fatherEarliest = dataCache.getEarliestEvent(fatherID);
            if (setOfDesiredEvents.contains(fatherEarliest)) {
                LatLng fatherEarliestPoint = new LatLng(fatherEarliest.getLatitude(), fatherEarliest.getLongitude());
                PolylineOptions options = new PolylineOptions()
                        .add(startPoint)
                        .add(fatherEarliestPoint)
                        .color(Color.GREEN)
                        .width((float) width);
                lines.add(map.addPolyline(options));
                drawFamilyTreeLines(fatherEarliestPoint, dataCache.getPersonByID(fatherID), width*.6);
            }
        }
        if (motherID != null) {
            Model.Event motherEarliest = dataCache.getEarliestEvent(motherID);
            if (setOfDesiredEvents.contains(motherEarliest)) {
                LatLng motherEarliestPoint = new LatLng(motherEarliest.getLatitude(), motherEarliest.getLongitude());
                PolylineOptions options = new PolylineOptions()
                        .add(startPoint)
                        .add(motherEarliestPoint)
                        .color(Color.GREEN)
                        .width((float) width);
                lines.add(map.addPolyline(options));
                drawFamilyTreeLines(motherEarliestPoint, dataCache.getPersonByID(motherID), width*.6);
            }
        }
    }

    private void drawLifeStoryLines(Person eventPerson) {
        List<Event> personEvents = dataCache.getPersonEventsByID(eventPerson.getPersonID());
        LatLng startPoint;
        LatLng endPoint = null;
        for (Model.Event event : personEvents) {
            startPoint = new LatLng(event.getLatitude(),event.getLongitude());
            if (endPoint != null ) {
                PolylineOptions options = new PolylineOptions()
                        .add(startPoint)
                        .add(endPoint)
                        .color(Color.MAGENTA)
                        .width(5);
                lines.add(map.addPolyline(options));
            }
            endPoint = startPoint;
        }
    }

}