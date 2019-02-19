package com.example.patientip.vda;

        import android.Manifest;
        import android.app.KeyguardManager;
        import android.content.Context;
        import android.content.pm.ApplicationInfo;
        import android.content.pm.PackageInfo;
        import android.content.pm.PackageManager;
        import android.os.Build;
        import android.os.Environment;
        import android.provider.Settings;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.FileWriter;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> apps = new ArrayList<>();
    private TextView mScore1, mScore2, mScore3, mScore4, mScore5, isPresent, mScoreTotal, mscore_msg;
    private ImageView image1, image2, image3, image4, image5;
    private int total,devscore,unkscore,scrscore,osscore,antiscore;
    private Button bstartApp,bfirstPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bstartApp = (Button) findViewById(R.id.startApp);
        bstartApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.first_layout);
                mScore1 = (TextView) findViewById(R.id.score1);
                image1 = (ImageView) findViewById(R.id.image1);

                if (Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED.equals("development_settings_enabled")) {
                    mScore1.setText("0");
                    image1.setImageResource(R.drawable.redcross);
                } else {
                    mScore1.setText("1");
                    image1.setImageResource(R.drawable.greencheck);
                }

                mScore2 = (TextView) findViewById(R.id.score2);
                image2 = (ImageView) findViewById(R.id.image2);

                try {
                    if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1) {
                        mScore2.setText("0");
                        image2.setImageResource(R.drawable.redcross);
                    } else {
                        mScore2.setText("1");
                        image2.setImageResource(R.drawable.greencheck);
                    }
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }

                mScore3 = (TextView) findViewById(R.id.score3);
                image3 = (ImageView) findViewById(R.id.image3);
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager.isDeviceSecure()) {
                    mScore3.setText("1");
                    image3.setImageResource(R.drawable.greencheck);
                } else {
                    mScore3.setText("0");
                    image3.setImageResource(R.drawable.redcross);
                }

                mScore4 = (TextView) findViewById(R.id.score4);
                image4 = (ImageView) findViewById(R.id.image4);
                if (Integer.parseInt(android.os.Build.VERSION.RELEASE.substring(0, 1)) == 8) {
                    mScore4.setText("2");
                    image4.setImageResource(R.drawable.greencheck);
                } else if (Integer.parseInt(android.os.Build.VERSION.RELEASE.substring(0, 1)) == 7) {
                    mScore4.setText("1");
                    image4.setImageResource(R.drawable.redcross);
                } else {
                    mScore4.setText("0");
                    image4.setImageResource(R.drawable.redcross);
                }

                mScore5 = (TextView) findViewById(R.id.score5);
                image5 = (ImageView) findViewById(R.id.image5);

                PackageManager p = getPackageManager();
                final List<PackageInfo> appinstall = p.getInstalledPackages(PackageManager.GET_PERMISSIONS);
                for (PackageInfo pi : appinstall) {
                    if (ApplicationInfo.FLAG_SYSTEM != 0) {
//                Log.e("packageInfo: ", "" + pi.packageName);
                        String s = p.getInstallerPackageName(pi.packageName);
                        if (s != null) {
                            Log.e("packageInfo: ", "" + pi.packageName);
                            apps.add(pi.packageName);
                        }
                    }
                }

                HashMap map = new HashMap();
                map.put("com.antivirus", 0);
                map.put("com.avast.android.mobilesecurity", 0);
                map.put("com.symantec.mobilesecurity", 0);
                map.put("org.antivirus", 0);
                map.put("com.kms.free", 0);
                map.put("com.cleanmaster.mguard", 0);
                map.put("com.com.wsandroid.suite", 0);
                map.put("com.avira.android", 0);
                map.put("com.zrgiu.antivirus", 0);
                map.put("com.lookout", 0);

                mScore5.setText("0");
                image5.setImageResource(R.drawable.redcross);
                for (String pkg : apps) {
                    if (map.containsKey(pkg)) {
                        mScore5.setText("2");
                        image5.setImageResource(R.drawable.greencheck);
                        break;
                    }
                }
                total = Integer.parseInt((String) mScore1.getText()) + Integer.parseInt((String) mScore2.getText()) + Integer.parseInt((String) mScore3.getText()) + Integer.parseInt((String) mScore4.getText()) + Integer.parseInt((String) mScore5.getText());
                devscore = Integer.parseInt((String) mScore1.getText());
                unkscore = Integer.parseInt((String) mScore2.getText());
                scrscore = Integer.parseInt((String) mScore3.getText());
                osscore = Integer.parseInt((String) mScore4.getText());
                antiscore= Integer.parseInt((String) mScore5.getText());
            }
        });
    }

    public void callEntity(View view)
    {
        setContentView(R.layout.second_layout);
                Entail entity = new Entail(getApplicationContext());
                 String security = entity.mainFunction("forward", "KB.txt", String.valueOf(total));

                mScoreTotal = (TextView) findViewById(R.id.mScoreTotal);
                mScoreTotal.setText("Score calculated : " + String.valueOf(total)+".");

                mscore_msg = (TextView) findViewById(R.id.mscore_msg);
                mscore_msg.setText("Your device has " + security +" security.");

    }

    public void Exit(View view)
    {
                // TODO Auto-generated method stub
                finish();
                System.exit(0);
    }

}
