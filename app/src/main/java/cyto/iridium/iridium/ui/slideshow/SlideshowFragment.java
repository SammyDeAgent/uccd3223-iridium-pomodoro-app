package cyto.iridium.iridium.ui.slideshow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import cyto.iridium.iridium.R;
import cyto.iridium.iridium.databinding.FragmentSlideshowBinding;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class SlideshowFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;
    private FragmentSlideshowBinding binding;

    private GifImageView treeview;

    private TextView earnedpoint;
    private Button resetBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences prefoutput2 = getActivity().getSharedPreferences("MySharePoint", 0);
        long point = prefoutput2.getLong("totalpoint", 0);
        SharedPreferences.Editor prefEdit2 = prefoutput2.edit();

        slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textSlideshow;
//        slideshowViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        earnedpoint = binding.earnedpoint;
        earnedpoint.setText(String.valueOf(point));

        treeview = binding.growingtree;

        if(point >= 25200){
            treeview.setImageResource(R.drawable.tree7);
        }else if(point >= 21600){
            treeview.setImageResource(R.drawable.tree6);
        }else if(point >= 18000){
            treeview.setImageResource(R.drawable.tree5);
        }else if(point >= 10800){
            treeview.setImageResource(R.drawable.tree4);
        }else if(point >= 3600){
            treeview.setImageResource(R.drawable.tree3);
        }else if(point >= 1800){
            treeview.setImageResource(R.drawable.tree2);
        }else if(point >= 60){
            treeview.setImageResource(R.drawable.tree1);
        }else{
            treeview.setImageResource(R.drawable.tree0);
        }

        resetBtn = binding.resetbutton;
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefEdit2.putLong("totalpoint",0);
                prefEdit2.commit();
                Long point2 = prefoutput2.getLong("totalpoint", 0);
                earnedpoint.setText(String.valueOf(point2));
                treeview.setImageResource(R.drawable.tree0);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}