package ru.ivied.wastedtime;

import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

public class TimerActivity extends AppCompatActivity {
    public static final String TIMER_IS_ACTIVE = "timerIsActive";
    private Firebase myFirebaseRef;
    private FloatingActionButton fab;
    private ShimmerTextView moneyView;
    private boolean mTimerIsActive;
    MediaPlayer mPennySound;
    MediaPlayer mDollarSound;
    MediaPlayer mCoinsSound;
    private Handler timerHandler = new Handler();
    private TimerRunnable timerRunnable;
    private long increment;
    private  double  hourlyRate = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://luminous-fire-8474.firebaseio.com/");

        setContentView(R.layout.activity_timer);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPennySound = MediaPlayer.create(this, R.raw.penny);
        mDollarSound = MediaPlayer.create(this, R.raw.dollar);
        mCoinsSound = MediaPlayer.create(this, R.raw.coins);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(mTimerIsActive)      stopTimer();
                else          startTimer();

            }


        });

        Shimmer shimmer = new Shimmer();
        moneyView = (ShimmerTextView) findViewById(R.id.shimmer_tv);
        shimmer.start(moneyView);

    }

    @Override protected void onResume() {
        super.onResume();
        myFirebaseRef.child("time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Long value = (Long) snapshot.getValue();
                if(value != null) {
                    increment = value;
                }

                moneyView.setText(TimeToMoneyConverter.moneyToString(TimeToMoneyConverter.convertMillis(increment, hourlyRate)));
                //Snackbar.make(fab, " = " + increment, Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();


            }

            @Override public void onCancelled(FirebaseError error) {
            }
        });
    }

    private void stopTimer() {

        mTimerIsActive = false;
        timerHandler.removeCallbacks(timerRunnable);

        fab.setImageResource(android.R.drawable.ic_lock_idle_alarm);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
    }

    private void startTimer() {
        myFirebaseRef.child("time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Long value = (Long) snapshot.getValue();
                if (value != null) {
                    increment = value;
                }
                mTimerIsActive = true;
                timerRunnable = new TimerRunnable(increment);
                timerHandler.postDelayed(timerRunnable, 0);
            }

            @Override public void onCancelled(FirebaseError error) {
            }
        });
        fab.setImageResource(android.R.drawable.ic_media_pause);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
    }


    class TimerRunnable implements Runnable {

        public long increment;
        private long mStartTime;
        private int mDollars;
        private int mCents;

        public TimerRunnable(long increment) {
            this.increment = increment;
            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void run() {

            long millis = System.currentTimeMillis() - mStartTime + increment;
            TimeToMoneyConverter.Money money = TimeToMoneyConverter.convertMillis(millis, hourlyRate);
            if(money.getDollars() != mDollars){
                mDollarSound.start();
            }else if ( mCents/10 != money.getCents()/10){
                mCoinsSound.start();
            }else if (mCents != money.getCents()) {
                mPennySound.start();
            }
            mDollars = money.getDollars();
            mCents = money.getCents();
            moneyView.setText(TimeToMoneyConverter.moneyToString(money));
            myFirebaseRef.child("time").setValue(millis);


            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getBoolean(TIMER_IS_ACTIVE))      startTimer();
        else          stopTimer();
    }


    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TIMER_IS_ACTIVE, mTimerIsActive);
    }


}
