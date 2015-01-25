package io.dwak.reactiveviews.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import io.dwak.reactiveviews.R;
import io.dwak.reactiveviews.viewmodel.FavoriteFoodViewModel;
import io.dwak.reactor.Reactor;
import io.dwak.reactor.ReactorComputation;
import io.dwak.reactor.ReactorComputationFunction;

/**
 * Created by vishnu on 1/25/15.
 */
public class MainView extends RelativeLayout{

    private TextView mTextView;
    private Button mButton;
    private TextView mSliderValue;
    private SeekBar mSeekBar;
    private EditText mEditText;
    private TextView mEditTextDisplay;
    private FavoriteFoodViewModel mViewModel;
    private Context mContext;

    public MainView(Context context) {
        super(context);
        mContext = context;
    }

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTextView = (TextView) findViewById(R.id.text);
        mButton = (Button) findViewById(R.id.button);
        mSliderValue = (TextView) findViewById(R.id.slider_value);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mEditTextDisplay = (TextView) findViewById(R.id.edit_text_display);
    }

    private void bindReactions() {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setEditTextValue(s.toString());
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mViewModel.setFavoritePercentage(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react() {
                mEditTextDisplay.setText(mViewModel.getEditTextValue());
            }
        });
        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react() {
                mTextView.setText(mViewModel.getFavoriteFood());
            }
        });

        final ReactorComputation favoriteFoodPercentageComputation = Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react() {
                mSeekBar.setProgress(mViewModel.getFavoritePercentage());
                mSliderValue.setText("Percent favorite: " + String.valueOf(mViewModel.getFavoritePercentage()));

                if (mViewModel.getFavoritePercentage() == 0) {
                    Toast.makeText(mContext, "You don't like this food at all!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react() {
                mViewModel.setFavoriteFood(mViewModel.isPizza() ? FavoriteFoodViewModel.PIZZA : FavoriteFoodViewModel.MANGOES);
            }
        });

        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.setIsPizza(!mViewModel.isPizza());
            }
        });
    }

    public void setViewModel(FavoriteFoodViewModel viewModel) {
        mViewModel = viewModel;
        bindReactions();
    }
}
