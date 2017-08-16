package com.vyn.motoclick.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.vyn.motoclick.R;

/**
 * Created by Yurka on 01.07.2017.
 */

public class AboutProgram extends AppCompatActivity {

    TextView btnGroupFacebook;
    TextView btnPrivacyPolicy;
    TextView btnWorkers;
    Dialog dialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about_program);

        btnGroupFacebook = (TextView) findViewById(R.id.btnGroupFacebook);
        btnPrivacyPolicy = (TextView) findViewById(R.id.btnPrivacyPolicy);
        btnWorkers = (TextView) findViewById(R.id.btnWorkers);

        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPrivacyPolicy.setTextColor(getResources().getColor(R.color.colorWhiteDark));
                startActivity(new Intent(AboutProgram.this, PrivacyPolicyActivity.class).putExtra("flagExit", "aboutProgram"));
            }
        });

        btnGroupFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGroupFacebook.setTextColor(getResources().getColor(R.color.colorWhiteDark));

                //открыть ссылку в браузере
                if ("iw".equals(getResources().getConfiguration().locale.getLanguage())) {
                    dialogGroupFacebook();
                } else {
                    Uri address = Uri.parse("https://m.facebook.com/UniteAllMotorcyclists");
                    Intent openlink = new Intent(Intent.ACTION_VIEW, address);
                    startActivity(openlink);
                }
            }
        });

        btnWorkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnWorkers.setTextColor(getResources().getColor(R.color.colorWhiteDark));
                dialogWorkers();
                btnWorkers.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
    }

    protected void onResume() {
        super.onResume();
        btnGroupFacebook.setTextColor(getResources().getColor(R.color.colorBlack));
    }

    private void dialogWorkers() {
        final String[] developers = {getString(R.string.worker1), getString(R.string.worker2), getString(R.string.worker3)};

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setItems(developers, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                switch (item) {
                    case 0:
                        //      intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=HTIG"));
                        //      startActivity(intent);
                        //     dialog.dismiss();
                        break;
                    case 1:
                        //      intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Yurka+Sergeant+Matatov"));
                        //        startActivity(intent);
                        //        dialog.dismiss();
                        break;
                    case 2:
                /*        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        emailIntent.setType("plain/text");
                        // Кому
                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"Docmat63@gmail.com"});
                        // тема
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Remote Secyrity Phone");
                        // отправка!
                        startActivity(Intent.createChooser(emailIntent, getString(R.string.toastSendMail)));
                        dialog.dismiss();
                        break;*/
                }
            }
        });
        dialog = adb.show();
    }

    private void dialogGroupFacebook() {
        String[] arrGroups = new String[]{
                "Group for Israel",
                "General Group",
        };
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setItems(arrGroups, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        Uri address = Uri.parse("https://m.facebook.com/IsraelTypicalMoto");
                        Intent openlink = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlink);
                        dialog.dismiss();
                        break;
                    case 1:
                        address = Uri.parse("https://m.facebook.com/UniteAllMotorcyclists");
                        openlink = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlink);
                        dialog.dismiss();
                        break;
                }
            }
        });
        dialog = adb.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AboutProgram.this, MapsActivity.class));
    }
}