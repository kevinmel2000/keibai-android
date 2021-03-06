package io.github.keibai.activities.event;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

import io.github.keibai.R;
import io.github.keibai.form.DefaultAwesomeValidation;
import io.github.keibai.http.Http;
import io.github.keibai.http.HttpCallback;
import io.github.keibai.http.HttpUrl;
import io.github.keibai.models.Event;
import io.github.keibai.models.meta.Error;
import io.github.keibai.runnable.RunnableToast;
import okhttp3.Call;

public class CreateEventActivity extends AppCompatActivity {

    private DefaultAwesomeValidation validation;
    private Http http;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        if (http == null) {
            http = new Http(getApplicationContext());
        }

        Toolbar toolbar = findViewById(R.id.toolbar_create_event);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        validation = new DefaultAwesomeValidation(getApplicationContext());
        validation.addValidation(this, R.id.edit_event_create_name, "[a-zA-Z0-9\\s]+", R.string.event_name_error);
        validation.addValidation(this, R.id.edit_event_create_category, "[a-zA-Z0-9\\s]+", R.string.category_error);
        validation.addValidation(this, R.id.edit_event_create_location, "[a-zA-Z0-9\\s]+", R.string.location_error);
    }

    @Override
    protected void onStop() {
        super.onStop();

        http.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_create_event_save:
                onSave();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Event eventFromForm() {
        EditText formName = findViewById(R.id.edit_event_create_name);
        Spinner formAuctionType = findViewById(R.id.spinner_event_create_type);
        EditText formLocation = findViewById(R.id.edit_event_create_location);
        EditText formCategory = findViewById(R.id.edit_event_create_category);

        Event event = new Event();
        event.name = formName.getText().toString();
        event.auctionType = String.valueOf(formAuctionType.getSelectedItem());
        event.location = formLocation.getText().toString();
        event.category = formCategory.getText().toString();
        event.status = Event.OPENED;

        return event;
    }

    public void onSave() {
        if (!validation.validate()) {
            return;
        }

        Toast.makeText(getApplicationContext(), R.string.submitting, Toast.LENGTH_SHORT).show();
        Event attemptEvent = eventFromForm();
        http.post(HttpUrl.newEventUrl(), attemptEvent, new HttpCallback<Event>(Event.class) {

            @Override
            public void onError(final Error error) throws IOException {
                runOnUiThread(new RunnableToast(getApplicationContext(), error.toString()));
            }

            @Override
            public void onSuccess(Event response) throws IOException {
                Intent intent = ActiveEventsActivity.getEventDetailIntent(getApplicationContext(), response);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call call, IOException  e) {
                runOnUiThread(new RunnableToast(getApplicationContext(), e.toString()));
            }
        });
    }
}
