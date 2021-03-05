package com.example.pdrpositioning;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private static  final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer, sensorMagneticField,stepCounter, stepDetector;
    private TextView stepDetectorTV, stepCounterTV;
    private TextView tv_orientationValue, coordinatesTV;
    private int stepCount = 0;
    private int stepDetected = 0;
    private double xValue = 0;
    private double yValue = 0;
    private String coordinates = "(0,0)";
    private float orientationVal;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    //add PointsGraphSeries of DataPoint type
    PointsGraphSeries<DataPoint> xySeries;

    private Button btnAddPt;

    private EditText mX,mY;

    GraphView mScatterPlot;

    //make xyValueArray global
    private ArrayList<XYValue> xyValueArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_orientationValue = (TextView)findViewById(R.id.orientation_value);
        stepCounterTV = (TextView)findViewById(R.id.stepCounterVal);
        stepDetectorTV = (TextView)findViewById(R.id.stepDetectorVal);
        coordinatesTV = (TextView)findViewById(R.id.coordinatesTV);
        //declare variables in oncreate
        mScatterPlot = (GraphView) findViewById(R.id.scatterPlot);
        xyValueArray = new ArrayList<>();

        //coordinatesTV.setText(coordinates);

        Log.d(TAG, "onCreate: Initializing Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener sensorEventListenerAccelrometer = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGravity = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                orientationVal = (float)(-floatOrientation[0]*180/3.14159);
               // tv_orientationValue.setText(Float.toString(orientationVal));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGeoMagnetic = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                // imageView.setRotation((float) (-floatOrientation[0]*180/3.14159));
                float orientationVal = (float)(-floatOrientation[0]*180/3.14159);
              //  tv_orientationValue.setText(Float.toString(orientationVal));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);



        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(stepCounter != null){
            sensorManager.registerListener(MainActivity.this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onCreate: Registered stepCounter Listener");
        }
        else{
           // stepCounterTV.setText("stepCounter not supported!");
        }

        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if(stepDetector != null){
            sensorManager.registerListener(MainActivity.this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onCreate: Registered stepCounter Listener");
        }
        else{
          //  stepDetectorTV.setText("stepCounter not supported!");
        }

        xyValueArray.add(new XYValue(0,0));
        init();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if(sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            stepCount = (int) event.values[0];
           // stepCounterTV.setText(String.valueOf(stepCount));
        }
        if(sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            stepDetected++;
         //   stepDetectorTV.setText(Integer.toString(stepDetected));
            double realOrientVal = orientationVal;
            realOrientVal = realOrientVal>=0?realOrientVal:360+realOrientVal;
            realOrientVal =(double)(realOrientVal * (3.14159/180));
            xValue += Math.sin(realOrientVal);
            yValue += Math.cos(realOrientVal);
            xyValueArray.add(new XYValue(yValue,xValue));
            init();
            String newCoordinates = "(" + Double.toString(yValue)+","+ Double.toString(xValue)+")";
            //coordinates = coordinates + newCoordinates;
            coordinates = newCoordinates;
          //  coordinatesTV.setText(newCoordinates);
        }


    }

    private void init(){
        //declare the xySeries Object
        xySeries = new PointsGraphSeries<>();

        /*btnAddPt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mX.getText().toString().equals("") && !mY.getText().toString().equals("") ){
                    double x = Double.parseDouble(mX.getText().toString());
                    double y = Double.parseDouble(mY.getText().toString());
                    Log.d(TAG, "onClick: Adding a new point. (x,y): (" + x + "," + y + ")" );
                    xyValueArray.add(new XYValue(x,y));
                    init();
                }else {
                    toastMessage("You must fill out both fields!");
                }
            }
        });  */

        //little bit of exception handling for if there is no data.
        if(xyValueArray.size() != 0){
            createScatterPlot();
        }else{
            Log.d(TAG, "onCreate: No data to plot.");
        }
    }
    private void createScatterPlot() {
        Log.d(TAG, "createScatterPlot: Creating scatter plot.");

        //sort the array of xy values
        xyValueArray = sortArray(xyValueArray);

        //add the data to the series
        for(int i = 0;i <xyValueArray.size(); i++){
            try{
                double x = xyValueArray.get(i).getX();
                double y = xyValueArray.get(i).getY();
                xySeries.appendData(new DataPoint(x,y),true, 1000);
            }catch (IllegalArgumentException e){
                Log.e(TAG, "createScatterPlot: IllegalArgumentException: " + e.getMessage() );
            }
        }

        //set some properties
        xySeries.setShape(PointsGraphSeries.Shape.RECTANGLE);
        xySeries.setColor(Color.BLUE);
        xySeries.setSize(20f);

        //set Scrollable and Scaleable
        mScatterPlot.getViewport().setScalable(true);
        mScatterPlot.getViewport().setScalableY(true);
        mScatterPlot.getViewport().setScrollable(true);
        mScatterPlot.getViewport().setScrollableY(true);

        //set manual x bounds
        mScatterPlot.getViewport().setYAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxY(30);
        mScatterPlot.getViewport().setMinY(-30);

        //set manual y bounds
        mScatterPlot.getViewport().setXAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxX(30);
        mScatterPlot.getViewport().setMinX(-30);

        mScatterPlot.addSeries(xySeries);
    }
    /**
     * Sorts an ArrayList<XYValue> with respect to the x values.
     * @param array
     * @return
     */
    private ArrayList<XYValue> sortArray(ArrayList<XYValue> array){
        /*
        //Sorts the xyValues in Ascending order to prepare them for the PointsGraphSeries<DataSet>
         */
        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(),2))));
        int m = array.size() - 1;
        int count = 0;
        Log.d(TAG, "sortArray: Sorting the XYArray.");


        while (true) {
            m--;
            if (m <= 0) {
                m = array.size() - 1;
            }
            Log.d(TAG, "sortArray: m = " + m);
            try {
                //print out the y entrys so we know what the order looks like
                //Log.d(TAG, "sortArray: Order:");
                //for(int n = 0;n < array.size();n++){
                //Log.d(TAG, "sortArray: " + array.get(n).getY());
                //}
                double tempY = array.get(m - 1).getY();
                double tempX = array.get(m - 1).getX();
                if (tempX > array.get(m).getX()) {
                    array.get(m - 1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m - 1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                } else if (tempX == array.get(m).getX()) {
                    count++;
                    Log.d(TAG, "sortArray: count = " + count);
                } else if (array.get(m).getX() > array.get(m - 1).getX()) {
                    count++;
                    Log.d(TAG, "sortArray: count = " + count);
                }
                //break when factorial is done
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "sortArray: ArrayIndexOutOfBoundsException. Need more than 1 data point to create Plot." +
                        e.getMessage());
                break;
            }
        }
        return array;
    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

}
