package com.bamo.mini.barcodereaderchar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
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
        try {
            int len = bytes.length;
            Bitmap bMap = parentActivity.getTestBitMap(); //just to test the process without using the camera image first.
//            Bitmap bMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            String code = decodeBarCode(bMap);
            if (code != null) {
                //get the code and display the code result on the activity file
                //then  stop the capture or put a button to allow stop the capture
                codeContainer.setText(code);
            }
        }
        catch (Exception ex){
            Toast.makeText(parentActivity,ex.getMessage(),3000).show();
        }
    }

    //this class decodes the barcode images and return the result. if
    public  String decodeBarCode(Bitmap raw) throws  Exception{
        String result = "";
        int whitePixelRange= Color.WHITE;
        int blackPixelRange = Color.BLACK;
        //peform all necessary preprocessing here
        int width = raw.getWidth();
        int height = raw.getHeight();
        //get the pixel in the middle and decode the image information there.
         int middle = height/2;
         String code = decodeBitmap(raw,width,height,whitePixelRange,blackPixelRange);
         return code;
    }

    private ArrayList buildPatternList(Bitmap raw,int width,int height,int white,int black){
        //start from the top of the image  and search till the middle of the image for the pattern
        //also need to get the first position of the pixel to be used.
        int numberCount=(13*7)+6;//the 6 is for the border section of the image
        Object [] pixelCountAndStartIndex  = getPixelCountPerStroke(raw,width,height,white,black);
        int pixelCount = (int)pixelCountAndStartIndex[0];
        int startIndex = (int)pixelCountAndStartIndex[1];
        ArrayList<Character> result =  new ArrayList<>();
        ArrayList<Integer> countList = (ArrayList<Integer>) pixelCountAndStartIndex[2];
        ArrayList<Character> binaryList = (ArrayList<Character>) pixelCountAndStartIndex[3];
        while (result.size() < numberCount){
            char current = binaryList.get(startIndex);
            int num = countList.get(startIndex);
            int len = num/pixelCount;
            for (int i=0;i<len;i++){
                result.add(current);
            }
            startIndex++;
        }
        return result;
    }

    private Object [] getPixelCountPerStroke(Bitmap raw,int width,int height,int white,int black){
        //start from the top of the image  and search till the middle of the image for the pattern
        boolean blackFlag = false;
        boolean whiteflag = false;
        int currentIndex = -1;
        int count=0;
        boolean fresh = true;
        ArrayList<Integer> countList = new ArrayList<>();
        ArrayList<Character> binaryList = new ArrayList<>();
        int i = height/2;
        for (int j=0;j<width;j++){
            //check for the pattern three longer patter
            int currentPixel = raw.getPixel(j,i);
            int red = Color.red(currentPixel);
            int blue = Color.blue(currentPixel);
            int green = Color.green(currentPixel);
            if (isBlackRange(red,blue,green)){
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
            if (isWhiteRange(red,blue,green)){
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
            count++;
        }

        int size  = getPrevalentSize(countList);
//        return size;
        //just testing to get the position of the start marker,
        int startIndex = getStartMarkerPosition(size,countList,binaryList);
        return new Object[]{size,startIndex,countList,binaryList};
//        return new ArrayList []{countList,binaryList};
    }

    private boolean confirmMiddleBorder(ArrayList<Integer> countList,ArrayList<Character> binaryList,int startIndex,int bitPerNumber, int size,int numberCount,int offset){
        int counter =offset;
        int newIndex = startIndex-1;

        while ((counter <= (numberCount*bitPerNumber * size) && newIndex  < countList.size())){
            newIndex++;
            counter+=countList.get(newIndex);
             //i am at the current row now.
        }
        if (counter > (((numberCount*bitPerNumber)) * size) ){
            if ((binaryList.get(newIndex)=='1' && binaryList.get(newIndex+1)=='0' && binaryList.get(newIndex+2)=='1' && countList.get(newIndex) > size && countList.get(newIndex+1) == size && countList.get(newIndex+2) >= size)){
                return true;
            }
        }
        if (counter == (((numberCount*bitPerNumber)) *  size) ){
            if ((binaryList.get(newIndex+1)=='1' && binaryList.get(newIndex+2)=='0' && binaryList.get(newIndex+3)=='1' && countList.get(newIndex+1) == size && countList.get(newIndex+2) == size && countList.get(newIndex+3) >= size)){
                return true;
            }
        }
        return false;
    }

    private boolean confirmLastBorder(ArrayList<Integer> countList,ArrayList<Character> binaryList,int startIndex,int bitPerNumber, int size,int numberCount,int offset){
        int counter =offset;
        int newIndex = startIndex-1;
        int maxCount = (((numberCount*bitPerNumber)+3) * size);
        while ((counter <= maxCount || newIndex >=countList.size())){
            newIndex++;
            counter+=countList.get(newIndex);
        }
        if (counter > (((numberCount*bitPerNumber)+3) * size) ){
            if ((binaryList.get(newIndex)=='1' && binaryList.get(newIndex+1)=='0' && binaryList.get(newIndex+2)=='1' && countList.get(newIndex) > size && countList.get(newIndex+1) == size && countList.get(newIndex+2) >= size)){
                return true;
            }
        }
        if (counter == (((numberCount*bitPerNumber)+3) * size) ){
            if ((binaryList.get(newIndex+1)=='1' && binaryList.get(newIndex+2)=='0' && binaryList.get(newIndex+3)=='1' && countList.get(newIndex+1) == size && countList.get(newIndex+2) == size && countList.get(newIndex+3) >= size)){
                return true;
            }
        }
        return false;
    }

    private int  getStartMarkerPosition(int size, ArrayList<Integer> countList, ArrayList<Character> binaryList) {
        //get the part where the pattern want to start matching
        //then check if all pixel are added together from that point that the calculation will be correct.
        int position =0;
        int numberCount=6;
        int bitPerNumber =7;
        while ((position +3) <=countList.size()){
            if (binaryList.get(position)=='1' && binaryList.get(position+1)=='0' && binaryList.get(position+2)=='1'){
                boolean control =  countList.get(position)==size && countList.get(position+1)==size && countList.get(position+2) >=size;
                if (!control) {
                    position++;
                    continue;
                }
                //validate border here
                int offset = countList.get(position+2) -3;
              if(confirmMiddleBorder(countList,binaryList,position+3,bitPerNumber,size,numberCount,offset ) && confirmLastBorder(countList,binaryList,position+3,bitPerNumber,size,13,offset)){
                  return offset > 0? position+2:position+3;
              }
                position++;
                continue;
            }
            else{
                position++;
                continue;
            }
        }
        return -1;
    }

    private boolean isBlackRange(int red, int blue, int green) {
        //
        int diff = 50;
        return red <= diff && blue <= diff && green <= diff;
    }


    private boolean isWhiteRange(int red, int blue, int green){
        int diff = 205;
        return red >= diff && blue >= diff && green >=diff;
    }
    //the black and the white parameter represents the code for the white and the black respectively
    public String decodeBitmap(Bitmap raw,int width,int height,int white,int black) throws  Exception{

        ArrayList binaryArray = buildPatternList(raw,width,height,white,black);
//        ArrayList<Integer> patternList = (ArrayList<Integer>)countAndPattern[0];
//        ArrayList<Character> charList = (ArrayList<Character>)countAndPattern[1];
//        int startLocation = estimateStartLocation(patternList,charList);
//        int endLocation = estimateEndLocation(patternList,charList);
//        List<Character> list = charList.subList(startLocation,endLocation);
        //arraylist of seven character array each.
        ArrayList<Character []> binaryString = getBinaryEncoding(binaryArray);
        String result = decodeResult(binaryString);
        return result;
    }


    private String decodeResult(ArrayList<Character []> encoded) throws  Exception{
        //perform basic look up here
        char [] resultChar = new char[encoded.size()];
        int currentIndex = 0;
        BarcodeBinaryEncoding decoder = new BarcodeBinaryEncoding();
        int firstIndex =-1;
        for (Character [] current: encoded) {
            BarcodeBinaryEncoding.CodePosition position  = null;
            if (currentIndex < 6){
                position = BarcodeBinaryEncoding.CodePosition.lEFT;
            }else{
                position = BarcodeBinaryEncoding.CodePosition.RIGHT;
            }
            resultChar[currentIndex] = decoder.decode(current,position);
            currentIndex++;
        }
        return new String(resultChar);
    }

    private ArrayList<Character []> getBinaryEncoding(List <Character> charlist){
        ArrayList<Character []> result  = new ArrayList<>();
        int startIndex = 0;
        int count =0;
        while (startIndex+7 < charlist.size()){
            if (count==6)
                startIndex+=3;
            List<Character> temp =charlist.subList(startIndex,startIndex+7);
            Character [] tempArray = new Character[temp.size()];
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

    public void startProcess(MainActivity parentActivity,EditText codeContainer) {
        try {
//            int len = bytes.length;
            Bitmap bMap = parentActivity.getTestBitMap(); //just to test the process without using the camera image first.
//            Bitmap bMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            String code = decodeBarCode(bMap);
            if (code != null) {
                //get the code and display the code result on the activity file
                //then  stop the capture or put a button to allow stop the capture
                codeContainer.setText(code);
            }
        }
        catch (Exception ex){
            Toast.makeText(parentActivity,ex.getMessage(),3000).show();
        }
    }
}
