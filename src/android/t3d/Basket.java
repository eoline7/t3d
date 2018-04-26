package android.t3d;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;

public class Basket extends View {
	private Handler mHandler;
	private Paint mPaint = new Paint();
	private Path mPath = new Path();

	static int mMargin = 4;

	boolean mOver;
	int mScore;
	int mVanish;

	private Bitmap mWindBack;
	private Bitmap mNullBack;
	private Bitmap mCurrBack;
    private Bricks mCurModel;
	private byte[][][] mContainer;

	private Bitmap mActWindow;
	private Canvas mActCanvas;

	private Bitmap mNextWind;
	private Bitmap mNextBack;
	private Bitmap mNextLast = null;
	private Canvas mNextCanvas;
    private Bricks mNextModel;
    private Rect mNextSrc;
    private Rect mNextDst;
    
    private static final int mFlyStep = 4;
    private int mNextFly;

    private long mStartTime;
    private long mDelayTime;

    private long mLightTime;
    private long mLightDelay = 10000;
    private long mLightPos;

    public Basket(Context context, Handler handler) {
		super(context);
		mHandler = handler;
        detector.setOnDoubleTapListener(DoubleTapListener);
        mPaint.setAntiAlias(true);
		float scale = getResources().getDisplayMetrics().density;
		mPaint.setTextSize(48 * scale);

        int square = GlobeVar.gSquare - 4 * mMargin;
        
		mNullBack = Bitmap.createBitmap(square, square, Bitmap.Config.ARGB_8888);
		mCurrBack = Bitmap.createBitmap(square, square, Bitmap.Config.ARGB_8888);

		mActWindow = Bitmap.createBitmap(square, square, Bitmap.Config.ARGB_8888);
        mActCanvas = new Canvas(mActWindow);
		mActCanvas.translate(mActWindow.getWidth()/2, mActWindow.getHeight()/2);

		int nsqr = (int)(GlobeVar.gBrickSize*5);
		mNextWind = Bitmap.createBitmap(nsqr, nsqr, Bitmap.Config.ARGB_8888);
		mNextBack = Bitmap.createBitmap(nsqr, nsqr, Bitmap.Config.ARGB_8888);
		mNextCanvas = new Canvas(mNextBack);
		mNextCanvas.translate(mNextBack.getWidth()*4/5, mNextBack.getHeight()*4/5);
		mNextSrc = new Rect(0,0,mNextBack.getWidth(),mNextBack.getHeight());
		mNextDst = new Rect();
		mNextDst.left = (int)GlobeVar.gPosiHalf;
		mNextDst.top = (int)GlobeVar.gNegaHalf + mMargin;
		mNextDst.right = mNextDst.left + GlobeVar.gNextSquare;
		mNextDst.bottom = mNextDst.top + GlobeVar.gNextSquare;

        drawWindow();
        Load();
	}

	void Renew() {
		GlobeVar._saveContainer = null;
		GlobeVar._saveModel = null;
		mContainer = new byte[GlobeVar.gDeep][GlobeVar.gSide][GlobeVar.gSide];
		mCurModel = new Bricks(GlobeVar.getModel(0));
		mNextModel = new Bricks(GlobeVar.getModel(-1));
		mScore = 0;
		mVanish = 0;
		reset();
	}

	private void reset() {
		System.gc();
		mOver = false;
		mNextFly = 0;
		ShowScore();
		mDelayTime = 5000;
		GlobeVar.gModelLevel = 0;
		GlobeVar.gEyeX = 0;
		GlobeVar.gEyeY = 0;
		drawBasket();
		drawBlocks();
		drawNextModel();
		dida();
		invalidate();
	}

	void Save() {
		if (!mOver) {
        	GlobeVar._saveContainer = mContainer;
        	GlobeVar._saveModel = mCurModel;
        	GlobeVar._saveNext = mNextModel;
        	GlobeVar._saveScore = mScore;
        	GlobeVar._saveVanish = mVanish;
			Config.setScore(getContext(), mScore);
		}
	}
	void Load() {
        if (GlobeVar._saveContainer==null) {
        	Renew();
        } else {
        	mContainer = GlobeVar._saveContainer;
        	mCurModel  = GlobeVar._saveModel;
        	mNextModel  = GlobeVar._saveNext;
    		GlobeVar._saveContainer = null;
    		GlobeVar._saveModel = null;
    		mScore = GlobeVar._saveScore;
    		mVanish = GlobeVar._saveVanish;
        	Config.setScore(getContext(), 0);
    		reset();
        }
	}

	void Move(int dx, int dy) {
		if (mOver || mCurModel.isMoving()) return;
		if (CheckMotion(mCurModel.mModel,dx,dy,0)) {
			mCurModel.Shift(dx, dy, 0);
			invalidate();
		}
	}

	boolean MoveXY(int dx, int dy) {
		if (mOver || mCurModel.isMoving()) return false;
		if (dx==0 && dy==0) return false;

		int h = (int)Math.signum(dx);
		int v = (int)Math.signum(dy);
		int x=0, y=0;
		for (int i=0; i<GlobeVar.gSide; i++) {
			if (!CheckMotion(mCurModel.mModel,x+h,y+v,0))
				break;
			if(x!=dx) x+=h;
			if(y!=dy) y+=v;
		}
		if (x!=0 || y!=0) {
			mCurModel.Shift(x, y, 0);
			invalidate();
			return true;
		}
		return false;
	}
	
	private void EyeMove(int dx, int dy) {
		GlobeVar.gEyeX += Math.signum(dy) * 0.1f;
		GlobeVar.gEyeY -= Math.signum(dx) * 0.1f;
		if (GlobeVar.gEyeX > 1.0f) GlobeVar.gEyeX = 1.0f;
		if (GlobeVar.gEyeX < -1.0f) GlobeVar.gEyeX = -1.0f;
		if (GlobeVar.gEyeY > 1.0f) GlobeVar.gEyeY = 1.0f;
		if (GlobeVar.gEyeY < -1.0f) GlobeVar.gEyeY = -1.0f;
		drawBasket();
		drawBlocks();
		invalidate();	
	}

	private boolean CheckMotion(Brick[] bs, int dx, int dy, int dz) {
		for (int i=0; i<bs.length; i++) {
			Brick b = bs[i];
			int x = b.x + dx;
			int y = b.y + dy;
			int z = b.z + dz;
			if (x<0 || x>=GlobeVar.gSide) return false;
			if (y<0 || y>=GlobeVar.gSide) return false;
			if (z<0 || z>=GlobeVar.gDeep) return false;
			if (mContainer[z][y][x]!=0) return false;
		}
		return true;
	}

	void OnTimer() {
		boolean inval = false;
		long cutTime = SystemClock.elapsedRealtime();

		if (GlobeVar.gLight && cutTime - mLightTime >= mLightDelay) {
			mLightPos = 0;
			mLightTime = cutTime;
			inval = true;
		}

		if(!mOver && !mCurModel.isMoving() && cutTime - mStartTime >= mDelayTime) {
			if (CheckMotion(mCurModel.mModel,0,0,1)) {
				mCurModel.Shift(0, 0, 1);
			} else {
				Freeze();
			}
			dida();
			inval = true;
		}

		if (inval) invalidate();
	}

	void dida() {
		mStartTime = SystemClock.elapsedRealtime();
	}

	void light() {
		mLightPos = GlobeVar.gDeep + 1;
		mLightTime = SystemClock.elapsedRealtime();
	}

	void Drop(int n) {
		if(mOver || mCurModel.isMoving()) return;

		if (CheckMotion(mCurModel.mModel,0,0,1)) {
			for(int i=0; i<n; i++) {
				mCurModel.Shift(0, 0, 1);
				if (!CheckMotion(mCurModel.mModel,0,0,1)) break;
			}
		} else {
			Freeze();
		}
		invalidate();
		dida();
	}

	void DropDown() {
		if(mOver || mCurModel.isMoving()) return;

		while(CheckMotion(mCurModel.mModel,0,0,1)) {
			mCurModel.Shift(0, 0, 1);
		}
		Freeze();
		invalidate();
		dida();
	}

	private void Freeze() {
		for (int i=0; i<mCurModel.mModel.length; i++) {
			Brick b = mCurModel.mModel[i];
			mContainer[b.z][b.y][b.x] = 1;
			mScore += GlobeVar.gBonusTick;
		}

		//消层检查
		int ts = 0;
		for (int k=0; k<GlobeVar.gDeep; k++) {
			boolean vanish = true;
			for (int j=0; j<GlobeVar.gSide; j++) {
				for (int i=0; i<GlobeVar.gSide; i++) {
					if (mContainer[k][j][i]==0) {
						vanish = false;
						break;
					}
				}
			}
			if (vanish) {
				for (int n=k; n>0; n--) {
					for (int j=0; j<GlobeVar.gSide; j++) {
						for (int i=0; i<GlobeVar.gSide; i++) {
							mContainer[n][j][i] = mContainer[n-1][j][i];
						}
					}
				}
				ts++;
				mVanish++;
				mScore += GlobeVar.gSide * GlobeVar.gSide * GlobeVar.gBonusTick * ts;
			}
		}
		if(ts > 0 ) {
			//加快速度
			if (mDelayTime>500) mDelayTime=(int)(mDelayTime * 0.928f);
			//扩大造型范围
			if (GlobeVar.gModelLevel<4) GlobeVar.gModelLevel=mVanish/5;
		}
		drawBlocks();

		//结束检查
		for (int j=0; j<GlobeVar.gSide; j++) {
			for (int i=0; i<GlobeVar.gSide; i++) {
				if (mContainer[0][j][i] != 0) {
					mOver = true;
					break;
				}
			}
		}

		ShowScore();
		if(mOver) {
			Config.addScore(getContext(), mScore);
			mHandler.sendEmptyMessage(2);
		} else {
			//播放声音
			GlobeVar.playVoice(ts>0?1:0);
			//新造型
			mCurModel = mNextModel;
			mNextModel = new Bricks(GlobeVar.getModel(-1));
			drawNextModel();
		}
	}

	private void ShowScore() {
		mHandler.sendEmptyMessage(3);
	}

	void RotationX(int dx) {
		if(mOver || mCurModel.isMoving()) return;

		for (int i=0; i<mCurModel.mModel.length; i++) {
			mCurModel.setRotCenter(i);
			Brick[] bs = mCurModel.Rotation(dx,0,0);
			if (CheckMotion(bs,0,0,0)) {
				mCurModel.RotX(dx);
				invalidate();
				return;
			}
		}
	}
	void RotationY(int dy) {
		if(mOver || mCurModel.isMoving()) return;
		
		for (int i=0; i<mCurModel.mModel.length; i++) {
			mCurModel.setRotCenter(i);
			Brick[] bs = mCurModel.Rotation(0,dy,0);
			if (CheckMotion(bs,0,0,0)) {
				mCurModel.RotY(dy);
				invalidate();
				return;
			}
		}
	}
	void RotationZ(int dz) {
		if(mOver || mCurModel.isMoving()) return;
		
		for (int i=0; i<mCurModel.mModel.length; i++) {
			mCurModel.setRotCenter(i);
			Brick[] bs = mCurModel.Rotation(0,0,dz);
			if (CheckMotion(bs,0,0,0)) {
				mCurModel.RotZ(dz);
				invalidate();
				return;
			}
		}
	}

	@Override
    protected void onDraw(Canvas c) {
		boolean inval = false;
		float pos = GlobeVar.gNegaHalf + 2 * mMargin;
		
		//网格
		mActCanvas.drawColor(Color.BLACK);
		mActCanvas.drawBitmap(mNullBack, pos, pos, mPaint);
		//灯效
		if (mLightPos <= GlobeVar.gDeep) {
			mPaint.setColor(Color.WHITE);
			mPaint.setStrokeWidth(3);
			float z = GlobeVar.gBrickSize * mLightPos;
			Point p1 = GlobeVar.Projection(GlobeVar.gNegaHalf, GlobeVar.gNegaHalf, z);
			Point p2 = GlobeVar.Projection(GlobeVar.gPosiHalf, GlobeVar.gNegaHalf, z);
			Point p3 = GlobeVar.Projection(GlobeVar.gPosiHalf, GlobeVar.gPosiHalf, z);
			Point p4 = GlobeVar.Projection(GlobeVar.gNegaHalf, GlobeVar.gPosiHalf, z);
			mActCanvas.drawLine(p1.X, p1.Y, p2.X, p2.Y, mPaint);
			mActCanvas.drawLine(p2.X, p2.Y, p3.X, p3.Y, mPaint);
			mActCanvas.drawLine(p3.X, p3.Y, p4.X, p4.Y, mPaint);
			mActCanvas.drawLine(p4.X, p4.Y, p1.X, p1.Y, mPaint);
			mPaint.setStrokeWidth(1);

			mLightPos++;
			inval = true;
		}
		//砖块
		mActCanvas.drawBitmap(mCurrBack, pos, pos, mPaint);
		if(!mOver && mNextFly==0) {
			mCurModel.Draw(mActCanvas);
			if(mCurModel.NextFrame()) inval = true; 
		}
		
		//显示
		c.translate(getWidth()/2, getHeight()/2);
		c.drawBitmap(mWindBack, GlobeVar.gNegaHalf, GlobeVar.gNegaHalf, mPaint);
		c.drawBitmap(mActWindow, pos, pos, mPaint);
		//预览
		c.drawBitmap(mNextWind, mNextSrc, mNextDst, mPaint);
		if (!mOver && mNextFly>0) {
			mNextFly--;
			int d = mFlyStep - mNextFly;
			Rect r = new Rect(mNextDst);
			r.left -= d*d*GlobeVar.gNextSquare/10;
			r.right -= d*d*GlobeVar.gNextSquare/10;
			mPaint.setAlpha(0xff - d*30);
			c.drawBitmap(mNextLast, mNextSrc, r, mPaint);
			mPaint.setAlpha(0xff);
			inval = true;
		}
		else {
			c.drawBitmap(mNextBack, mNextSrc, mNextDst, mPaint);
		}

		if (inval) invalidate();
	}

	//绘制窗口背景
	private void drawWindow()
	{
		mWindBack = Bitmap.createBitmap(GlobeVar.gSquare, GlobeVar.gSquare, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mWindBack);
		canvas.translate(mWindBack.getWidth()/2, mWindBack.getHeight()/2);
		//外框
		RectF rect = new RectF();
		rect.left = GlobeVar.gNegaHalf + mMargin;
		rect.top = rect.left;
		rect.right = -rect.left;
		rect.bottom = rect.right;
		mPaint.setColor(0xa0808080);
		canvas.drawRoundRect(rect, 5, 5, mPaint);
		//内框
		rect.left += mMargin;
		rect.top += mMargin;
		rect.right -= mMargin;
		rect.bottom -= mMargin;
		mPaint.setColor(Color.BLACK);
		canvas.drawRect(rect, mPaint);
		
		//预览窗
		canvas = new Canvas(mNextWind);
		rect.left = 0;
		rect.top = 0;
		rect.right = mNextWind.getWidth();
		rect.bottom = mNextWind.getHeight();
		mPaint.setColor(0xa0808080);
		canvas.drawRoundRect(rect, 16, 16, mPaint);
		rect.left += mMargin;
		rect.top += mMargin;
		rect.right -= mMargin;
		rect.bottom -= mMargin;
		mPaint.setColor(Color.BLACK);
		canvas.drawRoundRect(rect, 16, 16, mPaint);
		mPaint.setColor(Color.WHITE);
		canvas.drawText(" ◣", rect.left, rect.bottom, mPaint);
	}

	//绘制网格
	private void drawBasket() {
		//准备画布
		Canvas canvas = new Canvas(mNullBack);
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		canvas.translate(mNullBack.getWidth()/2, mNullBack.getHeight()/2);
		//层线
		mPaint.setColor(Color.GRAY);
		float z;
		Point p1,p2,p3,p4;
		for (int i=0; i<GlobeVar.gDeep; i++) {
			z = GlobeVar.gBrickSize * i;
			p1 = GlobeVar.Projection(GlobeVar.gNegaHalf, GlobeVar.gNegaHalf, z);
			p2 = GlobeVar.Projection(GlobeVar.gPosiHalf, GlobeVar.gNegaHalf, z);
			p3 = GlobeVar.Projection(GlobeVar.gPosiHalf, GlobeVar.gPosiHalf, z);
			p4 = GlobeVar.Projection(GlobeVar.gNegaHalf, GlobeVar.gPosiHalf, z);
	        canvas.drawLine(p1.X, p1.Y, p2.X, p2.Y, mPaint);
	        canvas.drawLine(p2.X, p2.Y, p3.X, p3.Y, mPaint);
	        canvas.drawLine(p3.X, p3.Y, p4.X, p4.Y, mPaint);
	        canvas.drawLine(p4.X, p4.Y, p1.X, p1.Y, mPaint);
		}

		z = GlobeVar.gBrickSize * GlobeVar.gDeep;
		p1 = GlobeVar.Projection(GlobeVar.gNegaHalf, GlobeVar.gNegaHalf, z);
		p2 = GlobeVar.Projection(GlobeVar.gPosiHalf, GlobeVar.gNegaHalf, z);
		p3 = GlobeVar.Projection(GlobeVar.gPosiHalf, GlobeVar.gPosiHalf, z);
		p4 = GlobeVar.Projection(GlobeVar.gNegaHalf, GlobeVar.gPosiHalf, z);
		mPath.rewind();
		mPath.moveTo(p1.X, p1.Y);
		mPath.lineTo(p2.X, p2.Y);
		mPath.lineTo(p3.X, p3.Y);
		mPath.lineTo(p4.X, p4.Y);
		mPath.close();
		mPaint.setColor(0xff000050);
		canvas.drawPath(mPath, mPaint);

		mPaint.setColor(Color.GRAY);
		for (int i=0; i<GlobeVar.gSide+1; i++) {
			float d = GlobeVar.gBrickSize * i;
			p1 = GlobeVar.Projection(GlobeVar.gNegaHalf+d, GlobeVar.gNegaHalf, 0);
			p2 = GlobeVar.Projection(GlobeVar.gNegaHalf+d, GlobeVar.gNegaHalf, z);
			p3 = GlobeVar.Projection(GlobeVar.gNegaHalf+d, GlobeVar.gPosiHalf, z);
			p4 = GlobeVar.Projection(GlobeVar.gNegaHalf+d, GlobeVar.gPosiHalf, 0);
	        canvas.drawLine(p1.X, p1.Y, p2.X, p2.Y, mPaint);
	        canvas.drawLine(p2.X, p2.Y, p3.X, p3.Y, mPaint);
	        canvas.drawLine(p3.X, p3.Y, p4.X, p4.Y, mPaint);
			p1 = GlobeVar.Projection(GlobeVar.gNegaHalf, GlobeVar.gNegaHalf+d, 0);
			p2 = GlobeVar.Projection(GlobeVar.gNegaHalf, GlobeVar.gNegaHalf+d, z);
			p3 = GlobeVar.Projection(GlobeVar.gPosiHalf, GlobeVar.gNegaHalf+d, z);
			p4 = GlobeVar.Projection(GlobeVar.gPosiHalf, GlobeVar.gNegaHalf+d, 0);
	        canvas.drawLine(p1.X, p1.Y, p2.X, p2.Y, mPaint);
	        canvas.drawLine(p2.X, p2.Y, p3.X, p3.Y, mPaint);
	        canvas.drawLine(p3.X, p3.Y, p4.X, p4.Y, mPaint);
		}
	}

	static final int[] FloorColors = {
			0xff007f00, 0xff00afaf, 0xff7f7f00, 0xffff00ff, 0xff7f007f,
			0xff007f7f, 0xffff0000, 0xff7fff7f, 0xffffff00, 0xff00008f,
			0xffffff7f, 0xff00ffff, 0xff7f7fef, 0xff4000af, 0xff345678,
			0xffafaf3f, 0xff00afaf, 0xff00af00, 0xffaf0000, 0xff3030cf
	};

	//绘制已经掉下的砖块
	private void drawBlocks() {
		//准备画布
		Canvas canvas = new Canvas(mCurrBack);
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		canvas.translate(mCurrBack.getWidth()/2, mCurrBack.getHeight()/2);
		//绘制砖块
		for (int k=GlobeVar.gDeep-1; k>=0; k--) {
			mPaint.setColor(FloorColors[GlobeVar.gDeep-k-1]);
			for (int j=0; j<GlobeVar.gSide; j++) {
				for (int i=0; i<GlobeVar.gSide; i++) {
					if (mContainer[k][j][i]!=0) {
						Brick b = new Brick(i,j,k);
						Point3D vs[] = Bricks.getVertexs(b);
				    	Point ps[] = new Point[vs.length];
				    	for(int n=0;n<ps.length;n++) {
				    		ps[n] = GlobeVar.Projection(vs[n]);
				    	}
				        DrawPath(canvas, ps[0], ps[1], ps[2], ps[3]);
						DrawPath(canvas, ps[0], ps[4], ps[5], ps[1]);
						DrawPath(canvas, ps[1], ps[5], ps[6], ps[2]);
						DrawPath(canvas, ps[2], ps[6], ps[7], ps[3]);
						DrawPath(canvas, ps[0], ps[3], ps[7], ps[4]);
						//DrawPath(canvas, ps[4], ps[7], ps[6], ps[5]);
					}
				}
			}
		}
	}
	private void DrawPath(Canvas c, Point p1, Point p2, Point p3, Point p4) {
    	if (Bricks.CheckVisible(p1,p2,p3)) {
    		mPath.rewind();
    		mPath.moveTo(p1.X, p1.Y);
    		mPath.lineTo(p2.X, p2.Y);
    		mPath.lineTo(p3.X, p3.Y);
    		mPath.lineTo(p4.X, p4.Y);
    		mPath.close();
    		c.drawPath(mPath, mPaint);
    	}
    }

	//绘制预览目标
	private void drawNextModel() {
		if (mNextLast!=null) mNextLast.recycle();
		mNextLast = Bitmap.createBitmap(mNextBack,0,0,mNextBack.getWidth(),mNextBack.getHeight());

		mNextCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		float ex = GlobeVar.gEyeX;
		float ey = GlobeVar.gEyeY;
		GlobeVar.gEyeX = 0;
		GlobeVar.gEyeY = 0;
		mNextModel.Draw(mNextCanvas);
		GlobeVar.gEyeX = ex;
		GlobeVar.gEyeY = ey;
	}

	//触摸动作
	private GestureDetector gesture = null;
	private boolean behold = false;
	private Point holdpos = new Point();
	private int tolerance = 16;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX() - getWidth()/2;
		float y = event.getY() - getHeight()/2;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//主窗口触摸
			if (x>GlobeVar.gNegaHalf && x<GlobeVar.gPosiHalf) {
				gesture = detector;
				if (!mOver &&
					x>mCurModel.mOTA.left-tolerance && x<mCurModel.mOTA.right+tolerance && 
					y>mCurModel.mOTA.top-tolerance && y<mCurModel.mOTA.bottom+tolerance)
				{
					holdpos.X = event.getX();
					holdpos.Y = event.getY();
					mCurModel.mHolding = true;
					invalidate();
				}
				behold = mCurModel.mHolding;
			}
			//预览窗触摸
			else if (x>mNextDst.left && x<mNextDst.right && y>mNextDst.top && y<mNextDst.bottom) {
				gesture = nextDetector;
			}
			//其它触摸
			else {
				gesture = null;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mCurModel.mHolding) {
				mCurModel.mHolding = false;
				invalidate();
			}
			break;
		}

		if (gesture!=null) {
			gesture.onTouchEvent(event);
			return true;
		}
		return false;
	}

	//主窗口手势
	GestureDetector detector = new GestureDetector(new OnGestureListener() {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (behold) RotationZ(-1);
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			int dx=0,dy=0;
			if (behold) {
				if (mCurModel.mHolding) {
					dx = (int)((e2.getRawX()-holdpos.X) / mCurModel.mBTSW);
					dy = (int)((e2.getRawY()-holdpos.Y) / mCurModel.mBTSH);
					if (MoveXY(dx, dy)) {
						holdpos.X = e2.getRawX();
						holdpos.Y = e2.getRawY();
					}
				}
			}
			else {
				if (distanceX>0) dx=1;
				if (distanceX<0) dx=-1;
				if (distanceY>0) dy=1;
				if (distanceY<0) dy=-1;
				EyeMove(dx, dy);
			}
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return true;
		}
    });

	//双击
	OnDoubleTapListener DoubleTapListener = new GestureDetector.OnDoubleTapListener() {
		@Override
		public boolean onDoubleTap(MotionEvent arg0) {
			if (!behold) {
				GlobeVar.gEyeX = 0;
				GlobeVar.gEyeY = 0;
				EyeMove(0, 0);
			}
			return true;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent arg0) {
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent arg0) {
			return true;
		}
	};
	
	//预览窗手势
	GestureDetector nextDetector = new GestureDetector(new OnGestureListener() {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Drop(GlobeVar.gDeep);
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			mNextFly = mFlyStep;
			DropDown();
			return true;
		}
    });
}
