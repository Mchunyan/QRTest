package com.config.mcy.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.qrcode.QRCodeActivity;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SecondActivity extends SwipeBackActivity {

    private SwipeBackLayout mSwipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }

    public void scanerClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                SecondActivity.this.startActivity(new Intent(SecondActivity.this, QRCodeActivity.class));
                break;
        }
    }
}
