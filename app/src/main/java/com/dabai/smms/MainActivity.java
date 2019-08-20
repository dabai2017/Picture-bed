package com.dabai.smms;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.wildma.pictureselector.PictureSelector;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import okhttp3.Call;

public class MainActivity extends AppCompatActivity {

    //变量
    String filepath;
    boolean issele;
    private String link, dellink;
    //控件
    private ConstraintLayout cons;
    ImageView iv;
    CheckBox cb;
    ProgressBar progressBar;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //初始化
        iv = findViewById(R.id.imageView);
        cons = findViewById(R.id.cons);
        cb = findViewById(R.id.checkBox);
        progressBar = findViewById(R.id.progressBar);
        button = findViewById(R.id.button);

        IntroView(iv, "1", "点击这里选择图片");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*结果回调*/
        if (requestCode == PictureSelector.SELECT_REQUEST_CODE) {
            if (data != null) {
                //取得相片路径
                filepath = data.getStringExtra(PictureSelector.PICTURE_PATH);
                Bitmap bm = BitmapFactory.decodeFile(filepath);
                iv.setImageBitmap(bm);
                issele = true;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void upfile(View view) {
        //上传方法开始
        if (issele) {

            button.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            final File picfile = new File(filepath);

            String url = "https://sm.ms/api/upload";
            String ua = "Mozilla/5.0 (Linux; Android ; M5 Build/MRA58K) tuchuang/1.0";

            HashMap<String, String> header = new HashMap<String, String>();
            header.put("User-Agent", ua);
            OkHttpUtils.post()
                    .url(url)
                    .headers(header)
                    .addFile("smfile", picfile.getName(), picfile)
                    .addParams("ssl", cb.isChecked() ? "true" : "false")
                    .build()
                    .execute(new StringCallback() {

                        @Override
                        public void onError(Call call, Exception e, int i) {

                            progressBar.setVisibility(View.INVISIBLE);
                            button.setEnabled(true);
                            Snackbar.make(cons, "上传失败" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResponse(String s, int i) {

                            JSONObject requestjson = null;
                            try {
                                progressBar.setVisibility(View.INVISIBLE);
                                button.setEnabled(true);

                                requestjson = new JSONObject(s);
                                JSONObject datajson = requestjson.getJSONObject("data");
                                link = datajson.getString("url");
                                dellink = datajson.getString("delete");

                                View vi = getLayoutInflater().inflate(R.layout.dialog_result, null);
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title("双击复制")
                                        .cancelable(false)
                                        .customView(vi, true)
                                        .positiveText("关闭")
                                        .show();


                                TextView te1 = vi.findViewById(R.id.textView2);
                                TextView te2 = vi.findViewById(R.id.textView4);
                                TextView te3 = vi.findViewById(R.id.textView6);
                                TextView te4 = vi.findViewById(R.id.textView8);


                                te4.setText(link);
                                te1.setText("![" + picfile.getName() + "](" + link + ")");
                                te2.setText("<img src=\"" + link + "\" alt=\"" + picfile.getName() + "\">");
                                te3.setText(dellink);

                                te4.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        copytext(link);
                                    }
                                });

                                te1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        copytext("![" + picfile.getName() + "](" + link + ")");
                                    }
                                });
                                te2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        copytext("<img src=\"" + link + "\" alt=\"" + picfile.getName() + "\">");
                                    }
                                });
                                te3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        copytext(dellink);
                                    }
                                });


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    iv.setImageDrawable(getDrawable(R.drawable.upfile3));
                                    issele = false;
                                }


                            } catch (Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                button.setEnabled(true);
                                Snackbar.make(cons, "上传失败:" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });


        } else {
            //没选择图片提示
            Snackbar.make(cons, "请先选择图片", Snackbar.LENGTH_LONG).show();
        }
        //上传方法结束
    }

    public void choose(View view) {
        /**
         * 图库选择  可裁剪
         *
         * */
        PictureSelector
                .create(MainActivity.this, PictureSelector.SELECT_REQUEST_CODE)
                .selectPicture(false, 200, 200, 1, 1);

    }

    public void copytext(String tx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P + 1) {
            ClipboardManager clip = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clip.setText(tx);
            Toast.makeText(this, "复制成功", Toast.LENGTH_LONG).show();
        }
    }

    private void IntroView(View v, String id, String text) {

        new MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .enableIcon(true)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .setDelayMillis(200)
                .setTargetPadding(30)
                .enableFadeAnimation(true)
                .performClick(false)
                .setInfoText(text)
                .setTarget(v)
                .setUsageId(id) //THIS SHOULD BE UNIQUE ID
                .show();
    }


}
