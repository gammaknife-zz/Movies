package com.ravenrobotics.android.movies.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends ActionBarActivity {

    public final static String MOVIE_TITLE = "MOVIE_TITLE";
    public final static String MOVIE_RELEASE_DATE = "RELEASE_DATE";
    public final static String MOVIE_POSTER_PATH = "POSTER_PATH";
    public final static String MOVIE_VOTE_AVERAGE = "VOTE_AVERAGE";
    public final static String MOVIE_OVERVIEW = "OVERVIEW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_detail, new PlaceholderFragment())
                    .commit();
        }
    } // end method onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    } // end method onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    } // end method onOptionsItemSelected

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        } // end constructor

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.activity_detail, container, false);

            // Gets the strings passed into the activity via the intent
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                if (intent.hasExtra(MOVIE_TITLE)) {
                    String title = intent.getStringExtra(MOVIE_TITLE);
                    ((TextView) rootView.findViewById(R.id.movie_title)).setText(title);
                }
                if (intent.hasExtra(MOVIE_RELEASE_DATE)) {
                    String release_date_input = intent.getStringExtra(MOVIE_RELEASE_DATE);
                    String release_date_output = "";
                    Date date;
                    SimpleDateFormat format_input = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat format_output = new SimpleDateFormat("MMMM d, yyyy");
                    try {
                        date = format_input.parse(release_date_input);
                        release_date_output = format_output.format(date);
                    } catch (ParseException e) {
                        Log.e("Detail", "Date parse error");
                    }
                    ((TextView) rootView
                            .findViewById(R.id.movie_release_date))
                            .setText(release_date_output);
                }
                if (intent.hasExtra(MOVIE_POSTER_PATH)) {
                    final String movie_poster_url = intent.getStringExtra(MOVIE_POSTER_PATH);
                    ImageView imageView = (ImageView) rootView.findViewById(R.id.movie_poster);
                    Picasso.with(getActivity())
                            .load(movie_poster_url)
                            .noFade()
                            .into(imageView);
                }
                if (intent.hasExtra(MOVIE_VOTE_AVERAGE)) {
                    String vote_average = intent.getStringExtra(MOVIE_VOTE_AVERAGE);
                    ((TextView) rootView
                            .findViewById(R.id.movie_vote_average))
                            .setText(vote_average);
                }
                if (intent.hasExtra(MOVIE_OVERVIEW)) {
                    String overview = intent.getStringExtra(MOVIE_OVERVIEW);
                    TextView textView = (TextView) rootView.findViewById(R.id.movie_overview);
                    textView.setText(overview);
                }
            } // end if (intent != null)

            // redraw View tree
            rootView.invalidate();

            return rootView;
        } // end method onCreateView

    } // end class PlaceholderFragment

} // end class DetailActivity
