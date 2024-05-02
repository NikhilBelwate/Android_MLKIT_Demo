package com.demo.mlkittest.mlBarCodeScanner;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.demo.mlkittest.mlBarCodeScanner.GraphicOverlay;

class CameraImageGraphic extends GraphicOverlay.Graphic {

    private final Bitmap bitmap;

    public CameraImageGraphic(GraphicOverlay overlay, Bitmap bitmap) {
        super(overlay);
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, getTransformationMatrix(), null);
    }
}
