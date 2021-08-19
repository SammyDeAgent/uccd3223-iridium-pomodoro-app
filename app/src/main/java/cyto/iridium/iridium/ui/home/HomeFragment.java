package cyto.iridium.iridium.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.squareup.picasso.Picasso;

import java.io.InputStream;

import cyto.iridium.iridium.AuthWithAuthCodeActivity;
import cyto.iridium.iridium.MainActivity;
import cyto.iridium.iridium.R;
import cyto.iridium.iridium.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private String avatarURL;
    private String displayName;

    private AccountAuthParams mAuthParam;
    private AccountAuthService mAuthService;
    private final String TAG = "Account";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
//
//        final TextView textView = getActivity().findViewById(R.id.text_home);
//        textView.setText("Welcome! " + authAccount.getDisplayName());

        SharedPreferences loginInfo = this.getActivity().getSharedPreferences(
                "Iridium_Login",
                Context.MODE_PRIVATE
        );

        avatarURL = loginInfo.getString("auth_Avatar",null);
        displayName = loginInfo.getString("auth_Name", "undefined");

        final ImageView imageView = binding.avatarImage;
        final TextView textView = binding.textHome;

        //Picasso.get().load(avatarURL).into(imageView);
        //Glide.with(this).load(avatarURL).into(imageView);
//        Glide.with(this).load(avatarURL)
//                .placeholder(R.drawable.ic_launcher_background)
//                .error(R.drawable.ic_launcher_background)
//                .into(imageView);
        Glide.with(getContext()).load(avatarURL).apply(RequestOptions.circleCropTransform()).into(imageView);

        textView.setText(displayName);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

    private void signOut() {
        Task<Void> signOutTask = mAuthService.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "signOut Success");
                Toast.makeText(getContext(), "Signed Out Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "signOut fail");
                Toast.makeText(getContext(), "Sign Out Procedure Failed", Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = new Intent(
                this.getActivity(),
                AuthWithAuthCodeActivity.class
        );
        startActivity(intent);
        getActivity().finish();
    }

    private void cancelAuthorization() {
        Task<Void> task = mAuthService.cancelAuthorization();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "cancelAuthorization success");
                Toast.makeText(getContext(), "Authorization has been Revoked Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "cancelAuthorization failure：" + e.getClass().getSimpleName());
                Toast.makeText(getContext(), "Authorization Revoked Procedure Failed" + e.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = new Intent(
                this.getActivity(),
                AuthWithAuthCodeActivity.class
        );
        startActivity(intent);
        this.getActivity().finish();
    }


    @Override
    public void onViewCreated(View view, @NonNull Bundle savedInstanceState){
        binding.hwidSignout.setOnClickListener(this);
        binding.cancelAuthorization.setOnClickListener(this);

        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setProfile()
                .setAuthorizationCode()
                .setIdToken()
                .setUid()
                .setEmail()
                .createParams();
        mAuthService = AccountAuthManager.getService(this.getActivity(),mAuthParam);
        mAuthService.getSignInIntent();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}