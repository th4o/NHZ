package org.nhzcrypto.util;

import java.math.BigInteger;

import org.json.JSONObject;
import org.nhzcrypto.node.NodesManager;
import org.nhzcrypto.transactions.NHZTransaction;
import org.nhzcrypto.transactions.NHZTransaction.MessagingAliasAssignmentAttachment;
import org.nhzcrypto.transactions.NHZTransaction.MessagingArbitraryMessageAttachment;

import com.other.util.HttpUtil;

public class NHZApi {
    public interface ResponseListener{
        public void onResponse(boolean success, String info);
    }

    private ResponseListener mResponseListener;
    
    static public NHZTransaction makeAliasTransaction(String secret, String alias, String uri, int fee, short deadline){
        byte type = NHZTransaction.TYPE_MESSAGING;
        byte subtype = NHZTransaction.SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT;
        int timestamp = NodesManager.sharedInstance().getCurrentNode().getTimestamp();
        byte[] senderPublicKey = Crypto.getPublicKey(secret);
        long recipient = new BigInteger("1739068987193023818").longValue();;
        int amount = 0;
        long referencedTransaction = 0;;
        byte[] signature = new byte[64];

        NHZTransaction orgTransaction = new NHZTransaction(type, subtype, timestamp, deadline, 
                senderPublicKey, recipient, amount, fee, referencedTransaction, signature);
        
        MessagingAliasAssignmentAttachment attach = new MessagingAliasAssignmentAttachment(alias, uri);
        orgTransaction.setAttachment(attach);
        
        orgTransaction.sign(secret);
        
        return orgTransaction;
    }

    static public NHZTransaction makeArbitraryMessageTransaction(String secret, String acctId, byte[] message, short deadline){
        byte type = NHZTransaction.TYPE_MESSAGING;
        byte subtype = NHZTransaction.SUBTYPE_MESSAGING_ARBITRARY_MESSAGE;
        int timestamp = NodesManager.sharedInstance().getCurrentNode().getTimestamp();
        byte[] senderPublicKey = Crypto.getPublicKey(secret);
        long recipient = new BigInteger(acctId).longValue();;
        int amount = 0;
        int fee = 1;
        long referencedTransaction = 0;;
        byte[] signature = new byte[64];

        NHZTransaction orgTransaction = new NHZTransaction(type, subtype, timestamp, deadline, 
                senderPublicKey, recipient, amount, fee, referencedTransaction, signature);
        
        MessagingArbitraryMessageAttachment attach = 
                new MessagingArbitraryMessageAttachment(message);
        orgTransaction.setAttachment(attach);
        
        orgTransaction.sign(secret);
        
        return orgTransaction;
    }

    private String mAddr;
    private String mBytes;
    public void broadcastTransaction(String addr, NHZTransaction transaction, ResponseListener listener){
        mAddr = addr;
        mBytes = NHZUtil.convert(transaction.getBytes());
        mResponseListener = listener;
        new Thread(new Runnable(){
            @Override
            public void run() {
                String base_url = "http://" + mAddr + ":7776";
                String httpUrl = String.format(
                        "%s/nhz?requestType=broadcastTransaction&transactionBytes=%s", 
                        base_url, mBytes);
                
                try {
                    String result = HttpUtil.getHttp(httpUrl);
                    mResponseListener.onResponse(true, result);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                mResponseListener.onResponse(false, null);
            }}).start();
    }
    

    static public byte[] getPublicKey(String addr, String accountId){
        String base_url = "http://" + addr + ":7776";
        String httpUrl = String.format(
                "%s/nhz?requestType=getAccountPublicKey&account=%s", 
                base_url, accountId);

        try {
            String result = HttpUtil.getHttp(httpUrl);
            JSONObject jsonObj = new JSONObject(result);
            String hex = null;

            if (!jsonObj.isNull("publicKey")){
                hex = jsonObj.getString("publicKey");
                return NHZUtil.convert(hex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
