package cyto.iridium.iridium.avatarclock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private ImageView treeview;
    private Button resetbutton;

    private TextView earnedpoint;
    private Button clockpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefoutput2 = getSharedPreferences("MySharePoint", 0);
        long point = prefoutput2.getLong("point", 0);

        earnedpoint = findViewById(R.id.earnedpoint);
        earnedpoint.setText(String.valueOf(point));

        clockpage = findViewById(R.id.clockpage);
        clockpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClockActivity.class);
                startActivity(intent);
            }
        });

        treeview = findViewById(R.id.growingtree);
        switch ((int) point) {
            case 5:
                treeview.setImageResource(R.drawable.tree1);
                break;
            case 10:
                treeview.setImageResource(R.drawable.tree2);
                break;
            case 15:
                treeview.setImageResource(R.drawable.tree3);
                break;
            case 20:
                treeview.setImageResource(R.drawable.tree4);
                break;
            case 18000:
                treeview.setImageResource(R.drawable.tree5);
                break;
            case 21600:
                treeview.setImageResource(R.drawable.tree6);
                break;
            case 25200:
                treeview.setImageResource(R.drawable.tree7);
                break;
        }

        resetbutton=findViewById(R.id.resetbutton);
        resetbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());

            }
        });


    }
}