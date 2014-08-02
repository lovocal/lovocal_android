package com.lovocal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lovocal.R;
import com.lovocal.activities.AuthActivity;

/**
 * Created by anshul1235 on 18/07/14.
 */
public class AskLoginFragment extends AbstractLavocalFragment implements View.OnClickListener{

    private Button          mLogin;
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_asklogin, container, false);

        mLogin=(Button)contentView.findViewById(R.id.buttonlogin);
        mLogin.setOnClickListener(this);

        return contentView;

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }
    public static AskLoginFragment newInstance() {
        AskLoginFragment f = new AskLoginFragment();
        return f;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.buttonlogin)
        {
            final Intent authActivity = new Intent(getActivity(),
        AuthActivity.class);
        startActivity(authActivity);
        }

    }
}
