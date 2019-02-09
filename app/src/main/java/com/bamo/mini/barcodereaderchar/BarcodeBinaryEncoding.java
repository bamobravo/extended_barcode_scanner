package com.bamo.mini.barcodereaderchar;

import android.support.v4.app.SupportActivity;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;

class BarcodeBinaryEncoding {

    private Hashtable<Character, BitArray> leftEvenParity = new Hashtable<>();
    private Hashtable<Character, BitArray> leftOddParity = new Hashtable<>();
    private Hashtable<Character,BitArray> right = new Hashtable<>();
    private Hashtable<Integer,Boolean []> leftSideEanMapping = new Hashtable<>();
    enum CodePosition{
        RIGHT,
        lEFT,
        lEFT_EVEN

    }

    public BarcodeBinaryEncoding() throws Exception {
        buildEncoding();
    }

    private void buildEncoding() throws  Exception{
        buildEanMapping();
        buildLeftSideEncoding();
        buildRightsideEncoding();
    }

    private void buildRightsideEncoding() {

    }

    private void buildEanMapping(){
        //true means odd parity while false means even parity
        leftSideEanMapping.put(0,new Boolean[]{true,true,true,true,true,true});
        leftSideEanMapping.put(1,new Boolean[]{true,true,false,true,false,false});
        leftSideEanMapping.put(2,new Boolean[]{true,true,false,false,true,false});
        leftSideEanMapping.put(3,new Boolean[]{true,true,false,false,false,true});
        leftSideEanMapping.put(4,new Boolean[]{true,false,true,true,false,false});
        leftSideEanMapping.put(5,new Boolean[]{true,false,false,true,true,false});
        leftSideEanMapping.put(6,new Boolean[]{true,false,false,false,true,true});
        leftSideEanMapping.put(7,new Boolean[]{true,false,true,false,true,false});
        leftSideEanMapping.put(8,new Boolean[]{true,false,true,false,false,true});
        leftSideEanMapping.put(9,new Boolean[]{true,false,false,true,false,true});
    }

    private void buildLeftSideEncoding() throws Exception{
        leftEvenParity.put('0',new BitArray("0001101"));
        leftEvenParity.put('1',new BitArray("0011001"));
        leftEvenParity.put('2',new BitArray("0010011"));
        leftEvenParity.put('3',new BitArray("0111101"));
        leftEvenParity.put('4',new BitArray("0100011"));
        leftEvenParity.put('5',new BitArray("0110001"));
        leftEvenParity.put('6',new BitArray("0101111"));
        leftEvenParity.put('7',new BitArray("0111011"));
        leftEvenParity.put('8',new BitArray("0110111"));
        leftEvenParity.put('9',new BitArray("0001011"));
        buildOtherEncodings();
    }

    private void buildOtherEncodings() throws Exception{
        //invert the result for the even
        char [] keys = new char[]{'0','1','2','3','4','5','6','7','8','9'};
        for (int i = 0; i < keys.length; i++) {
            leftOddParity.put(keys[i],leftEvenParity.get(keys[i]).reverseAndFlipBit());
            right.put(keys[i],leftEvenParity.get(keys[i]).flipBit());
        }
    }

    public char decode(Character[] current,CodePosition position) {
        char [] convert = convertCharacterObject(current);
        return decode(convert,position);
    }
    //convert the character array to character
    public char decode(char [] current,CodePosition position) {
        char result =' ';
        switch (position){
            case lEFT:
                result= lookup(leftOddParity,current);
            break;
            case RIGHT:
                result= lookup(right,current);
            break;
            case lEFT_EVEN:
                result= lookup(leftEvenParity,current);
            break;
        }
        return result;
    }

    private char lookup(Hashtable<Character, BitArray> dictionary, char[] current) {

        Set<Character> keys = dictionary.keySet();
        for (char key: keys) {
            BitArray currentValue = dictionary.get(key);
            if (Arrays.equals(currentValue.getBitString(),current)){
                return key;
            }
        }
        return ' ';
    }

    private char [] convertCharacterObject(Character [] input){
        char [] result = new char[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i]=input[i].charValue();
        }
        return result;
    }
}
