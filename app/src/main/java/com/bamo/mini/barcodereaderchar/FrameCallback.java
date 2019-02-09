package com.bamo.mini.barcodereaderchar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FrameCallback implements  PreviewCallback {

    private MainActivity parentActivity;
    private EditText codeContainer;

    public  FrameCallback(MainActivity parentActivity,EditText codeContainer){
        this.parentActivity = parentActivity;
        this.codeContainer = codeContainer;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
       int len = bytes.length;
       Bitmap bMap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
       String code = decodeBarCode(bMap);
       if (code !=null){
           //get the code and display the code result on the activity file
           //then  stop the capture or put a button to allow stop the capture
           codeContainer.setText(code);
       }
    }

    //this class decodes the barcode images and return the result. if
    public  String decodeBarCode(Bitmap raw){
        String result = "";
        int whitePixelRange=255;
        int blackPixelRange = 0;
        //peform all necessary preprocessing here
        int width = raw.getWidth();
        int height = raw.getHeight();
        //get the pixel in the middle and decode the image information there.
         int middle = height/2;
         String code = decodeBitmap(raw,width,height,whitePixelRange,blackPixelRange);
         return code;
    }

    private ArrayList[] buildPatternList(Bitmap raw,int width,int height,int white,int black){
        //start from the top of the image  and search till the middle of the image for the pattern
        boolean blackFlag = false;
        boolean whiteflag = false;
        int currentIndex = -1;
        boolean fresh = true;
        ArrayList<Integer> countList = new ArrayList<>();
        ArrayList<Character> binaryList = new ArrayList<>();
        int i = height/2;
        for (int j=0;j<width;j++){
            //check for the pattern three longer pattern
            int currentPixel = raw.getPixel(j,i);
            if (currentPixel<=black){
                //start checking for the pattern
                if (whiteflag || fresh){
                    countList.add(1);
                    currentIndex+=1;
                    whiteflag=false;
                    binaryList.add('1');
                    fresh=false;
                }
                else if (blackFlag){
                    countList.set(currentIndex,countList.get(currentIndex)+1);
                }
                blackFlag=true;
            }
            if (currentPixel >= white){
                if (blackFlag || fresh){
                    countList.add(1);
                    currentIndex+=1;
                    blackFlag=false;
                    binaryList.add('0');
                    fresh=false;
                }
                else if (whiteflag){
                    countList.set(currentIndex,countList.get(currentIndex)+1);
                }
                whiteflag=true;
            }
        }
        return new ArrayList []{countList,binaryList};
    }
    //the black and the white parameter represents the code for the white and the black respectively
    public String decodeBitmap(Bitmap raw,int width,int height,int white,int black) throws  Exception{

        ArrayList [] countAndPattern = buildPatternList(raw,width,height,white,black);
        ArrayList<Integer> patternList = (ArrayList<Integer>)countAndPattern[0];
        ArrayList<Character> charList = (ArrayList<Character>)countAndPattern[1];
        int startLocation = estimateStartLocation(patternList,charList);
        int endLocation = estimateEndLocation(patternList,charList);
        List<Character> list = charList.subList(startLocation,endLocation);
        //arraylist of seven character array each.
        ArrayList<Character []> binaryString = getBinaryEncoding(list);
        String result = decodeResult(binaryString);
        return result;
    }


    private String decodeResult(ArrayList<Character []> encoded) throws  Exception{
        //perform basic look up here
        char [] resultChar = new char[encoded.size()];
        int currentIndex = 0;
        BarcodeBinaryEncoding decoder = new BarcodeBinaryEncoding();
        for (Character [] current: encoded) {
            resultChar[currentIndex] = decoder.decode(current);
        }
        return new String(resultChar);
    }

    private ArrayList<Character []> getBinaryEncoding(List <Character> charlist){
        ArrayList<Character []> result  = new ArrayList<>();
        int startIndex = 0;
        int count =0;
        while (startIndex+7 < result.size()){
            if (count==6)
                startIndex+=3;
            List<Character> temp =charlist.subList(startIndex,startIndex+7);
            Character [] tempArray = new Character[][temp.size()];
            result.add(temp.toArray(tempArray));
            startIndex+=7;
            count++;
        }
        return result;
    }
    private int estimateStartLocation(ArrayList<Integer> patternList,ArrayList<Character> binEncode){
        // from the patternlist get the number with the maximum count and return it as the gin value
        int binSize = getPrevalentSize(patternList);
        //use a window size of three forward and three backward to get start and end of the code
        int index=0;
        while (index+3 < binEncode.size()){
            int avg = (patternList.get(index)+patternList.get(index+1) + patternList.get(index+2))/3;
            boolean matched = binEncode.get(index)=='0' && binEncode.get(index+1)=='1' && binEncode.get(index+2)=='0';
            if (avg==binSize && matched){
                return index+3;
            }
            index++;
        }
        return -1;
    }

    private int estimateEndLocation(ArrayList<Integer> patternList,ArrayList<Character> binEncode){
        // from the patternlist get the number with the maximum count and return it as the gin value
        int binSize = getPrevalentSize(patternList);
        //use a window size of three forward and three backward to get start and end of the code
        int index=binEncode.size();
        while (index-3 >= 0){
            int avg = (patternList.get(index)+patternList.get(index-1) + patternList.get(index-2))/3;
            boolean matched = binEncode.get(index)=='0' && binEncode.get(index-1)=='1' && binEncode.get(index-2)=='0';
            if (avg==binSize && matched){
                return index-3;
            }
            index++;
        }
        return -1;
    }
    private int getPrevalentSize(ArrayList<Integer> pattern){
        HashMap<Integer,Integer> table = new HashMap<>();
        int max =1;
        int maxValue =0;
        for (int i=0; i < pattern.size(); i++){
            int currentValue = pattern.get(i);
            if (table.containsKey(currentValue)){
                table.put(currentValue,table.get(currentValue)+1);
                if (table.get(currentValue) > max){
                    max = table.get(currentValue);
                    maxValue=currentValue;
                }
            }
            else{
                table.put(currentValue,1);
            }
        }
        return maxValue;
    }
}
