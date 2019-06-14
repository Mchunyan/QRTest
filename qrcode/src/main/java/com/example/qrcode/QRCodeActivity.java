package com.example.qrcode;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.huantansheng.easyphotos.EasyPhotos;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.example.qrcode.Constants.PIC;

public class QRCodeActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private boolean isLight = false;
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcodelayput);
        initViews();
    }


    protected void initViews() {
        barcodeView = findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.decodeContinuous(callback);
        barcodeView.setTorchListener(new DecoratedBarcodeView.TorchListener() {
            @Override
            public void onTorchOn() {
                Toast.makeText(QRCodeActivity.this, "onTorchOn", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTorchOff() {
                Toast.makeText(QRCodeActivity.this, "onTorchOff", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            // Prevent duplicate scans
            if (result.getText() == null) return;
            barcodeView.setStatusText(result.getText());
            parseQRCode(result.getText(), new Bundle());
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PIC:
                    if (data != null) {
                        List<String> pathList = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                        final String picturePath = pathList.get(0);
                        barcodeView.pause();
                        Observable.create(new ObservableOnSubscribe<Result>() {
                            @Override
                            public void subscribe(ObservableEmitter<Result> e) {
                                Result result = PicUtils.scanningImage(picturePath);
                                e.onNext(result);
                            }
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new io.reactivex.Observer<Result>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        if (mCompositeDisposable == null)
                                            mCompositeDisposable = new CompositeDisposable();
                                        mCompositeDisposable.add(d);
                                    }

                                    @Override
                                    public void onNext(Result result) {
                                        parseQRCode(result.getText(), new Bundle());
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        barcodeView.resume();
                                        Toast.makeText(QRCodeActivity.this, "照片中未识别到二维码", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    } else {

                    }
                    break;
            }
        }
    }


    //解析二维码中的字符串
    private void parseQRCode(String result, Bundle bundle) {

        if (TextUtils.isEmpty(result)) {
            Toast.makeText(this, "照片中未识别到二维码", Toast.LENGTH_SHORT).show();
            return;
        }


        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }


    public void scanerClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_zxing_gallery) {
            EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                    .setFileProviderAuthority(Constants.fileProvider)
                    .setCount(1)
                    .start(PIC);

        } else if (i == R.id.tv_zxing_flashlight) {
            if (!isLight) {
                barcodeView.setTorchOn();
                isLight = true;
            } else {
                isLight = false;
                barcodeView.setTorchOff();
            }

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
    }
}