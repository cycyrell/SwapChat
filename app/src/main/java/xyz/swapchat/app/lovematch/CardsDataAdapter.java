package xyz.teamcatalyst.breedr.lovematch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.teamcatalyst.breedr.data.Item;
import xyz.teamcatalyst.breedr.GlideApp;
import xyz.teamcatalyst.breedr.R;

public class CardsDataAdapter extends ArrayAdapter<Item> {

    public CardsDataAdapter(Context context, List<Item> items) {
        super(context, R.layout.card_content, items);
    }

    @Override
    public View getView(int position, final View contentView, ViewGroup parent) {
        ViewHolder viewHolder;
        View rowView = contentView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.card_content, parent, false);
            viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        GlideApp.with(parent.getContext()).load(getItem(position).getItemImage()).fitCenter().into(viewHolder.mImage);
        viewHolder.mName.setText(getItem(position).getName());

//        if (Prefs.getBoolean("dislike" + getItem(position).getItemId(), false)) {
//            viewHolder.cardLL.setBackgroundResource(R.color.material_color_blue_grey_300);
//        } else {
//            viewHolder.cardLL.setBackgroundResource(R.color.white);
//        }
        return rowView;
    }

    static class ViewHolder {
        @BindView(R.id.image) ImageView mImage;
        @BindView(R.id.name) TextView mName;
        @BindView(R.id.card_ll) LinearLayout cardLL;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}