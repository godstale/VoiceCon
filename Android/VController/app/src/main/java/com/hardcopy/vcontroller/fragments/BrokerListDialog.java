package com.hardcopy.vcontroller.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hardcopy.vcontroller.R;

public class BrokerListDialog extends Dialog {

    // Global
    public static final String tag = "BrokerListDialog";
    
    private String mDialogTitle;
    
    // Context, system
    private Context mContext;
    private IDialogListener mDialogListener;
    private OnClickListener mClickListener;
    
    // Layout
    private Button mBtnConnect;
    private Button mBtnDisconnect;
    private Button mBtnDelete;
    private Button mBtnClose;
    
    private TextView mTextEnabled;
    
    // Params
    private Object mContentObject;
    
    // Constructor
    public BrokerListDialog(Context context) {
        super(context);
        mContext = context;
    }
    public BrokerListDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }
    
    /*****************************************************
     *        Overrided methods
     ******************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        //----- Set title
        if(mDialogTitle != null) {
            setTitle(mDialogTitle);
        } else {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();    
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_conn_command_list);
        mClickListener = new OnClickListener(this);
        
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(mClickListener);
        mBtnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        mBtnDisconnect.setOnClickListener(mClickListener);
        mBtnDelete = (Button) findViewById(R.id.btn_delete);
        mBtnDelete.setOnClickListener(mClickListener);
        mBtnClose = (Button) findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(mClickListener);

        setContent();
    }
    
    @Override
    protected  void onStop() {
        super.onStop();
    }

    
    /*****************************************************
     *        Public methods
     ******************************************************/
    public void setDialogParams(IDialogListener listener, String title, Object co) {
        mDialogListener = listener;
        mDialogTitle = title;
        mContentObject = co;
    }
    
    /*****************************************************
     *        Private methods
     ******************************************************/
    private void setContent() {
        // TODO: do what you want
    }    // End of setContent()
    
    /*****************************************************
     *        Sub classes
     ******************************************************/
    private class OnClickListener implements View.OnClickListener {
        BrokerListDialog mDialogContext;
        
        public OnClickListener(BrokerListDialog context) {
            mDialogContext = context;
        }
        
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.btn_connect:
                    mDialogContext.dismiss();
                    if(mDialogListener != null)
                        mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_CONNECTION_CONNECT, 0, 0, null, null, mContentObject);
                    break;

                case R.id.btn_disconnect:
                    mDialogContext.dismiss();
                    if(mDialogListener != null)
                        mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_CONNECTION_DISCONNECT, 0, 0, null, null, mContentObject);
                    break;

                case R.id.btn_delete:
                    mDialogContext.dismiss();
                    if(mDialogListener != null)
                        mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_CONNECTION_DELETE, 0, 0, null, null, mContentObject);
                    break;

                case R.id.btn_close:
                    mDialogContext.dismiss();
                    if(mDialogListener != null)
                        mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_CLOSE, 0, 0, null, null, mContentObject);
                    break;
            }
        }
    }    // End of class OnClickListener
}
