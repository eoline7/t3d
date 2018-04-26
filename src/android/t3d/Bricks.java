package android.t3d;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class Bricks {
	Brick[] mModel;
	boolean mHolding = false;
	RectF mOTA = new RectF();
	float mBTSW = 1000;
	float mBTSH = 1000;
	private Paint mPaint = new Paint();
	private Path mPath = new Path();
    private float mRotAlpha = (float)Math.PI / 2;
    private float mRotDelta = (float)Math.PI / 12;
    private float mRotX;
    private float mRotY;
    private float mRotZ;
    private int mRotIndex;

	public Bricks(Brick[] model) {
		mModel = model;
		if (GlobeVar.gSide > 5) Shift(1, 1, 0);
		mPaint.setAntiAlias(true);
	}

	public void Draw(Canvas c) {
		mOTA.top = GlobeVar.gPosiHalf;
		mOTA.left = GlobeVar.gPosiHalf;
		mOTA.right = GlobeVar.gNegaHalf;
		mOTA.bottom = GlobeVar.gNegaHalf;
		for (int i=0; i<mModel.length; i++) {
			Brick b = mModel[i];
			Point3D vs[] = getVertexs(b);
	    	Point ps[] = new Point[vs.length];
	    	for(int j=0;j<ps.length;j++) {
	    		ps[j] = DimTransf(vs[j]);
	    		if(ps[j].X < mOTA.left) mOTA.left=ps[j].X;
	    		if(ps[j].X > mOTA.right) mOTA.right=ps[j].X;
	    		if(ps[j].Y < mOTA.top) mOTA.top=ps[j].Y;
	    		if(ps[j].Y > mOTA.bottom) mOTA.bottom=ps[j].Y;
	    	}
	    	if (i==0) {
	    		mBTSW = Math.abs(ps[4].X - ps[6].X);
	    		mBTSH = Math.abs(ps[4].Y - ps[6].Y);
	    	}
	    	DrawSolid(c, ps, b);
		}
	}

	public boolean isMoving() {
		return (mRotX!=0 || mRotY!=0 || mRotZ!=0);
	}

	// X轴旋转
	public void RotX(int dx) {
		mRotX = Math.signum(dx) * mRotDelta;
	}
	// Y轴旋转
	public void RotY(int dy) {
		mRotY = Math.signum(dy) * mRotDelta;
	}
	// Z轴旋转
	public void RotZ(int dz) {
		mRotZ = Math.signum(dz) * mRotDelta;
	}

	//下一帧
	public boolean NextFrame() {
        boolean inval = false;
        if (mRotX > 0) {
    		mRotX += mRotDelta;
    		if (mRotX >= mRotAlpha) {
    			mRotX = 0;
        		mModel = Rotation(1,0,0);
    		}
    		inval = true;
        } else if (mRotX < 0) {
    		mRotX -= mRotDelta;
    		if (mRotX <= -mRotAlpha) {
    			mRotX = 0;
        		mModel = Rotation(-1,0,0);
    		}
    		inval = true;
        }

        if (mRotY > 0) {
        	mRotY += mRotDelta;
    		if (mRotY >= mRotAlpha) {
    			mRotY = 0;
        		mModel = Rotation(0,1,0);
    		}
    		inval = true;
        } else if (mRotY < 0) {
        	mRotY -= mRotDelta;
    		if (mRotY <= -mRotAlpha) {
    			mRotY = 0;
        		mModel = Rotation(0,-1,0);
    		}
    		inval = true;
        }

        if (mRotZ > 0) {
        	mRotZ += mRotDelta;
    		if (mRotZ >= mRotAlpha) {
    			mRotZ = 0;
        		mModel = Rotation(0,0,1);
    		}
    		inval = true;
        } else if (mRotZ < 0) {
        	mRotZ -= mRotDelta;
    		if (mRotZ <= -mRotAlpha) {
    			mRotZ = 0;
        		mModel = Rotation(0,0,-1);
    		}
    		inval = true;
        }
        return inval;
	}

	//计算旋转90度后的位置
	public Brick[] Rotation(int dx, int dy, int dz) {
		Brick b0 = mModel[mRotIndex];
		Brick[] bs = new Brick[mModel.length];
		for (int i=0; i<bs.length; i++) {
			Brick b = mModel[i];
			int x = b.x - b0.x;
			int y = b.y - b0.y;
			int z = b.z - b0.z;
			if (dx > 0) {
				int ny = z;
				int nz = -y;
				y = ny;
				z = nz;
			} else if (dx < 0) {
				int ny = -z;
				int nz = y;
				y = ny;
				z = nz;
			}
			if (dy > 0) {
				int nz = x;
				int nx = -z;
				z = nz;
				x = nx;
			} else if (dy < 0) {
				int nz = -x;
				int nx = z;
				z = nz;
				x = nx;
			}
			if (dz > 0) {
				int nx = y;
				int ny = -x;
				x = nx;
				y = ny;
			} else if (dz < 0) {
				int nx = -y;
				int ny = x;
				x = nx;
				y = ny;
			}
			x += b0.x;
			y += b0.y;
			z += b0.z;
			bs[i] = new Brick(x,y,z);
		}
		return bs;
	}

	//更换旋转中心
	public void setRotCenter(int i) {
		if (i<0 || i>=mModel.length) i=0;
		mRotIndex = i;
	}

	//自由旋转中心
	Point3D freeRotCenter() {
		int minX = GlobeVar.gSide, maxX = 0;
		int minY = GlobeVar.gSide, maxY = 0;
		int minZ = GlobeVar.gSide, maxZ = 0;
		for (int i=0; i<mModel.length; i++) {
			Brick b = mModel[i];
			if (b.x < minX) minX=b.x;
			if (b.x > maxX) maxX=b.x;
			if (b.y < minY) minY=b.y;
			if (b.y > maxY) maxY=b.y;
			if (b.z < minZ) minZ=b.z;
			if (b.z > maxZ) maxZ=b.z;
		}
		return new Point3D((minX+maxX+1)/2.0f, (minY+maxY+1)/2.0f, (minZ+maxZ+1)/2.0f);
	}

	//平移
	public void Shift(int dx, int dy, int dz) {
		for (int i=0; i<mModel.length; i++) {
			Brick b = mModel[i];
			b.x += dx;
			b.y += dy;
			b.z += dz;
		}
	}

	//计算8个顶点
	static Point3D[] getVertexs(Brick b) {
		Point3D ps[] = new Point3D[8];
		ps[0] = new Point3D(
				b.x * GlobeVar.gBrickSize + GlobeVar.gNegaHalf,
				b.y * GlobeVar.gBrickSize + GlobeVar.gNegaHalf,
				b.z * GlobeVar.gBrickSize);
		ps[1] = new Point3D(ps[0].X + GlobeVar.gBrickSize, ps[0].Y, ps[0].Z);
		ps[2] = new Point3D(ps[1].X, ps[1].Y + GlobeVar.gBrickSize, ps[0].Z);
		ps[3] = new Point3D(ps[0].X, ps[2].Y, ps[0].Z);
		ps[4] = new Point3D(ps[0].X, ps[0].Y, ps[0].Z + GlobeVar.gBrickSize);
		ps[5] = new Point3D(ps[1].X, ps[1].Y, ps[4].Z);
		ps[6] = new Point3D(ps[2].X, ps[2].Y, ps[4].Z);
		ps[7] = new Point3D(ps[3].X, ps[3].Y, ps[4].Z);
		return ps;
	}

	//绘制方块
	private void DrawSolid(Canvas c, Point ps[], Brick b) {
		mPaint.setColor(0x7060e0f0);
        if (CheckNear(b.x,b.y,b.z-1)) DrawPath(c, ps[0], ps[1], ps[2], ps[3]);
		mPaint.setColor(0x8060e0f0);
		if (CheckNear(b.x,b.y-1,b.z)) DrawPath(c, ps[0], ps[4], ps[5], ps[1]);
		if (CheckNear(b.x+1,b.y,b.z)) DrawPath(c, ps[1], ps[5], ps[6], ps[2]);
		if (CheckNear(b.x,b.y+1,b.z)) DrawPath(c, ps[2], ps[6], ps[7], ps[3]);
		if (CheckNear(b.x-1,b.y,b.z)) DrawPath(c, ps[0], ps[3], ps[7], ps[4]);
		if (CheckNear(b.x,b.y,b.z+1)) DrawPath(c, ps[4], ps[7], ps[6], ps[5]);
		
    	if (mHolding)
    		mPaint.setColor(0xfff060e0);
    	else
    		mPaint.setColor(0xff60e0f0);
    	DrawFrame(c, ps);
	}

	//绘制一个面
	private void DrawPath(Canvas c, Point p1, Point p2, Point p3, Point p4) {
    	if (CheckVisible(p1,p2,p3)) {
    		mPath.rewind();
    		mPath.moveTo(p1.X, p1.Y);
    		mPath.lineTo(p2.X, p2.Y);
    		mPath.lineTo(p3.X, p3.Y);
    		mPath.lineTo(p4.X, p4.Y);
    		mPath.close();
    		c.drawPath(mPath, mPaint);
    	}
    }

	//检查遮挡
	private boolean CheckNear(int x, int y, int z) {
		for (int i=0; i<mModel.length; i++) {
			Brick b = mModel[i];
			if (b.x==x && b.y==y && b.z==z)
				return false;
		}
		return true;
	}

    //判断是否可见
    static boolean CheckVisible(Point p1, Point p2, Point p3) {
    	float X = p2.X - p1.X;
    	float Y = p2.Y - p1.Y;
    	float R = (float)Math.sqrt(X * X + Y * Y);
    	float x = p3.X - p1.X;
    	float y = p3.Y - p1.Y;
    	y = y * X / R - x * Y / R;
    	return y > 0;
    }

    //绘制边框
    private void DrawFrame(Canvas c, Point ps[]) {
        c.drawLine(ps[0].X, ps[0].Y, ps[1].X, ps[1].Y, mPaint);
        c.drawLine(ps[1].X, ps[1].Y, ps[2].X, ps[2].Y, mPaint);
        c.drawLine(ps[2].X, ps[2].Y, ps[3].X, ps[3].Y, mPaint);
        c.drawLine(ps[3].X, ps[3].Y, ps[0].X, ps[0].Y, mPaint);

        c.drawLine(ps[4].X, ps[4].Y, ps[5].X, ps[5].Y, mPaint);
        c.drawLine(ps[5].X, ps[5].Y, ps[6].X, ps[6].Y, mPaint);
        c.drawLine(ps[6].X, ps[6].Y, ps[7].X, ps[7].Y, mPaint);
        c.drawLine(ps[7].X, ps[7].Y, ps[4].X, ps[4].Y, mPaint);

        c.drawLine(ps[0].X, ps[0].Y, ps[4].X, ps[4].Y, mPaint);
        c.drawLine(ps[1].X, ps[1].Y, ps[5].X, ps[5].Y, mPaint);
        c.drawLine(ps[2].X, ps[2].Y, ps[6].X, ps[6].Y, mPaint);
        c.drawLine(ps[3].X, ps[3].Y, ps[7].X, ps[7].Y, mPaint);
    }

    //计算投影
    private Point DimTransf(Point3D p) {
    	//移到原点
    	Brick b0 = mModel[mRotIndex];
    	float dx = (b0.x+0.5f) * GlobeVar.gBrickSize + GlobeVar.gNegaHalf;
    	float dy = (b0.y+0.5f) * GlobeVar.gBrickSize + GlobeVar.gNegaHalf;
    	float dz = (b0.z+0.5f) * GlobeVar.gBrickSize;
    	float X = p.X - dx;
    	float Y = p.Y - dy;
    	float Z = p.Z - dz;
    	//自转
    	// X-axis
    	double y = Math.sin(mRotX) * Z + Math.cos(mRotX) * Y;
    	double z = Math.cos(mRotX) * Z - Math.sin(mRotX) * Y;
    	// Y-axis
    	double nz = Math.sin(mRotY) * X + Math.cos(mRotY) * z;
    	double x  = Math.cos(mRotY) * X - Math.sin(mRotY) * z;
    	// Z-axis
    	double nx = Math.sin(mRotZ) * y + Math.cos(mRotZ) * x;
    	double ny = Math.cos(mRotZ) * y - Math.sin(mRotZ) * x;
    	//移回原位
    	x = nx + dx;
    	y = ny + dy;
    	z = nz + dz;

    	return GlobeVar.Projection((float)x, (float)y, (float)z);
    }
}
