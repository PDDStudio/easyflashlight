package com.pddstudio.easyflashlight.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.pddstudio.easyflashlight.EasyFlashlight;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize EasyFlashlight (Activity as parameter)
        EasyFlashlight.init(this);
        setupFlashButton();
    }

    private boolean isOn = false;

    private void setupFlashButton() {

        Button cameraBtn = (Button) findViewById(R.id.camera_toggle);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOn) {
                    EasyFlashlight.getInstance().turnOff();
                    isOn = false;
                } else {
                    EasyFlashlight.getInstance().turnOn();
                    isOn = true;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //For Android M Support, call this in your onRequestPermissionsResult method
        EasyFlashlight.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
