package mx.hgo.fracccionamientos.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import mx.hgo.fracccionamientos.ControlVehicular;
import mx.hgo.fracccionamientos.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ImageView btnControl;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        /*************************** EVENTO DE LOS BOTONES *******************************/
        btnControl = root.findViewById(R.id.imgControlVehicular);

        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ControlVehicular.class);
                startActivity(i);
            }
        });

        /*************************************************************************/
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        return root;
    }
}