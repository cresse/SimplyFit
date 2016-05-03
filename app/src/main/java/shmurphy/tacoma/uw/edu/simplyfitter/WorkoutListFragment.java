package shmurphy.tacoma.uw.edu.simplyfitter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import shmurphy.tacoma.uw.edu.simplyfitter.model.CalendarDay;
import shmurphy.tacoma.uw.edu.simplyfitter.model.Workout;
//import shmurphy.tacoma.uw.edu.simplyfitter.model.dummy.DummyContent;
//import shmurphy.tacoma.uw.edu.simplyfitter.model.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class WorkoutListFragment extends Fragment  {

    private static final String WORKOUT_URL
            = "http://cssgate.insttech.washington.edu/~shmurphy/SimplyFit/test.php?cmd=workouts";

    private RecyclerView mRecyclerView;
    private int mColumnCount = 1;

    private TextView mTitleTextView;

    private OnListFragmentInteractionListener mListener;

    public String mDay;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WorkoutListFragment() {
    }

    /**
     * Sets the day field so the workout list will be associated with one specific day.
     * @param day
     */
    public void setDay(String day) {
        mDay = day;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Workouts for April " + mDay + ", 2016");


        View view = inflater.inflate(R.layout.fragment_workout_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
//            recyclerView.setAdapter(new MyWorkoutRecyclerViewAdapter(DummyContent.ITEMS, mListener));
        }

        DownloadWorkoutsTask task = new DownloadWorkoutsTask();
        task.execute(new String[]{WORKOUT_URL});

        FloatingActionButton floatingActionButton = (FloatingActionButton)
                getActivity().findViewById(R.id.workout_fab);
        floatingActionButton.show();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Workout item);
    }

    private class DownloadWorkoutsTask extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... urls) {
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
                    response = "Unable to download the list of workouts, Reason: "
                            + e.getMessage();
                }
                finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {

//            Log.d("debug!!", result);
            // Something wrong with the network or the URL.
            if (result.startsWith("Unable to")) {
                Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            }
            List<Workout> workoutList = new ArrayList<Workout>(40);

            result = Workout.parseWorkoutJSON(result, workoutList, mDay);
            // sending the day to the parseJSON so that it can know which day to grab workouts for


            // Something wrong with the JSON returned.
            if (result != null) {
                Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            }
            // Everything is good, show the list of courses.
            if (!workoutList.isEmpty()) {
//                Log.d("debug-listener", mListener.toString());
                mRecyclerView.setAdapter(new MyWorkoutRecyclerViewAdapter(workoutList, mListener));
            }
        }

    }
}
