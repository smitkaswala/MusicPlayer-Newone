package com.musiclover.newmusic.ui;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dtrung98.insetsview.ext.WindowThemingKt;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.musiclover.newmusic.App;
import com.ldt.musicr.R;
import com.musiclover.newmusic.service.MusicPlayerRemote;
import com.musiclover.newmusic.service.MusicService;
import com.musiclover.newmusic.ui.intro.IntroController;
import com.musiclover.newmusic.ui.nowplaying.NowPlayingLayerFragment;
import com.musiclover.newmusic.ui.playingqueue.PlayingQueueLayerFragment;
import com.musiclover.newmusic.ui.page.BackStackController;
import com.musiclover.newmusic.util.NavigationUtil;
import com.onesignal.OneSignal;


public class AppActivity extends MusicServiceActivity {

    private static final String TAG = "AppActivity";
    private static final int CODE_PERMISSIONS_WRITE_STORAGE = 1;
    private static final String ONESIGNAL_APP_ID = "########-####-####-####-############";

    public ConstraintLayout mAppRootView;

    public FrameLayout mLayerContainerView;
    private AppUpdateManager appUpdateManager;
    BottomNavigationView mBottomNavigationView;

    private static final int UPDATE_REQUEST_CODE = 101;

    private void bindView() {
        mAppRootView = findViewById(R.id.appRoot);
        mLayerContainerView = findViewById(R.id.layer_container);
        mBottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    public BackStackController mBackStackController;
    public NowPlayingLayerFragment mNowPlayingController;
    public PlayingQueueLayerFragment mPlayingQueueLayerFragment;

    public CardLayerController getCardLayerController() {
        return mCardLayerController;
    }

    public CardLayerController mCardLayerController;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        switch (requestCode) {
            case CODE_PERMISSIONS_WRITE_STORAGE: {
                if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // Granted
                        onPermissionGranted();
                    } else onPermissionDenied();
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public interface PermissionListener {
        void onPermissionGranted();

        void onPermissionDenied();
    }

    private PermissionListener mPermissionListener;

    @NonNull
    public final int[] getCurrentSystemInsets() {
        return mCurrentSystemInsets;
    }

    private int[] mCurrentSystemInsets = new int[]{0, 0, 0, 0};

    public void setPermissionListener(PermissionListener listener) {
        mPermissionListener = listener;

    }

    public void removePermissionListener() {
        mPermissionListener = null;
    }

    private void onPermissionGranted() {
        if (mPermissionListener != null) mPermissionListener.onPermissionGranted();
    }

    private void onPermissionDenied() {
        if (mPermissionListener != null) mPermissionListener.onPermissionDenied();
    }

    IntroController mIntroController;
    private boolean mUseDynamicTheme = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (!App.getInstance().getPreferencesUtility().isFirstTime()) {
            mUseDynamicTheme = false;
            App.getInstance().getPreferencesUtility().notFirstTime();
            setTheme(R.style.AppTheme);
            Log.d(TAG, "onCreate: not the first time");
        } else Log.d(TAG, "onCreate: the first time");
        if (mUseDynamicTheme) {
            setTheme(R.style.AppThemeNoWallpaper);
        }

        App.getInstance().getPreferencesUtility().notFirstTime();
        WindowThemingKt.setUpDarkSystemUIVisibility(this.getWindow());

        // will not handle saving/restoring instance state
        super.onCreate(null);

        setContentView(R.layout.activity_layout);

        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(result -> {
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                try {
                    appUpdateManager.startUpdateFlowForResult(result,AppActivity.this,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).setAllowAssetPackDeletion(true).build(),
                            UPDATE_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        // save the window system insets for in-app using
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            mCurrentSystemInsets[0] = insets.getSystemWindowInsetLeft();
            mCurrentSystemInsets[1] = insets.getSystemWindowInsetTop();
            mCurrentSystemInsets[2] = insets.getSystemWindowInsetRight();
            mCurrentSystemInsets[3] = insets.getSystemWindowInsetBottom();
            return ViewCompat.onApplyWindowInsets(v, insets);
        });
        bindView();

        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mAppRootView.post(new Runnable() {
            @Override
            public void run() {
                boolean isPermissionGranted = checkSelfPermission();
                if (!isPermissionGranted) {

                    if (mIntroController == null) {
                        mIntroController = new IntroController();
                    }

                    mIntroController.init(AppActivity.this, savedInstanceState);
                } else {
                    showMainUI();
                }
                if (mUseDynamicTheme) {
                    mAppRootView.postDelayed(() ->
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                            , 2500);
                }
            }
        });

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mCardLayerController != null) {
            mCardLayerController.onConfigurationChanged(newConfig);
        }
    }

    public void showMainUI() {
        /* remove the intro navigation */
        if (mIntroController != null) {
            removePermissionListener();
            mIntroController.getNavigationController().popAllFragments();
        }

        //runLoading();
        mCardLayerController = new CardLayerController(this);
        mBackStackController = new BackStackController();
        mNowPlayingController = new NowPlayingLayerFragment();
        mPlayingQueueLayerFragment = new PlayingQueueLayerFragment();

        mBackStackController.attachBottomNavigationView(this);

        mCardLayerController.init(mLayerContainerView, mBackStackController, mNowPlayingController, mPlayingQueueLayerFragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(result -> {
            if (result.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // If an in-app update is already running, resume the update.
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            result,
                            AppUpdateType.IMMEDIATE,
                            AppActivity.this,
                            UPDATE_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_REQUEST_CODE){

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: ");
        mAppRootView.post(() -> handlePlaybackIntent(intent));
    }

    public boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, CODE_PERMISSIONS_WRITE_STORAGE);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        CODE_PERMISSIONS_WRITE_STORAGE);

            }
        } else onPermissionGranted();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        if (mAppRootView != null)
            mAppRootView.post(() -> handlePlaybackIntent(getIntent()));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setTheme(boolean white) {
        Log.d(TAG, "setTheme: " + white);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View root = mAppRootView;
            if (root != null && !white)
                root.setSystemUiVisibility(0);
            else if (root != null)
                root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

    }

    public boolean backStackStreamOnTouchEvent(MotionEvent event) {
        if (mBackStackController != null) return mBackStackController.streamOnTouchEvent(event);
        return false;
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        if (mimeType == null) {
            mimeType = "";
        }
        boolean handled = false;

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            Log.d(TAG, "handlePlaybackIntent: type media play from search");
            handled = true;
        }

        if (uri != null && uri.toString().length() > 0) {
            Log.d(TAG, "handlePlaybackIntent: type play file");
            MusicPlayerRemote.playFromUri(uri);
            NavigationUtil.navigateToNowPlayingController(this);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            Log.d(TAG, "handlePlaybackIntent: type playlist");
            handled = true;
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            Log.d(TAG, "handlePlaybackIntent: type album");
            handled = true;
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            Log.d(TAG, "handlePlaybackIntent: type artist");
            handled = true;
        } else if (!handled && MusicService.ACTION_ON_CLICK_NOTIFICATION.equals(intent.getAction())) {
            NavigationUtil.navigateToNowPlayingController(this);
            handled = true;
        } else if (!handled) {
            Log.d(TAG, "handlePlaybackIntent: unhandled: " + intent.getAction());
        }

        if (handled) {
            setIntent(new Intent());
        }
    }

    public void popUpPlaylistTab() {
        if (mPlayingQueueLayerFragment != null) mPlayingQueueLayerFragment.popUp();
    }

    public BackStackController getBackStackController() {
        return mBackStackController;
    }

    public NowPlayingLayerFragment getNowPlayingController() {
        return mNowPlayingController;
    }

    public PlayingQueueLayerFragment getPlayingQueueLayerFragment() {
        return mPlayingQueueLayerFragment;

    }

}
