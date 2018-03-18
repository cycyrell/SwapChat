package xyz.teamcatalyst.breedr.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.data.UserFeedback;

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 07/08/2017
 */
public class FeedbackItem extends AbstractItem<FeedbackItem, FeedbackItem.ViewHolder> {

    public final UserFeedback userFeedback;

    public FeedbackItem(UserFeedback userFeedback) {
        this.userFeedback = userFeedback;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_image_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_feedback;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        //get the context
        Context ctx = viewHolder.itemView.getContext();

        //define our data for the view
        viewHolder.mName.setText(userFeedback.getUserName());
        viewHolder.mComment.setText(userFeedback.getComment());
        viewHolder.ratingBar.setRating(userFeedback.getRating().floatValue());
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private final View view;
        @BindView(R.id.display_picture)
        ImageView mDisplayPicture;
        @BindView(R.id.comment)
        TextView mComment;
        @BindView(R.id.name)
        TextView mName;
        @BindView(R.id.rating)
        RatingBar ratingBar;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }
}
