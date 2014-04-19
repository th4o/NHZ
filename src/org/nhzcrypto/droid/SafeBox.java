package org.nhzcrypto.droid;

import java.util.HashMap;

import org.nhzcrypto.node.NodesManager;
import org.nhzcrypto.util.NHZApi;
import org.nhzcrypto.util.NHZUtil;

import android.content.Context;
import android.content.SharedPreferences;

import com.other.util.MyApplication;


public class SafeBox {
    private HashMap<String, String> mSecretMap;
    public void unlock(String account, String secret){
        mSecretMap.put(account, secret);
    }
    
    public void lock(String account){
        mSecretMap.remove(account);
    }
    
    public String getSecret(String account){
        return mSecretMap.get(account);
    }
    
    public boolean isUnlock(String account){
        return null != mSecretMap.get(account);
    }
    
    public byte[] getPublicKey(String account){
        Context context = MyApplication.getAppContext();
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        String key = prefer.getString(account, null);
        if ( null != key ){
            return NHZUtil.convert(key);
        }
        String addr = NodesManager.sharedInstance().getCurrentNode().getIP();
        byte[] publicKey = NHZApi.getPublicKey(addr, account);
        if ( null != publicKey ){
            SharedPreferences.Editor editor = prefer.edit();
            editor.putString(account, NHZUtil.convert(publicKey));
            editor.commit();
        }

        return publicKey;
    }

    final static private String mPrefFileName = "SafeBoxPublicKeyPrefFile";
    private void init(){
        mSecretMap = new HashMap<String, String>();
    }
    
    /**
     * Singleton
     */
    public static SafeBox sharedInstance(){
        if ( null == mSafeBox )
            mSafeBox = new SafeBox();

        return mSafeBox;
    }
    
    private static SafeBox mSafeBox;
    private SafeBox(){
        init();
    }
}
