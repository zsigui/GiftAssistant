package com.excelliance.open.platform;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

import java.lang.Thread;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.widget.*;
import android.view.View;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.Serializable;

import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.excelliance.open.SimpleUtil;

public class NextChapter extends Activity {
    private final String TAG = "PlatformService NextChapter";
	private Parcelable nextChapterParcel = null;
    private String mPackageName;
    private Resources mResources;
    private Serializable detail = null;
	private Serializable localDetail = null;
	private String rGameId = null;
	private String rSavePath = null;
	private String rForceUpdate = null;
	private long rSize = 0;
	private boolean rPatch = false;
	
    private TextView textStatus;
    private TextView textProgress;
    private ProgressBar progressBar;
    private Dialog exitDialog = null;
    private Dialog notEnoughSpaceDialog = null;
    private Dialog dlWithoutWifiDialog = null;
    private Dialog unfinishedDlDialog = null;
    private Dialog updateWithoutWifiDialog = null;
    private Dialog dlBackgroundWithoutWifiDialog = null;
    private Runnable runnable = null;
    private String strStatus = null;
    private int timer = 0;
    private boolean showProgressbar = true;
    private boolean patching = false;
    Thread mThread = null;
    private volatile boolean stopRequested;

    public static final String ACTION_NOTIFY = ".action.NextChapter.Notify";
    public static final String ACTION_DLOAD = ".action.NextChapter.Download";
    protected static final int STOP = 0x10000;
    protected static final int NEXT = 0x10001;
    private int iCount = 0;
    private String dlType;
	public static final String GAME_RUNNING = "gameRunning";
	private boolean gameRunning = false;
	
	public static String GAMEID = null;
	public static String PROGRESS = null;
	public static String DOWNLOADED = null;
    public static String SIZE = null;
	public static String ERROR = null;
	public static String PROGRESS_INTENT = null;
	public static String DOWNLOADED_INTENT = null;
	public static String ERROR_INTENT = null;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case STOP:
                Thread.currentThread().interrupt();
                break;
            case NEXT:
                if(!Thread.currentThread().isInterrupted()) {
                    if(iCount<=100)
                        progressBar.setProgress(iCount);
                }
                break;
            }
        }
    };

	//download information receiver
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
			callNextChapterParcel("onReceive", new Class[]{Context.class, Intent.class}, new Object[]{context, intent});
            String action = intent.getAction();
            String packageName = getPackageName();
            int resId = 0;
            int resId2 = 0;
            if (action.equals(packageName+ERROR_INTENT)) {//download error
                String gameId = intent.getStringExtra(GAMEID);
                String error = intent.getStringExtra(ERROR);
                Log.d(TAG, "gameId="+gameId+" error="+error+" detail="+detail);
                showProgressbar = true;
                if (detail==null || rGameId==null || gameId==null || !gameId.equals(rGameId))
                    return;
                stopRequested = true;
                strStatus = error;
                handler.removeCallbacks(runnable);
                textStatus.setText(strStatus);
            } else if (action.equals(PROGRESS_INTENT)) {//download progress
                Log.d(TAG, "action PROGRESS_INTENT");
                String gameId = intent.getStringExtra(GAMEID);
                int progress = intent.getIntExtra(PROGRESS, 0);
                Log.d(TAG, "gameId="+gameId+" progress="+progress+" detail="+detail);
                if (detail==null || rGameId==null || gameId==null || !gameId.equals(rGameId))
                    return;

                if(progress == -2){
                    return;
                }

                if(progress == -1) {
                    handler.removeCallbacks(runnable);
                    resId = getResources().getIdentifier("lebian_dl_dl_failed", "string", packageName);
                    strStatus = getResources().getString(resId);
                    textStatus.setText(strStatus);
                    showProgressbar = true;
                    return;
                }
                if(dlWithoutWifiDialog != null && dlWithoutWifiDialog.isShowing()) {
                    dlWithoutWifiDialog.dismiss();
                }
                if(unfinishedDlDialog != null && unfinishedDlDialog.isShowing()) {
                    unfinishedDlDialog.dismiss();
                }
                if(updateWithoutWifiDialog != null && updateWithoutWifiDialog.isShowing()) {
                    updateWithoutWifiDialog.dismiss();
                }

                if (progress==100 && detail!=null) {
                    handler.removeCallbacks(downloadCallBack);
                    stopRequested = true;
                    Thread tmpThread = mThread;
                    mThread = null;
                    if (tmpThread != null) {
                        tmpThread.interrupt();
                    }
                }

                progressBar.setProgress(progress);
				if(progress < 5){
					progressBar.setProgress(5);
				}
                int pro = progress;
                if(progress == 99 && rPatch) {
                    patching = true;
                    pro = 100;
                    resId = getResources().getIdentifier("lebian_dl_status_extract", "string", packageName);
                    strStatus = getResources().getString(resId);
                    textProgress.setVisibility(View.GONE);
                    progressBar.setProgress(0);
                    mThread = new Thread(new Runnable() {
                        public void run() {
                            stopRequested = false;

                            while ( !stopRequested ) {
                                try {
                                    iCount = (iCount + 1);
                                    Thread.sleep(1200);
                                    Message msg = handler.obtainMessage(NEXT);
                                    handler.sendMessage(msg);
                                } catch (InterruptedException x) {
                                    Thread.currentThread().interrupt();
                                }
                            }

                        }
                    });
                    mThread.start();
                }

                if (progress==100 && detail!=null) {
                    showProgressbar = true;
                    resId = getResources().getIdentifier("lebian_dl_status_downloaded", "string", packageName);
                    strStatus = getResources().getString(resId);
					textProgress.setText(String.format("%.2fMB/%.2fMB",((float)(rSize))/(1024.0f * 1024.0f),((float)(rSize))/(1024.0f * 1024.0f)));
                }
            } else if (action.equals(packageName+DOWNLOADED_INTENT)) {//downloaded size
                String gameId = intent.getStringExtra(GAMEID);
                int downloaded = intent.getIntExtra(DOWNLOADED, 0);
                int size = intent.getIntExtra(SIZE, 0);
                if(size > 0 && rSize != size){
                    Log.d(TAG, "size change rSize:"+rSize);
                    rSize = size;
                }
                if (detail==null || rGameId==null || gameId==null || !gameId.equals(rGameId))
                    return;
				if(showProgressbar && downloaded>0) {
                    resId = getResources().getIdentifier("lebian_dl_status_updating", "string", packageName);
                    resId2 = getResources().getIdentifier("lebian_dl_status_downloading", "string", packageName);
                    strStatus = getResources().getString(dlType.equals("update")?resId:resId2);
                    textStatus.setText(strStatus);
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 800);
                    progressBar.setVisibility(View.VISIBLE);
                    textProgress.setVisibility(View.VISIBLE);
                    showProgressbar = false;

                    Log.d(TAG, "PROGRESS_INTENT start downloadCallBack");
                    handler.removeCallbacks(downloadCallBack);
                    handler.postDelayed(downloadCallBack, 10000);
                }
				textProgress.setText(String.format("%.2fMB/%.2fMB",((float)(downloaded))/(1024.0f * 1024.0f)*0.9f,((float)(rSize))/(1024.0f * 1024.0f)));
			} else if (action.equals(packageName+ACTION_NOTIFY)) {//network not available
                Log.d(TAG, "network not available");
                handler.removeCallbacks(runnable);
                resId = getResources().getIdentifier("lebian_dl_status_no_network", "string", packageName);
                strStatus = getResources().getString(resId);
                textStatus.setText(strStatus);
            }
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			nextChapterParcel = savedInstanceState.getParcelable("nextChapterParcel");
		}
else {
			try {
                Class clazz = Class.forName("com.excelliance.open.platform.NextChapterParcel");
    			Constructor cons = clazz.getDeclaredConstructor(Parcel.class);
                cons.setAccessible(true);
    			nextChapterParcel = (Parcelable)cons.newInstance((Parcel)null);
            } catch (Exception e) {
                Log.d(TAG, "no NextChapterParcel");
				e.printStackTrace();
            }
		}
		if(nextChapterParcel != null) {
			callNextChapterParcel("init", new Class[]{Activity.class, Handler.class, Bundle.class}, new Object[]{this, handler, savedInstanceState});
		}
		GAMEID = (String)callNextChapterParcel("getGAMEID", null, null);
		PROGRESS = (String)callNextChapterParcel("getPROGRESS", null, null);
		DOWNLOADED = (String)callNextChapterParcel("getDOWNLOADED", null, null);
        SIZE = (String)callNextChapterParcel("getSIZE", null, null);
		ERROR = (String)callNextChapterParcel("getERROR", null, null);
		PROGRESS_INTENT = (String)callNextChapterParcel("getPROGRESS_INTENT", null, null);
		DOWNLOADED_INTENT = (String)callNextChapterParcel("getDOWNLOADED_INTENT", null, null);
		ERROR_INTENT = (String)callNextChapterParcel("getERROR_INTENT", null, null);
		gameRunning = getIntent().getBooleanExtra(GAME_RUNNING, false);
        mPackageName = getPackageName();
        mResources = getResources();
        setContentView(mResources.getIdentifier("lebian_next_chapter", "layout", mPackageName));
		Drawable background = getBackGroundDrawable();
		if(background != null)
			getWindow().setBackgroundDrawable(background);
        int resId = mResources.getIdentifier("lebian_text_status", "id", mPackageName);
        int resId2 = mResources.getIdentifier("lebian_text_progress", "id", mPackageName);
        int resId3 = mResources.getIdentifier("lebian_progressBar", "id", mPackageName);
        textStatus = (TextView)findViewById(resId);
        textProgress = (TextView)findViewById(resId2);
        progressBar = (ProgressBar)findViewById(resId3);
        
		int textcolor = getTextColor();
        textStatus.setTextColor(textcolor);
		textcolor = getProgressTextColor();
        textProgress.setTextColor(textcolor);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        IntentFilter filter = new IntentFilter();
        String packageName = getPackageName();
        filter.addAction(packageName+ACTION_NOTIFY);
        filter.addAction(PROGRESS_INTENT);
		filter.addAction(packageName+DOWNLOADED_INTENT);
        filter.addAction(packageName+ERROR_INTENT);
        registerReceiver(myReceiver, filter);

        runnable=new Runnable() {
            @Override
            public void run() {
                if(timer>=4)
                    timer = 0;
                switch (timer) {
                case 0:
                    textStatus.setText(strStatus);
                    break;
                case 1:
                    textStatus.setText(strStatus+".");
                    break;
                case 2:
                    textStatus.setText(strStatus+"..");
                    break;
                case 3:
                    textStatus.setText(strStatus+"...");
                    break;
                }
                timer++;
                handler.postDelayed(this, 800);
            }
        };

        Intent intent = getIntent();
        resId = getResources().getIdentifier("lebian_dl_status_connecting", "string", packageName);
        strStatus = getResources().getString(resId);
        progressBar.setVisibility(View.VISIBLE);
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 800);
        detail = intent.getSerializableExtra("detail");
        localDetail = intent.getSerializableExtra("localDetail");
        dlType = intent.getStringExtra("dlType");
		rGameId = intent.getStringExtra("gameId");
		rSavePath = intent.getStringExtra("savePath");
		rForceUpdate = intent.getStringExtra("forceUpdate");
		rSize = intent.getLongExtra("size", 0);
		rPatch = intent.getBooleanExtra("patch", false);
		String mainActivity = intent.getStringExtra("mainActivity");
        if (detail != null) {
            Log.d(TAG, "gameId:"+rGameId+", savePath:"+rSavePath);
            if (rSavePath == null) {
                Log.d(TAG, "space not enough");
                showNotEnoughSpaceDialog();
            } else {
                Log.d(TAG, "action.NextChapterBC savePath != null");
				boolean rmAutoDl = ((Boolean)callNextChapterParcel("getREMOVE_AUTO_DOWNLOAD", null, null)).booleanValue();
                if ((rmAutoDl|| SimpleUtil.dataConnection(NextChapter.this)) && !gameRunning) {
					boolean updatePrompt = getSharedPreferences("excl_lb_prompt", MODE_PRIVATE).getBoolean("gameUpdate", false);
                    if(dlType.equals("update")) {
                        if(!updatePrompt && localDetail!=null)
                            showUpdateWithoutWifiDialog(detail,localDetail, SimpleUtil.dataConnection(NextChapter.this));
                        else if(localDetail!=null){
                            progressBar.setVisibility(View.GONE);
                            textProgress.setVisibility(View.GONE);
                            textStatus.setVisibility(View.INVISIBLE);
                        }
                    } else{
                    	if(!updatePrompt){
                        	showDlWithoutWifiDialog(detail, mainActivity, SimpleUtil.dataConnection(NextChapter.this));
                		}
						else{
							progressBar.setVisibility(View.GONE);
							textProgress.setVisibility(View.GONE);
							textStatus.setVisibility(View.INVISIBLE);
						}
                	}
                    return;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if(!SimpleUtil.isNetworkConnected(this) && !patching) {
            handler.removeCallbacks(runnable);
            int resId = getResources().getIdentifier("lebian_dl_status_no_network", "string", getPackageName());
            strStatus = getResources().getString(resId);
            textStatus.setText(strStatus);
        }
		callNextChapterParcel("onResume", null, null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart enter");
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
		if (nextChapterParcel != null) {
			callNextChapterParcel("onSaveInstanceState", new Class[]{Bundle.class}, new Object[]{outState});
            outState.putParcelable("nextChapterParcel", nextChapterParcel);
        }
    }

    protected void onPause() {
        super.onPause();
		callNextChapterParcel("onPause", null, null);
    }

    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(myReceiver);
        }
        catch (Exception e) {
        }
		callNextChapterParcel("onDestroy", null, null);
    }

    private final Runnable downloadCallBack = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "downloadCallBack enter");
			String downloadstatus = (String)callNextChapterParcel("parseUserinfo", new Class[]{String.class}, new Object[]{"downloadstatus"});
            if(downloadstatus != null) {
                if(downloadstatus.equals("1")||downloadstatus.equals("2")) {
					boolean rmAutoDl = ((Boolean)callNextChapterParcel("getREMOVE_AUTO_DOWNLOAD", null, null)).booleanValue();
                    if(SimpleUtil.isNetworkConnected(NextChapter.this) && (rmAutoDl || !SimpleUtil.checkWifiState(NextChapter.this))) {
                        showUnfinishedDlDialog();
                    }
                }
            }
            handler.postDelayed(downloadCallBack, 10000);
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if(!SimpleUtil.isNetworkConnected(this) || SimpleUtil.checkWifiState(this) || showProgressbar)
                showExitDialog();
            else
                showDlBackgroundWithoutWifiDialog();
        }

        return false;
    }

    private void showExitDialog() {
        if (exitDialog == null) {
            Builder builder = new Builder(this);
			final boolean exitGame = (!gameRunning || (detail != null && rForceUpdate.equals("1")));
            int resId = getResources().getIdentifier(exitGame?"lebian_exit_dialog_titile":"lebian_exit_dialog_titile2", "string", getPackageName());
            builder.setMessage(getResources().getString(resId));

            resId = getResources().getIdentifier("lebian_hint", "string", getPackageName());
            builder.setTitle(getResources().getString(resId));

            resId = getResources().getIdentifier("lebian_exit_dialog_yes", "string", getPackageName());
            builder.setPositiveButton(getResources().getString(resId), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
					if(gameRunning && exitGame)
						quitGame();
                }
            });

            resId = getResources().getIdentifier("lebian_exit_dialog_no", "string", getPackageName());
            builder.setNegativeButton(getResources().getString(resId), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    exitDialog = null;
                }
            });
            exitDialog = builder.create();
        }
        if (!exitDialog.isShowing() && !isFinishing()) {
            exitDialog.show();
        }
    }

    private void showDlWithoutWifiDialog(final Serializable detail, final String mainActivity, final boolean dataConnection) {
        if (dlWithoutWifiDialog == null) {
			final boolean forceUpdate = rForceUpdate.equals("1");
            CustomDialog.Builder builder = new CustomDialog.Builder(this);
			int resId = getResources().getIdentifier("lebian_dl_update_without_wifi", "string", getPackageName());
            int resId2 = getResources().getIdentifier("lebian_dl_update_wifi", "string", getPackageName());
            builder.setMessage(String.format(getResources().getString(dataConnection?resId:resId2),((float)(rSize))/(1024.0f * 1024.0f)));

            resId = getResources().getIdentifier("lebian_hint", "string", getPackageName());
            builder.setTitle(getResources().getString(resId));

			if(!forceUpdate){
				resId = getResources().getIdentifier("lebian_next_no_prompt", "string", getPackageName());
				builder.setCheckBox(getResources().getString(resId), false,new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
						getSharedPreferences("excl_lb_prompt", MODE_PRIVATE).edit().putBoolean("gameUpdate", isChecked).commit();
					}
				});
			}

            resId = getResources().getIdentifier("lebian_exit_dialog_yes", "string", getPackageName());
            builder.setPositiveButton(getResources().getString(resId), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
					callNextChapterParcel("downloadComponent", new Class[]{Serializable.class}, new Object[]{detail});
                    dlWithoutWifiDialog = null;
					getSharedPreferences("excl_lb_prompt", MODE_PRIVATE).edit().putBoolean("gameUpdate", false).commit();
                }
            });

            resId = getResources().getIdentifier("lebian_quit_game", "string", getPackageName());
			resId2 = getResources().getIdentifier("lebian_no_update", "string", getPackageName());
            builder.setNegativeButton(getResources().getString(forceUpdate?resId:resId2), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dlWithoutWifiDialog = null;
					if(forceUpdate || mainActivity==null || mainActivity.length()<=0)
                        finish();
                    else {
						callNextChapterParcel("startMainActivity", new Class[]{String.class}, new Object[]{mainActivity});
                    }
                }
            });
            dlWithoutWifiDialog = builder.create();
            dlWithoutWifiDialog.setCancelable(false);
            dlWithoutWifiDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
        if (!dlWithoutWifiDialog.isShowing() && !isFinishing()) {
            dlWithoutWifiDialog.show();
        }
    }

    private void showUnfinishedDlDialog() {
        if (unfinishedDlDialog == null) {
            CustomDialog.Builder builder = new CustomDialog.Builder(this);
            int resId = getResources().getIdentifier("lebian_dl_unfinished_dl_without_wifi", "string", getPackageName());
            builder.setMessage(getResources().getString(resId));

            resId = getResources().getIdentifier("lebian_hint", "string", getPackageName());
            builder.setTitle(getResources().getString(resId));

            resId = getResources().getIdentifier("lebian_exit_dialog_yes", "string", getPackageName());
            builder.setPositiveButton(getResources().getString(resId), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if(detail!=null) {
                        Log.d(TAG, "showUnfinishedDlDialog downloadComponent id:"+rGameId);
						callNextChapterParcel("downloadComponent", new Class[]{Serializable.class}, new Object[]{detail});
                        
                        int resIdTemp = getResources().getIdentifier("lebian_dl_status_connecting", "string", getPackageName());
                        strStatus = getResources().getString(resIdTemp);
                        handler.removeCallbacks(runnable);
                        handler.postDelayed(runnable, 800);
                    } else {
                        Log.d(TAG, "showUnfinishedDlDialog detail==null");
                    }

                }
            });

            resId = getResources().getIdentifier("lebian_quit_game", "string", getPackageName());
            builder.setNegativeButton(getResources().getString(resId), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
					if(gameRunning)
						quitGame();
                }
            });
            unfinishedDlDialog = builder.create();
            unfinishedDlDialog = builder.create();
            unfinishedDlDialog.setCancelable(false);
            unfinishedDlDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
        if (!unfinishedDlDialog.isShowing() && !isFinishing()) {
            unfinishedDlDialog.show();
        }
    }

	private void showUpdateWithoutWifiDialog(final Serializable detail, final Serializable localDetail, final boolean dataConnection) {
        if (updateWithoutWifiDialog == null) {
            final boolean forceUpdate = rForceUpdate.equals("1");
            CustomDialog.Builder builder = new CustomDialog.Builder(this);
            int resId = getResources().getIdentifier("lebian_dl_update_without_wifi", "string", getPackageName());
            int resId2 = getResources().getIdentifier("lebian_dl_update_wifi", "string", getPackageName());
            builder.setMessage(String.format(getResources().getString(dataConnection?resId:resId2),((float)(rSize))/(1024.0f * 1024.0f)));

            resId = getResources().getIdentifier("lebian_hint", "string", getPackageName());
            builder.setTitle(getResources().getString(resId));
            if(!forceUpdate){
				resId = getResources().getIdentifier("lebian_next_no_prompt", "string", getPackageName());
                builder.setCheckBox(getResources().getString(resId), false,new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
                        getSharedPreferences("excl_lb_prompt", MODE_PRIVATE).edit().putBoolean("gameUpdate", isChecked).commit();
                    }
                });
            }
            resId = getResources().getIdentifier("lebian_exit_dialog_yes", "string", getPackageName());
            builder.setPositiveButton(getResources().getString(resId), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

					callNextChapterParcel("downloadComponent", new Class[]{Serializable.class}, new Object[]{detail});
                    getSharedPreferences("excl_lb_prompt", MODE_PRIVATE).edit().putBoolean("gameUpdate", false).commit();
                }
            });

            resId = getResources().getIdentifier("lebian_quit_game", "string", getPackageName());
            resId2 = getResources().getIdentifier("lebian_no_update", "string", getPackageName());
            builder.setNegativeButton(getResources().getString(forceUpdate?resId:resId2), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    updateWithoutWifiDialog = null;
                    if(forceUpdate)
                        finish();
                    else {
						callNextChapterParcel("startGame", new Class[]{Serializable.class,Boolean.class}, new Object[]{localDetail,false});
                    }

                }
            });

            updateWithoutWifiDialog = builder.create();
            updateWithoutWifiDialog.setCancelable(false);
    	    updateWithoutWifiDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
    		    @Override
    		    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
    		    {
    		    if (keyCode == KeyEvent.KEYCODE_SEARCH)
    		     {
    		      return true;
    		     }
    		     else
    		     {
    		      return false;
    		     }
    		    }
            });
        }
        if (!updateWithoutWifiDialog.isShowing() && !isFinishing()) {
            updateWithoutWifiDialog.show();
        }
    }

    private void showDlBackgroundWithoutWifiDialog() {
		Log.d(TAG, "showDlBackgroundWithoutWifiDialog enter");
        if (dlBackgroundWithoutWifiDialog == null) {
            CustomDialog.Builder builder = new CustomDialog.Builder(this);
            int resId = getResources().getIdentifier("lebian_dl_dl_without_wifi_background", "string", getPackageName());
            builder.setMessage(getResources().getString(resId));

            resId = getResources().getIdentifier("lebian_hint", "string", getPackageName());
            builder.setTitle(getResources().getString(resId));
			final boolean exitGame = (!gameRunning || (detail != null && rForceUpdate.equals("1")));
            resId = getResources().getIdentifier("lebian_dl_dl_background", "string", getPackageName());
            builder.setPositiveButton(getResources().getString(resId), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
					boolean forceUpdate =  (detail != null && rForceUpdate.equals("1"));
					if(gameRunning)
						quitGame();
                }
            });

            resId = getResources().getIdentifier("lebian_quit_game", "string", getPackageName());
            builder.setNegativeButton(getResources().getString(resId), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
					callNextChapterParcel("quitDownloadBg", null, null);
                    dialog.dismiss();
                    finish();
					if(gameRunning)
						quitGame();
                }
            });
            dlBackgroundWithoutWifiDialog = builder.create();
        }
        if (!dlBackgroundWithoutWifiDialog.isShowing() && !isFinishing()) {
            dlBackgroundWithoutWifiDialog.show();
        }
    }

    private void showNotEnoughSpaceDialog() {
        if (notEnoughSpaceDialog == null) {
            boolean hasSD = SimpleUtil.hasExternalStorage();
            CustomDialog.Builder builder = new CustomDialog.Builder(this);
            int resId = getResources().getIdentifier("lebian_not_enought_space", "string", getPackageName());
            builder.setTitle(getResources().getString(resId));
            long requiredSize = rSize;
            if (!hasSD)
                requiredSize += (20<<20);
            resId = getResources().getIdentifier("lebian_storage_space_requirement_dload", "string", getPackageName());
            int resId2 = getResources().getIdentifier("lebian_storage_sd", "string", getPackageName());
            int resId3 = getResources().getIdentifier("lebian_storage_phone", "string", getPackageName());
            String body = String.format(getResources().getString(resId), requiredSize/(float)(1024*1024), hasSD?getResources().getString(resId2):getResources().getString(resId3));
            builder.setMessage(body);

            resId = getResources().getIdentifier("lebian_exit_dialog_yes", "string", getPackageName());
            builder.setPositiveButton(getResources().getString(resId), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
					if(!gameRunning){
	                    if (localDetail==null || rForceUpdate.equals("1"))
	                        finish();
	                    else
							callNextChapterParcel("startGame", new Class[]{Serializable.class,Boolean.class}, new Object[]{localDetail,false});
					}
					else{
						finish();
						if (rForceUpdate.equals("1"))
							quitGame();
					}
                }
            });
            notEnoughSpaceDialog = builder.create();
            notEnoughSpaceDialog.setCancelable(false);
            notEnoughSpaceDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
        if (!notEnoughSpaceDialog.isShowing() && !isFinishing()) {
            notEnoughSpaceDialog.show();
        }
    }
	
    public Drawable getBackGroundDrawable(){
    	Drawable background = null;
    	int resId = getResources().getIdentifier("lebian_background", "drawable", getPackageName());
    	if(resId == 0){
			resId = getResources().getIdentifier("lebian_main_background_normal", "drawable", getPackageName());
			if(resId == 0)
				return null;
		}	
    	background = getResources().getDrawable(resId);
    	return background;
    }
    
    public int getTextColor(){
    	return Color.rgb(255, 128, 64);
    }
	
	public int getProgressTextColor(){
		return Color.rgb(255, 255, 255);
	}

	public Resources getResources() {
		if (nextChapterParcel!=null)
			return (Resources)callNextChapterParcel("getResources", null, null);

		return super.getResources();
	}

    private Object callNextChapterParcel(String name, Class[] type, Object[] args) {
        try {
            Class clazz = Class.forName("com.excelliance.open.platform.NextChapterParcel");
			Method method = clazz.getDeclaredMethod(name, type);
			method.setAccessible(true);
			return method.invoke(nextChapterParcel, args);
        } catch (Exception e) {
            Log.d(TAG, "no NextChapterParcel");
			e.printStackTrace();
        }

        return null;
    }
	
	public void quitGame(){
		Log.d(TAG, "quit enter");
		try {
            unregisterReceiver(myReceiver);
        }
        catch (Exception e) {
        }
		callNextChapterParcel("quitGame", null, null);
	}

}
