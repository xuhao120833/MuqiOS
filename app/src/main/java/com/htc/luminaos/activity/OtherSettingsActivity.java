package com.htc.luminaos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.htc.luminaos.R;
import com.htc.luminaos.databinding.ActivityOtherSettingsBinding;
import com.htc.luminaos.service.TimeOffService;
import com.htc.luminaos.utils.Contants;
import com.htc.luminaos.utils.ShareUtil;
import com.htc.luminaos.widget.FactoryResetDialog;

public class OtherSettingsActivity extends BaseActivity implements View.OnKeyListener {

    private ActivityOtherSettingsBinding otherSettingsBinding;
    long cur_time = 0;

    private int cur_screen_saver_index = 0;
    String[] screen_saver_title;
    int[] screen_saver_value;

    private int cur_time_off_index = 0;
    String[] time_off_title;
    int[] time_off_value;

    String[] boot_source_name;
    String[] boot_source_value;
    private int boot_source_index = 0;

    String[] power_mode_name;
    String[] power_mode_value;
    private int power_mode_index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        otherSettingsBinding = ActivityOtherSettingsBinding.inflate(LayoutInflater.from(this));
        setContentView(otherSettingsBinding.getRoot());
        initView();
        initData();
    }

    private void initView(){
        otherSettingsBinding.rlButtonSound.setOnClickListener(this);
        otherSettingsBinding.buttonSoundSwitch.setOnClickListener(this);

        otherSettingsBinding.rlResetFactory.setOnClickListener(this);
        otherSettingsBinding.rlScreenSaver.setOnClickListener(this);
        otherSettingsBinding.rlTimerOff.setOnClickListener(this);

        otherSettingsBinding.rlScreenSaver.setOnKeyListener(this);
        otherSettingsBinding.rlTimerOff.setOnKeyListener(this);

        otherSettingsBinding.rlBootInput.setOnKeyListener(this);
        otherSettingsBinding.rlBootInput.setOnClickListener(this);

        otherSettingsBinding.rlPowerMode.setOnKeyListener(this);
        otherSettingsBinding.rlPowerMode.setOnClickListener(this);

        otherSettingsBinding.rlDeveloper.setOnClickListener(this);
        otherSettingsBinding.rlBootInput.requestFocus();
        otherSettingsBinding.rlBootInput.requestFocusFromTouch();

        if ((boolean)ShareUtil.get(this,Contants.KEY_DEVELOPER_MODE,false)){
            otherSettingsBinding.rlDeveloper.setVisibility(View.VISIBLE);
        }
    }

    private void initData(){
        otherSettingsBinding.buttonSoundSwitch.setChecked(getButtonSound());

        screen_saver_title =  getResources().getStringArray(R.array.screen_saver_title);
        screen_saver_value = getResources().getIntArray(R.array.screen_saver_value);
        cur_screen_saver_index = getCurScreenSaverIndex();
        otherSettingsBinding.screenSaverTv.setText(screen_saver_title[cur_screen_saver_index]);

        time_off_title =  getResources().getStringArray(R.array.time_off_title);
        time_off_value = getResources().getIntArray(R.array.time_off_value);
        cur_time_off_index =(int) ShareUtil.get(this, Contants.TimeOffIndex,0);
        otherSettingsBinding.timerOffTv.setText(time_off_title[cur_time_off_index]);
        /*if ((boolean) ShareUtil.get(this, Contants.TimeOffStatus,false)){
            int  timeOffTime =(int) ShareUtil.get(this, Contants.TimeOffTime,0);
            otherSettingsBinding.timerOffTv.setText(timeOffTime/60+"Min");
        }else {
            otherSettingsBinding.timerOffTv.setText(time_off_title[cur_time_off_index]);
        }*/

        boot_source_name =  getResources().getStringArray(R.array.boot_source_name);
        boot_source_value = getResources().getStringArray(R.array.boot_source_value);
        String source_value = get_power_signal();
        for (int i=0;i<boot_source_value.length;i++){
            if (source_value.equals(boot_source_value[i])) {
                boot_source_index = i;
                break;
            }
        }
        otherSettingsBinding.bootInputTv.setText(boot_source_name[boot_source_index]);

        power_mode_name =  getResources().getStringArray(R.array.power_mode_name);
        power_mode_value = getResources().getStringArray(R.array.power_mode_value);
        otherSettingsBinding.powerModeTv.setText(power_mode_name[power_mode_index]);
    }

    private String get_power_signal(){

        return SystemProperties.get("persist.sys.default_source","LOCAL");
    }

    private void set_power_signal(String source){
        SystemProperties.set("persist.sys.default_source",source);
    }

    private int getCurScreenSaverIndex(){
        int screen_off_timeout = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,300000);
        for (int i=0;i<screen_saver_value.length;i++){
            if (screen_off_timeout==screen_saver_value[i])
                return i;
        }
        return 0;
    }

    private void updateScreenSaver(int index){
        Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,screen_saver_value[index]);
        otherSettingsBinding.screenSaverTv.setText(screen_saver_title[index]);
    }

    private void setTimeOff(int index){
        otherSettingsBinding.timerOffTv.setText(time_off_title[index]);
        ShareUtil.put(this,Contants.TimeOffIndex,index);
        Intent intent = new Intent(this, TimeOffService.class);
        if (index==0){
            ShareUtil.put(this,Contants.TimeOffStatus,false);
            intent.putExtra(Contants.TimeOffStatus,false);
            intent.putExtra(Contants.TimeOffTime,-1);
        }else {
            ShareUtil.put(this,Contants.TimeOffStatus,true);
            ShareUtil.put(this,Contants.TimeOffTime,time_off_value[index]);
            intent.putExtra(Contants.TimeOffStatus,true);
            intent.putExtra(Contants.TimeOffTime,time_off_value[index]);
        }
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_button_sound:
            case R.id.button_sound_switch:
                otherSettingsBinding.buttonSoundSwitch.setChecked(!otherSettingsBinding.buttonSoundSwitch.isChecked());
                setButtonSound(otherSettingsBinding.buttonSoundSwitch.isChecked());
                break;
            case R.id.rl_reset_factory:
                FactoryResetDialog factoryResetDialog = new FactoryResetDialog(this,R.style.DialogTheme);
                factoryResetDialog.show();
                break;
            case R.id.rl_screen_saver:
                if (cur_screen_saver_index==screen_saver_title.length-1)
                    cur_screen_saver_index =0;
                else
                    cur_screen_saver_index++;
                updateScreenSaver(cur_screen_saver_index);
                break;
            case R.id.rl_timer_off:
                if (cur_time_off_index==time_off_title.length-1)
                    cur_time_off_index =0;
                else
                    cur_time_off_index++;

                setTimeOff(cur_time_off_index);
                break;
            case R.id.rl_boot_input:
                if (boot_source_index==boot_source_name.length-1)
                    boot_source_index =0;
                else
                    boot_source_index++;

                otherSettingsBinding.bootInputTv.setText(boot_source_name[boot_source_index]);
                set_power_signal(boot_source_value[boot_source_index]);
                break;
            case R.id.rl_power_mode:
                if (power_mode_index==power_mode_name.length-1)
                    power_mode_index =0;
                else
                    power_mode_index++;

                otherSettingsBinding.powerModeTv.setText(power_mode_name[power_mode_index]);
                break;
            case R.id.rl_developer:
                startNewActivity(DeveloperModeActivity.class);
                break;
        }
    }

   private boolean getButtonSound(){
        return Settings.System.getInt(getContentResolver(),
                Settings.System.SOUND_EFFECTS_ENABLED, 0)==1;
   }

    private void setButtonSound(boolean ret){
        Settings.System.putInt(getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED,ret?1:0);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if ((event.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT ||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT)
                && (System.currentTimeMillis()-cur_time<150)){
            return true;
        }

        if (keyCode==KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() ==KeyEvent.ACTION_UP){
            switch (v.getId()){
                case R.id.rl_screen_saver:
                    if (cur_screen_saver_index==0)
                        cur_screen_saver_index =screen_saver_title.length-1;
                    else
                        cur_screen_saver_index--;
                    updateScreenSaver(cur_screen_saver_index);
                    break;
                case R.id.rl_timer_off:
                    if (cur_time_off_index==0)
                        cur_time_off_index =time_off_title.length-1;
                    else
                        cur_time_off_index--;

                    setTimeOff(cur_time_off_index);
                    break;

                case R.id.rl_boot_input:
                    if (boot_source_index==0)
                        boot_source_index =boot_source_name.length-1;
                    else
                        boot_source_index--;

                    otherSettingsBinding.bootInputTv.setText(boot_source_name[boot_source_index]);
                    set_power_signal(boot_source_value[boot_source_index]);
                    break;
                case R.id.rl_power_mode:
                    if (power_mode_index==0)
                        power_mode_index =power_mode_name.length-1;
                    else
                        power_mode_index--;

                    otherSettingsBinding.powerModeTv.setText(power_mode_name[power_mode_index]);
                    break;
            }
        }else if (keyCode==KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() ==KeyEvent.ACTION_UP){
            switch (v.getId()){
                case R.id.rl_screen_saver:
                    if (cur_screen_saver_index==screen_saver_title.length-1)
                        cur_screen_saver_index =0;
                    else
                        cur_screen_saver_index++;
                    updateScreenSaver(cur_screen_saver_index);
                    break;
                case R.id.rl_timer_off:
                    if (cur_time_off_index==time_off_title.length-1)
                        cur_time_off_index =0;
                    else
                        cur_time_off_index++;

                    setTimeOff(cur_time_off_index);
                    break;
                case R.id.rl_boot_input:
                    if (boot_source_index==boot_source_name.length-1)
                        boot_source_index =0;
                    else
                        boot_source_index++;

                    otherSettingsBinding.bootInputTv.setText(boot_source_name[boot_source_index]);
                    set_power_signal(boot_source_value[boot_source_index]);
                    break;
                case R.id.rl_power_mode:
                    if (power_mode_index==power_mode_name.length-1)
                        power_mode_index =0;
                    else
                        power_mode_index++;

                    otherSettingsBinding.powerModeTv.setText(power_mode_name[power_mode_index]);
                    break;
            }
        }

        return false;
    }
}