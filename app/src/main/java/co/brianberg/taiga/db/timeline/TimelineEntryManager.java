package co.brianberg.taiga.db.timeline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.brianberg.taiga.db.DatabaseHelper;

/**
 * Timeline entry manager
 */
public class TimelineEntryManager {

  // Table columns
  private static final int COLUMN_ID = 0;
  private static final int COLUMN_CONTENT_TYPE = 1;
  private static final int COLUMN_EVENT_TYPE = 2;
  private static final int COLUMN_CREATED_DATE = 3;
  private static final int COLUMN_DATA = 4;
  private static final int COLUMN_PROJECT = 5;

  private SQLiteDatabase database;

  private DatabaseHelper dbHelper;

  private String[] allColumns = {
      TimelineEntry._ID,
      TimelineEntry.CONTENT_TYPE,
      TimelineEntry.EVENT_TYPE,
      TimelineEntry.CREATED_DATE,
      TimelineEntry.DATA,
      TimelineEntry.PROJECT
  };

  private SimpleDateFormat dateFormat;

  public TimelineEntryManager(Context context) {
    dbHelper = DatabaseHelper.getInstance(context);
    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  }


  /**
   * Open the database
   *
   * @throws SQLException
   */
  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  /**
   * Close the database
   */
  public void close() {
    dbHelper.close();
  }


  /**
   * Add timeline entry
   *
   * @param id            ID
   * @param contentType   Content type
   * @param eventType     Event type
   * @param createdDate   Created date
   * @param data          Data
   * @param projectId     Associated project ID
   * @return              Added timeline entry
   */
  public TimelineEntry addTimelineEntry(int id, int contentType, String eventType, Date createdDate,
                                        TimelineEntry.Data data, int projectId) {
    ContentValues values = new ContentValues();
    values.put(TimelineEntry._ID, id);
    values.put(TimelineEntry.CONTENT_TYPE, contentType);
    values.put(TimelineEntry.EVENT_TYPE, eventType);
    values.put(TimelineEntry.CREATED_DATE, dateFormat.format(createdDate));
    values.put(TimelineEntry.DATA, new Gson().toJson(data));
    values.put(TimelineEntry.PROJECT, projectId);
    return createTimelineEntry(values);
  }

  /**
   * Add a timeline entry
   *
   * @param entry     Timeline entry
   * @return          Added timeline entry
   */
  public TimelineEntry addTimelineEntry(TimelineEntry entry) {
    return addTimelineEntry(entry.getId(), entry.getContentType(), entry.getEventType(), entry.getCreatedDate(),
        entry.getData(), entry.getProjectId());
  }


  /**
   * Retrieve a timeline entry from the database
   *
   * @param id    Timeline entry ID
   * @return      Retrieved timeline entry
   */
  public TimelineEntry getTimelineEntry(int id) {
    Cursor cursor = getTimelineEntry(String.valueOf(id));
    cursor.moveToFirst();
    TimelineEntry entry = fromCursor(cursor);
    cursor.close();
    return entry;
  }


  /**
   * Update a timeline entry
   *
   * @param entry   TimelineEntry to be updated
   * @return        True if successful, false if not
   */
  public boolean updateTimelineEntry(TimelineEntry entry) {
    ContentValues values = new ContentValues();
    values.put(TimelineEntry._ID, entry.getId());
    values.put(TimelineEntry.CONTENT_TYPE, entry.getContentType());
    values.put(TimelineEntry.EVENT_TYPE, entry.getEventType());
    values.put(TimelineEntry.CREATED_DATE, dateFormat.format(entry.getCreatedDate()));
    values.put(TimelineEntry.DATA, new Gson().toJson(entry.getData()));
    values.put(TimelineEntry.PROJECT, entry.getProjectId());
    int rowsAffected = updateTimelineEntry(String.valueOf(entry.getId()), values);
    return rowsAffected > 0;
  }

  /**
   * Upsert a timeline entry
   *
   * @param entry   Timeline entry to be updated or inserted
   * @return        True if successful, false if not
   */
  public boolean upsertTimelineEntry(TimelineEntry entry) {
    return updateTimelineEntry(entry) || addTimelineEntry(entry) != null;
  }


  /**
   * Delete a timeline entry
   *
   * @param id  TimelineEntry ID
   * @return    Database rows affected
   */
  public int deleteTimelineEntry(String id) {
    return database.delete(
        DatabaseHelper.TABLE_TIMELINE_ENTRY,
        TimelineEntry._ID + " = ?",
        new String[]{id}
    );
  }


  /**
   * Get all timeline entries ordered by created date
   *
   * @return List of timeline entries
   */
  public List<TimelineEntry> getAllTimelineEntries() {
    return getAllTimelineEntries(COLUMN_CREATED_DATE, false, null);
  }

  /**
   * Get a timeline entries by project ID ordered by created date
   *
   * @param projectId   Timeline entry project ID
   * @param limit       Limit number of rows
   * @return            Retrieved timeline entries
   */
  public List<TimelineEntry> getTimelineEntriesByProject(int projectId, String limit) {
    List<TimelineEntry> entries = new ArrayList<>();
    String sortOrder = "date(" + parseColumn(COLUMN_CREATED_DATE) + ") DESC";
    String selection = parseColumn(COLUMN_PROJECT) + " = " + projectId;
    Cursor cursor = getTimelineEntries(selection, null, sortOrder, limit);
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      TimelineEntry e = fromCursor(cursor);
      entries.add(e);
      cursor.moveToNext();
    }
    cursor.close();
    return entries;
  }


  public List<TimelineEntry> getAllTimelineEntries(int column, boolean asc, String limit) {
    List<TimelineEntry> entries = new ArrayList<>();
    String tableColumn = parseColumn(column);
    String sortOrder;
    if (column == COLUMN_CREATED_DATE) {
      sortOrder = "date(" + tableColumn + ") " + (asc ?  "ASC" :  "DESC");
    } else {
      sortOrder = tableColumn + (asc ? " ASC" : " DESC");
    }
    Cursor cursor = getTimelineEntries(null, null, sortOrder, limit);
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      TimelineEntry e = fromCursor(cursor);
      entries.add(e);
      cursor.moveToNext();
    }
    cursor.close();
    return entries;
  }

  /**
   * Get timeline entries with selection, order, and limit criteria
   *
   * @param selection       Columns to select
   * @param selectionArgs   Selection arguments
   * @param sortOrder       Record sort order
   * @param limit           Limit number of rows
   * @return                Database cursor
   */
  public Cursor getTimelineEntries(String selection, String[] selectionArgs, String sortOrder, String limit) {
    if (selectionArgs != null) {
      selectionArgs[0] = "%" + selectionArgs[0] + "%";
    }
    return database.query(
        DatabaseHelper.TABLE_TIMELINE_ENTRY,
        allColumns,
        selection,
        selectionArgs,
        null,
        null,
        sortOrder,
        limit
    );
  }


  /**
   * Get a timeline entry by ID
   *
   * @param id  Timeline entry ID
   * @return    Database cursor
   */
  public Cursor getTimelineEntry(String id) {
    String[] selectionArgs = new String[] { id };
    return database.query(
        DatabaseHelper.TABLE_TIMELINE_ENTRY,
        allColumns,
        parseColumn(COLUMN_ID) + " = ?",
        selectionArgs,
        null, null, null,
        "1"
    );
  }


  /**
   * Create a timeline entry record in the database
   *
   * @param values  Timeline entry values
   * @return        Created timeline entry
   */
  private TimelineEntry createTimelineEntry(ContentValues values) {
    database.insert(DatabaseHelper.TABLE_TIMELINE_ENTRY, null, values);
    Cursor cursor = database.query(
        DatabaseHelper.TABLE_TIMELINE_ENTRY,
        allColumns,
        TimelineEntry._ID + " = " + values.getAsInteger(TimelineEntry._ID),
        null, null, null, null
    );
    cursor.moveToFirst();
    TimelineEntry newTimelineEntry = fromCursor(cursor);
    cursor.close();
    return newTimelineEntry;
  }

  /**
   * Update a timeline entry
   *
   * @param id      Timeline entry ID
   * @param values  New values
   * @return        Rows affected
   */
  public int updateTimelineEntry(String id, ContentValues values) {
    return database.update(
        DatabaseHelper.TABLE_TIMELINE_ENTRY,
        values,
        TimelineEntry._ID + " = ?",
        new String[] { id }
    );
  }


  /**
   * Covert a database cursor to a timeline entry
   *
   * @param cursor  Database cursor to be converted
   * @return        Converted timeline entry
   */
  public TimelineEntry fromCursor(Cursor cursor) {
    try {
      int id = cursor.getInt(COLUMN_ID);
      int contentType = cursor.getInt(COLUMN_CONTENT_TYPE);
      String eventType = cursor.getString(COLUMN_EVENT_TYPE);
      Date createdDate = dateFormat.parse(cursor.getString(COLUMN_CREATED_DATE));
      TimelineEntry.Data data = new Gson().fromJson(cursor.getString(COLUMN_DATA), TimelineEntry.Data.class);
      int projectId = cursor.getInt(COLUMN_PROJECT);
      return new TimelineEntry(id, contentType, eventType, createdDate, data, projectId);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }


  /**
   * Covert column number into column name
   *
   * @param column  Column number
   * @return        column name
   */
  private String parseColumn(int column) {
    switch(column) {
      case COLUMN_ID:
        return TimelineEntry._ID;
      case COLUMN_CONTENT_TYPE:
        return TimelineEntry.CONTENT_TYPE;
      case COLUMN_EVENT_TYPE:
        return TimelineEntry.EVENT_TYPE;
      case COLUMN_CREATED_DATE:
        return TimelineEntry.CREATED_DATE;
      case COLUMN_DATA:
        return TimelineEntry.DATA;
      case COLUMN_PROJECT:
        return TimelineEntry.PROJECT;
      default:
        return TimelineEntry._ID;
    }
  }

}

