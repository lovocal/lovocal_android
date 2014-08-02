package com.lovocal.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lovocal.R;
import com.lovocal.data.DatabaseColumns;

/**
 * Adapter used to display categorylist
 * Created by anshul1235 on 17/07/14.
 */

public class CategoryListGridAdapter extends CursorAdapter {

    private static final String TAG          = "CategoryListGridAdapter";


    private int                 mCount;

    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public CategoryListGridAdapter(final Context context) {
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


        return view;
    }


    /**
     * @param parent
     * @return
     */
    private View inflateCategoryView(ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_category_list, parent, false);

        view.setTag(R.id.categoryname, view.findViewById(R.id.categoryname));
        return view;
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {

        ((TextView) view.getTag(R.id.categoryname))
                .setText(cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.CATEGORY_NAME)));







    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {

        return null;
    }

}
