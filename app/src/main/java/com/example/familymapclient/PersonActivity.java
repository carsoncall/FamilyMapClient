package com.example.familymapclient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;

public class PersonActivity extends AppCompatActivity {

    public static final String PERSON_KEY = "ReceivedPersonKey";
    DataCache dataCache = DataCache.getInstance();
    List<Model.Event> events;
    Map<Person, String> familyRelationships;
    List<Person> persons;
    String personID;
    Person person;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Intent intent = getIntent();
        personID = intent.getStringExtra(PERSON_KEY);
        person = dataCache.getPersonByID(personID);


        TextView personActivityFirstName = findViewById(R.id.person_activity_first_name);
        personActivityFirstName.setText(person.getFirstName());

        TextView personActivityLastName = findViewById(R.id.person_activity_last_name);
        personActivityLastName.setText(person.getLastName());

        TextView personActivityGender = findViewById(R.id.person_activity_gender);
        if (person.getGender().equals("m")) {
            personActivityGender.setText(R.string.male);
        } else {
            personActivityGender.setText(R.string.female);
        }

        ExpandableListView expandableListView = findViewById(R.id.expandable_list_view);

        events = dataCache.getPersonEventsByID(personID);
        events.removeIf(event -> !dataCache.setOfFilteredEvents.contains(event));

        familyRelationships = dataCache.getFamily(personID);
        persons = new ArrayList<>();
        persons.addAll(familyRelationships.keySet());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        expandableListView.setAdapter(new ExpandableListAdapter(events, persons));
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private static final int EVENT_POSITION = 0;
        private static final int PERSON_POSITION = 1;

        private final List<Event> events;
        private final List<Person> persons;

        ExpandableListAdapter(List<Event> events, List<Person> persons) {
            this.events = events;
            this.persons = persons;
        }


        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch(groupPosition) {
                case EVENT_POSITION:
                    return events.size();
                case PERSON_POSITION:
                    return persons.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int i) {
            return null;
        }

        @Override
        public Object getChild(int i, int i1) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int i, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENT_POSITION:
                    titleView.setText(R.string.events_title);
                    break;
                case PERSON_POSITION:
                    titleView.setText(R.string.persons_title);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch (groupPosition) {
                case EVENT_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeEventView(itemView, childPosition);
                    break;
                case PERSON_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_item, parent, false);
                    initializePersonView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
            return itemView;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }

        private void initializeEventView(View eventItemView, final int childPosition) {
            ImageView eventItemIcon = eventItemView.findViewById(R.id.event_item_icon);
            Drawable drawable = new IconDrawable(eventItemIcon.getContext(), FontAwesomeIcons.fa_map_marker)
                    .colorRes(R.color.black)
                    .sizeDp(32);
            eventItemIcon.setImageDrawable(drawable);

            TextView eventItemTypeView = eventItemView.findViewById(R.id.event_item_type_and_location);
            Model.Event event = events.get(childPosition);
            StringBuilder typeAndLocation = new StringBuilder(event.getEventType().toUpperCase(Locale.ROOT))
                    .append(": ")
                    .append(event.getCity())
                    .append(", ")
                    .append(event.getCountry())
                    .append("(").append(event.getYear()).append(")");
            eventItemTypeView.setText(typeAndLocation);

            TextView eventItemLocation = eventItemView.findViewById(R.id.event_person);
            String name = person.getFirstName() + " " + person.getLastName();
            eventItemLocation.setText(name);

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), EventActivity.class);
                    i.putExtra(EventActivity.EVENT_KEY, event.getEventID());
                    startActivity(i);
                }
            });
        }

        private void initializePersonView(View personItemView, final int childPosition) {

            Person personItem = persons.get(childPosition);

            Drawable drawable;
            ImageView personItemIcon = personItemView.findViewById(R.id.person_item_icon);
            if (personItem.getGender().equals("m")) {
                drawable = new IconDrawable(personItemIcon.getContext(), FontAwesomeIcons.fa_male)
                        .colorRes(R.color.teal_700)
                        .sizeDp(32);
            } else {
                drawable = new IconDrawable(personItemIcon.getContext(), FontAwesomeIcons.fa_female)
                        .colorRes(R.color.purple_200)
                        .sizeDp(32);
            }

            personItemIcon.setImageDrawable(drawable);

            TextView personItemName = personItemView.findViewById(R.id.person_item_name);
            StringBuilder fullName = new StringBuilder(personItem.getFirstName())
                    .append(" ")
                    .append(personItem.getLastName());
            personItemName.setText(fullName);

            TextView personRelationshipView = personItemView.findViewById(R.id.person_item_relationship);
            personRelationshipView.setText(familyRelationships.get(personItem));

            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(view.getContext(), PersonActivity.class);
                    i.putExtra(PersonActivity.PERSON_KEY, persons.get(childPosition).getPersonID());
                    startActivity(i);
                }
            });
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

}