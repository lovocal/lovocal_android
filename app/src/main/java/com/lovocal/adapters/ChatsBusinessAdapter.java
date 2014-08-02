
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
import com.lovocal.widgets.CircleImageView;
import com.squareup.picasso.Picasso;


/**
 * Adapter for displaying list of all ongoing chats
 */
public class ChatsBusinessAdapter extends CursorAdapter {

    private static final String TAG             = "ChatsBusinessAdapter";

    private final String        mUserNameFormat = "%s %s";

    public ChatsBusinessAdapter(final Context context, final Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public void bindView(final View view, final Context context,
                    final Cursor cursor) {

        ((TextView) view.getTag(R.id.text_user_name))
                        .setText(String.format(mUserNameFormat, cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.NAME)),""));

        ((TextView) view.getTag(R.id.text_chat_message))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.MESSAGE)));

        ((TextView) view.getTag(R.id.text_chat_time))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN)));

        
        CircleImageView circleImageView=(CircleImageView) view.getTag(R.id.image_user);
        
   	 Picasso.with(context)
        .load(R.drawable.ic_launcher)
        .resizeDimen(R.dimen.big_chat_detail_image_size, R.dimen.big_chat_detail_image_size)
        .centerCrop().into(circleImageView.getTarget());
        
        

    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                    final ViewGroup parent) {
        final View view = LayoutInflater.from(context)
                        .inflate(R.layout.layout_chat_item, parent, false);

        view.setTag(R.id.image_user, view.findViewById(R.id.image_user));
        view.setTag(R.id.text_user_name, view.findViewById(R.id.text_user_name));
        view.setTag(R.id.text_chat_message, view
                        .findViewById(R.id.text_chat_message));
        view.setTag(R.id.text_chat_time, view.findViewById(R.id.text_chat_time));
        return view;
    }

}
