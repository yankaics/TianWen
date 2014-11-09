package me.jeremy.ccst.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;
import me.jeremy.ccst.R;
import me.jeremy.ccst.api.Api;
import me.jeremy.ccst.api.TypeParams;
import me.jeremy.ccst.data.GsonRequest;
import me.jeremy.ccst.data.MyStringRequest;
import me.jeremy.ccst.data.RequestManager;
import me.jeremy.ccst.data.center.Records;
import me.jeremy.ccst.model.question.CreateAnswerDetailRequest;
import me.jeremy.ccst.model.question.CreateAnswerSheetRequest;
import me.jeremy.ccst.model.question.CreateQuestionAnswer;
import me.jeremy.ccst.model.question.QuestionResponse;
import me.jeremy.ccst.model.question.QuestionnaireDetailResponse;
import me.jeremy.ccst.ui.fragment.FiledFragment;
import me.jeremy.ccst.ui.fragment.MultiFragment;
import me.jeremy.ccst.ui.fragment.SingleFragment;
import me.jeremy.ccst.utils.ParamsUtils;
import me.jeremy.ccst.utils.TaskUtils;
import me.jeremy.ccst.utils.ToastUtils;
import me.jeremy.ccst.utils.ToolUtils;
import me.jeremy.ccst.utils.UserUtils;

/**
 * Created by qiugang on 2014/9/27.
 */
public class QuestionPager extends FragmentActivity {

    private Gson gson = new Gson();

    private Type type = new TypeToken<QuestionnaireDetailResponse>() {
    }.getType();

    private List<QuestionResponse> questions = new ArrayList<QuestionResponse>();

    private QuestionnaireDetailResponse questionnaireDetailResponse;

    private String id = null;

    private ActionBar mActionBar;

    private Menu mMenu;

    private ProgressDialog progressDialog;

    private int maxPosition = -1;

    /**
     * Answer params
     */

    private CreateAnswerSheetRequest answerSheet = new CreateAnswerSheetRequest();

    private List<CreateQuestionAnswer> answers = new ArrayList<CreateQuestionAnswer>();


    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    private int flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_pager);
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);

        //Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        id = getIntent().getStringExtra("QuestionnaireId");
        if (ToolUtils.isConnectInternet()) {
            executeRequest(new MyStringRequest(Request.Method.GET, Api.Host_ALIYUN + "detail/" + id,
                    responseListener(), errorListener()));
        } else {
            ToastUtils.showShort("网络未连接，不能捡肥皂");
        }
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int position = 0;

            @Override
            public void onPageScrolled(int i, float v, int i2) {
                    if (i2 == 0) {
                        if (maxPosition != -1) {
                            if (position == maxPosition) {
                                mMenu.findItem(R.id.action_save).setVisible(true);
                            }
                        }
                    }
            }

            @Override
            public void onPageSelected(int i) {
                position = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == maxPosition) {
//                    ToastUtils.showShort("最后一页");
                }

            }
        });
        mPager.setOffscreenPageLimit(1);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            String questionType = questions.get(position).getQuestionType();
            if (position == questions.size() - 1) {

            }
            if (TypeParams.QUESTION_FIELD.equals(questionType)) {
                return new FiledFragment(questions.get(position), position + 1);
            } else if (TypeParams.QUESTION_CHOOSE_SINGEL.equals(questionType)) {
                return new SingleFragment(questions.get(position),
                        position + 1);
            } else {
                return new MultiFragment(questions.get(position),
                        position + 1);
            }
        }

        @Override
        public int getCount() {
            return questions.size();
        }


    }

    private Response.Listener<String> responseListener() {
        return new Response.Listener<String>() {
            QuestionnaireDetailResponse temple;

            @Override
            public void onResponse(final String response) {
                TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, Object>() {

                    @Override
                    protected Object doInBackground(Object... params) {
                        temple = gson.fromJson(response, type);
                        questionnaireDetailResponse = temple;
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        questions.clear();
                        for (QuestionResponse q : temple.getQuestions()) {
                            questions.add(q);
                        }
                        maxPosition = questions.size() - 1;
                        mPagerAdapter.notifyDataSetChanged();
                        mActionBar.setTitle(ParamsUtils.getQuestionNums(questions.size()));
                    }
                });
            }
        };
    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (ToolUtils.isConnectInternet()) {
                    progressDialog.dismiss();
                }
            }
        };
    }

    protected void executeRequest(Request<?> request) {
        RequestManager.addRequest(request, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            if (ToolUtils.isConnectInternet()) {
                progressDialog = ProgressDialog.show(QuestionPager.this, null, "玩儿命提交中...", true, true);
                commitData();
            } else {
                ToastUtils.showShort("网络未连接，不能扔肥皂");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.question, menu);
        mMenu = menu;
        mMenu.findItem(R.id.action_save).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void commitData() {
        int position = 0;
        Integer questionId = 0;
        String questionType = null;
        for (; position < questions.size(); position++) {
            CreateQuestionAnswer questionAnswer = new CreateQuestionAnswer();
            List<CreateAnswerDetailRequest> answerDetails = new ArrayList<CreateAnswerDetailRequest>();
            //get question id
            questionId = questions.get(position).getId();
            questionType = questions.get(position).getQuestionType();
            questionAnswer.setQuestionId(questionId);
            questionAnswer.setQuestionType(questionType);

            if (questionType.equals(TypeParams.QUESTION_FIELD)) {
                if (Records.getStringDataCenter().get(questionId) != null) {
                    CreateAnswerDetailRequest detail = new CreateAnswerDetailRequest();
                    detail.setContent(Records.getStringDataCenter().get(questionId));
                    answerDetails.add(detail);
                }
            } else {
                if (Records.getDataCenter().get(questionId) != null) {
                    for (Integer integer : Records.getDataCenter().get(questionId)) {
                        CreateAnswerDetailRequest detail = new CreateAnswerDetailRequest();
                        detail.setAnswerId(integer);
                        answerDetails.add(detail);
                    }
                }
                Log.d(questionId + "添加了", answerDetails.toString());
            }
            questionAnswer.setAnswers(answerDetails);
            answers.add(questionAnswer);
        }
        answerSheet.setQuestions(answers);
        answerSheet.setQuestionnaireId(Integer.parseInt(id));
        if (UserUtils.getUserId() != 0) {
            answerSheet.setUserId(UserUtils.getUserId());
            if (Records.getDataCenter() != null) {
                Records.getDataCenter().clear();
                Records.getStringDataCenter().clear();
                String params = new Gson().toJson(answerSheet);
                Log.d("提交的数据=======》", params);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(params);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                executeRequest(new GsonRequest<Boolean>(Api.Host_ALIYUN + "doquestionnaire/", jsonObject,
                        Boolean.class, postDataResponseListener(), errorListener()));
            }
        } else {
            ToastUtils.showShort("没有登录哦,别想碰我");
        }
    }

    private Response.Listener<Boolean> postDataResponseListener() {
        return new Response.Listener<Boolean>() {
            @Override
            public void onResponse(final Boolean response) {
                TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        progressDialog.dismiss();
                        Log.d("提交数据结果=======》", response.toString());
                        if (true == response) {
                            ToastUtils.showShort("提交成功\n捡过的肥皂不能再捡啦");
                            QuestionPager.this.finish();
                        } else if (false == response) {
                            ToastUtils.showShort("提交失败\n再扔一次肥皂");
                        }
                    }
                });
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Records.getDataCenter().clear();
        Records.getStringDataCenter().clear();
    }
}
