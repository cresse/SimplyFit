/* TCSS 450 - Mobile Apps - Group 11 */

package shmurphy.tacoma.uw.edu.simplyfitter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import shmurphy.tacoma.uw.edu.simplyfitter.authenticate.SignInActivity;
import shmurphy.tacoma.uw.edu.simplyfitter.model.CalendarDay;
import shmurphy.tacoma.uw.edu.simplyfitter.model.Exercise;
import shmurphy.tacoma.uw.edu.simplyfitter.model.Workout;

public class MainActivity extends AppCompatActivity implements CalendarListFragment.OnListFragmentInteractionListener,
        WorkoutListFragment.OnListFragmentInteractionListener, AddWorkoutFragment.AddWorkoutListener,
        AddAerobicFragment.AddAerobicListener, AddWeightsFragment.AddWeightsListener, WorkoutListFragment.DeleteWorkoutListener,
        ExerciseListFragment.DeleteExerciseListener, WorkoutListFragment.EditWorkoutListener,
        ExerciseListFragment.EditExerciseListener,
        ExerciseListFragment.OnListFragmentInteractionListener {

    private int mDate;   // used to keep track of the date we're on
    private String mUserID;
    private int mWorkoutID;
    private int mExerciseID;
    private boolean mEditMode = false;

    public AddWorkoutFragment mAddWorkoutFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // get the username from the user that just logged in.
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.LOGIN_PREFS),
                Context.MODE_PRIVATE);
        mUserID = sharedPreferences.getString("username","");

            Log.d("debug, userID", "NOW the user ID is " + mUserID);

        // add workout button. on click starts the add workout fragment
        // we send the mDate field to the fragment so it knows which day we're adding a workout to
        FloatingActionButton workoutFAB = (FloatingActionButton) findViewById(R.id.workout_fab);
        workoutFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAddWorkoutFragment = new AddWorkoutFragment();
                mAddWorkoutFragment.setDate(mDate);
                mAddWorkoutFragment.setUserID(mUserID);
                mAddWorkoutFragment.setMEditingMode(false);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mAddWorkoutFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // add exercise button. on click starts the add exercise fragment
        FloatingActionButton exerciseFAB = (FloatingActionButton) findViewById(R.id.add_exercise_fab);
        exerciseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExerciseOptionFragment exerciseOptionFragment = new ExerciseOptionFragment();
                exerciseOptionFragment.setWorkoutID(mWorkoutID);
                mEditMode = false;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, exerciseOptionFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // if we've already logged in, start the calendar list fragment
        if (savedInstanceState == null ||
                getSupportFragmentManager().findFragmentById(R.id.calendarlist_fragment) == null) {

            CalendarListFragment calendarListFragment = new CalendarListFragment();
            calendarListFragment.setmUserID(mUserID);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, calendarListFragment)
                    .commit();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // logout button
        if (id == R.id.action_logout) {
            SharedPreferences sharedPreferences =
                    getSharedPreferences(getString(R.string.LOGIN_PREFS),
                            Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(getString(R.string.LOGGEDIN), false)
                    .commit();
            // takes us back to the sign in activity
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * From CalendarListFragment
     * Launches the WorkoutListFragment when a CalendarDay is selected
     * We send the mDate field to the WorkoutListFragment so that it knows which day to
     * display workouts for.
     * @param item the calendar day item
     */
    @Override
    public void onListFragmentInteraction(CalendarDay item) {
        mDate = item.mDay;

        WorkoutListFragment workoutListFragment = new WorkoutListFragment();
        workoutListFragment.setDay(mDate);
        workoutListFragment.setmUserID(mUserID);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, workoutListFragment)
                .addToBackStack(null)
                .commit();

    }

    /**
     * From WorkoutListFragment
     *
     * Here we will implement the exercise list fragment (not implemented yet)
     * @param item
     */
    @Override
    public void onListFragmentInteraction(Workout item) {
        mWorkoutID = item.mID;
        ExerciseListFragment exerciseListFragment = new ExerciseListFragment();
        exerciseListFragment.setMWorkout(item);
//        exerciseListFragment.setMWorkout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, exerciseListFragment)
                .addToBackStack(null)
                .commit();

    }

    /**
     * From ExerciseListFragment
     *
     * @param item
     */
    @Override
    public void onListFragmentInteraction(Exercise item) {
        mExerciseID = item.mID;
        Log.d("ExerciseID", Integer.toString(mExerciseID));

    }

    /**
     * From AddWorkoutFragment
     * This is called when the add workout button is pushed on the AddWorkoutFragment.
     * It executes the AddWorkoutTask to add the new workout and then returns back to the
     * WorkoutListFragment by popping the stack.
     * @param url
     */
    @Override
    public void addWorkout(String url, String deleteURL) {
        // if we are editing a workout, the deleteURL will not be null.
        // when editing workout info, we delete the old one, then add a new one with the new info.
        if(deleteURL != null) {deleteWorkout(deleteURL);}

        AddWorkoutTask task = new AddWorkoutTask();
        task.execute(new String[]{url.toString()});
        getSupportFragmentManager().popBackStackImmediate();
    }

    /**
     * WorkoutListFragment.DeleteWorkoutListener
     *
     * @param url
     */
    @Override
    public void deleteWorkout(String url) {
        DeleteWorkoutTask task = new DeleteWorkoutTask();
        task.execute(new String[]{url.toString()});
//        getSupportFragmentManager().popBackStackImmediate();
        // TODO - refresh this fragment instead of popping
    }

    /**
     * WorkoutListFragment.EditWorkoutListener
     *
     * To edit a workout, we first launch a new add workout fragment.
     * We set the fields to the same information as the workout we are editing.
     * The deleteURL comes from the WorkoutRecyclerView. We build the delete URL with the information
     * from whichever workout we are editing. We delete the workout right before adding the "new"
     * one with updated info to the database.
     */
    @Override
    public void editWorkout(Workout workout, String deleteURL) {
        mAddWorkoutFragment = new AddWorkoutFragment();
        mAddWorkoutFragment.setWorkoutID(workout.mID);
        mAddWorkoutFragment.setDate(mDate);
        mAddWorkoutFragment.setUserID(mUserID);
        mAddWorkoutFragment.setMDeleteURL(deleteURL);
        mAddWorkoutFragment.setMEditingMode(true);
        mAddWorkoutFragment.setPreviousWorkout(workout);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mAddWorkoutFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * From WorkoutListFragment.
     *
     * @param url
     */
    @Override
    public void deleteExercise(String url) {
        DeleteExerciseTask task = new DeleteExerciseTask();
        task.execute(new String[]{url.toString()});
        getSupportFragmentManager().popBackStackImmediate();
    }

    /**
     * From ExerciseListFragment
     */
    @Override
    public void editExercise(Exercise exercise, String deleteURL) {
        mEditMode = true;
        if(exercise.mType.equals("Aerobic")) {
            Log.d("Main", "Editing an aerobic");
            AddAerobicFragment addAerobicFragment = new AddAerobicFragment();
            addAerobicFragment.setWorkoutID(mWorkoutID);
            addAerobicFragment.setmType("Aerobic");
            addAerobicFragment.setMDeleteURL(deleteURL);
            addAerobicFragment.setMEditingMode(true);
            addAerobicFragment.setPreviousExercise(exercise);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addAerobicFragment)
                    .addToBackStack(null)
                    .commit();
        } else if (exercise.mType.equals("Flexibility")) {
            Log.d("Main", "Editing a flex");

            AddAerobicFragment addFlexibilityFragment = new AddAerobicFragment();
            addFlexibilityFragment.setWorkoutID(mWorkoutID);
            addFlexibilityFragment.setmType("Flexibility");
            addFlexibilityFragment.setMDeleteURL(deleteURL);
            addFlexibilityFragment.setMEditingMode(true);
            addFlexibilityFragment.setPreviousExercise(exercise);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addFlexibilityFragment)
                    .addToBackStack(null)
                    .commit();
        } else if(exercise.mType.equals("Weight")) {
            Log.d("Main", "Editing a strength");

            AddWeightsFragment addWeightsFragment = new AddWeightsFragment();
            addWeightsFragment.setWorkoutID(mWorkoutID);
            addWeightsFragment.setmDeleteURL(deleteURL);
            addWeightsFragment.setMEditingMode(true);
            addWeightsFragment.setPreviousExercise(exercise);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addWeightsFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * From AddAerobicFragment
     * This is called when the add exercise button is pushed on the AddExerciseFragment.
     * It executes the AddExerciseTask to add the new exercise and then returns back to the
     * ExerciseListFragment by popping the stack.
     * @param url
     */
    @Override
    public void addAerobicExercise(String url, String deleteURL) {
        if(deleteURL != null) {
            deleteExercise(deleteURL);
        }


        AddExerciseTask task = new AddExerciseTask();
        task.execute(new String[]{url.toString()});
//         Takes you back to the previous fragment by popping the current fragment out.
        if(!mEditMode) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        getSupportFragmentManager().popBackStackImmediate(); // back to the exercise list fragment

    }

    /**
     * From AddWeightsFragment
     * This is called when the add exercise button is pushed on the AddExerciseFragment.
     * It executes the AddExerciseTask to add the new exercise and then returns back to the
     * ExerciseListFragment by popping the stack.
     * @param url
     */
    @Override
    public void addWeightsExercise(String url, String deleteURL) {
        if(deleteURL != null) {deleteExercise(deleteURL);}

        AddExerciseTask task = new AddExerciseTask();
        task.execute(new String[]{url.toString()});
        // Takes you back to the previous fragment by popping the current fragment out.
        getSupportFragmentManager().popBackStackImmediate();
        getSupportFragmentManager().popBackStackImmediate();
    }

    /**
     * Add the new workout to our database table.
     */
    private class AddWorkoutTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to add workout, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return response;
        }

        /**
         * It checks to see if there was a problem with the URL(Network) which is when an
         * exception is caught. It tries to call the parse Method and checks to see if it was successful.
         * If not, it displays the exception.
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            // Something wrong with the network or the URL.
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");
                if (status.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Workout successfully added!"
                            , Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to add: "
                            + jsonObject.get("error")
                            , Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something wrong with the data" +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Add the new workout to our database table.
     */
    private class DeleteWorkoutTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to delete workout, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return response;
        }

        /**
         * It checks to see if there was a problem with the URL(Network) which is when an
         * exception is caught. It tries to call the parse Method and checks to see if it was successful.
         * If not, it displays the exception.
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            // Something wrong with the network or the URL.
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");
                if (status.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Workout deleted!"
                            , Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to delete: "
                                    + jsonObject.get("error")
                            , Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something wrong with the data" +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Add the new workout to our database table.
     */
    private class DeleteExerciseTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to delete exercise, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return response;
        }

        /**
         * It checks to see if there was a problem with the URL(Network) which is when an
         * exception is caught. It tries to call the parse Method and checks to see if it was successful.
         * If not, it displays the exception.
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            // Something wrong with the network or the URL.
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");
                if (status.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Exercise deleted!"
                            , Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to delete: "
                                    + jsonObject.get("error")
                            , Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something wrong with the data" +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Add the new workout to our database table.
     */
    private class AddExerciseTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to add exercise, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return response;
        }

        /**
         * It checks to see if there was a problem with the URL(Network) which is when an
         * exception is caught. It tries to call the parse Method and checks to see if it was successful.
         * If not, it displays the exception.
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String exerciseStatus = (String) jsonObject.get("result");

                if (exerciseStatus.equals("success")) {   // check that the weights exercise was added
                    Toast.makeText(getApplicationContext(), "Exercise successfully added!"
                            , Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to add weights exercise: "
                                    + jsonObject.get("error")
                            , Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something wrong with the data" +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void launch(View v) {
        TimePickerFragment fragment = null;
        if (v.getId() == R.id.start_time_button) {
            fragment = new TimePickerFragment();
            mAddWorkoutFragment.setStartTimePicker(fragment);
            fragment.setAddWorkoutFragment(mAddWorkoutFragment);
            fragment.setType("Start");
        } else if (v.getId() == R.id.end_time_button) {
            fragment = new TimePickerFragment();
            mAddWorkoutFragment.setEndTimePickerFragment(fragment);
            fragment.setAddWorkoutFragment(mAddWorkoutFragment);
            fragment.setType("End");
        }
        if (fragment != null)
            fragment.show(getSupportFragmentManager(), "launch");
    }

}
