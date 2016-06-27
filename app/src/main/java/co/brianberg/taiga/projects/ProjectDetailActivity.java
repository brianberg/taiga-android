package co.brianberg.taiga.projects;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.sql.SQLException;

import co.brianberg.taiga.R;
import co.brianberg.taiga.db.project.Project;
import co.brianberg.taiga.db.project.ProjectManager;
import co.brianberg.taiga.timeline.ProjectTimelineActivity;

/**
 * An activity representing a single Project detail screen.
 */
public class ProjectDetailActivity extends AppCompatActivity
    implements ProjectDetailFragment.OnProjectDetailFragmentInteractionListener {

  /**
   * Key to identify the project ID bundle argument
   */
  public static final String ARG_PROJECT_ID = "project_id";

  /**
   * ID to identify the view timeline event
   */
  public static final int EVENT_VIEW_TIMELINE = 0;

  /**
   * Project reference
   */
  private Project mProject;

  /**
   * Project database manager
   */
  private ProjectManager mProjectManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_project_detail);
    Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
    setSupportActionBar(toolbar);

    // Show the Up button in the action bar.
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    mProjectManager = new ProjectManager(this);

    if (savedInstanceState == null) {
      Bundle args = getIntent().getExtras();
      if (args.containsKey(ARG_PROJECT_ID)) {
        int projectId = args.getInt(ARG_PROJECT_ID);
        try {
          mProjectManager.open();
          mProject = mProjectManager.getProject(projectId);
          ProjectDetailFragment fragment = ProjectDetailFragment.newInstance(mProject);
          getSupportFragmentManager().beginTransaction()
              .add(R.id.project_detail_container, fragment, "fragment_project_detail")
              .commit();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      navigateUpTo(new Intent(this, co.brianberg.taiga.projects.ProjectListActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
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
  public void onProjectDetailFragmentInteraction(int eventId) {
    switch (eventId) {
      case EVENT_VIEW_TIMELINE:
        Bundle args = new Bundle();
        args.putInt(ProjectTimelineActivity.ARG_PROJECT_ID, mProject.getId());
        Intent projectTimelineIntent = new Intent(ProjectDetailActivity.this, ProjectTimelineActivity.class);
        projectTimelineIntent.putExtras(args);
        startActivity(projectTimelineIntent);
    }
  }

  private int getStatusBarHeight() {
    int result = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = getResources().getDimensionPixelSize(resourceId);
    }
    return result;
  }

}
