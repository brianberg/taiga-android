package co.brianberg.taiga.db.timeline;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Represents timeline entry item
 */
public class TimelineEntry implements BaseColumns, Parcelable {

  // Table column names
  public static final String CONTENT_TYPE = "content_type";
  public static final String EVENT_TYPE = "event_type";
  public static final String CREATED_DATE = "created_date";
  public static final String DATA = "data";
  public static final String PROJECT = "project";

  // User story event
  private static final String EVENT_US_CREATE = "userstories.userstory.create";
  private static final String EVENT_US_CHANGE = "userstories.userstory.change";
  private static final String EVENT_US_DELETE = "userstories.userstory.delete";

  // Task event types
  private static final String EVENT_TASK_CREATE = "tasks.task.create";
  private static final String EVENT_TASK_CHANGE = "tasks.task.change";
  private static final String EVENT_TASK_DELETE = "tasks.task.delete";

  // Issue event types
  private static final String EVENT_ISSUE_CREATE = "issues.issue.create";
  private static final String EVENT_ISSUE_CHANGE = "issues.issue.change";
  private static final String EVENT_ISSUE_DELETE = "issues.issue.delete";

  private int id;

  @SerializedName("content_type")
  private int contentType;

  @SerializedName("event_type")
  private String eventType;

  @SerializedName("created")
  private Date createdDate;

  private Data data;

  @SerializedName("project")
  private int projectId;

  public static final Parcelable.Creator<TimelineEntry> CREATOR = new Parcelable.Creator<TimelineEntry>() {
    public TimelineEntry createFromParcel(Parcel parcel) {
      return new TimelineEntry(parcel);
    }
    public TimelineEntry[] newArray(int size) {
      return new TimelineEntry[size];
    }
  };

  public TimelineEntry(int id, int contentType, String eventType, Date createdDate, Data data, int projectId) {
    this.id = id;
    this.contentType = contentType;
    this.eventType = eventType;
    this.createdDate = createdDate;
    this.data = data;
    this.projectId = projectId;
  }

  private TimelineEntry(Parcel parcel) {
    id = parcel.readInt();
    contentType = parcel.readInt();
    createdDate = new Date(parcel.readLong());
    data = new Gson().fromJson(parcel.readString(), Data.class);
    projectId = parcel.readInt();
  }

  public int getId() {
    return id;
  }

  public int getContentType() {
    return contentType;
  }

  public String getEventType() {
    return eventType;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public int getProjectId() {
    return projectId;
  }

  public String getDescription() {
    String desc = "Unable to parse timeline entry details";
    String user = data.getUser().getName();
    Item item;
    switch (eventType) {
      case EVENT_US_CREATE:
        item = data.getUserStory();
        desc = "Created a new User Story #" + item.getRef() + " " + item.getSubject();
        break;
      case EVENT_US_CHANGE:
        item = data.getUserStory();
        desc = "Updated the User Story #" + item.getRef() + " " + item.getSubject();
        break;
      case EVENT_US_DELETE:
        item = data.getUserStory();
        desc = "Deleted the User Story #" + item.getRef() + " " + item.getSubject();
        break;
      case EVENT_TASK_CREATE:
        item = data.getTask();
        desc = "Created a new Task #" + item.getRef() + " " + item.getSubject();
        break;
      case EVENT_TASK_CHANGE:
        item = data.getTask();
        desc = "Updated the Task #" + item.getRef() + " " + item.getSubject();
        break;
      case EVENT_TASK_DELETE:
        item = data.getTask();
        desc = "Deleted the Task #" + item.getRef() + " " + item.getSubject();
      case EVENT_ISSUE_CREATE:
        item = data.getIssue();
        desc = "Created a new Issue #" + item.getRef() + " " + item.getSubject();
        break;
      case EVENT_ISSUE_CHANGE:
        item = data.getIssue();
        desc = "Updated the Issue #" + item.getRef() + " " + item.getSubject();
        break;
      case EVENT_ISSUE_DELETE:
        item = data.getIssue();
        desc = "Deleted the Issue #" + item.getRef() + " " + item.getSubject();
        break;
    }
    return desc;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeInt(id);
    parcel.writeInt(contentType);
    parcel.writeString(eventType);
    parcel.writeLong(createdDate.getTime());
    parcel.writeString(new Gson().toJson(data));
    parcel.writeInt(projectId);
  }

  /**
   * Represents timeline entry data
   */
  public class Data {

    private Member user;

    @SerializedName("userstory")
    private Item userStory;

    private Item task;

    private Item issue;

    public Data() {}
    
    public Member getUser() {
      return user;
    }

    public Item getUserStory() {
      return userStory;
    }

    public Item getTask() {
      return task;
    }

    public Item getIssue() {
      return issue;
    }

  }

  /**
   * Represents member information for timeline entry
   */
  public class Member {

    private int id;
    
    private String name;

    @SerializedName("photo")
    private String photoUrl;

    public Member(int id, String name, String photoUrl) {
      this.id = id;
      this.name = name;
      this.photoUrl = photoUrl;
    }
    
    public int getId() {
      return id;
    }
    
    public String getName() {
      return name;
    }
    
    public String getPhotoUrl() {
      return photoUrl;
    }

  }

  /**
   * Represents timeline entry data item
   */
  public class Item {

    private int id;

    private String subject;

    private int ref;

    public Item(int id, String subject, int ref) {
      this.id = id;
      this.subject = subject;
      this.ref = ref;
    }

    public int getId() {
      return id;
    }

    public String getSubject() {
      return subject;
    }

    public int getRef() {
      return ref;
    }

  }

}
