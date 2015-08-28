package com.ravenrobotics.android.movies.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MoviePosterFragment extends Fragment {

    private final static String API_KEY = "[INSERT_YOUR_API_KEY_HERE]";
    private final static int NUMBER_OF_COLUMNS = 3;

    private ImageAdapter posterAdapter;
    private ArrayList<Movie> movieList;

    private String sortOrder;
    private String baseURL;

    public MoviePosterFragment() {
    } // end constructor

    // Checks if there is an active network connection
    // source: https://stackoverflow.com/questions/4238921...
    // .../detect-whether-there-is-an-internet-connection-available-on-android
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    } // end method isNetworkAvailable

    private void updateMovies() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortOrder = sharedPref.getString(getString(R.string.prefs_sortOrder_key),
                getString(R.string.prefs_sortOrder_default));
        FetchMoviesTask ft = new FetchMoviesTask();
        ft.execute(sortOrder);
    } // end method updateMovies

    @Override
    public void onResume() {
        final String LOG_TAG = "onResume";
        super.onResume();
        // Checks whether sort order preference has changed
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefOrder = sharedPref.getString(getString(R.string.prefs_sortOrder_key),
                getString(R.string.prefs_sortOrder_default));
        // If preferences have changed, refresh movie data
        if (!prefOrder.equals(sortOrder)) {
            sortOrder = prefOrder;
//            Log.v(LOG_TAG, "Updating movie data from server");
            posterAdapter.clear();
            updateMovies();
        }
    } // end method onResume

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Saves the movie data
        savedInstanceState.putParcelableArrayList(getString(R.string.saved_state_movieList_key), movieList);
        // Saves the current sort order, base image URL
        savedInstanceState.putString(getString(R.string.prefs_sortOrder_key), sortOrder);
        savedInstanceState.putString(getString(R.string.saved_state_baseURL_key), baseURL);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    } // end method onSaveInstanceState

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Specifies that this Fragment has menu events
        setHasOptionsMenu(true);
        // Initializes movieList and posterAdapter
        movieList = new ArrayList<Movie>();
        posterAdapter = new ImageAdapter(movieList);
    } // end method onCreate

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
    } // end method onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    } // end method onOptionsItemSelected

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Defines log tag for this method
        final String LOG_TAG = "onCreateView";
        // Inflates the root view for the fragment
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // Creates shared preferences object
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // Checks if state has been previously saved
        if (savedInstanceState != null) {

            String prefOrder = sharedPref
                    .getString(getString(R.string.prefs_sortOrder_key),
                            getString(R.string.prefs_sortOrder_default));
            String savedOrder = savedInstanceState
                    .getString(getString(R.string.prefs_sortOrder_key));

            // Shared preferences value overrides saved state
            // If prefOrder != savedOrder, changes order and asks server for update
            if (!prefOrder.equals(savedOrder)) {
                sortOrder = prefOrder;
                baseURL = "";
            }
            // pref and saved order are same
            else {
                sortOrder = savedOrder;
                baseURL = savedInstanceState.getString(getString(R.string.saved_state_baseURL_key));
                movieList = savedInstanceState
                        .getParcelableArrayList(getString(R.string.saved_state_movieList_key));
                if (movieList != null) {
                    // NOTE: posterAdapter is cleared on rotate etc.
                    // Populates posterAdapter with movieList
//                    Log.v(LOG_TAG, "movieList restored");
                    posterAdapter = new ImageAdapter(movieList);
                }
                // Logs error and triggers update if saved ParcelableArrayList is null
                else {
                    Log.e(LOG_TAG, "ParcelableArrayList movieList is null");
                }
            }
        }
        // No previously saved state, this is the first run
        else {
            // Initialize sortOrder with shared preferences/default value
            sortOrder = sharedPref.getString(getString(R.string.prefs_sortOrder_key),
                    getString(R.string.prefs_sortOrder_default));
            baseURL = "";
        }

        // If posterAdapter is still empty, movie data has not been set or is out of date
        // Updates movieList, posterAdapter
        if (posterAdapter.getCount() == 0) {
//            Log.v(LOG_TAG, "Updating movie data from server");
            updateMovies();
        }

        // Connects posterAdapter to GridView
        final GridView gridView = (GridView) rootView.findViewById(R.id.grid_item_poster);
        gridView.setAdapter(posterAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//            Log.v("USER INPUT", "User clicked on "+posterAdapter.getItem(position).getTitle());
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(DetailActivity.MOVIE_TITLE,
                        posterAdapter.getItem(position).getTitle());
                detailIntent.putExtra(DetailActivity.MOVIE_POSTER_PATH,
                        posterAdapter.getItem(position).getPosterPath());
                detailIntent.putExtra(DetailActivity.MOVIE_RELEASE_DATE,
                        posterAdapter.getItem(position).getReleaseDate());
                detailIntent.putExtra(DetailActivity.MOVIE_VOTE_AVERAGE,
                        posterAdapter.getItem(position).getVoteAverage());
                detailIntent.putExtra(DetailActivity.MOVIE_OVERVIEW,
                        posterAdapter.getItem(position).getOverview());
                startActivity(detailIntent);
            }
        });

        return rootView;
    } // end method onCreateView

    public Movie[] getMovieDataFromJson(String moviesJsonStr, String baseURL)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_RESULTS = "results";

        List<Movie> resultList = new ArrayList<>();

        if (moviesJsonStr == null)
            return null;

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);
        for (int i = 0; i < moviesArray.length(); ++i) {

            JSONObject jsonMovie = moviesArray.getJSONObject(i);

            String title = "";
            if (!jsonMovie.get("original_title").toString().equals("null"))
                title = (String) jsonMovie.get("original_title");

            String posterPath = "";
            if (!jsonMovie.get("poster_path").toString().equals("null")) {
                // sizes are: w92, w154, w185, w342, w500, w780 and original
                posterPath = baseURL + "w342" + jsonMovie.get("poster_path");
            }

            String releaseDate = "";
            if (!jsonMovie.get("release_date").toString().equals("null"))
                releaseDate = (String) jsonMovie.get("release_date");

            String voteAverage = "";
            if (!jsonMovie.get("vote_average").toString().equals("null"))
                voteAverage = "" + jsonMovie.get("vote_average");

            String overview = "";
            if (!jsonMovie.get("overview").toString().equals("null")) {
                overview = (String) jsonMovie.get("overview");
            }

            // Stops adding movies without posters
            if (!posterPath.equals("")) {
                Movie movie = new Movie(title,
                        posterPath,
                        releaseDate,
                        voteAverage,
                        overview);
                resultList.add(movie);
            }
        }

        Movie[] results = new Movie[resultList.size()];
        resultList.toArray(results);
        return results;
    } // end method getMovieDataFromJson

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(Movie[] result) {
            if (result != null) {
                posterAdapter.clear();
                for (Movie movie : result) {
                    posterAdapter.add(movie);
                }
            }
        } // end method onPostExecute

        private String getJsonResponse(String uriString) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // Create the request to TheMovieDB, and open the connection
                URL url = new URL(uriString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                return buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movies data, there's no point in
                // attempting to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        } // end method getJsonResponse

        @Override
        protected Movie[] doInBackground(String... params) {

            // These variables must be declared outside the try/catch so that their resources
            // can be released in the finally block.

            String jsonResponse = null;
            baseURL = "";
            String sortOrder = "";

            if (!isNetworkAvailable()) {
                Log.v(LOG_TAG, "NETWORK NOT AVAILABLE");
                return null;
            }

            if (params != null) {
                sortOrder = params[0];
//                Log.v(LOG_TAG, "Using sort order: " + sortOrder);
            }

            // Constructs the URI for the TheMovieDB configuration query
            Uri.Builder URI = new Uri.Builder();
            URI.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("configuration")
                    .appendQueryParameter("api_key", API_KEY);

            // Executes query
            jsonResponse = getJsonResponse(URI.build().toString());

            // Extracts base URL for images from config data
            try {
                baseURL = getConfigDataFromJson(jsonResponse);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Config JSON parsing error ", e);
                return null;
            }

            // reset jsonResponse string
            jsonResponse = null;

            // Construct the URL for the TheMovieDB movies query
            URI = new Uri.Builder();
            URI.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie");

            if (sortOrder.equals("Most Popular"))
                URI.appendQueryParameter("sort_by", "popularity.desc");
            else if (sortOrder.equals("Most Recent")) {
                String today;
                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
                today = date_format.format(new Date());
                URI.appendQueryParameter("release_date.lte", today);
                URI.appendQueryParameter("sort_by", "release_date.desc");
            } else
                Log.e(LOG_TAG, "Unrecognized sort order passed in: " + sortOrder);

            URI.appendQueryParameter("api_key", API_KEY);
            // Log.v("URL", URI.build().toString());

            // Executes query
            String movieData = getJsonResponse(URI.build().toString());

            try {
                return getMovieDataFromJson(movieData, baseURL);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Movies JSON parsing error ", e);
            }

            return null;
        } // end method doInBackground

        private String getConfigDataFromJson(String jsonResponse)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_IMAGES = "images";

            try {
                JSONObject configJson = new JSONObject(jsonResponse);
                JSONObject imagesJson = configJson.getJSONObject(TMDB_IMAGES);
                return (String) imagesJson.get("base_url");
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Config JSON parsing error ", e);
                return null;
            }
        }

    } // end class FetchMoviesTask

    public class ImageAdapter extends ArrayAdapter<Movie> {

        public ImageAdapter(ArrayList<Movie> items) {
            super(getActivity(), 0, items);
        }

        // creates a new ImageView for each item referenced by the Adapter
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initializes some attributes
                Context context = getContext();
                imageView = new ImageView(context);
            } else {
                imageView = (ImageView) convertView;
            }
            int width = (int) ((double) parent.getWidth() / ((double) NUMBER_OF_COLUMNS));
            // race condition... parent.getWidth() sometimes returns 0
            if (width == 0) width = 360;
            String url = getItem(position).getPosterPath();
            if (!url.equals("")) {
                Picasso.with(getActivity())
                        .load(url)
                        .noFade()
                        .resize(width, 0) // fit width
                        .into(imageView);
            }
            return imageView;
        }
    } // end class ImageAdapter

} // end class MoviePosterFragment

