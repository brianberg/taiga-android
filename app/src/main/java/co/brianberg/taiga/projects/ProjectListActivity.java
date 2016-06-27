package co.brianberg.taiga.projects;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.sql.SQLException;
import java.util.List;

import co.brianberg.taiga.R;
import co.brianberg.taiga.db.project.Project;
import co.brianberg.taiga.db.project.ProjectManager;
import co.brianberg.taiga.service.Taiga;

/**
 * An activity representing a list of Projects. The activity
 * presents a list of items, which when touched, lead to a
 * {@link ProjectDetailActivity} representing item details.
 */
public class ProjectListActivity extends AppCompatActivity
    implements ProjectListFragment.OnProjectListFragmentInteractionListener {

  /**
   * Key to identify the member ID bundle argument
   */
  public static final String ARG_MEMBER_ID = "member_id";

  /**
   * Member ID for which to retrieve projects
   */
  private int mMemberId;

  /**
   * List of projects
   */
  private List<Project> mProjects;

  /**
   * Project database manager
   */
  private ProjectManager mProjectManager;

  /**
   * Keep track of the retrieve projects task to ensure we can cancel it if requested.
   */
  private RetrieveProjectsTask mRetrieveProjectsTask;

  // UI references
  private ProgressBar mProgressBar;
  private FrameLayout mProjectListContainer;
  private ProjectListFragment mProjectListFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_project_list);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setTitle(getTitle());

    if (savedInstanceState == null) {
      Bundle args = getIntent().getExtras();
      if (args != null && args.containsKey(ARG_MEMBER_ID)) {
        mMemberId = args.getInt(ARG_MEMBER_ID);
      }
    }

    mProjectListContainer = (FrameLayout) findViewById(R.id.project_list_container);
    mProgressBar = (ProgressBar) findViewById(R.id.progress_project_list);

    // Get projects
    mProjectManager = new ProjectManager(this);
    try {
      mProjectManager.open();
      mProjects = mProjectManager.getAllProjects();
      if (mProjects.size() > 0) {
        hideProgressBar();
        showListFragment(mProjects);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    mRetrieveProjectsTask = new RetrieveProjectsTask(mMemberId);
    mRetrieveProjectsTask.execute();
  }

  @Override
  public void onResume() {
    super.onResume();
    try {
      mProjectManager.open();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPause() {
    mProjectManager.close();
    super.onPause();
  }

  @Override
  public void onProjectListFragmentInteraction(Project item) {
    Bundle args = new Bundle();
    args.putInt(ProjectDetailActivity.ARG_PROJECT_ID, item.getId());
    Intent projectDetailIntent = new Intent(ProjectListActivity.this, ProjectDetailActivity.class);
    projectDetailIntent.putExtras(args);
    startActivity(projectDetailIntent);
  }

  private void onRetrieveSuccess(List<Project> projects) {
    for (Project p : projects) {
      mProjectManager.upsertProject(p);
    }
    if (mProjectListFragment != null) {
      mProjectListFragment.updateProjects(projects);
    } else {
      showListFragment(projects);
    }
    mProjects = projects;
  }

  private void hideProgressBar() {
    mProgressBar.setVisibility(View.GONE);
    mProjectListContainer.setVisibility(View.VISIBLE);
  }

  private void showListFragment(List<Project> projects) {
    mProjectListFragment = ProjectListFragment.newInstance(projects);
    getSupportFragmentManager().beginTransaction()
        .add(R.id.project_list_container, mProjectListFragment)
        .commit();
  }

  /**
   * Represents an asynchronous login/registration task used to authenticate
   * the user.
   */
  private class RetrieveProjectsTask extends AsyncTask<Void, Void, List<Project>> {

    private final String TAG = RetrieveProjectsTask.class.getSimpleName();

    private final int mMemberId;

    RetrieveProjectsTask(int memberId) {
      mMemberId = memberId;
    }

    @Override
    protected List<Project> doInBackground(Void... params) {
      return Taiga.getProjectList(ProjectListActivity.this.getBaseContext(), mMemberId,
          new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
              Log.d(TAG, error.toString());
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Snackbar.make(mProjectListContainer, R.string.project_list_retrieve_error, Snackbar.LENGTH_LONG);
                }
              });
            }
          }
      );
    }

    @Override
    protected void onPostExecute(final List<Project> projects) {
      mRetrieveProjectsTask = null;
      hideProgressBar();
      if (projects != null) {
        onRetrieveSuccess(projects);
      }
    }

    @Override
    protected void onCancelled() {
      mRetrieveProjectsTask = null;
      hideProgressBar();
    }
  }

}
