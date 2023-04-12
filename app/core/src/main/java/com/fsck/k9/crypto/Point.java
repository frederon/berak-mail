package com.fsck.k9.crypto;


import java.math.BigInteger;


public class Point {
    public BigInteger x;
    public BigInteger y;

    public Point(BigInteger x, BigInteger y){
        this.x = x;
        this.y = y;
    }

    public Point(Point p){
        this.x = p.x;
        this.y = p.y;
    }

    public boolean equals(Point p){
        return this.x == p.x && this.y == p.y;
    }
}
