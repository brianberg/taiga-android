package co.brianberg.taiga.timeline;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import co.brianberg.taiga.R;
import co.brianberg.taiga.db.timeline.TimelineEntry;
import co.brianberg.taiga.projects.ProjectTimelineRecyclerViewAdapter;
import co.brianberg.taiga.ui.SimpleDividerItemDecoration;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProjectTimelineFragment extends Fragment {

  /**
   * Key to identify the project timeline bundle argument
   */
  private static final String ARG_PROJECT_TIMELINE = "project_timeline";

  /**
   * List of timeline entries for the project
   */
  private List<TimelineEntry> mTimelineEntryList;

  /**
   * Project timeline entry list view adapter
   */
  private ProjectTimelineRecyclerViewAdapter mRecyclerViewAdapter;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ProjectTimelineFragment() {}

  public static ProjectTimelineFragment newInstance(List<TimelineEntry> timelineEntries) {
    ProjectTimelineFragment fragment = new ProjectTimelineFragment();
    Bundle args = new Bundle();
    args.putParcelableArrayList(ARG_PROJECT_TIMELINE, (ArrayList<TimelineEntry>) timelineEntries);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      Bundle args = getArguments();
      if (args != null && args.containsKey(ARG_PROJECT_TIMELINE)) {
        mTimelineEntryList = args.getParcelableArrayList(ARG_PROJECT_TIMELINE);
      } else {
        mTimelineEntryList = new ArrayList<>();
      }
      mRecyclerViewAdapter = new ProjectTimelineRecyclerViewAdapter(mTimelineEntryList);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_timeline_project, container, false);
    if (view instanceof RecyclerView) {
      Context context = view.getContext();
      RecyclerView recyclerView = (RecyclerView) view;
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
      recyclerView.setAdapter(mRecyclerViewAdapter);
      recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
    }
    return view;
  }
}
