package com.bamo.mini.barcodereaderchar;

class BitArray {
    private char[] bits = new char[7];

    public BitArray(String input) throws Exception{
        if (input.length()!=7) {
            throw new Exception("input to contain seven binary characters");

        }
        //parse the input into the bit array

        char [] charArr = input.toCharArray();
        for (int i = 0; i < charArr.length; i++) {
            if (charArr[i]!='1' && charArr[i]!='0') {
                System.out.println(charArr[i]);
                throw new Exception("invalid input value");
            }
            bits[i]=charArr[i];
        }
    }

    public BitArray(char [] arr) throws Exception{
        if (arr.length !=7) {
            throw new Exception("invalid input value");
        }
        this.bits = arr;
    }

    @Override
    public String toString(){
        String result ="";
        for (int i = 0; i < bits.length; i++) {
            result+=bits[i];
        }
        return result;
    }

    public char[] getBitString() {
        return bits;
    }

    public BitArray flipBit() throws  Exception{
        //peform a non mutable reverser of this list and create a new object.
        char[] temp = new char[bits.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i]=bits[i]=='1'?'0':'1';
        }
        return new BitArray(temp);
    }

    public BitArray reverseAndFlipBit() throws Exception{
        char[] temp = new char[bits.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i]=bits[temp.length-(i+1)]=='1'?'0':'1';
        }
        return new BitArray(temp);
    }
}
