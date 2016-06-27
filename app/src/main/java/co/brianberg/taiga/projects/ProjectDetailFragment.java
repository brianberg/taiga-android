package co.brianberg.taiga.projects;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import java.sql.SQLException;
import java.util.List;

import co.brianberg.taiga.R;
import co.brianberg.taiga.db.project.Project;
import co.brianberg.taiga.db.timeline.TimelineEntry;
import co.brianberg.taiga.db.timeline.TimelineEntryManager;
import co.brianberg.taiga.service.Taiga;

/**
 * A fragment representing a single Project detail screen.
 * This fragment is contained in a {@link ProjectDetailActivity}.
 */
public class ProjectDetailFragment extends Fragment {

  /**
   * Key to identify the project bundle argument
   */
  private static final String ARG_PROJECT = "project";

  /**
   * Fragment interaction listener
   */
  private OnProjectDetailFragmentInteractionListener mListener;

  /**
   * Project reference
   */
  private Project mProject;

  /**
   * Keep track of the retrieve timeline task to ensure we can cancel it if requested.
   */
  private RetrieveTimelineTask mRetrieveTimelineTask;

  /**
   * Timeline database manager
   */
  private TimelineEntryManager mTimelineEntryManager;

  /**
   * Project timeline entry list view adapter
   */
  private ProjectTimelineRecyclerViewAdapter mRecyclerViewAdapter;

  // UI references
  private ProgressBar mProgressBar;
  private RecyclerView mProjectTimeline;
  private Button mViewTimelineBtn;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ProjectDetailFragment() {}

  public static ProjectDetailFragment newInstance(Project project) {
    ProjectDetailFragment fragment = new ProjectDetailFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARG_PROJECT, project);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      initialize(savedInstanceState);
    } else {
      initialize(getArguments());
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.project_detail, container, false);

    if (mProject != null) {
      Activity activity = this.getActivity();
      CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
      if (appBarLayout != null) {
        appBarLayout.setTitle(mProject.getName());
        ImageView projectLogoView = (ImageView) activity.findViewById(R.id.project_detail_logo_view);
        Picasso.with(activity).load(mProject.getLogoBigUrl()).into(projectLogoView);
      }
      mRetrieveTimelineTask = new RetrieveTimelineTask(mProject.getId());
      mRetrieveTimelineTask.execute();
    }

    mTimelineEntryManager = new TimelineEntryManager(getContext());
    try {
      mTimelineEntryManager.open();
    } catch (SQLException e ) {
      e.printStackTrace();
    }

    ((TextView) rootView.findViewById(R.id.project_detail_desc)).setText(mProject.getDescription());
    mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_project_timeline);
    mProjectTimeline = (RecyclerView) rootView.findViewById(R.id.project_timeline_list);

    mViewTimelineBtn = (Button) rootView.findViewById(R.id.btn_view_project_timeline);
    mViewTimelineBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mListener.onProjectDetailFragmentInteraction(ProjectDetailActivity.EVENT_VIEW_TIMELINE);
      }
    });

    return rootView;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnProjectDetailFragmentInteractionListener) {
      mListener = (OnProjectDetailFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnProjectDetailFragmentInteractionListener");
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    try {
      mTimelineEntryManager.open();
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPause() {
    mTimelineEntryManager.close();
    super.onPause();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putParcelable(ARG_PROJECT, mProject);
    super.onSaveInstanceState(outState);
  }

  private void initialize(Bundle args) {
    if (args.containsKey(ARG_PROJECT)) {
      mProject = args.getParcelable(ARG_PROJECT);
    }
  }

  private void onRetrieveTimelineSuccess(List<TimelineEntry> timelineEntries) {
    for (TimelineEntry te : timelineEntries) {
      mTimelineEntryManager.upsertTimelineEntry(te);
    }
    List<TimelineEntry> latestEntries;
    if (timelineEntries.size() > 3) {
      latestEntries = timelineEntries.subList(0, 3);
    } else {
      latestEntries = timelineEntries;
    }
    hideProgressBar();
    mRecyclerViewAdapter = new ProjectTimelineRecyclerViewAdapter(latestEntries);
    mProjectTimeline.setVisibility(View.VISIBLE);
    mProjectTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
    mProjectTimeline.setAdapter(mRecyclerViewAdapter);
    mProjectTimeline.setHasFixedSize(true);
    mViewTimelineBtn.setVisibility(View.VISIBLE);
  }

  private void hideProgressBar() {
    mProgressBar.setVisibility(View.GONE);
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   */
  public interface OnProjectDetailFragmentInteractionListener {
    void onProjectDetailFragmentInteraction(int eventId);
  }

  /**
   * Represents an asynchronous login/registration task used to authenticate
   * the user.
   */
  private class RetrieveTimelineTask extends AsyncTask<Void, Void, List<TimelineEntry>> {

    private final String TAG = RetrieveTimelineTask.class.getSimpleName();

    private final int mProjectId;

    RetrieveTimelineTask(int projectId) {
      mProjectId = projectId;
    }

    @Override
    protected List<TimelineEntry> doInBackground(Void... params) {
      return Taiga.getProjectTimeline(getActivity().getBaseContext(), mProjectId,
          new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
              Log.d(TAG, error.toString());
            }
          }
      );
    }

    @Override
    protected void onPostExecute(final List<TimelineEntry> timelineEntries) {
      mRetrieveTimelineTask = null;
      if (timelineEntries != null) {
        onRetrieveTimelineSuccess(timelineEntries);
      }
    }

    @Override
    protected void onCancelled() {
      mRetrieveTimelineTask = null;
      hideProgressBar();
    }
  }

}
