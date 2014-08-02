package com.lovocal.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lovocal.R;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.utils.Logger;
import com.lovocal.widgets.CircleImageView;
import com.squareup.picasso.Picasso;

/**
 * Adapter used to display service list
 * Created by anshul1235 on 17/07/14.
 */

public class ServicesListGridAdapter extends CursorAdapter {

    private static final String TAG          = "ServicesListGridAdapter";


    private int                 mCount;

    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public ServicesListGridAdapter(final Context context) {
        super(context, null, 0);
        mCount = 0;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public void notifyDataSetChanged() {

        if ((mCursor == null) || mCursor.getCount() == 0) {
            mCount = 0;
        } else {
            mCount = mCursor.getCount() ;
        }
        super.notifyDataSetChanged();
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

            if (view == null) {
                view = inflateCategoryView(parent);
            }

            mCursor.moveToPosition(position);
            bindView(view, parent.getContext(), mCursor);

//

        return view;
    }


    /**
     * @param parent
     * @return
     */
    private View inflateCategoryView(ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_service_list, parent, false);

        view.setTag(R.id.servicename, view.findViewById(R.id.servicename));
        view.setTag(R.id.image_services, view.findViewById(R.id.image_services));
        view.setTag(R.id.text_service_description, view.findViewById(R.id.text_service_description));

        return view;
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {

        ((TextView) view.getTag(R.id.servicename))
                .setText(cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.NAME)));

        ((TextView) view.getTag(R.id.text_service_description))
                .setText(cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.DESCRIPTION)));


        CircleImageView circleImageView=(CircleImageView) view.getTag(R.id.image_services);


        if(!cursor.getString(cursor.getColumnIndex(DatabaseColumns.IMAGE)).equals("")) {
            Picasso.with(context)
                    .load(cursor.getString(cursor.getColumnIndex(DatabaseColumns.IMAGE)))
                    .resizeDimen(R.dimen.services_image_width, R.dimen.services_image_height)
                    .centerCrop().into(circleImageView.getTarget());
        }

        else
        {
            Picasso.with(context)
                    .load(R.drawable.image)
                    .resizeDimen(R.dimen.services_image_width, R.dimen.services_image_height)
                    .centerCrop().into(circleImageView.getTarget());
        }


    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {


        return null;
    }

}
