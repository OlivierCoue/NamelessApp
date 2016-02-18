package com.oliviercoue.httpwww.nameless.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Olivier on 18/02/2016.
 */
public class ChatImageHelper {

    public ChatImageHelper(){

    }

    public Bitmap getBitmap(String filePath, int width, int heigth){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = Math.min(bmOptions.outWidth/width, bmOptions.outHeight/heigth);;
        bmOptions.inPurgeable = true;
        return BitmapFactory.decodeFile(filePath, bmOptions);
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
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
