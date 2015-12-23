package ru.ivied.wastedtime.ui.fragment;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.Date;

import ru.ivied.wastedtime.DateUtils;
import ru.ivied.wastedtime.FirebaseConstants;
import ru.ivied.wastedtime.R;
import ru.ivied.wastedtime.TimeToMoneyConverter;
import ru.ivied.wastedtime.TimerActivity;

public class TimerFragment extends Fragment implements UpdatableFragment {

    private Firebase myFirebaseRef;

    private ShimmerTextView moneyView;

    private int mDollars;
    private int mCents;

    private MediaPlayer mPennySound;
    private MediaPlayer mDollarSound;
    private MediaPlayer mCoinsSound;
    private ValueEventListener moneyListener;


    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myFirebaseRef = new Firebase(getString(R.string.firebase_url));
        setRetainInstance(true);

        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        moneyView = (ShimmerTextView) view.findViewById(R.id.shimmer_tv);

        mPennySound = MediaPlayer.create(getActivity(), R.raw.penny);
        mDollarSound = MediaPlayer.create(getActivity(), R.raw.dollar);
        mCoinsSound = MediaPlayer.create(getActivity(), R.raw.coins);


        Shimmer shimmer = new Shimmer();
        shimmer.start(moneyView);

        moneyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long millis = (Long) snapshot.getValue();
                if (millis != null) {

                    TimeToMoneyConverter.Money money = TimeToMoneyConverter.convertMillis(millis, TimerActivity.HOURLY_RATE);

                    if (money.getDollars() != mDollars) {
                        mDollarSound.start();
                    } else if (mCents / 10 != money.getCents() / 10) {
                        mCoinsSound.start();
                    } else if (mCents != money.getCents()) {
                        mPennySound.start();
                    }

                    mDollars = money.getDollars();
                    mCents = money.getCents();
                    moneyView.setText(TimeToMoneyConverter.moneyToString(money));

                }
            }

            @Override public void onCancelled(FirebaseError error) {
            }
        };
        myFirebaseRef.child(FirebaseConstants.TIME_LINE).child(DateUtils.getStartOfDay(new Date()).toString()).addValueEventListener(moneyListener);

    }

    @Override public void onPause() {
        super.onPause();
        myFirebaseRef.removeEventListener(moneyListener);
    }

    @Override public void onTimerCount(final long startTime) {



    }


}
