
package com.lovocal.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lovocal.R;
import com.lovocal.activities.AbstractLavocalActivity;
import com.lovocal.utils.AppConstants;


/**
 * Adapter for adding items in the Navigation in the home screen
 *
 * @author Anshul Kamboj
 */
public class NavDrawerAdapter extends BaseAdapter {

    private static final String TAG = "NavDrawerAdapter";

    /** View type for primary options */
    private static final int VIEW_PRIMARY = 0;

    /** View type for secondary options */
    private static final int VIEW_SECONDARY = 1;

    /**
     * Navigation Drawer primary options
     */
    private final String[] mNavDrawerPrimaryOptions;

    /**
     * Navigation Drawer secondary options
     */
    private final String[] mNavDrawerSecondaryOptions;

    /**
     * Layout Inflater for inflating layouts
     */
    private final LayoutInflater mLayoutInflater;


    /**
     * Construct the adapter for the Navigation drawer
     *
     * @param context                         {@link android.content.Context} reference
     * @param drawerItemPrimaryOptionsResId   The resource id of an aray that contains the strings
     *                                        of the titles in the nav drawer
     * @param drawerItemSecondaryOptionsResId The resource id of an array that contains the strings
     *                                        of the descriptions of the items in the navigation
     *                                        drawer
     */
    public NavDrawerAdapter(final Context context, final int drawerItemPrimaryOptionsResId, final int drawerItemSecondaryOptionsResId) {
        mLayoutInflater = LayoutInflater.from(context);
        mNavDrawerPrimaryOptions = context.getResources()
                                          .getStringArray(drawerItemPrimaryOptionsResId);
        mNavDrawerSecondaryOptions = context.getResources()
                                            .getStringArray(drawerItemSecondaryOptionsResId);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mNavDrawerPrimaryOptions.length + mNavDrawerSecondaryOptions.length;
    }

    @Override
    public int getItemViewType(final int position) {

        if (position < mNavDrawerPrimaryOptions.length) {
            return VIEW_PRIMARY;
        } else if (position < (mNavDrawerPrimaryOptions.length + mNavDrawerSecondaryOptions.length)) {
            return VIEW_SECONDARY;
        } else {
            throw new IllegalStateException("Invalid position " + position);
        }
    }

    @Override
    public Object getItem(final int position) {
        final int viewType = getItemViewType(position);

        if (viewType == VIEW_PRIMARY) {
            return mNavDrawerPrimaryOptions[position];
        } else {
            return mNavDrawerSecondaryOptions[position - mNavDrawerPrimaryOptions.length];
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {

        final int viewType = getItemViewType(position);
        final String title = (String) getItem(position);

        View view = convertView;

        if (viewType == VIEW_PRIMARY) {

            if (view == null) {
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.layout_nav_drawer_item_primary, parent,
                                              false);
                view.setTag(R.id.text_nav_item_title, view
                        .findViewById(R.id.text_nav_item_title));
            }
            if(position<2&&TextUtils.isEmpty(AppConstants.UserInfo.INSTANCE.getFirstName())){
                ((TextView) view.getTag(R.id.text_nav_item_title)).setVisibility(View.GONE);
            }
            ((TextView) view.getTag(R.id.text_nav_item_title))
                    .setText(title);
        } else if (viewType == VIEW_SECONDARY) {
            if (view == null) {
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.layout_nav_drawer_item_secondary, parent,
                                              false);
                view.setTag(R.id.text_nav_item_title, view
                        .findViewById(R.id.text_nav_item_title));
            }

            ((TextView) view.getTag(R.id.text_nav_item_title))
                    .setText(title);


            if(position<2&&TextUtils.isEmpty(AppConstants.UserInfo.INSTANCE.getFirstName())){
                ((TextView) view.getTag(R.id.text_nav_item_title)).setVisibility(View.GONE);
            }
        }

        return view;
    }


}
