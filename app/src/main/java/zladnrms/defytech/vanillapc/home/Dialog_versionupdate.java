package zladnrms.defytech.vanillapc.home;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;

import zladnrms.defytech.vanillapc.R;

public class Dialog_versionupdate extends Dialog {

    BootstrapButton btn_dismiss, btn_ok;

    boolean ok = false;

    public Dialog_versionupdate(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_versionupdate);

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

    public boolean getOk(){
        return this.ok;
    }
}
