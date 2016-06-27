package co.brianberg.taiga.projects;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import co.brianberg.taiga.R;
import co.brianberg.taiga.db.project.Project;
import co.brianberg.taiga.projects.ProjectListFragment.OnProjectListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Project} and makes a call to the
 * specified {@link OnProjectListFragmentInteractionListener}.
 */
public class ProjectRecyclerViewAdapter extends RecyclerView.Adapter<ProjectRecyclerViewAdapter.ViewHolder> {

  private final List<Project> mValues;
  private final OnProjectListFragmentInteractionListener mListener;

  public ProjectRecyclerViewAdapter(List<Project> items, OnProjectListFragmentInteractionListener listener) {
    mValues = items;
    mListener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.project_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mNameView.setText(holder.mItem.getName());
    holder.mDescriptionView.setText(holder.mItem.getDescription());

    String logoSmallUrl = holder.mItem.getLogoSmallUrl();
    if (logoSmallUrl != null) {
      holder.mLogoView.setVisibility(View.VISIBLE);
      Picasso.with(holder.mView.getContext()).load(logoSmallUrl).into(holder.mLogoView);
    }

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onProjectListFragmentInteraction(holder.mItem);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    public final View mView;
    public final ImageView mLogoView;
    public final TextView mNameView;
    public final TextView mDescriptionView;
    public Project mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mLogoView = (ImageView) view.findViewById(R.id.list_project_logo);
      mNameView = (TextView) view.findViewById(R.id.list_project_name);
      mDescriptionView = (TextView) view.findViewById(R.id.list_project_desc);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mNameView.getText() + ":" + mDescriptionView.toString() + "'";
    }
  }

}
