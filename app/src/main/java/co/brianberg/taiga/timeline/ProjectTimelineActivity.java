package co.brianberg.taiga.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.brianberg.taiga.R;
import co.brianberg.taiga.db.timeline.TimelineEntry;
import co.brianberg.taiga.db.timeline.TimelineEntryManager;
import co.brianberg.taiga.projects.ProjectDetailActivity;

public class ProjectTimelineActivity extends AppCompatActivity {

  /**
   * Key to identify the project ID bundle argument
   */
  public static final String ARG_PROJECT_ID = "project_id";

  /**
   * Project ID for which to retrieve timeline entries
   */
  private int mProjectId;

  /**
   * List of project timeline entries
   */
  private List<TimelineEntry> mTimelineEntries;

  /**
   * Timeline entry database manager
   */
  private TimelineEntryManager mTimelineEntryManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_timeline_project);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Show the Up button in the action bar.
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    mTimelineEntryManager = new TimelineEntryManager(this);

    if (savedInstanceState == null) {
      Bundle args = getIntent().getExtras();
      if (args != null && args.containsKey(ARG_PROJECT_ID)) {
        mProjectId = args.getInt(ARG_PROJECT_ID);
        try {
          mTimelineEntryManager.open();
          mTimelineEntries = mTimelineEntryManager.getTimelineEntriesByProject(mProjectId, null);
        } catch (SQLException e) {
          e.printStackTrace();
          mTimelineEntries = new ArrayList<>();
        }
        ProjectTimelineFragment timelineFragment = ProjectTimelineFragment.newInstance(mTimelineEntries);
        getSupportFragmentManager().beginTransaction()
            .add(R.id.project_timeline_container, timelineFragment)
            .commit();
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      Bundle args = new Bundle();
      args.putInt(ProjectDetailActivity.ARG_PROJECT_ID, mProjectId);
      Intent projectDetailIntent = new Intent(this, ProjectDetailActivity.class);
      projectDetailIntent.putExtras(args);
      navigateUpTo(projectDetailIntent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onResume() {
    super.onResume();
    try {
      mTimelineEntryManager.open();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPause() {
    mTimelineEntryManager.close();
    super.onPause();
  }

}
