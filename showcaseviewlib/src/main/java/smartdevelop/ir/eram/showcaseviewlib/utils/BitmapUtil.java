package smartdevelop.ir.eram.showcaseviewlib.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by John Oliver Magdaleno on 05/08/2018.
 */

public class BitmapUtil {

    public static Bitmap rotate(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }
}
