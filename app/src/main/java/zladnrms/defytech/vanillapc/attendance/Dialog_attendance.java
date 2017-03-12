package zladnrms.defytech.vanillapc.attendance;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;

import zladnrms.defytech.vanillapc.R;

public class Dialog_attendance extends Dialog {

    TextView tv_name;
    BootstrapButton btn_dismiss, btn_ok;

    String username;
    boolean ok = false;

    public Dialog_attendance(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_attendace);

        tv_name = (TextView) findViewById(R.id.tv_name);
        btn_dismiss = (BootstrapButton) findViewById(R.id.btn_dismiss);
        btn_ok = (BootstrapButton) findViewById(R.id.btn_ok);

        btn_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ok = true;
                dismiss();
            }
        });
    }

    public void setName(String name){
        username = name;
        tv_name.setText(username);
    }

    public boolean getOk(){
        return this.ok;
    }
}
