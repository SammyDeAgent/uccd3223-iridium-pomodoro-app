/*
 *  Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 */

package cyto.iridium.iridium;

import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

public class AuthWithAuthCodeActivity extends Activity implements View.OnClickListener {

    private final String TAG = "Account";
    private TextView textView;
    private ImageView imageView;
    private AccountAuthService mAuthService;
    private AccountAuthParams mAuthParam;

    //login by code
    private static final int REQUEST_SIGN_IN_LOGIN_CODE = 1003;

    Handler mHandler;
    Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huawei_authcode);
        textView = findViewById(R.id.hwid_log_text);
        findViewById(R.id.hwid_signInCode).setOnClickListener(this);
//        findViewById(R.id.hwid_signout).setOnClickListener(this);
//        findViewById(R.id.cancel_authorization).setOnClickListener(this);

        imageView = findViewById(R.id.animImage);

        Animation scale = new ScaleAnimation(
                1f, -1f, // Start and end values for the X axis scaling
                1f, 1f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        scale.setFillAfter(true); // Needed to keep the result of the animation
        scale.setDuration(2000);
        scale.setRepeatMode(Animation.REVERSE);
        scale.setRepeatCount(Animation.INFINITE);

        AnimationSet gemRotate = new AnimationSet(true);
        gemRotate.addAnimation(scale);

        imageView.setAnimation(gemRotate);
    }

    private void signInCode() {
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setProfile()
                .setAuthorizationCode()
                .setIdToken()
                .setUid()
                .setEmail()
                .createParams();
        mAuthService = AccountAuthManager.getService(AuthWithAuthCodeActivity.this, mAuthParam);
        startActivityForResult(mAuthService.getSignInIntent(),REQUEST_SIGN_IN_LOGIN_CODE);
    }

    private void signOut() {
        Task<Void> signOutTask = mAuthService.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "signOut Success");
                textView.setText("Sign Out Success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "signOut fail");
                textView.setText("Sign Out Failed");
            }
        });

        Intent intent = new Intent(
                this,
                AuthWithAuthCodeActivity.class
        );
        startActivity(intent);
        finish();
    }

    private void cancelAuthorization() {
        Task<Void> task = mAuthService.cancelAuthorization();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "cancelAuthorization success");
                textView.setText("Authorization Cancel Success");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "cancelAuthorization failure：" + e.getClass().getSimpleName());
                textView.setText("Authorization Cancel Failure：" + e.getClass().getSimpleName());
            }
        });

        Intent intent = new Intent(
                this,
                AuthWithAuthCodeActivity.class
        );
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hwid_signInCode:
                signInCode();
                break;
            case R.id.hwid_signout:
                signOut();
                break;
            case R.id.cancel_authorization:
                cancelAuthorization();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Process the sign-in authorization result and obtain an ID token from AuthHuaweiId.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_IN_LOGIN_CODE) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                // The sign-in is successful, and the user's HUAWEI ID information and ID token are obtained.
                AuthAccount authAccount = authAccountTask.getResult();
                Log.i(TAG, "Authorization code:" + authAccount.getAuthorizationCode());
                Log.i(TAG, "Access Token:" + authAccount.getIdToken());
                textView.setText("Signed In Successfully");

                SharedPreferences loginInfo = getSharedPreferences(
                        "Iridium_Login",
                        MODE_PRIVATE
                );
                SharedPreferences.Editor loginInfoEdit = loginInfo.edit();

                loginInfoEdit.putString("auth_Logged", "1");
                loginInfoEdit.putString("auth_Uid", authAccount.getUid());
                loginInfoEdit.putString("auth_Name", authAccount.getDisplayName());
                loginInfoEdit.putString("auth_Avatar", authAccount.getAvatarUriString());
                loginInfoEdit.putString("auth_Email", authAccount.getEmail());
                loginInfoEdit.putString("authAccount", authAccount.toString());

                loginInfoEdit.apply();

                Intent intent = new Intent(
                        AuthWithAuthCodeActivity.this,
                        MainActivity.class
                );
                startActivity(intent);
                finish();

            } else {
                // The sign-in failed. No processing is required. Logs are recorded to facilitate fault locating.
                Log.e(TAG, "sign in failed : " +((ApiException)authAccountTask.getException()).getStatusCode());
                textView.setText("Sign In Procedure Failed");
            }
        }
    }
}
