package co.brianberg.taiga.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import co.brianberg.taiga.db.project.Project;
import co.brianberg.taiga.db.status.task.TaskStatus;
import co.brianberg.taiga.db.status.us.UserStoryStatus;
import co.brianberg.taiga.db.timeline.TimelineEntry;

/**
 * Database helper containing wrapper functions for
 * interacting with the SQLite database
 */
public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "taiga.db";
  private static final int DATABASE_VERSION = 1;

  public static final String TABLE_PROJECT = "project";
  public static final String TABLE_TIMELINE_ENTRY = "timeline_entry";

  // Projects table creation statement
  private static final String CREATE_PROJECTS_TABLE =
      "CREATE TABLE " + TABLE_PROJECT + "(" +
          Project._ID + " INTEGER PRIMARY KEY, " +
          Project.NAME + " TEXT NOT NULL, " +
          Project.DESC + " TEXT, " +
          Project.TAGS + " TEXT, " +
          Project.LOGO_SMALL + " TEXT, " +
          Project.LOGO_BIG + " TEXT, " +
          Project.PRIVATE + " INTEGER NOT NULL" +
          ");";

  private static final String CREATE_TIMELINE_ENTRY_TABLE =
      "CREATE TABLE " + TABLE_TIMELINE_ENTRY + "(" +
          TimelineEntry._ID + " INTEGER PRIMARY KEY, " +
          TimelineEntry.CONTENT_TYPE + " INTEGER NOT NULL, " +
          TimelineEntry.EVENT_TYPE + " TEXT NOT NULL, " +
          TimelineEntry.CREATED_DATE + " TEXT NOT NULL, " + 
          TimelineEntry.DATA + " TEXT, " +
          TimelineEntry.PROJECT + " INTEGER NOT NULL, " +
          "FOREIGN KEY(" + TimelineEntry.PROJECT + ") " + 
            "REFERENCES " + TABLE_PROJECT + "(" + Project._ID + ")" + 
          ")";

  private static DatabaseHelper sInstance;

  public static DatabaseHelper getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new DatabaseHelper(context.getApplicationContext());
    }
    return sInstance;
  }

  private DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_PROJECTS_TABLE);
    db.execSQL(CREATE_TIMELINE_ENTRY_TABLE);
  }


  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(DatabaseHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to " + newVersion +
            ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECT);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMELINE_ENTRY);
    onCreate(db);
  }

}
