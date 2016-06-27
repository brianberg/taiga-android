package co.brianberg.taiga.db.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.brianberg.taiga.db.DatabaseHelper;

/**
 * Project manager class responsible for storing and retrieving projects
 */
public class ProjectManager {

  // Table columns
  private static final int COLUMN_ID = 0;
  private static final int COLUMN_NAME = 1;
  private static final int COLUMN_DESC = 2;
  private static final int COLUMN_TAGS = 3;
  private static final int COLUMN_LOGO_SMALL = 4;
  private static final int COLUMN_LOGO_BIG = 5;
  private static final int COLUMN_PRIVATE = 6;

  private SQLiteDatabase database;

  private DatabaseHelper dbHelper;

  private String[] allColumns = {
      Project._ID,
      Project.NAME,
      Project.DESC,
      Project.TAGS,
      Project.LOGO_SMALL,
      Project.LOGO_BIG,
      Project.PRIVATE
  };

  public ProjectManager(Context context) {
    dbHelper = DatabaseHelper.getInstance(context);
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
   * Add a project
   *
   * @param id            ID
   * @param name          Name
   * @param description   Description
   * @param tags          List of project tags
   * @param logoSmall     Small logo URL
   * @param isPrivate     Is the project private
   * @return              Added project
   */
  public Project addProject(int id, String name, String description, List<String> tags, String logoSmall,
                            String logoBig, boolean isPrivate) {
    ContentValues values = new ContentValues();
    values.put(Project._ID, id);
    values.put(Project.NAME, name);
    values.put(Project.DESC, description);
    values.put(Project.TAGS, new Gson().toJson(tags));
    values.put(Project.LOGO_SMALL, logoSmall);
    values.put(Project.LOGO_BIG, logoBig);
    values.put(Project.PRIVATE, isPrivate ? 1 : 0);
    return createProject(values);
  }

  /**
   * Add a project
   *
   * @param project   Project
   * @return          Added project
   */
  public Project addProject(Project project) {
    return addProject(project.getId(), project.getName(), project.getDescription(), project.getTags(),
        project.getLogoSmallUrl(), project.getLogoBigUrl(), project.isPrivate());
  }

  /**
   * Retrieve a project from the database
   *
   * @param id    Project ID
   * @return      Retrieved project
   */
  public Project getProject(int id) {
    Cursor cursor = getProject(String.valueOf(id));
    cursor.moveToFirst();
    Project project = fromCursor(cursor);
    cursor.close();
    return project;
  }

  /**
   * Update a project
   *
   * @param project   Project to be updated
   * @return          True if successful, false if not
   */
  public boolean updateProject(Project project) {
    ContentValues values = new ContentValues();
    values.put(Project.NAME, project.getName());
    values.put(Project.DESC, project.getDescription());
    values.put(Project.TAGS, new Gson().toJson(project.getTags()));
    values.put(Project.LOGO_SMALL, project.getLogoSmallUrl());
    values.put(Project.LOGO_BIG, project.getLogoBigUrl());
    values.put(Project.PRIVATE, project.isPrivate() ? 1 : 0);
    int rowsAffected = updateProject(String.valueOf(project.getId()), values);
    return rowsAffected > 0;
  }

  /**
   * Upsert a project
   *
   * @param project   Project to be updated or inserted
   * @return          True if successful, false if not
   */
  public boolean upsertProject(Project project) {
    return updateProject(project) || addProject(project) != null;
  }

  /**
   * Delete a project
   *
   * @param id  Project ID
   * @return    Database rows affected
   */
  public int deleteProject(String id) {
    return database.delete(
        DatabaseHelper.TABLE_PROJECT,
        Project._ID + " = ?",
        new String[]{id}
    );
  }

  /**
   * Get all projects ordered by name
   *
   * @return List of projects
   */
  public List<Project> getAllProjects() {
    return getAllProjects(COLUMN_NAME, false);
  }

  /**
   * Get all projects ordered by a particular column
   *
   * @param column  Column number
   * @param asc     True if columns should be sorted in ascending order
   * @return        List of projects
   */
  public List<Project> getAllProjects(int column, boolean asc) {
    List<Project> projects = new ArrayList<>();
    String tableColumn = parseColumn(column);
    String sortOrder = asc ? tableColumn + " ASC" : tableColumn + " DESC";
    Cursor cursor = getProjects(null, null, sortOrder);
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Project p = fromCursor(cursor);
      projects.add(p);
      cursor.moveToNext();
    }
    cursor.close();
    return projects;
  }

  /**
   * Get a project by name
   *
   * @param name   Project Name
   * @return       Retrieved project
   */
  public Project getProjectByName(String name) {
    Cursor cursor = database.query(
        DatabaseHelper.TABLE_PROJECT,
        allColumns,
        Project.NAME + " = ?",
        new String[] { name },
        null,
        null,
        "1"
    );
    cursor.moveToFirst();
    return fromCursor(cursor);
  }

  /**
   * Search projects by name
   *
   * @param selectionArgs   Project name
   * @return                Database cursor
   */
  public Cursor getProjects(String[] selectionArgs) {
    String selection = COLUMN_NAME + " LIKE ?";
    String sortOrder = COLUMN_NAME + " ASC";
    return getProjects(selection, selectionArgs, sortOrder);
  }

  /**
   * Get projects with selection and order criteria
   *
   * @param selection       Columns to select
   * @param selectionArgs   Selection arguments
   * @param sortOrder       Record sort order
   * @return                Database cursor
   */
  public Cursor getProjects(String selection, String[] selectionArgs, String sortOrder) {
    if (selectionArgs != null) {
      selectionArgs[0] = "%" + selectionArgs[0] + "%";
    }
    return database.query(
        DatabaseHelper.TABLE_PROJECT,
        allColumns,
        selection,
        selectionArgs,
        null,
        null,
        sortOrder
    );
  }

  /**
   * Get a project by ID
   *
   * @param id  Project ID
   * @return    Database cursor
   */
  public Cursor getProject(String id) {
    String[] selectionArgs = new String[] { id };
    return database.query(
        DatabaseHelper.TABLE_PROJECT,
        allColumns,
        Project._ID + " = ?",
        selectionArgs,
        null, null, null,
        "1"
    );
  }

  /**
   * Create a project record in the database
   *
   * @param values  Project values
   * @return        Created project
   */
  private Project createProject(ContentValues values) {
    database.insert(DatabaseHelper.TABLE_PROJECT, null, values);
    Cursor cursor = database.query(
        DatabaseHelper.TABLE_PROJECT,
        allColumns,
        Project._ID + " = " + values.getAsInteger(Project._ID),
        null, null, null, null
    );
    cursor.moveToFirst();
    Project newProject = fromCursor(cursor);
    cursor.close();
    return newProject;
  }

  /**
   * Update a project
   *
   * @param id      Project ID
   * @param values  New project values
   * @return        Rows affected
   */
  public int updateProject(String id, ContentValues values) {
    return database.update(
        DatabaseHelper.TABLE_PROJECT,
        values,
        Project._ID + " = ?",
        new String[] { id }
    );
  }

  /**
   * Covert a database cursor to a project
   *
   * @param cursor  Database cursor to be converted
   * @return        Converted project
   */
  public Project fromCursor(Cursor cursor) {
    int id = cursor.getInt(COLUMN_ID);
    String name = cursor.getString(COLUMN_NAME);
    String description = cursor.getString(COLUMN_DESC);
    boolean isPrivate = cursor.getInt(COLUMN_PRIVATE) == 1;

    List<String> tags = null;
    String[] tagArray = new Gson().fromJson(cursor.getString(COLUMN_TAGS), String[].class);
    if (tagArray != null && tagArray.length > 0) {
      tags = new ArrayList<>(Arrays.asList(tagArray));
    }

    String logoSmallUrl = cursor.getString(COLUMN_LOGO_SMALL);
    String logoBigUrl = cursor.getString(COLUMN_LOGO_BIG);

    return new Project(id, name, description, tags, logoSmallUrl, logoBigUrl, isPrivate);
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
        return Project._ID;
      case COLUMN_NAME:
        return Project.NAME;
      case COLUMN_DESC:
        return Project.DESC;
      case COLUMN_TAGS:
        return Project.TAGS;
      case COLUMN_LOGO_SMALL:
        return Project.LOGO_SMALL;
      case COLUMN_LOGO_BIG:
        return Project.LOGO_BIG;
      case COLUMN_PRIVATE:
        return Project.PRIVATE;
      default:
        return Project._ID;
    }
  }

}
