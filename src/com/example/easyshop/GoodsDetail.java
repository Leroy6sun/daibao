package com.example.easyshop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.example.assist.CommentlistAdapter;
import com.example.assist.GoodslistAdapter;
import com.example.customview.ListViewForScrollView;
import com.example.entity.Goods;
import com.example.entity.LeaveWord;
import com.example.entity.MyUser;
import com.example.singleton.GoodsSingleton;
import com.example.singleton.LeavewordSingleton;
import com.example.singleton.UserSingleton;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GoodsDetail extends Activity implements OnClickListener{

	private ListViewForScrollView LvGoodsDetail;
	private ScrollView SvGoodsDetail;
	private ImageView IvGoodsDetail_rb,IvGoodsDetail_head,IvGoodsDetail_iv1,IvGoodsDetail_iv2,IvGoodsDetail_iv3,IvGoodsDetail_iv4;
	private ImageView IvGoodsDetail_zan,IvGoodsDetail_comment;
	private TextView TvGoodsDetail_newp,TvGoodsDetail_ownername,TvGoodsDetail_rank;
	private TextView TvGoods_name,TvGooddt_hotm;
	private TextView TvGoodsDetail_oldp,TvGoodsDetail_want;
	private LinearLayout LlGoodDt_tv,LlGoodDt;
	private EditText EtGooddt_com;
	private Button BtGooddt_send;
	private Goods good;
	private TextView tv_goodsintro;	
	private List<LeaveWord> lword_list = null;
	MyUser user = UserSingleton.getInstance();
	MyUser owner = null ;
	int position;
	String AUTHOR_IMAGE_NAME;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.goods_detail);
		position = getIntent().getIntExtra("position",0);
		//Log.d("gooddetail","come:"+position);
		good = GoodsSingleton.getInstance().get(position);
		owner = good.getAuthor();
		
		// init the author pic
		AUTHOR_IMAGE_NAME = owner.getObjectId()+"_temphead.png";
		FileInputStream localstream = null;
		try {
			localstream = openFileInput(AUTHOR_IMAGE_NAME);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IvGoodsDetail_head = (ImageView)findViewById(R.id.IvGoodsDetail_head);
		Bitmap bm = BitmapFactory.decodeStream(localstream);
		if(bm != null){
			IvGoodsDetail_head.setImageBitmap(bm);
		}
		else download_pic();
		//Log.d("gooddetail","goods_name"+good.getName()+"  owner:"+owner+"  owner:"+owner.getGender());
		init_pic();  //init the good's four pic
		TvGoodsDetail_newp = (TextView) findViewById(R.id.TvGoodsDetail_newp);
		TvGoodsDetail_newp.setText(String.valueOf(good.getPrice()));
		TvGoodsDetail_oldp = (TextView) findViewById(R.id.TvGoodsDetail_oldp);
		TvGooddt_hotm =(TextView) findViewById(R.id.TvGooddt_hotm);
		TvGoodsDetail_ownername = (TextView) findViewById(R.id.TvGoodsDetail_ownername);
		TvGoodsDetail_rank = (TextView) findViewById(R.id.TvGoodsDetail_rank);
		TvGoodsDetail_ownername.setText(owner.getNick());
		TvGoodsDetail_rank.setText("V"+owner.getGrade());
		TvGoodsDetail_oldp.setText(String.valueOf(good.getOld_price()));
		tv_goodsintro = (TextView) findViewById(R.id.tv_goodsintro);
		tv_goodsintro.setText(good.getIntro());
		TvGoods_name = (TextView) findViewById(R.id.TvGoods_name);
		TvGoods_name.setText(good.getName());
		LvGoodsDetail =(ListViewForScrollView) findViewById(R.id.LvGoodsDetail);
		SvGoodsDetail =(ScrollView) findViewById(R.id.SvGoodsDetail);
		IvGoodsDetail_rb =(ImageView) findViewById(R.id.IvGoodsDetail_rb);
		TvGoodsDetail_want =(TextView) findViewById(R.id.TvGoodsDetail_want);
		IvGoodsDetail_zan =(ImageView) findViewById(R.id.IvGoodsDetail_zan);
		IvGoodsDetail_comment =(ImageView) findViewById(R.id.IvGoodsDetail_comment);
		LlGoodDt =(LinearLayout) findViewById(R.id.LlGoodDt);
		LlGoodDt_tv =(LinearLayout) findViewById(R.id.LlGoodDt_tv);
		EtGooddt_com =(EditText) findViewById(R.id.EtGooddt_com);
		BtGooddt_send =(Button) findViewById(R.id.BtGooddt_send);

		SvGoodsDetail.smoothScrollTo(0, 0);
		Log.i("ObjectId", good.getObjectId());
		if(LeavewordSingleton.getInstance()==null){
		     Log.i("GoodId","getword:begin");
			getword();
		}
		/*
	    lword_list = LeavewordSingleton.getGoodsLWord(good.getObjectId());
		if(lword_list != null){
		     Log.i("GoodId","CommentlistAdapter:begin");
			CommentlistAdapter listadapter = new CommentlistAdapter(this,lword_list);
			LvGoodsDetail.setDividerHeight(0);
			LvGoodsDetail.setAdapter(listadapter);
			TvGooddt_hotm.setText("热门留言");
		}else{
			LvGoodsDetail.setVisibility(View.GONE);
			TvGooddt_hotm.setText("暂无留言");
		}*/
		
		IvGoodsDetail_rb.setOnClickListener(this);
		TvGoodsDetail_want.setOnClickListener(this);
		IvGoodsDetail_zan.setOnClickListener(this);
		IvGoodsDetail_comment.setOnClickListener(this);
		BtGooddt_send.setOnClickListener(this);
	}
	void init_pic(){
		IvGoodsDetail_iv1 = (ImageView)findViewById(R.id.IvGoodsDetail_iv1);
		IvGoodsDetail_iv2 = (ImageView)findViewById(R.id.IvGoodsDetail_iv2);
		IvGoodsDetail_iv3 = (ImageView)findViewById(R.id.IvGoodsDetail_iv3);
		IvGoodsDetail_iv4 = (ImageView)findViewById(R.id.IvGoodsDetail_iv4);
		int num_pic;
		String path[]=null;
		if(good.getHead_path()==null)
			num_pic=0;
		else{
			path = good.getHead_path().split(",");
			num_pic = path.length;
		}
			
		
		for(int i=0;i<num_pic;i++){
			switch(i){
			case 0:
				download(path[i],good.getObjectId()+"_0image.png",IvGoodsDetail_iv1);
				break;
			case 1:
				download(path[i],good.getObjectId()+"_1image.png",IvGoodsDetail_iv2);
				break;
			case 2:
				download(path[i],good.getObjectId()+"_2image.png",IvGoodsDetail_iv3);
				break;
			case 3:
				download(path[i],good.getObjectId()+"_3image.png",IvGoodsDetail_iv4);
				break;
			}
		}
		
		for(int i=num_pic;i<4;i++){
			switch(i){
			case 0:
				IvGoodsDetail_iv1.setVisibility(View.GONE);
				break;
			case 1:
				IvGoodsDetail_iv2.setVisibility(View.GONE);
				break;
			case 2:
				IvGoodsDetail_iv3.setVisibility(View.GONE);
				break;
			case 3:
				IvGoodsDetail_iv4.setVisibility(View.GONE);
				break;
			}
		}
	}
	void download(String path,String img_name,final ImageView img){
		final String file_name = img_name;
		FileInputStream localstream = null;
			try {
				localstream = openFileInput(file_name);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	    final Bitmap bm = BitmapFactory.decodeStream(localstream);
	    if(bm != null){
	    	img.setImageBitmap(bm);
			return;
		}
		BmobFile file =new BmobFile(file_name,"",
				path);
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
	            	FileInputStream localstream = null;
	        		try {
	        			localstream = openFileInput(file_name);
	        		} catch (FileNotFoundException e1) {
	        			// TODO Auto-generated catch block
	        			e1.printStackTrace();
	        		} 
	                final Bitmap bm = BitmapFactory.decodeStream(localstream);
	                img.setImageBitmap(bm);
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
	void download_pic(){
		if(owner.getHead_path()==null||owner.getHead_path().equals("")){
			return;
		}
		Log.i("path:",owner.getHead_path());
		BmobFile file =new BmobFile(owner.getObjectId()+"_temphead.png","",
				owner.getHead_path());
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
	            	FileInputStream localstream = null;
	        		try {
	        			localstream = openFileInput(AUTHOR_IMAGE_NAME);
	        		} catch (FileNotFoundException e1) {
	        			// TODO Auto-generated catch block
	        			e1.printStackTrace();
	        		}
	        		Bitmap bm = BitmapFactory.decodeStream(localstream);
	        		IvGoodsDetail_head.setImageBitmap(bm);
	        		
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
	
	public void getword(){
		BmobQuery<LeaveWord> w = new BmobQuery<LeaveWord>();
		w.setLimit(20);
		w.include("user");
		w.findObjects(new FindListener<LeaveWord>() {
			@Override
			public void done(List<LeaveWord> object, BmobException e) {
				if(e==null){
					CommentlistAdapter listadapter = new CommentlistAdapter(GoodsDetail.this,object);
					LvGoodsDetail.setAdapter(listadapter);
		    		
				}else{
					Log.i("bmob----getcomment", "failed"+e.getMessage()+","+e.getErrorCode());
				}
			}
		});		
	}
	
	private List<Map<String,Object>> getData() {
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		for(int i = 0 ; i <4; i ++ )
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("username", "username");
			map.put("comment", "comment");
			map.put("time", "time");
			map.put("headimage", R.drawable.tip_selected);
			list.add(map);
		}
		return list;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch(v.getId()){
		case R.id.IvGoodsDetail_rb:
			finish();
			break;
		case R.id.TvGoodsDetail_want:
			intent.setClass(GoodsDetail.this, Chat.class);
			intent.putExtra("goodname",TvGoods_name.getText().toString());
			intent.putExtra("price",TvGoodsDetail_newp.getText().toString());
			intent.putExtra("position",position);
			startActivity(intent);
			break;
		case R.id.IvGoodsDetail_comment:
			LlGoodDt_tv.setVisibility(View.VISIBLE);
			LlGoodDt.setVisibility(View.GONE);
			break;
		case R.id.BtGooddt_send:
			LeaveWord leave_word = new LeaveWord();
			leave_word.setContent(EtGooddt_com.getText().toString());
			leave_word.setGoodId(good.getObjectId().toString());
			leave_word.setUser(user);
			leave_word.save(new SaveListener<String>() {
				
				@Override
				public void done(String objectId, cn.bmob.v3.exception.BmobException e) {
					// TODO Auto-generated method stub
					if(e==null){
						Toast.makeText(GoodsDetail.this, "评论成功", Toast.LENGTH_SHORT).show();
			        }else{
			            Log.i("bmob","留言失败："+e.getMessage()+","+e.getErrorCode());
			        }
				}
			});
			LlGoodDt_tv.setVisibility(View.GONE);
			LlGoodDt.setVisibility(View.VISIBLE);
			break;
		case R.id.IvGoodsDetail_zan:
			break;
		default:
			LlGoodDt_tv.setVisibility(View.GONE);
			LlGoodDt.setVisibility(View.VISIBLE);
			break;
		}
		
	}
}
