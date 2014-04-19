package org.nhzcrypto.addresses;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.Zxing.CaptureActivity;
import org.nhzcrypto.accounts.Account;
import org.nhzcrypto.accounts.AccountsManager;
import org.nhzcrypto.alias.Alias;
import org.nhzcrypto.alias.AliasInputDialog;
import org.nhzcrypto.droid.QRCodeParse;
import org.nhzcrypto.droid.R;
import org.nhzcrypto.transactions.SendCoinsActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.other.util.QRCode;

public class AddressesPage {
    private Context mContext;
    private View mAddressesPage;
    private AddressesListView mAddressesListView;
    public AddressesPage(View addressesPage){
        mAddressesPage = addressesPage;
        mContext = mAddressesPage.getContext();
        mAddressesListView = (AddressesListView)mAddressesPage.findViewById(R.id.listview_addresses);
        mAddressesListView.setAccountList(AddressesManager.sharedInstance().getAccountList());
        mAddressesListView.setOnItemClickListener(new AddressesListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                openItemMenu(arg2);
            }});
        mAddressesListView.setIconOnClickListener(new AddressesListView.IconOnClickListener() {
            @Override
            public void onClick(int iconType, int pos) {
                if ( AddressesListView.ICON_TYPE_SEND == iconType )
                    sendNHZ(pos);
            }
        });

        Button btnAdd = (Button)mAddressesPage.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInputTypeSelectDialog();
            }
        });
        
        mItemOptions = new CharSequence[4];
        mItemOptions[0] = mContext.getText(R.string.send_nhz);
        mItemOptions[1] = mContext.getText(R.string.qrcode);
        mItemOptions[2] = mContext.getText(R.string.edit);
        mItemOptions[3] = mContext.getText(R.string.remove);
        
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mAccountInputView = inflater.inflate(R.layout.account_input, null);
        mEditTextId = (EditText)mAccountInputView.findViewById(R.id.edittext_accid_input);
        mEditTextTag = (EditText)mAccountInputView.findViewById(R.id.edittext_acctag_input);
    }
    
    private void sendNHZ(int pos){
        Account account = AddressesManager.sharedInstance().getAccountList().get(pos);
        SendCoinsActivity.open(mContext, "", account.mId);
    }

    /**
     * items menu --- transactions, remove, etc
     */
    private CharSequence[] mItemOptions;
    private int mCurrentItemPos;
    private void openItemMenu(int pos){
        LinkedList<Account> accountList = AddressesManager.sharedInstance().getAccountList();
        if ( null == accountList || 0 == accountList.size() )
            return;
        
        mCurrentItemPos = pos;
        new AlertDialog.Builder(mContext)
        .setTitle(accountList.get(pos).mId)
        .setItems(mItemOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if ( 3 == which ){
                    AddressesManager.sharedInstance().removeAccount(mContext, mCurrentItemPos);
                    LinkedList<Account> accList = AddressesManager.sharedInstance().getAccountList();
                    mAddressesListView.setAccountList(accList);
                }else if ( 2 == which ){
                    openAccountInputDialog(true, mCurrentItemPos);
                }else if ( 1 == which ){
                    Account acc = AddressesManager.sharedInstance().getAccountList().get(mCurrentItemPos);
                    QRCode.showQRCode((Activity)mContext, acc.mId);
                }else if ( 0 == which ){
                    sendNHZ(mCurrentItemPos);
                }
            }
        })
        .show();
    }
    
    private void openInputTypeSelectDialog(){
        CharSequence options[] = new CharSequence[3];
        options[0] = mContext.getText(R.string.add_by_alias);
        options[1] = mContext.getText(R.string.add_by_number);
        options[2] = mContext.getText(R.string.qrcode_scan);
        
        new AlertDialog.Builder(mContext)
        .setTitle(R.string.add_account)
        .setItems(options, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if ( 2  == arg1 )
                    accountQRCodeScan();
                else if ( 1  == arg1 )
                    openAccountInputDialog(false, 0);
                else
                    openAliasInputDialog();
            }})
        .setNegativeButton(R.string.back, null)
        .show();
    }

    private void accountQRCodeScan(){
        //mContext.startActivity(new Intent(mContext, CaptureActivity.class));
        CaptureActivity.startScanCode(mContext, new CaptureActivity.CodeReceiver() {
            @Override
            public boolean onResult(boolean success, String code) {
                if ( success ){
                    String regex = "^\\d+$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(code);
                    if (matcher.find()) {
                        AddressesManager.sharedInstance().addAccount(mContext, code, "QR");
                        mAddressesListView.notifyDataSetInvalidated();
                        return true;
                    }
                    String msg = (String) mContext.getText(R.string.qrcode_no_acc);
                    msg += "   \r\n\r\n" + code;
                    new AlertDialog.Builder(mContext)
                            .setMessage(msg)
                            .setNegativeButton(R.string.back, null)
                            .show();
                    return false;                	
                }
                
                return false;
            }
        });
    }

    private void openAliasInputDialog(){
        new AliasInputDialog().open(mContext, new Alias.AliasResponse() {
            @Override
            public void onResult(int result, Alias alias) {
                if ( Alias.RESULT_SUCCESS == result ){
                    AddressesManager.sharedInstance().addAccount(mContext, alias);
                    mAddressesListView.setAccountList(
                            AddressesManager.sharedInstance().getAccountList());
                    
                }else{
                    Message msg = new Message();
                    msg.obj = AddressesPage.this;
                    msg.what = MSG_ERROR_INFO;
                    if ( Alias.RESULT_NOT_EXIST == result )
                        msg.arg1 = R.string.alias_not_exist;
                    else if ( Alias.RESULT_NO_ACC == result )
                        msg.arg1 = R.string.alias_no_acc;
                    else
                        msg.arg1 = R.string.alias_failed;
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private View mAccountInputView;
    private AlertDialog mAccountInputDialog;
    private EditText mEditTextId;
    private EditText mEditTextTag;
    private boolean mIsEdit;
    private Account mEditAccount;
    private void openAccountInputDialog(boolean isEdit, int pos){
        mIsEdit = isEdit;

        if ( mIsEdit ){
            mEditAccount = AddressesManager.sharedInstance().getAccountList().get(pos);
            mEditTextId.setText(mEditAccount.mId);
            mEditTextTag.setText(mEditAccount.mTag);
        }else{
            mEditTextId.setText("");
            mEditTextTag.setText("");
        }

        if ( null == mAccountInputDialog ){
            mAccountInputDialog = new AlertDialog.Builder(mContext)
            .setTitle(R.string.add)
            .setView(mAccountInputView)
            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    String accid = mEditTextId.getText().toString();
                    if ( accid.length() <= 0 )
                        return;
                    
                    String tag = mEditTextTag.getText().toString();
                    if ( tag.length() <= 0 )
                        tag = "null";
                    
                    if ( mIsEdit ){
                        mEditAccount.mId = accid;
                        mEditAccount.mTag = tag;
                        AddressesManager.sharedInstance().saveAccountList(mContext);
                        mAddressesListView.notifyDataSetInvalidated();
                    }else{
                        AddressesManager.sharedInstance().addAccount(
                                mContext, accid, tag);
                        mAddressesListView.setAccountList(
                                AddressesManager.sharedInstance().getAccountList());
                    }
                }})
            .setNegativeButton(R.string.back, null)
            .create();
        }
        mAccountInputDialog.show();
    }
    
    /**
     * handle msg to update UI
     * @param msg
     */
    private static final int MSG_ERROR_INFO = 0;
    public void handleMessage(Message msg) {
        if ( MSG_ERROR_INFO == msg.what )
            Toast.makeText(mContext, msg.arg1, Toast.LENGTH_LONG).show();
    }

    static private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ( msg.obj instanceof AddressesPage ){
                AddressesPage instance = (AddressesPage)msg.obj;
                instance.handleMessage(msg);
            }
        }
    };
    
    public void update(){
    }
    
    public void release(){
        
    }
}
