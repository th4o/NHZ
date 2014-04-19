package org.nhzcrypto.transactions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

import org.nhzcrypto.node.NodeContext;
import org.nhzcrypto.node.NodesManager;
import org.nhzcrypto.util.Crypto;
import org.nhzcrypto.util.NHZUtil;


public class SendCoin {
    static private class SendRunnale implements Runnable{
        String mSecret;
        String mRecipient;
        float mAmount;
        ResponseListener mListener;
        public SendRunnale(String secret, String recipient, float amount, ResponseListener listener){
            mSecret = secret;
            mRecipient = recipient;
            mAmount = amount;
            mListener = listener;
        }
        
        @Override
        public void run() {
            SendCoin sender = new SendCoin(mListener);
            sender.send(mSecret, mRecipient, mAmount);
        }
    }

    static public void sendCoin(String secret, String recipient, float amount, ResponseListener listener){
        new Thread(new SendRunnale(secret, recipient, amount, listener)).start();
    }

    public interface ResponseListener{
        public void onResponse(boolean success, String info);
    }

    private ResponseListener mResponseListener;
    private SendCoin(ResponseListener listener){
        mResponseListener = listener;
    }

    private void send(String secret, String recipientStr, float amountF){
        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
        if ( !nodeContext.isActive()){
            mResponseListener.onResponse(false, null);
            return;
        }
        
        byte type = NHZTransaction.TYPE_PAYMENT;
        byte subtype = NHZTransaction.SUBTYPE_PAYMENT_ORDINARY_PAYMENT;
        int timestamp = nodeContext.getTimestamp();
        short deadline = 1500;
        byte[] senderPublicKey = Crypto.getPublicKey(secret);
        long recipient = new BigInteger(recipientStr).longValue();;
        int amount = (int)amountF;
        int fee = 1;
        long referencedTransaction = 0;;
        byte[] signature = new byte[64];
        
        NHZTransaction orgTransaction = new NHZTransaction(type, subtype, timestamp, deadline, 
                senderPublicKey, recipient, amount, fee, referencedTransaction, signature);
        orgTransaction.sign(secret);
        String transactionBytes = NHZUtil.convert(orgTransaction.getBytes());

        String ip = nodeContext.getIP();
        String base_url = "http://" + ip + ":7776";
        String httpUrl = String.format(
                "%s/nhz?requestType=broadcastTransaction&transactionBytes=%s", 
                base_url, transactionBytes);

        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(httpUrl).openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while((line = br.readLine()) != null)
                sb.append(line);

            String strResult = sb.toString();
            mResponseListener.onResponse(true, strResult);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }        
        mResponseListener.onResponse(false, null);
    }
}
