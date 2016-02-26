package com.oliviercoue.nameless.components.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Olivier on 18/02/2016.
 *
 */
public class ChatImageHelper {

    public ChatImageHelper(){

    }

    public static File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public Bitmap getBitmap(String filePath, float width){
        int newWidth;
        int newHeight;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        bmOptions.inJustDecodeBounds = false;

        newWidth = bmOptions.outWidth > width ? (int) width : bmOptions.outWidth;
        newHeight = bmOptions.outWidth > width ? ((int) (bmOptions.outHeight * (width / bmOptions.outWidth))) : bmOptions.outHeight;

        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bmOptions.outWidth, bmOptions.outHeight), new RectF(0, 0, newWidth, newHeight), Matrix.ScaleToFit.CENTER);
        Bitmap b = BitmapFactory.decodeFile(filePath);
        if(b != null)
            return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
        else
            return null;
    }

    public ByteArrayInputStream toByteArray(Bitmap in){
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        in.compress(Bitmap.CompressFormat.JPEG, 100, bos2);
        byte[] bitmapdata2 = bos2.toByteArray();
        return  new ByteArrayInputStream(bitmapdata2);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, (float) pixels, (float) pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
