package com.lovocal.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lovocal.R;
import com.lovocal.activities.AbstractLavocalActivity;
import com.lovocal.http.Api;
import com.lovocal.utils.AppConstants.Keys;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.lovocal.utils.AppConstants.UserInfo;
import com.lovocal.utils.SharedPreferenceHelper;
import com.squareup.otto.Bus;

/**
 * Created by anshul1235 on 14/07/14.
 */
public abstract class AbstractLavocalFragment extends Fragment implements Callback{

    private static final String TAG = "AbstractLavocalFragment";

    /**
     * Flag that indicates that this fragment is attached to an Activity
     */
    private boolean mIsAttached;

    /**
     * Stores the id for the container view
     */
    protected int mContainerViewId;

    protected RestAdapter mRestAdapter;

    protected Api         mApiService;

    protected Bus         mBus;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


        mRestAdapter=((AbstractLavocalActivity) getActivity())
                .getRestAdapter();

        mApiService=((AbstractLavocalActivity)getActivity()).getApiService();

        mBus=((AbstractLavocalActivity)getActivity()).getBus();
        mIsAttached=true;
    }

    /**
     * A Tag to add to all async tasks. This must be unique for all Fragments types
     *
     * @return An Object that's the tag for this fragment
     */
    protected abstract Object getTaskTag();

    /**
     * Whether this Fragment is currently attached to an Activity
     *
     * @return <code>true</code> if attached, <code>false</code> otherwise
     */
    public boolean isAttached() {
        return mIsAttached;
    }

    /**
     * Call this method in the onCreateView() of any subclasses
     *
     * @param container          The container passed into onCreateView()
     * @param savedInstanceState The Instance state bundle passed into the onCreateView() method
     */
    protected void init(final ViewGroup container,
                        final Bundle savedInstanceState) {
        mContainerViewId = container.getId();

    }

    /**
     * Helper method to load fragments into layout
     *
     * @param containerResId The container resource Id in the content view into which to load the
     *                       fragment
     * @param fragment       The fragment to load
     * @param tag            The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the backstack
     * @param backStackTag   The tag used for the backstack tag
     */
    public void loadFragment(final int containerResId,
                             final AbstractLavocalFragment fragment, final String tag,
                             final boolean addToBackStack, final String backStackTag) {

        if (mIsAttached) {
            ((AbstractLavocalActivity) getActivity())
                    .loadFragment(containerResId, fragment, tag, addToBackStack, backStackTag);
        }

    }

    public void setActionBarDisplayOptions(final int displayOptions) {
        if (mIsAttached) {

            ((AbstractLavocalActivity) getActivity())
                    .setActionBarDisplayOptions(displayOptions);
        }
    }

    /**
     * Pops the fragment from the backstack, checking to see if the bundle args have {@linkplain
     * Keys#UP_NAVIGATION_TAG} which gives the name of the backstack tag to pop to. This is mainly
     * for providing Up navigation
     */
    public void onUpNavigate() {
        final Bundle args = getArguments();

        if ((args != null) && args.containsKey(Keys.UP_NAVIGATION_TAG)) {
            getFragmentManager()
                    .popBackStack(args.getString(Keys.UP_NAVIGATION_TAG),
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            getFragmentManager().popBackStack();
        }
    }

    /**
     * Whether this fragment will handle the particular dialog click or not
     *
     * @param dialog The dialog that was interacted with
     * @return <code>true</code> If the fragment will handle it, <code>false</code> otherwise
     */
    public boolean willHandleDialog(final DialogInterface dialog) {


        return false;
    }

    /**
     * Handle the click for the dialog. The fragment will receive this call, only if {@link
     * #willHandleDialog(DialogInterface)} returns <code>true</code>
     *
     * @param dialog The dialog that was interacted with
     * @param which  The button that was clicked
     */
    public void onDialogClick(final DialogInterface dialog, final int which) {


    }

    /**
     * Is the user logged in
     */
    protected boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getMobileNumber());
    }

    /**
     * for refreshing the user fragment after user login and logout
     */
    protected void userRefresh(boolean flag)
    {
        SharedPreferenceHelper.set(R.string.pref_force_user_refetch, flag);

    }


    /**
     * @author Anshul Kamboj Enum to handle the different types of request methods
     */

    public enum RequestMethod {
        POST,
        GET,
        PUT,
        DELETE
    }
    /**
     * Handles the behaviour for onBackPressed().
     *
     * @return <code>true</code> If the fragment will handle onBackPressed
     */
    public boolean onBackPressed() {

        return false;

    }

    @Override
    public void onStop() {
        super.onStop();
       //TODO Cancel all requests
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContainerViewId = 0;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsAttached = false;
    }

    @Override
    public void success(Object o, Response response) {


    }

    @Override
    public void failure(RetrofitError error) {

    }
}
