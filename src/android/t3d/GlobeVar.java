package android.t3d;

import android.content.Context;
import android.media.MediaPlayer;

class Point {
	public float X,Y;
	public Point() {}
	public Point(float x,float y) {
		X = x;
		Y = y;
	}
}

class Point3D extends Point {
	public float Z;
	public Point3D() {
		super();
	}
	public Point3D(float x,float y,float z) {
		super(x,y);
		Z = z;
	}
}

class Brick {
	public int x,y,z;
	public Brick() {}
	public Brick(int i,int j,int k) {
		x = i;
		y = j;
		z = k;
	}
}

/* ����ϵͳ
 * ԭ��λ����Ļ����
 * X�����ҡ�Y�����¡�Z������
 */

class GlobeVar {
	static int gSide = 8;		//��
	static int gDeep = 10;		//��
	static int gScrPos = -40;	//ͶӰλ��
	static int gEyePos = -300;	//����λ��
	static int gSquare;			//�����δ�С
	static float gNegaHalf;		//���Ͻ�λ��
	static float gPosiHalf;		//���½�λ��
	static float gBrickSize;	//ש���С
	static float gEyeX;			//�ӽ�X
	static float gEyeY;			//�ӽ�Y
	static int gModelLevel;
	static int gBonusTick = 10;
	static boolean gVoice = true;//��������
	static boolean gLight = true;//�ƹ���Ч
	static int gNextSquare;		//Ԥ������С

	//���ֽ���
	static byte[][][] _saveContainer;
	static Bricks _saveModel;
	static Bricks _saveNext;
	static int _saveScore;
	static int _saveVanish;

	//��ʼ��
	static void Init(int width, int height) {
		gSquare = height;
		gPosiHalf = gSquare / 2.0f;
		gNegaHalf = - gPosiHalf;
		gBrickSize = (float)gSquare / gSide;
		gScrPos = -(int)gBrickSize;
		gEyePos = gScrPos * 7;
		gNextSquare = (width - height) / 2;
	}

	//���� (���Ͻ����ϲ�Ϊ0,0,0)
	private static final Brick[][] model1 = {{new Brick(0,0,0)}};
	private static final Brick[][] model2 = {{new Brick(0,0,0),new Brick(1,0,0)}};
	private static final Brick[][] model3 = {
		{new Brick(1,0,0),new Brick(0,0,0),new Brick(2,0,0)},	//-
		{new Brick(0,0,0),new Brick(1,0,0),new Brick(0,1,0)}	//7
	};
	private static final Brick[][] model4 = {
		{new Brick(1,0,0),new Brick(0,0,0),new Brick(2,0,0),new Brick(0,1,0)},	//L
		{new Brick(1,0,0),new Brick(0,0,0),new Brick(1,1,0),new Brick(2,1,0)},	//z
		{new Brick(1,1,0),new Brick(1,0,0),new Brick(0,1,0),new Brick(2,1,0)},	//t
		{new Brick(1,0,0),new Brick(0,0,0),new Brick(2,0,0),new Brick(3,0,0)},	//--
		{new Brick(0,0,0),new Brick(1,0,0),new Brick(0,1,0),new Brick(1,1,0)},	//��
		{new Brick(0,0,1),new Brick(0,0,0),new Brick(1,0,1),new Brick(0,1,1)},	//Y
		{new Brick(0,0,1),new Brick(0,1,0),new Brick(1,0,1),new Brick(0,1,1)},	//s
		{new Brick(0,0,1),new Brick(1,0,0),new Brick(1,0,1),new Brick(0,1,1)}	//5
	};
	private static final Brick[][] model5 = {
		{new Brick(1,0,0),new Brick(0,0,0),new Brick(2,0,0),new Brick(0,1,0),new Brick(2,1,0)},	//n
		{new Brick(1,1,0),new Brick(1,0,0),new Brick(1,2,0),new Brick(0,1,0),new Brick(2,1,0)},	//+
		{new Brick(1,1,0),new Brick(1,0,0),new Brick(1,2,0),new Brick(2,0,0),new Brick(0,2,0)},	//Z
		{new Brick(1,1,0),new Brick(1,0,0),new Brick(0,1,0),new Brick(2,0,0),new Brick(0,2,0)},	//W
		{new Brick(1,0,1),new Brick(1,1,1),new Brick(1,0,0),new Brick(0,0,0),new Brick(2,1,1)},	//S
		{new Brick(1,0,1),new Brick(0,0,1),new Brick(1,0,0),new Brick(2,0,1),new Brick(1,1,1)},	//T
	};
	private static final Brick[][] model6 = {
		{new Brick(1,1,0),new Brick(0,1,0),new Brick(2,1,0),
		 new Brick(1,0,0),new Brick(0,2,0),new Brick(2,2,0)},	//A
		{new Brick(1,1,1),new Brick(1,1,0),new Brick(1,0,1),
		 new Brick(1,2,1),new Brick(0,1,0),new Brick(2,1,0)},	//ʮ
	};
	private static final Brick[][] model7 = {
		{new Brick(1,1,0),new Brick(1,0,0),new Brick(1,2,0),
		 new Brick(0,0,0),new Brick(2,0,0),new Brick(0,2,0),new Brick(2,2,0)},	//H
		{new Brick(1,2,0),new Brick(0,2,0),new Brick(2,2,0),
		 new Brick(0,1,0),new Brick(2,1,0),new Brick(0,0,0),new Brick(2,0,0)},	//U
	};
	private static final Brick[][] model8 = {
		{new Brick(0,0,0),new Brick(1,0,0),new Brick(0,1,0),new Brick(1,1,0),
		 new Brick(0,0,1),new Brick(1,0,1),new Brick(0,1,1),new Brick(1,1,1)},	//��
		{new Brick(0,0,0),new Brick(1,0,0),new Brick(2,0,0),new Brick(2,1,0),
		 new Brick(2,2,0),new Brick(1,2,0),new Brick(0,2,0),new Brick(0,1,0)},	//��
	};
	private static final Brick[][] model99 = {
		{new Brick(1,2,0),new Brick(1,1,0),new Brick(1,3,0),new Brick(1,0,0),
		 new Brick(1,4,0),new Brick(0,0,0),new Brick(2,0,0),new Brick(0,2,0),
		 new Brick(2,2,0),new Brick(0,4,0),new Brick(2,4,0)}
	};

	private static final Brick[][] models[] = {
		model4, model1, model2, model3, model5, model6, model7, model8, model99
	}; 
	
	//ȡ����
	static Brick[] getModel(int id) {
		//���
		if (id < 0) {
			int ms1 = model4.length;
			int ms2 = 0;
			int ms3 = 0;
			switch (gModelLevel) {
			case 4:
				ms3 += 1;
			case 3:
				ms2 += model6.length + model7.length + model8.length;
			case 2:
				ms2 += model5.length;
			case 1:
				ms1 += model1.length + model2.length + model3.length;
			default:
				if (ms3 > 0) {
					if (Math.random() > 0.99) {
						id = ms1 + ms2;
					}
				}
				if (id < 0 && ms2 > 0) {
					if (Math.random() > 0.8) {
						id = ms1 + (int)(Math.random() * ms2);
					}
				}
				if (id < 0) {
					id = (int)(Math.random() * ms1);
				}
			}
		}
		//ȡ��
		Brick[] bs = null;
		for (int i=0; i<models.length; i++) {
			if (id < models[i].length) {
				bs = models[i][id];
				break;
			}
			id -= models[i].length;
		}
		//����
		Brick[] nb = new Brick[bs.length];
		for (int i=0;i<bs.length;i++) {
			Brick b = bs[i];
			nb[i] = new Brick(b.x,b.y,b.z);
		}
		return nb;
	}

	//ͶӰ
    static Point Projection(Point3D p) {
    	return Projection(p.X,p.Y,p.Z);
    }
    static Point Projection(float x,float y,float z) {
    	if (gEyeX==0 && gEyeY==0) {
    		return ProjectionS(x,y,z);
    	} else {
    		return ProjectionT(x,y,z);
    	}
    }

    static Point ProjectionS(float x,float y,float z) {
        float d = (gScrPos-gEyePos)/(z-gEyePos);
    	return new Point(x*d, y*d);
    }

    static Point ProjectionT(float x,float y,float z) {
        // rotation around X-axis
        double ny = Math.sin(gEyeX) * z + Math.cos(gEyeX) * y;
        double nz = Math.cos(gEyeX) * z - Math.sin(gEyeX) * y;
        // rotation around Y-axis
        double nx  = Math.cos(gEyeY) * x - Math.sin(gEyeY) * nz;
        nz = Math.sin(gEyeY) * x + Math.cos(gEyeY) * nz;
        // 3D-to-2D projection
        //     y2-y1     y1x2-y2x1       X-x1
        // Y = ----- X + --------- = y2 ------
        //     x2-x1       x2-x1        x2-x1
        double d = (gScrPos-gEyePos)/(nz-gEyePos);
    	return new Point((float)(nx*d), (float)(ny*d));
    }

    private static MediaPlayer[] mPlayer = new MediaPlayer[3];

    static void initVoice(Context context) {
		mPlayer[0] = MediaPlayer.create(context, R.raw.kada);
		mPlayer[1] = MediaPlayer.create(context, R.raw.vanish);
		mPlayer[2] = MediaPlayer.create(context, R.raw.over);
	}
    static void releaseVoice() {
		for (int i=0; i<mPlayer.length; i++) {
			mPlayer[i].release();
			mPlayer[i] = null;
		}
	}
    static void playVoice(int i) {
		if (gVoice) mPlayer[i].start();
	}
}
