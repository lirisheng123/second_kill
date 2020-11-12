package com.lirisheng.my_second_kill.util;

public class Barn {
    public static void main(String[ ] args){
        int x = 5;
        boolean b1 = true;
        boolean b2 = false;
        if((x==4) && !b2)
            System.out.print("1 ");
        System.out.print("2 ");
        if((b2=true) && b1)
            System.out.print("3 ");

    }

}
