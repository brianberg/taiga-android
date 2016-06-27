package co.brianberg.taiga.db.project;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Class represents a Taiga project
 */
public class Project implements BaseColumns, Parcelable {

  // Table column names
  public static final String NAME = "name";
  public static final String DESC = "description";
  public static final String TAGS = "tags";
  public static final String LOGO_SMALL = "logo_small";
  public static final String LOGO_BIG = "logo_big";
  public static final String PRIVATE = "private";

  private int id;

  private String name;

  private String description;

  private List<String> tags;

  @SerializedName("logo_small_url")
  private String logoSmallUrl;

  @SerializedName("logo_big_url")
  private String logoBigUrl;

  @SerializedName("is_private")
  private boolean isPrivate;

  public static final Parcelable.Creator<Project> CREATOR = new Parcelable.Creator<Project>() {
    public Project createFromParcel(Parcel parcel) {
      return new Project(parcel);
    }
    public Project[] newArray(int size) {
      return new Project[size];
    }
  };

  public Project(int id, String name, String description, List<String> tags, String logoSmallUrl, String logoBigUrl,
                 boolean isPrivate) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.tags = tags;
    this.logoSmallUrl = logoSmallUrl;
    this.logoBigUrl = logoBigUrl;
    this.isPrivate = isPrivate;
  }

  private Project(Parcel src) {
    id = src.readInt();
    name = src.readString();
    src.readStringList(tags);
    logoSmallUrl = src.readString();
    logoBigUrl = src.readString();
    isPrivate = src.readInt() == 1;
  }


  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public String getLogoSmallUrl() {
    return logoSmallUrl;
  }

  public String getLogoBigUrl() {
    return logoBigUrl;
  }

  public boolean isPrivate() {
    return isPrivate;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeInt(id);
    parcel.writeString(name);
    parcel.writeStringList(tags);
    parcel.writeString(logoSmallUrl);
    parcel.writeString(logoBigUrl);
    parcel.writeInt(isPrivate ? 1 : 0);
  }
}
