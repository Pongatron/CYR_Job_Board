package UI;

import java.util.ArrayList;
import java.util.List;

public class ZoomManager {

    public static float currentZoom = 1.0f;
    private static final float ZOOM_STEP = 0.1f;
    private static final float MAX_ZOOM = 2.0f;
    private static final float MIN_ZOOM = 0.5f;


    public static float getZoom(){
        return currentZoom;
    }
    public static void setZoom(float zoom){currentZoom = zoom;}

//    public static float getCurrentZoom() {
//        return currentZoom;
//    }
//
    public static void increaseZoom(){
        if(currentZoom < MAX_ZOOM)
            currentZoom += ZOOM_STEP;
    }
    public static void decreaseZoom(){
        if(currentZoom > MIN_ZOOM)
            currentZoom -= ZOOM_STEP;
    }


}
