package com.example.easyshop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.List;


import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.example.assist.CelllistAdapter;
import com.example.assist.GoodslistAdapter;
import com.example.customview.ListViewForScrollView;
import com.example.entity.CellInfo;
import com.example.entity.Goods;
import com.example.entity.MyUser;
import com.example.manager.GoodsManager;
import com.example.singleton.GoodsSingleton;
import com.example.singleton.UserSingleton;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class Home extends Activity implements OnPageChangeListener,OnClickListener, OnRefreshListener{

	private ImageButton IbHome_cell,IbHome_class,IbHome_add,IbHome_message,IbHome_mine;
	private ScrollView SvHome;
	private SwipeRefreshLayout mSwipeLayout; 
	private ListViewForScrollView LvHome_goods;
	private ViewPager VpHome_hotgoods = null; 
	private LinearLayout viewGroup = null;
	private String pathName[] = new String[5];
	private ImageView[] mImageViews = null;
	private ImageView[] mTips = null;
	private int index = 0; 
	MyUser user = UserSingleton.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        Bmob.initialize(this, "79f7c1d79f0db04370bf7b20720440db");
        SvHome = (ScrollView) findViewById(R.id.SvHome);
        IbHome_cell =(ImageButton) findViewById(R.id.IbHome_cell);
        IbHome_class =(ImageButton) findViewById(R.id.IbHome_class);
	    viewGroup = (LinearLayout) findViewById(R.id.viewGroup);
	    VpHome_hotgoods = (ViewPager) findViewById(R.id.VpHome_hotgoods);
	    LvHome_goods = (ListViewForScrollView)findViewById(R.id.LvHome_goods);
	    IbHome_add =(ImageButton) findViewById(R.id.IbHome_add);
	    IbHome_message =(ImageButton) findViewById(R.id.IbHome_message);
	    IbHome_mine =(ImageButton) findViewById(R.id.IbHome_mine);
	    mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_ly); 
	    mSwipeLayout.setOnRefreshListener(this);  
        mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,  
                android.R.color.holo_orange_light, android.R.color.holo_red_light); 
   
        SvHome.smoothScrollTo(0, 0);
        IbHome_cell.setOnClickListener(this);
        IbHome_class.setOnClickListener(this);
        IbHome_add.setOnClickListener(this);
        IbHome_message.setOnClickListener(this);
        IbHome_mine.setOnClickListener(this);
        LvHome_goods.setOnItemClickListener(itemListener);
        mTips = new ImageView[pathName.length];
	    for(int i = 0; i < mTips.length; i++)
	    {
	      ImageView iv = new ImageView(this);
	      iv.setLayoutParams(new LayoutParams(10,10));
	      mTips[i] = iv;
	      
	      if(i == 0)
	      {
	        iv.setBackgroundResource(R.drawable.back_bg);
	      }else
	      {
	        iv.setBackgroundResource(R.drawable.back_bg);
	      }
	      
	      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
	      
	      lp.leftMargin = 5;
	      lp.rightMargin = 5;
	      viewGroup.addView(iv,lp);
	    }
	    mImageViews = new ImageView[pathName.length];
	    
	    
	    VpHome_hotgoods.setAdapter(new MyPagerAdapter());
	    VpHome_hotgoods.setOnPageChangeListener(this);
	    onPageSelected(0);
        //=====================================================================
        if(GoodsSingleton.getInstance()==null) 	//判断是否已经保存了数据
        {
        	Log.d("getPopGoods", "GoodsSingleton.getInstance()==null");
        	getNewGoods();
        	getPopGoods();
        	getAllGoods();
        }else{
        	GoodslistAdapter goodslistadapter = new GoodslistAdapter(Home.this,GoodsSingleton.getTypeGoods("13"));
        	Log.d("getPopGoods", "GoodsSingleton.getInstance()!=null");
        	initResources(GoodsSingleton.getTypeGoods("13"));
    		LvHome_goods.setAdapter(goodslistadapter);
        }
        
        //=================================================
		
		
	    Intent intent = getIntent();
	    if(intent.getStringExtra("from")!=null&&intent.getStringExtra("from").equals("login"))
	    	download();
	    else if(intent.getStringExtra("from")!=null&&intent.getStringExtra("from").equals("other")){
	    }
	    else{
		    SharedPreferences  sharedPreferences = getSharedPreferences("login", 0);
		    String valid = sharedPreferences.getString("validate","0");
		    if(valid.equals("1")){   //if has valid login info
		    	String account = sharedPreferences.getString("account","0");
		    	String passwd = sharedPreferences.getString("passwd","0");
		    	login(account,passwd);
		    }
	    }
    }
    public void login(String account,String passwd){
		MyUser user = new MyUser();
		user.setUsername(account);
		user.setPassword(passwd);
		user.login(new SaveListener<MyUser>() {
			public void done(MyUser s, cn.bmob.v3.exception.BmobException e) {
				if(e==null){
					toast("登陆成功！用户："+s.getNick());
					//通过BmobUser user = BmobUser.getCurrentUser()获取登录成功后的本地用户信息
		            //如果是自定义用户对象MyUser，可通过MyUser user = BmobUser.getCurrentUser(MyUser.class)获取自定义用户信息
					UserSingleton.setInstance(BmobUser.getCurrentUser(MyUser.class));
				}else{
					toast("登陆失败！"+e.getMessage());
				}
			};
		});
	}
    public void onRefresh()  
    {  
        // Log.e("xxx", Thread.currentThread().getName());  
        // UI Thread  
    	BmobQuery<Goods> c = new BmobQuery<Goods>();
		c.setLimit(20);
		c.order("-createdAt");
		c.findObjects(new FindListener<Goods>() {
			@Override
			public void done(List<Goods> object, BmobException e) {
				if(e==null){
					GoodslistAdapter goodslistadapter = new GoodslistAdapter(Home.this,object);
		    		LvHome_goods.setAdapter(goodslistadapter);
		    		mSwipeLayout.setRefreshing(false);
		    		toast("数据刷新完毕");
				}else{
					Log.i("bmob----getGoods", "failed"+e.getMessage()+","+e.getErrorCode());
				}
			}
		});
    	getAllGoods();
    }  
    public void getNewGoods(){
		BmobQuery<Goods> c = new BmobQuery<Goods>();
		c.setLimit(20);
		c.order("-createdAt");
		c.findObjects(new FindListener<Goods>() {
			@Override
			public void done(List<Goods> object, BmobException e) {
				if(e==null){
					GoodslistAdapter goodslistadapter = new GoodslistAdapter(Home.this,object);
		    		LvHome_goods.setAdapter(goodslistadapter);
		    		
				}else{
					Log.i("bmob----getGoods", "failed"+e.getMessage()+","+e.getErrorCode());
				}
			}
		});		
	}
    public void getPopGoods(){
		BmobQuery<Goods> c = new BmobQuery<Goods>();
		c.setLimit(20);
		c.order("-createdAt");
		c.findObjects(new FindListener<Goods>() {
			@Override
			public void done(List<Goods> object, BmobException e) {
				if(e==null){
					Log.d("getPopGoods", "findObjects success");
					initResources(object);
				}else{
					Log.i("bmob----getGoods", "failed"+e.getMessage()+","+e.getErrorCode());
				}
			}
		});		
	}
    
    public void initResources(List<Goods> goods) {
    	Log.d("getPopGoods", "pathName.length"+pathName.length);
		  for(int i=0,j=-1;i<pathName.length&&j<goods.size();i++,j++){	
			final String file_name = goods.get(i).getObjectId()+"_0image.png";
			FileInputStream localstream = null;
			try {
					localstream = openFileInput(file_name);
				} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			final Bitmap bm = BitmapFactory.decodeStream(localstream);
			if(bm != null){
				
			    pathName[i] = getFilesDir()+"/"+file_name;
			    Log.d("getPopGoods", "file exist   "+i+"pathName"+pathName[i]);
				continue;
			}
			
			String origin_path;
			do{
				 j++;
				 origin_path= goods.get(j).getHead_path();
			  }while(origin_path==null);
			String path[] = origin_path.split(",");
			
			
			BmobFile file =new BmobFile(file_name,"",
					path[0]);
			 //允许设置下载文件的存储路径，默认下载文件的目录为：context.getApplicationContext().getCacheDir()+"/bmob/"
		    File saveFile = new File(getFilesDir(), file.getFilename());
		    file.download(saveFile, new DownloadFileListener() {
		        @Override
		        public void onStart() {
//		            toast("开始下载...");
		        }
		        @Override
		        public void done(String savePath,cn.bmob.v3.exception.BmobException e) {
		            if(e==null){
		            	Log.i("getPopGoods:",savePath);
		            	
		                //toast("下载成功");
		            }else{
		//                toast("下载失败："+e.getErrorCode()+","+e.getMessage());
		            }
		        }
		        @Override
		        public void onProgress(Integer value, long newworkSpeed) {
		//            Log.i("bmob","下载进度："+value+","+newworkSpeed);
		        }
		    });
			
		    pathName[i] = getFilesDir()+"/"+file_name;
			
		    Log.d("getPopGoods", "file not exist   "+i+"pathName"+pathName[i]);
		  }
		  		
		  for(index = 0; index < mImageViews.length; index++)
		    {
		      ImageView iv = new ImageView(this);
		      mImageViews[index] = iv;
		      int reqWidth = getWindowManager().getDefaultDisplay().getWidth();
		      int reqHeight = getWindowManager().getDefaultDisplay().getHeight();
		      iv.setImageBitmap(decodeSampledBitmapFromFile(pathName[index], reqWidth, reqHeight));
		      //iv.setImageBitmap(decodeSampledBitmapFromResource(getResources(), resId[index], reqWidth, reqHeight));
		    } 
	}
    public void getAllGoods(){
    	BmobQuery<Goods> c = new BmobQuery<Goods>();
		c.setLimit(10000);
		c.order("-createdAt");
		c.findObjects(new FindListener<Goods>() {
			@Override
			public void done(List<Goods> object, BmobException e) {
				if(e==null){
					GoodsSingleton.setInstance(object);   //将得到数据保存到单例对象中，后面数据量大可以考虑保存到本地数据库
				}else{
					Log.i("bmob----getAllGoods", "failed"+e.getMessage()+","+e.getErrorCode());
				}
			}
		});	
    }
    OnItemClickListener itemListener = new OnItemClickListener() {     
        public void onItemClick(AdapterView<?> parent, View view, int position,  
                long id) {  
            // 这里的view是我们在list.xml中定义的LinearLayout对象.  
            // 所以可以通过findViewById方法可以找到list.xml中定义的它的子对象,如下: 
        	Intent intent = new Intent();
        	intent.setClass(Home.this, GoodsDetail.class);
        	intent.putExtra("position", position);
        	Log.d("fenlei","form:"+position);
        	startActivity(intent);
        	overridePendingTransition(R.anim.fade, R.anim.hold);
        }

    };  
    void download(){
		if(user.getHead_path()==null||user.getHead_path().equals("")){
			return;
		}
		Log.i("path:",user.getHead_path());
		BmobFile file =new BmobFile(user.getObjectId()+"_temphead.png","",
				user.getHead_path());
		 //允许设置下载文件的存储路径，默认下载文件的目录为：context.getApplicationContext().getCacheDir()+"/bmob/"
	    File saveFile = new File(getFilesDir(), file.getFilename());
	    file.download(saveFile, new DownloadFileListener() {
	        @Override
	        public void onStart() {
//	            toast("开始下载...");
	        }
	        @Override
	        public void done(String savePath,cn.bmob.v3.exception.BmobException e) {
	            if(e==null){
	            	Log.i("download:",savePath);
	                //toast("下载成功");
	            }else{
	//                toast("下载失败："+e.getErrorCode()+","+e.getMessage());
	            }
	        }
	        @Override
	        public void onProgress(Integer value, long newworkSpeed) {
	//            Log.i("bmob","下载进度："+value+","+newworkSpeed);
	        }
	    });
	}
    class MyPagerAdapter extends PagerAdapter
    {
      @Override
      public int getCount()
      {
        return pathName.length;
      }
      @Override
      public boolean isViewFromObject(View arg0, Object arg1)
      {
        return arg0 == arg1;
      }
      @Override
      public Object instantiateItem(ViewGroup container, int position)
      {
	      if (index < 0) {
	        index = pathName.length - 1;
	      }
        try
        {
          View view = (View)mImageViews[position];
      	  view.setOnClickListener(new OnClickListener() {
      	   
      	   @Override
      	   public void onClick(View v) {
      		   Intent intent = new Intent();
      	   }
      	  });
      	  ViewPager viewPager = (ViewPager) container;
      	  viewPager.addView(view);
        }catch(Exception e){
      	  e.printStackTrace();
        }
        return mImageViews[position];
      }
      @Override
      public void destroyItem(ViewGroup container, int position, Object object)
      { 
      }
    }
    
    
    public void onPageSelected(int arg0)
    {
      for(int i = 0; i < mTips.length; i++)
      {
        if(arg0 == i)
        {
          mTips[i].setBackgroundResource(R.drawable.tip_selected);
        }else
        {
          mTips[i].setBackgroundResource(R.drawable.tip_normal);
        }
      }	
    }
    
    
    public void onPageScrollStateChanged(int arg0)
    {
    }
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2)
    {
    }
    
    public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        Log.i("decodeSampledBitmapFromFile:",pathName);
        options = getBestOptions(options, reqWidth, reqHeight);
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, mDesiredWidth, mDesiredHeight);
    }
    /**
     * @description 计算目标宽度，目标高度，inSampleSize
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return BitmapFactory.Options对象
     */
    private static BitmapFactory.Options getBestOptions(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 读取图片长宽
        int actualWidth = options.outWidth;
        int actualHeight = options.outHeight;
        // Then compute the dimensions we would ideally like to decode to.
        mDesiredWidth = getResizedDimension(reqWidth, reqHeight, actualWidth, actualHeight);
        mDesiredHeight = getResizedDimension(reqHeight, reqWidth, actualHeight, actualWidth);
        // 根据现在得到计算inSampleSize
        options.inSampleSize = calculateBestInSampleSize(actualWidth, actualHeight, mDesiredWidth, mDesiredHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return options;
    }
    private static int mDesiredWidth;
    private static int mDesiredHeight;
    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary) {
        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }
    private static int calculateBestInSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float inSampleSize = 1.0f;
        while ((inSampleSize * 2) <= ratio) {
            inSampleSize *= 2;
        }

        return (int) inSampleSize;
    }
    private static Bitmap createScaleBitmap(Bitmap tempBitmap, int desiredWidth, int desiredHeight) {
        // If necessary, scale down to the maximal acceptable size.
        if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight)) {
            // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
            Bitmap bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
            tempBitmap.recycle(); // 释放Bitmap的native像素数组
            return bitmap;
        } else {
            return tempBitmap; // 如果没有缩放，那么不回收
        }
    }
    
    //before before before
    
  
	 
	

    @Override
    public void onClick(View v) {
    	// TODO Auto-generated method stub
    	Intent intent = new Intent();
    	switch(v.getId()){
    	case R.id.IbHome_cell:
    		intent.setClass(Home.this, Cell.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intent);
    		overridePendingTransition(R.anim.fast_fade, R.anim.hold);
    		break;
    	case R.id.IbHome_class:
    		intent.setClass(Home.this, Category.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intent);
    		overridePendingTransition(R.anim.fast_fade, R.anim.hold);
    		break;
    	case R.id.IbHome_add:
    		intent.setClass(Home.this, Add.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intent);
    		break;
    	case R.id.IbHome_message:
    		intent.setClass(Home.this, Message.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intent);
    		overridePendingTransition(R.anim.fast_fade, R.anim.hold);
    		break;
    	case R.id.IbHome_mine:
    		intent.setClass(Home.this, Mine.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intent);
    		overridePendingTransition(R.anim.fast_fade,R.anim.hold);
    		break;
    	default:
    		break;
    	}
    }
    void toast(String s){
		Toast.makeText(this, s, Toast.LENGTH_LONG).show();
	}
}
