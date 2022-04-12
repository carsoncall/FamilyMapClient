package com.example.familymapclient;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import Model.Event;
import Model.Person;

public class SearchActivity extends AppCompatActivity {

    private static final int EVENT_VIEW_TYPE = 0;
    private static final int PERSON_VIEW_TYPE = 0;

    SearchView searchView;
    DataCache dataCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dataCache = DataCache.getInstance();

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

       searchView = findViewById(R.id.search_text);
       SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String s) {
               Pair<List<Person>, List<Event>> searchData = dataCache.searchData(s);
               SearchAdapter adapter = new SearchAdapter(searchData.first, searchData.second);
               recyclerView.setAdapter(adapter);
               return false;
           }

           @Override
           public boolean onQueryTextChange(String s) {
               return false;
           }
       };

       searchView.setOnQueryTextListener(listener);

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

    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder>{
        List<Model.Event> events;
        List<Person> persons;


        SearchAdapter(List<Person> persons, List<Model.Event> events) {
            this.events = events;
            this.persons = persons;
        }

        @Override
        public int getItemViewType(int position) {
            return position < persons.size() ? PERSON_VIEW_TYPE : EVENT_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            view = getLayoutInflater().inflate(R.layout.search_item, parent, false);
            return new SearchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            if (position < persons.size()) {
                holder.bind(persons.get(position));
            } else {
                holder.bind(events.get(position - persons.size()));
            }
        }

        @Override
        public int getItemCount() {
            return (events.size() + persons.size());
        }
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView imageView;
        private final TextView firstLine;
        private final TextView secondLine;

        private final int viewType;
        private Model.Event event;
        private Person person;
        private DataCache dataCache;

        public SearchViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            dataCache = DataCache.getInstance();

            itemView.setOnClickListener(this);

//            if(viewType == PERSON_VIEW_TYPE) {
//                firstLine = itemView.findViewById(R.id.first_line);
//                secondLine = null;
//            } else {
            imageView = itemView.findViewById(R.id.search_item_icon);
            firstLine = itemView.findViewById(R.id.first_line);
            secondLine = itemView.findViewById(R.id.second_line);
            //}
        }

        private void bind(Model.Event event) {

            Drawable drawable = new IconDrawable(this.imageView.getContext(), FontAwesomeIcons.fa_map_marker)
                    .colorRes(R.color.black)
                    .sizeDp(32);
            imageView.setImageDrawable(drawable);
            this.event = event;
            StringBuilder text = new StringBuilder()
                    .append(event.getEventType().toUpperCase(Locale.ROOT))
                    .append(": ").append(event.getCity())
                    .append(", ").append(event.getCountry())
                    .append(" (").append(event.getYear()).append(")");
            firstLine.setText(text);

            Person person = dataCache.getPersonByID(event.getPersonID());
            String text2 = person.getFirstName() + " " + person.getLastName();
            secondLine.setText(text2);
        }

        private void bind(Person person) {
            this.person = person;
            if (person.getGender().equals("m")) {
                Drawable drawable = new IconDrawable(this.imageView.getContext(), FontAwesomeIcons.fa_male)
                        .colorRes(R.color.teal_700)
                        .sizeDp(32);
                imageView.setImageDrawable(drawable);
            } else {
                Drawable drawable = new IconDrawable(this.imageView.getContext(), FontAwesomeIcons.fa_female)
                        .colorRes(R.color.purple_200)
                        .sizeDp(32);
                imageView.setImageDrawable(drawable);
            }
            String text = person.getFirstName() + " " + person.getLastName();
            firstLine.setText(text);
            secondLine.setText("");
        }

        @Override
        public void onClick(View view) {
            if (viewType == PERSON_VIEW_TYPE) {
                //TODO: pull up person activity
            } else {
                //TODO: pull up event activity
            }
        }
    }

}