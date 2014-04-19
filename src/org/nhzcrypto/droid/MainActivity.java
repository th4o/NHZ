package org.nhzcrypto.droid;

import org.nhzcrypto.accounts.AccountsManager;
import org.nhzcrypto.addresses.AddressesManager;
import org.nhzcrypto.node.NodeContext;
import org.nhzcrypto.node.NodesActivity;
import org.nhzcrypto.node.NodesManager;
import org.nhzcrypto.bottombar.Bar;
import org.nhzcrypto.service.NHZBackgroudService;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;

public class MainActivity extends Activity {
    
    private MainFunctionPage mMainFunctionPage;

    public static final int REQUEST_CODE_NODES = 123;
    /**
     * node information bar
     */
    private TextView mNodeInfoView;
    private ImageButton mChangeNodeButton;
    private NodeContext mNodeContext;
    private View mStateIconView;
    
    private void nodeInfoBarInit(){
        mNodeInfoView = (TextView)this.findViewById(R.id.text_node_info);
        mChangeNodeButton = (ImageButton)this.findViewById(R.id.btn_change_node);
        mStateIconView = (View)this.findViewById(R.id.view_state);
        mChangeNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivityForResult(
                        new Intent(MainActivity.this, NodesActivity.class), REQUEST_CODE_NODES);
            }
        });
    }

    private void nodeInfoBarUpdateResponse(){
        mNodeInfoView.setText(mNodeContext.getIP());
        if ( mNodeContext.isActive() ){
            mStateIconView.setBackgroundResource(R.drawable.icon_online);
            mNodeInfoView.setTextColor(Color.GREEN);
            NodesManager.sharedInstance().save(this);
        }
        else{
            mStateIconView.setBackgroundResource(R.drawable.icon_offline);
            mNodeInfoView.setTextColor(Color.RED);
        }
    }
    
    private void nodeInfoBarUpdate(){
        mNodeContext.setNodeUpdateListener(new NodeContext.NodeUpdateListener(){
            @Override
            public void onUpdate(NodeContext node) {
                Message msg = new Message();
                msg.what = MSG_NODE_UPDATE;
                msg.obj = MainActivity.this;
                mMessageHandler.sendMessageDelayed(msg, 1000);
            }});
        
        mNodeContext.update();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( REQUEST_CODE_NODES == requestCode && resultCode == RESULT_OK ){
            mStateIconView.setBackgroundResource(R.drawable.icon_offline);
            mNodeInfoView.setTextColor(Color.RED);
            nodeInfoBarUpdate();
        }
    }
    
    /**
     * change node
     */

    //
    // message process
    //
    static private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ( msg.obj instanceof MainActivity ){
                MainActivity instance = (MainActivity)msg.obj;
                instance.handleMessage(msg);
            }
        }
    };

    static final private int MSG_NODE_UPDATE = 0;
    public void handleMessage(Message msg) {
        switch (msg.what){
            case MSG_NODE_UPDATE:
                nodeInfoBarUpdateResponse();
                mMainFunctionPage.update();
                break;
            default:
                break;
        }
    }
    

    private Bar mBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        AccountsManager.sharedInstance().init(this);
        AddressesManager.sharedInstance().init(this);
        NodesManager.sharedInstance().init(this);
        mNodeContext = NodesManager.sharedInstance().getCurrentNode();
        nodeInfoBarInit();
        mMainFunctionPage = new MainFunctionPage(this);

        View priceBar = this.findViewById(R.id.price_bar);
        mBar = new Bar(priceBar);
        Settings.sharedInstance().registerSettingsChangeListener(mSettingsChangeListener);

        if ( !Settings.sharedInstance().isShowingPrice(this) )
            priceBar.setVisibility(View.GONE);
        
    }
    
    private Settings.SettingsChangeListener mSettingsChangeListener = new Settings.SettingsChangeListener(){
        @Override
        public void onChange(Settings settings) {
        }};
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        Settings.sharedInstance().unregisterSettingsChangeListener(mSettingsChangeListener);
        NodesManager.sharedInstance().release(this);
        AccountsManager.sharedInstance().release(this);
        AddressesManager.sharedInstance().release(this);
        mMainFunctionPage.release();
    }

    @Override
    public void onResume(){
        super.onResume();
        nodeInfoBarUpdate();
        mMainFunctionPage.update();
        if ( Settings.sharedInstance().isShowingPrice(this) ){

        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
//        if ( R.id.menu_exit == item.getItemId() ){
//            Intent serviceIntent = new Intent(this, NHZBackgroudService.class);
//            this.stopService(serviceIntent);
//            finish();
//        }
        return super.onOptionsItemSelected(item);
    }
}
