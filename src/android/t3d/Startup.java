package android.t3d;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Startup extends Activity {
	private FrameLayout mActLayout;
	private Splash mSplash;
	private Basket mBasket;
	private TextView mScore;
	private RelativeLayout mBtnLayout, mPreLayout;
	private TableLayout mTopLayout;
	private int mScreenSize;
	private int mButtonSize;

	public static final int SCREEN_ORIENTATION_SENSOR_LANDSCAPE = 6;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT < 9)
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
        	setRequestedOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        
        setContentView(R.layout.main);
        GlobeVar.initVoice(this);
        TextView tx = (TextView)findViewById(R.id.title);
        tx.setText(R.string.hello);

        Display display = getWindowManager().getDefaultDisplay();
        mScreenSize = display.getHeight();
        mButtonSize = mScreenSize / 10;

        mPreLayout = (RelativeLayout)findViewById(R.id.relativeLayout1);
        mBtnLayout = (RelativeLayout)findViewById(R.id.relativeLayout2);
        mTopLayout = (TableLayout)findViewById(R.id.tableLayout1);
        mBtnLayout.setVisibility(View.INVISIBLE);
        mTopLayout.setVisibility(View.INVISIBLE);

        mScore = (TextView)findViewById(R.id.score);

        //New
        getImgButton(R.id.btnNew).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.Renew();
			}
        });

        //Pause
        getImgButton(R.id.btnPause).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mHandler.sendEmptyMessage(1);
			}
        });

        //Up
        getImgButton(R.id.btnUp).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.Move(0, -1);
			}
        });
        //Down
        getImgButton(R.id.btnDown).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.Move(0, 1);
			}
        });
        //Left
        getImgButton(R.id.btnLeft).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.Move(-1, 0);
			}
        });
        //Right
        getImgButton(R.id.btnRight).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.Move(1, 0);
			}
        });

        //Drop Step
        getImgButton(R.id.btnDropStep).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.Drop(1);
			}
        });
        //Drop Down
        getImgButton(R.id.btnDropDown).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.Drop(GlobeVar.gDeep);
			}
        });

        //X轴 Up
        getImgButton(R.id.btnXUp).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.RotationX(1);
			}
        });
        //X轴 Down
        getImgButton(R.id.btnXDown).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.RotationX(-1);
			}
        });
        //Y轴 Left
        getImgButton(R.id.btnYLeft).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.RotationY(-1);
			}
        });
        //Y轴 Right
        getImgButton(R.id.btnYRight).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.RotationY(1);
			}
        });
        //Z轴 Clock
        getImgButton(R.id.btnZClock).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.RotationZ(-1);
			}
        });
        //Z轴 Anti
        getImgButton(R.id.btnZAnti).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBasket.RotationZ(1);
			}
        });

        //声音
        setVoiceImage();
        ImageView iv = (ImageView)findViewById(R.id.cbVoice);
        iv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Config.setVoice(Startup.this, !GlobeVar.gVoice);
		        setVoiceImage();
			}
        });

        //灯效
        setLightImage();
        iv = (ImageView)findViewById(R.id.cbLight);
        iv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Config.setLight(Startup.this, !GlobeVar.gLight);
				setLightImage();
			}
        });

        //排名
        iv = (ImageView)findViewById(R.id.cbPodium);
        iv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mTopLayout.getVisibility()==View.VISIBLE) {
					mTopLayout.setVisibility(View.INVISIBLE);
				} else {
					loadTopScores();
					mTopLayout.setVisibility(View.VISIBLE);
				}
			}
        });

        mActLayout = (FrameLayout)findViewById(R.id.frameLayout1);
        mSplash = new Splash(this, mHandler);
        mActLayout.addView(mSplash);

        //处理上次未排名的分数
        if (GlobeVar._saveContainer==null) {
        	int score = Config.getScore(this);
        	Config.addScore(this, score);
        	Config.setScore(this, 0);
        }
    }

	private ImageButton getImgButton(int resId) {
		ImageButton btn = (ImageButton)findViewById(resId);
		btn.setMinimumWidth(mButtonSize);
		btn.setMinimumHeight(mButtonSize);
		return btn;
	}

	private void setVoiceImage() {
        GlobeVar.gVoice = Config.getVoice(this);
        final ImageView iv = (ImageView)findViewById(R.id.cbVoice);
		if (GlobeVar.gVoice) {
			iv.setImageResource(R.drawable.voice);
		} else {
			iv.setImageResource(R.drawable.mute);
		}
	}

	private void setLightImage() {
        GlobeVar.gLight = Config.getLight(this);
        final ImageView iv = (ImageView)findViewById(R.id.cbLight);
		if (GlobeVar.gLight) {
			iv.setImageResource(R.drawable.light);
		} else {
			iv.setImageResource(R.drawable.night);
		}
	}

	private void loadTopScores() {
		int[] scores = Config.getScores(this);
		for (int i=0; i<scores.length; i++) {
			if (scores[i] <= 0) break;
			TableRow row = (TableRow)mTopLayout.getChildAt(i+2);
			TextView tv1, tv2;
			if (row==null) {
				row = new TableRow(this);
				tv1 = new TextView(this);
				tv2 = new TextView(this);
				row.setPadding(5, 5, 5, 0);
				tv1.setTextColor(Color.WHITE);
				tv1.setText(Integer.toString(i+1));
				tv2.setTextColor(Color.WHITE);
				tv2.setGravity(Gravity.RIGHT);
				row.addView(tv1);
				row.addView(tv2);
				mTopLayout.addView(row);
			} else {
				tv2 = (TextView)row.getChildAt(1);
			}
			tv2.setText(Integer.toString(scores[i]));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (Playing()) mHandler.sendEmptyMessage(1);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (Playing()) {
				mHandler.sendEmptyMessage(1);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private final int mBid = 101;
	private final int mDelay = 100;

	private boolean Playing() {
		return (mActLayout.findViewById(mBid) != null);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mActLayout.removeView(mSplash);
				mPreLayout.setVisibility(View.INVISIBLE);
				mTopLayout.setVisibility(View.INVISIBLE);
				mBtnLayout.setVisibility(View.VISIBLE);
				if (mBasket==null) {
					mBasket = new Basket(Startup.this, this);
					mBasket.setId(mBid);
					postDelayed(runnable, mDelay);
				}
				mBasket.dida();
				mBasket.light();
				mActLayout.addView(mBasket);
				if (mBasket.mOver) mBasket.Renew();
				break;
			case 1:
				mActLayout.removeView(mBasket);
				mBtnLayout.setVisibility(View.INVISIBLE);
				mPreLayout.setVisibility(View.VISIBLE);
				mActLayout.addView(mSplash);
				break;
			case 2:
				GlobeVar.playVoice(2);
				Toast.makeText(Startup.this, "Game Over", Toast.LENGTH_LONG).show();
				break;
			case 3:
				mScore.setText(Integer.toString(mBasket.mScore));
				break;
			}
		}
	};

	private Runnable runnable = new Runnable() {
	    @Override
	    public void run() {
	    	if (Playing()) mBasket.OnTimer();
	    	mHandler.postDelayed(this, mDelay);
	    }
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBasket!=null) mBasket.Save();
		GlobeVar.releaseVoice();
	}
}
