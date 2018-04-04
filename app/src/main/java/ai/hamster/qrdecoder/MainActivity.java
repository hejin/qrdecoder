package ai.hamster.qrdecoder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.CvType;

import org.opencv.android.Utils;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "MainActivity";
    private ImageScanner qrcodeScanner = null;
    private Bitmap bmp = null;
    private int nImgW = 0, nImgH = 0;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
        System.loadLibrary("iconv");
        System.loadLibrary("zbarjni");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.qrdecode_txt);
        tv.setText(stringFromJNI());

        Button btnGo = (Button) findViewById(R.id.button);
        btnGo.setOnClickListener(this);

        // load picture in
        ImageView img = (ImageView) findViewById(R.id.imageView);

        boolean bUseOpencv = true;

        if (bUseOpencv) {
            try {
                Mat mMat = Utils.loadResource(this, R.drawable.helloworld, Imgcodecs.CV_LOAD_IMAGE_COLOR);
                Mat result = mMat.clone();
                nImgH = mMat.rows();
                nImgW = mMat.cols();
                Imgproc.cvtColor(mMat, result, Imgproc.COLOR_RGB2BGRA);
                bmp = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(result, bmp);
                Log.i(TAG, "matToBitmap worked?!");
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            img.setImageBitmap(bmp);
        } else {
            img.setImageResource(R.drawable.helloworld);
        }

        /* Instance barcode scanner */
        qrcodeScanner = new ImageScanner();
        qrcodeScanner.setConfig(0, Config.X_DENSITY, 3);
        qrcodeScanner.setConfig(0, Config.Y_DENSITY, 3);

    }

    @Override
    public void onClick(View v) {

        //Your Logic
        try {
            Bitmap barcodeBmp = BitmapFactory.decodeResource(getResources(),
                    R.drawable.helloworld);
            int width = barcodeBmp.getWidth();
            int height = barcodeBmp.getHeight();
            int[] pixels = new int[(width * height)];
            barcodeBmp.getPixels(pixels, 0, width, 0, 0, width, height);
            Image barcode = new Image(width, height, "RGB4");
            barcode.setData(pixels);
            int result = qrcodeScanner.scanImage(barcode.convert("Y800"));
            SymbolSet syms = qrcodeScanner.getResults();
            TextView tv = (TextView) findViewById(R.id.qrdecode_txt);
            if (syms.size() > 0) {
                for (Symbol sym : syms) {
                    tv.setText("barcode result -> " + sym.getData());
                    Log.i(TAG, "QRcode decoding result: " + sym.getData());
                }
            } else {
                Log.i(TAG, "failed to decode.........");
            }
        } catch (Exception e) {
            Log.e(TAG, "exception caught: " + e.toString());
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
