package org.nhzcrypto.droid;

import org.nhzcrypto.alias.AliasAssignActivity;
import org.nhzcrypto.alias.AliasCheck;
import org.nhzcrypto.service.NHZBackgroudService;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

public class ToolsPage {

    private Context mContext;    
    private CheckBox mCheckBoxNotification;
    private View mViewNotification;
    private CheckBox mCheckBoxNotificationVibrate;
    private Spinner mSpinnerInterval;
    
    public ToolsPage(View page){
        mContext = page.getContext();
        
        // transaction notification
        mCheckBoxNotification = (CheckBox)page.findViewById(R.id.check_notification);
        mCheckBoxNotification.setChecked(Settings.sharedInstance().isNotificationEnable(mContext));
        mCheckBoxNotification.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.sharedInstance().setNotificationEnable(mContext, isChecked);
                updateStatus();
            }});

        // notification vibrate
        mCheckBoxNotificationVibrate = (CheckBox)page.findViewById(R.id.check_vibrate);
        mCheckBoxNotificationVibrate.setChecked(Settings.sharedInstance().isNotificationVibrateEnable(mContext));
        mCheckBoxNotificationVibrate.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.sharedInstance().setNotificationVibrateEnable(mContext, isChecked);
            }});
        
        // transactions refresh interval
        String options[] = new String[3];
        options[0] = "30" + (String)mContext.getText(R.string.minute);
        options[1] = "60" + (String)mContext.getText(R.string.minute);
        options[2] = "120" + (String)mContext.getText(R.string.minute);
        ArrayAdapter<String> spinnerAdapter = 
                new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item, options);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerInterval = (Spinner)page.findViewById(R.id.spinner_interval);
        mSpinnerInterval.setAdapter(spinnerAdapter);
        int index = Settings.sharedInstance().getNotificationInterval(mContext);
        mSpinnerInterval.setSelection(index);
        mSpinnerInterval.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                Settings.sharedInstance().setNotificationInterval(mContext, arg2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }});
        
        mViewNotification = page.findViewById(R.id.layout_interval);
        
        Button btnAliasCheck = (Button)page.findViewById(R.id.btn_alias_check);
        btnAliasCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AliasCheck().open(mContext);
            }
        });
        
        Button btnAliasAssign = (Button)page.findViewById(R.id.btn_alias_assign);
        btnAliasAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, AliasAssignActivity.class));
            }
        });
        
        updateStatus();
    }
    
    static public int getRefreshInterval(Context context){
        int index = Settings.sharedInstance().getNotificationInterval(context);
        if ( 0 == index )
            return 30;
        else if ( 2 == index )
            return 60;
        else
            return 120;
    }

    public void updateStatus(){
        if ( Settings.sharedInstance().isNotificationEnable(mContext) ){
            mViewNotification.setVisibility(View.VISIBLE);
            mCheckBoxNotificationVibrate.setVisibility(View.VISIBLE);
            Intent serviceIntent = new Intent(mContext, NHZBackgroudService.class);
            mContext.startService(serviceIntent);
        }else{
            mViewNotification.setVisibility(View.GONE);
            mCheckBoxNotificationVibrate.setVisibility(View.GONE);
            Intent serviceIntent = new Intent(mContext, NHZBackgroudService.class);
            mContext.stopService(serviceIntent);
        }
    }
    
    public void update(){
        
    }
    
    public void release(){
        
    }
}
