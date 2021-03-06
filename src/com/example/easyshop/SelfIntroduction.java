package com.example.easyshop;

import com.example.entity.MyUser;
import com.example.singleton.UserSingleton;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SelfIntroduction extends Activity {

	private EditText EtSelf;
	private Button BtSelf_save;
	MyUser user = UserSingleton.getInstance();;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.self_introduction);
		
		EtSelf =(EditText) findViewById(R.id.EtSelf);
		BtSelf_save =(Button) findViewById(R.id.BtSelf_save);
		EtSelf.setText(user.getMotto());
		BtSelf_save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				user.setMotto(EtSelf.getText().toString());
				finish();
			}
		});
	}


}
