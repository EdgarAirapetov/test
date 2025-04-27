package com.numplates.nomera3.presentation.view.widgets.numberplateview;

import static com.meera.core.extensions.CommonKt.dpToPx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.numplates.nomera3.R;
import com.numplates.nomera3.data.network.Vehicle;
import com.numplates.nomera3.data.network.core.INetworkValues;
import com.numplates.nomera3.presentation.view.utils.NGraphics;

/**
 * Created by artem on 08.06.18
 */
public class NumberPlateView extends View implements INetworkValues {

    private final Context context;
    private NumberPlateEnum plate;

    private int desiredWidth;
    private int desiredHeight;
    float scale = 1;


    private String fullNumberString;

    private String number;
    private String region;
    private String suffix;

    private Bitmap plateBg;

    private Paint paintNumber;
    private Paint paintRegion;
    private Paint paintSuffix;


    public NumberPlateView(Context context) {
        super(context);
        this.context = context;
        init(context, null);

    }

    public NumberPlateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context, attrs);
    }

    public NumberPlateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        paintNumber = new Paint();
        paintNumber.setColor(context.getResources().getColor(R.color.colorDarkGrey));
        paintNumber.setStyle(Paint.Style.FILL);
        paintNumber.setAntiAlias(true);

        paintRegion = new Paint();
        paintRegion.setColor(context.getResources().getColor(R.color.colorDarkGrey));
        paintRegion.setStyle(Paint.Style.FILL);
        paintRegion.setAntiAlias(true);


        paintSuffix = new Paint();
        paintSuffix.setColor(context.getResources().getColor(R.color.colorDarkGrey));
        paintSuffix.setStyle(Paint.Style.FILL);
        paintSuffix.setAntiAlias(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {


        if (plate != null && plateBg != null /*&& fullNumberString != null && !fullNumberString.isEmpty()*/) {

            float actualScale = dpToPx(Math.round(scale));

            paintNumber.setTextSize(32 * actualScale);
            paintRegion.setTextSize(18 * actualScale);
            paintSuffix.setTextSize(20 * actualScale);

            Rect src = new Rect(0, 0, plateBg.getWidth(), plateBg.getHeight());
            Rect dst = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(plateBg, src, dst, null);


            switch (plate) {
                case RU_AUTO: {
                    canvas.drawText(number, 20 * actualScale, 36 * actualScale, paintNumber);
                    canvas.drawText(region, 185 * actualScale, 27 * actualScale, paintRegion);
//                    canvas.drawText(suffix, 10, 64, paintRegion);
                    break;
                }
                case RU_MOTO: {
                    canvas.drawText(number, 22 * actualScale, 32 * actualScale, paintNumber);
                    canvas.drawText(region, 64 * actualScale, 64 * actualScale, paintRegion);
                    canvas.drawText(suffix, 10 * actualScale, 64 * actualScale, paintSuffix);
                    break;
                }
                case UA_AUTO: {
                    canvas.drawText(number, 28 * actualScale, 36 * actualScale, paintNumber);
                    canvas.drawText(region, 178 * actualScale, 27 * actualScale, paintRegion);
//                    canvas.drawText(suffix, 10, 64, paintRegion);
                    break;
                }
                case UA_MOTO: {
                    canvas.drawText(number, 22 * actualScale, 32 * actualScale, paintNumber);
                    canvas.drawText(region, 64 * actualScale, 64 * actualScale, paintRegion);
                    canvas.drawText(suffix, 10 * actualScale, 64 * actualScale, paintSuffix);
                    break;
                }
                case PEDESTRIAN: {
                    canvas.drawText(number, 22 * actualScale, 32 * actualScale, paintNumber);
                    canvas.drawText(region, 64 * actualScale, 64 * actualScale, paintRegion);
                    canvas.drawText(suffix, 10 * actualScale, 64 * actualScale, paintSuffix);
                    break;
                }

                case COMMON: {
                    canvas.drawText(number, 22 * actualScale, 32 * actualScale, paintNumber);
                    canvas.drawText(region, 64 * actualScale, 64 * actualScale, paintRegion);
                    canvas.drawText(suffix, 10 * actualScale, 64 * actualScale, paintSuffix);
                    break;
                }
            }

        }

    }

    private void initNumber(Builder builder) {

        if (builder.num == null || builder.plate == null) {
            return;
        }


        if (builder.num == null) {
            builder.num = "";
        }
        this.fullNumberString = builder.num;

        int length = fullNumberString.length();
        this.plate = builder.plate;
        switch (plate) {
            case RU_AUTO: {
                int d6 = length > 6 ? 6 : length;
                int d9 = length > 9 ? 9 : length;
                number = fullNumberString.substring(0, d6);
                region = fullNumberString.substring(d6, d9);

                break;
            }
            case RU_MOTO: {
                int d4 = length > 4 ? 4 : length;
                int d6 = length > 6 ? 6 : length;
                int d9 = length > 9 ? 9 : length;
                number = fullNumberString.substring(0, d4);
                suffix = fullNumberString.substring(d4, d6);
                region = fullNumberString.substring(d6, d9);
                break;
            }
            case UA_AUTO: {
                int d6 = length > 6 ? 6 : length;
                int d9 = length > 9 ? 9 : length;
                number = fullNumberString.substring(0, d6);
                region = fullNumberString.substring(d6, d9);
                break;
            }
            case UA_MOTO: {
                int d4 = length > 4 ? 4 : length;
                int d6 = length > 6 ? 6 : length;
                int d9 = length > 9 ? 9 : length;
                number = fullNumberString.substring(0, d4);
                suffix = fullNumberString.substring(d4, d6);
                region = fullNumberString.substring(d6, d9);
                break;
            }
        }

//        if (plate != null) {
//            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
//
//            decodeOptions.inScaled = false;
//            decodeOptions.inMutable = true;
//            plateBg = BitmapFactory.decodeResource(getResources(), plate.backgroundId, decodeOptions);
//
//            desiredWidth = NGraphics.dpToPx(decodeOptions.outWidth);
//            desiredHeight = NGraphics.dpToPx(decodeOptions.outHeight);
//        }
//        drawBitmap();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (fullNumberString == null) {
            this.setVisibility(View.GONE);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.UNSPECIFIED) {
            scale = ((float) widthSize) / (float) desiredWidth;
        } else if (widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.EXACTLY) {
            scale = ((float) heightSize) / (float) desiredHeight;
        } else {
            scale = Math.min(((float) widthSize) / (float) desiredWidth, ((float) heightSize) / (float) desiredHeight);
        }

        int widthSpec = MeasureSpec.makeMeasureSpec(Math.round(scale * desiredWidth), MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(Math.round(scale * desiredHeight), MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, heightSpec);
    }

    public static class Builder {
        boolean enabled;
        private final NumberPlateView view;
        private String num;
        private NumberPlateEnum plate;

        public Builder(NumberPlateView view) {
            this.view = view;
        }

//        public Builder setType(NumberPlateClass plate) {
//            this.plate = plate;
//            return this;
//        }

        public Builder setType(int  vehicle) {
            switch (vehicle){
                case VEHICLE_AUTO:
                    this.plate = NumberPlateEnum.RU_AUTO;
                    break;

                case VEHICLE_MOTO:
                    this.plate = NumberPlateEnum.RU_MOTO;
                    break;



                default:
                    this.plate = NumberPlateEnum.RU_AUTO;
                    break;
            }
            return this;
        }


        //        public Builder setKind(int plate) {
//            this.plate = plate;
//            return this;
//        }
        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setNum(String num) {
            this.num = num;
            return this;
        }

        public Builder setVehicle(Vehicle vehicle) {
            this.num = vehicle.getNumber();
            if (vehicle.getType() == null || vehicle.getCountry() == null) {
                return this;
            }
            String type = vehicle.getType().getTypeId();
            String country = vehicle.getCountry().getCountryId().toString();

            if ("1".equals(type)) {
                if("3159".equals(country)) {
                    this.plate = NumberPlateEnum.RU_AUTO;
                } else if("9908".equals(country)) {
                    this.plate = NumberPlateEnum.UA_AUTO;
                }


            } else if ("2".equals(type)) {
                if("3159".equals(country)) {
                    this.plate = NumberPlateEnum.RU_MOTO;
                } else if("9908".equals(country)) {
                    this.plate = NumberPlateEnum.UA_MOTO;
                }
            }

            return this;
        }

        public void build() {
            view.initNumber(this);
        }
    }


}
