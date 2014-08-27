
package com.lovocal.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lovocal.R;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.utils.AppConstants;
import com.lovocal.widgets.CircleImageView;
import com.lovocal.widgets.FlipImageView;
import com.squareup.picasso.Picasso;


/**
 * Adapter for displaying list of all ongoing chats
 */
public class ChatsAdapter extends CursorAdapter {

    private static final String TAG = "ChatsAdapter";

    private final String mUserNameFormat = "%s %s";

    public ChatsAdapter(final Context context, final Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {


        if (cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.CHAT_TYPE)).equals(AppConstants.ChatType.SERVICE)) {
            ((LinearLayout) view.getTag(R.id.chat_full_view)).setBackgroundColor(Color.LTGRAY);
        } else {
            ((LinearLayout) view.getTag(R.id.chat_full_view)).
                    setBackgroundColor(context.getResources().getColor(R.color.global_bg));
        }
        ((TextView) view.getTag(R.id.text_user_name))
                .setText(String.format(mUserNameFormat, cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.NAME)), ""));

        ((TextView) view.getTag(R.id.text_chat_message))
                .setText(cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.MESSAGE)));

        ((TextView) view.getTag(R.id.text_chat_time))
                .setText(cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN)));



        CircleImageView circleImageView = (CircleImageView) view.getTag(R.id.image_user);

        // if sender image is empty that means user has sent the chat! but he has not received
        //so we show his image ;)
        if(!cursor.getString(cursor.getColumnIndex(DatabaseColumns.RECEIVER_ID)).equals(AppConstants.UserInfo.INSTANCE.getId())){
            Picasso.with(context)
                    .load(cursor.getString(cursor.getColumnIndex(DatabaseColumns.RECEIVER_IMAGE)))
                    .resizeDimen(R.dimen.big_chat_detail_image_size, R.dimen.big_chat_detail_image_size)
                    .centerCrop().into(circleImageView.getTarget());
        }
        else {
            Picasso.with(context)
                    .load(cursor.getString(cursor.getColumnIndex(DatabaseColumns.SENDER_IMAGE)))
                    .resizeDimen(R.dimen.big_chat_detail_image_size, R.dimen.big_chat_detail_image_size)
                    .centerCrop().into(circleImageView.getTarget());
        }

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
        view.setTag(R.id.chat_full_view, view.findViewById(R.id.chat_full_view));
        //view.setTag(R.id.flip_star,view.findViewWithTag(R.id.flip_star));
        return view;
    }

}
