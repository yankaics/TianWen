package me.jeremy.ccst.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import me.jeremy.ccst.R;
import me.jeremy.ccst.model.user.UserInfoResponse;
import me.jeremy.ccst.utils.UserUtils;

/**
 * Created by qiugang on 14/10/28.
 */
public class UserInfoActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout phoneLayout;
    private RelativeLayout qqLayout;
    private RelativeLayout emailLayout;

    private TextView userName;
    private TextView phone;
    private TextView qq;
    private TextView eMail;

    private UserInfoResponse userInfoResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FadingActionBarHelper helper = new FadingActionBarHelper()
                .actionBarBackground(R.drawable.ab_background)
                .headerLayout(R.layout.header)
                .contentLayout(R.layout.perfrerence_user)
                .lightActionBar(true);
        setContentView(helper.createView(this));
        helper.initActionBar(this);
        getActionBar().setDisplayShowHomeEnabled(false);
        initView();
        initViewData();
    }

    private void initView() {
        phoneLayout = (RelativeLayout) findViewById(R.id.layout_phone);
        qqLayout = (RelativeLayout) findViewById(R.id.layout_qq);
        emailLayout = (RelativeLayout) findViewById(R.id.layout_email);

        userName = (TextView) findViewById(R.id.tv_center_userName);
        qq = (TextView) findViewById(R.id.tv_center_qq);
        eMail = (TextView) findViewById(R.id.tv_center_email);
        phone = (TextView) findViewById(R.id.tv_center_phone);

        qqLayout.setOnClickListener(this);
        emailLayout.setOnClickListener(this);
        phoneLayout.setOnClickListener(this);
    }

    private void initViewData() {
        userInfoResponse = UserUtils.getUserInfoResponse();
        userName.setText(userInfoResponse.getUserName());
        if (!TextUtils.isEmpty(userInfoResponse.getQq())) {
            qq.setText(userInfoResponse.getQq());
        } else {
            qq.setText("未填写");
        }

        if (!TextUtils.isEmpty(userInfoResponse.getEmail())) {
            eMail.setText(userInfoResponse.getEmail());
        } else {
            eMail.setText("未填写");
        }

        if (!TextUtils.isEmpty(userInfoResponse.getPhone())) {
            phone.setText(userInfoResponse.getPhone());
        } else {
            phone.setText("未填写");
        }
    }

    @Override
    public void onClick(View v) {
        Intent mIntent = new Intent(this, EditProfileActivity.class);
        Bundle args = new Bundle();
        switch (v.getId()) {
            case R.id.layout_email:
                args.putString("type","email");
                args.putString("value", userInfoResponse.getEmail());
                break;
            case R.id.layout_qq:
                args.putString("type", "qq");
                args.putString("value", userInfoResponse.getQq());
                break;
            case R.id.layout_phone:
                args.putString("type", "phone");
                args.putString("value", userInfoResponse.getPhone());
                break;
        }
        mIntent.putExtras(args);
        startActivity(mIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViewData();
    }

}
