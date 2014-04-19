package org.nhzcrypto.service;

import java.util.LinkedList;

import org.nhzcrypto.accounts.Account;
import org.nhzcrypto.accounts.AccountsManager;
import org.nhzcrypto.node.NodeContext;
import org.nhzcrypto.node.NodesManager;
import org.nhzcrypto.transactions.Transaction;
import org.nhzcrypto.util.NHZUtil;

import android.content.Context;

public class InfoCenter {

    
    public class AccountInfo{
        public String mId;
        public int mTime;
    }
    
    private LinkedList<AccountInfo> mAccountInfoList;
    
    private AccountInfo getAccountInfo(String accId){
        if ( null == mAccountInfoList )
            return null;

        for ( AccountInfo info : mAccountInfoList ){
            if ( info.mId.equals(accId) )
                return info;
        }
        return null;
    }
    
    public void setAccountList(LinkedList<Account> accList){
        LinkedList<AccountInfo> accountInfoList = new LinkedList<AccountInfo>();
        
        for ( Account acc : accList ){
            AccountInfo info = getAccountInfo(acc.mId);
            if ( null == info ){
                info = new AccountInfo();
                info.mId = acc.mId;
                info.mTime = NHZUtil.getTimestamp();
            }
            accountInfoList.add(info);
        }
        
        mAccountInfoList = accountInfoList;
    }
    
    public void init(){
        NodeContext mNodeContext = NodesManager.sharedInstance().getCurrentNode();
        LinkedList<Account> mAccountList = AccountsManager.sharedInstance().getAccountList();
        if ( null == mNodeContext || null == mAccountList )
            return;

        setAccountList(mAccountList);
    }

    public void refresh(Context context){
        NodeContext mNodeContext = NodesManager.sharedInstance().getCurrentNode();
        LinkedList<Account> mAccountList = AccountsManager.sharedInstance().getAccountList();
        if ( null == mNodeContext || null == mAccountList )
            return;

        setAccountList(mAccountList);

        for ( AccountInfo info : mAccountInfoList ){
            LinkedList<Transaction> list = Transaction.getTransactionList(
                    mNodeContext, info.mId, info.mTime);
            if ( null != list && list.size() > 0 ){
                info.mTime = NHZUtil.getTimestamp();
                for ( Transaction transaction : list ){
                    Transaction.loadTransaction(transaction);
                    NotifySender.send(context, transaction, info);
                }
            }
        }
    }
    
    private static InfoCenter mInfoCenter;
    public static InfoCenter shardInstance(){
        if ( null == mInfoCenter )
            mInfoCenter = new InfoCenter();

        return mInfoCenter;
    }
}
