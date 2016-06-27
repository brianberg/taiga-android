package co.brianberg.taiga.projects;

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
import co.brianberg.taiga.db.project.Project;
import co.brianberg.taiga.ui.SimpleDividerItemDecoration;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnProjectListFragmentInteractionListener}
 * interface.
 */
public class ProjectListFragment extends Fragment {

  /**
   * Key to identify the project list bundle argument
   */
  private static final String ARG_PROJECT_LIST = "project_list";

  /**
   * Fragment interaction listener
   */
  private OnProjectListFragmentInteractionListener mListener;

  /**
   * List of projects to display
   */
  private List<Project> mProjectList;

  /**
   * Project list view adapter
   */
  private ProjectRecyclerViewAdapter mRecyclerViewAdapter;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ProjectListFragment() {}

  public static ProjectListFragment newInstance(List<Project> projects) {
    ProjectListFragment fragment = new ProjectListFragment();
    Bundle args = new Bundle();
    args.putParcelableArrayList(ARG_PROJECT_LIST, (ArrayList<Project>) projects);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      Bundle args = getArguments();
      if (args != null && args.containsKey(ARG_PROJECT_LIST)) {
        mProjectList = args.getParcelableArrayList(ARG_PROJECT_LIST);
      } else {
        mProjectList = new ArrayList<>();
      }
      mRecyclerViewAdapter = new ProjectRecyclerViewAdapter(mProjectList, mListener);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_project_list, container, false);
    if (view instanceof RecyclerView) {
      Context context = view.getContext();
      RecyclerView recyclerView = (RecyclerView) view;
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
      recyclerView.setAdapter(mRecyclerViewAdapter);
      recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
    }
    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnProjectListFragmentInteractionListener) {
      mListener = (OnProjectListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnProjectDetailFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public void updateProjects(List<Project> projects) {
    mProjectList = projects;
    mRecyclerViewAdapter.notifyDataSetChanged();
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   */
  public interface OnProjectListFragmentInteractionListener {
    void onProjectListFragmentInteraction(Project item);
  }

}
