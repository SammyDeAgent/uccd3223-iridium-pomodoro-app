package cyto.iridium.iridium.ui.home;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

//import com.bumptech.glide.Glide;
import com.huawei.hms.support.account.result.AuthAccount;
import com.squareup.picasso.Picasso;

import java.io.InputStream;

import cyto.iridium.iridium.AuthWithAuthCodeActivity;
import cyto.iridium.iridium.R;
import cyto.iridium.iridium.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private String avatarURL;
    private String displayName;

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

        Picasso.get().load(avatarURL).into(imageView);
        //Glide.with(this).load(avatarURL).into(imageView);

        textView.setText(displayName);

        return root;
    }


    @Override
    public void onViewCreated(View view, @NonNull Bundle savedInstanceState){
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}