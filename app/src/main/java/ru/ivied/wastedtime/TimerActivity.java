package ru.ivied.wastedtime;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Date;

import ru.ivied.wastedtime.ui.fragment.TimeLineFragment;
import ru.ivied.wastedtime.ui.fragment.TimerFragment;

public class TimerActivity extends AppCompatActivity {

    private String[] mDrawerActions;

    private Firebase myFirebaseRef;

    private boolean mTimerIsActive;
    private Handler timerHandler = new Handler();
    private TimerRunnable timerRunnable;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private FloatingActionButton mFab;

    public static double HOURLY_RATE = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase(getString(R.string.firebase_url));

        setContentView(R.layout.activity_timer);

        mDrawerActions = getResources().getStringArray(R.array.drawer_actions);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mDrawerActions));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mFab = (FloatingActionButton) findViewById(R.id.fab);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mTimerIsActive) stopTimer();
                else startTimer();
            }
        });

        if(getFragmentManager().findFragmentByTag(TimerFragment.class.getSimpleName()) == null) {

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new TimerFragment(), TimerFragment.class.getSimpleName())
                    .commit();
        }
    }


    private void stopTimer() {

        mTimerIsActive = false;
        timerHandler.removeCallbacks(timerRunnable);

        mFab.setImageResource(android.R.drawable.ic_lock_idle_alarm);
        mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

    }

    private void saveWasteTime(final long startTime, final long increment) {
        myFirebaseRef.child(FirebaseConstants.TIME_LINE).child(DateUtils.getStartOfDay(new Date()).toString()).setValue(System.currentTimeMillis() - startTime + increment);

    }

    private void startTimer() {
        myFirebaseRef.child(FirebaseConstants.TIME_LINE).child(DateUtils.getStartOfDay(new Date()).toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long value = (Long) snapshot.getValue();
                if (value == null) {
                    myFirebaseRef.child(FirebaseConstants.TIME_LINE).child(DateUtils.getStartOfDay(new Date()).toString()).setValue(0);
                    value = 0L;
                }

                mTimerIsActive = true;
                timerRunnable = new TimerRunnable(value);
                timerHandler.postDelayed(timerRunnable, 0);
                mFab.setImageResource(android.R.drawable.ic_media_pause);
                mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            }


            @Override public void onCancelled(FirebaseError error) {
            }
        });
    }


    class TimerRunnable implements Runnable {

        long mIncrement;
        private long mStartTime;

        public TimerRunnable(long increment) {
            mStartTime = System.currentTimeMillis();
            mIncrement = increment;
        }

        @Override
        public void run() {
            saveWasteTime(mStartTime, mIncrement);
            timerHandler.postDelayed(this, 500);
        }
    }

    ;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimerIsActive) saveWasteTime(timerRunnable.mStartTime, timerRunnable.mIncrement);
        timerHandler.removeCallbacks(timerRunnable);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean(FirebaseConstants.TIMER_IS_ACTIVE)) startTimer();
        else stopTimer();
    }


    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTimerIsActive) {
            saveWasteTime(timerRunnable.mStartTime, timerRunnable.mIncrement);
            outState.putBoolean(FirebaseConstants.TIMER_IS_ACTIVE, mTimerIsActive);
        }
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = new TimerFragment();
                    break;
                case 1:
                    fragment = new TimeLineFragment();
                    break;
                default:
                    fragment = new TimerFragment();
                    break;

            }
            Fragment oldFragment = getFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
            if(oldFragment != null) fragment = oldFragment;
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName())
                    .commit();
            mDrawerLayout.closeDrawers();
        }
    }

}
