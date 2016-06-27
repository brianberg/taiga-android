package co.brianberg.taiga.projects;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import co.brianberg.taiga.R;
import co.brianberg.taiga.db.timeline.TimelineEntry;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TimelineEntry}.
 */
public class ProjectTimelineRecyclerViewAdapter extends
    RecyclerView.Adapter<ProjectTimelineRecyclerViewAdapter.ViewHolder> {

  private final List<TimelineEntry> mValues;

  public ProjectTimelineRecyclerViewAdapter(List<TimelineEntry> items) {
    mValues = items;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.project_timeline_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mDescriptionView.setText(holder.mItem.getDescription());

    SimpleDateFormat format = new SimpleDateFormat("M/dd/yy");
    holder.mDateView.setText(format.format(holder.mItem.getCreatedDate()));

    TimelineEntry.Member user = holder.mItem.getData().getUser();
    if (user != null) {
      holder.mUserNameView.setText(user.getName());
      String userPhotoUrl = user.getPhotoUrl();
      if (userPhotoUrl != null) {
        if (userPhotoUrl.startsWith("//")) {
          userPhotoUrl = "https:" + userPhotoUrl;
        }
        Picasso.with(holder.mView.getContext()).load(userPhotoUrl).into(holder.mPhotoView);
        holder.mPhotoView.setVisibility(View.VISIBLE);
      }
    }
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    public final View mView;
    public final ImageView mPhotoView;
    public final TextView mUserNameView;
    public final TextView mDescriptionView;
    public final TextView mDateView;
    public TimelineEntry mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mPhotoView = (ImageView) view.findViewById(R.id.list_project_timeline_entry_photo);
      mUserNameView = (TextView) view.findViewById(R.id.list_project_timeline_entry_user);
      mDescriptionView = (TextView) view.findViewById(R.id.list_project_timeline_entry_desc);
      mDateView = (TextView) view.findViewById(R.id.list_project_timeline_entry_date);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mDescriptionView.toString() + "'";
    }
  }

}
