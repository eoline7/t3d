package android.t3d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

class Splash extends View {
	private Handler handler;
	private Paint paint = new Paint();
	private String text = "µã»÷ÆÁÄ»¿ªÊ¼";
	private int alpha;
	private int dir = 0x10;
	private boolean inited;
	private int mside;

	public Splash(Context context, Handler handler) {
		super(context);
		this.handler = handler;
		paint.setAntiAlias(true);
		float scale = getResources().getDisplayMetrics().density;
 		paint.setTextSize(24 * scale);
		paint.setColor(0xffafffaf);
 		mside = (int)(60*scale);
	}

	private void init() {
		if (!inited) {
			inited = true;
			GlobeVar.Init(getWidth(), getHeight());
		}
	}

	@Override
    protected void onDraw(Canvas c) {
		init();
		paint.setAlpha(alpha);
		alpha += dir;
		if (alpha >= 0xff) {
			dir = -dir;
			alpha = 0xff;
		} else if (alpha < 0x10) {
			dir = -dir;
			alpha = 0x10;
		}

		float x = (getWidth()-paint.measureText(text))/2;
		float y = getHeight() - mside;
		c.drawText(text, x, y, paint);
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getX() > mside && event.getX() < getWidth()-mside)
			handler.sendEmptyMessage(0);
		return false;
	}
}
