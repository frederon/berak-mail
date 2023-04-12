package com.fsck.k9.crypto;


import java.math.BigInteger;

public class EllipticCurve {
    private BigInteger a = new BigInteger("0");
    private BigInteger b = new BigInteger("7");

    // 2 ** 256 - 2 ** 32 - 2 ** 9 - 2 ** 8 - 2 ** 7 - 2 ** 6 - 2 ** 4 - 1
    private BigInteger prime =
        new BigInteger("115792089237316195423570985008687907853269984665640564039457584007908834671663");

    private BigInteger gx = new BigInteger("55066263022277343669578718895168534326250603453777594175500187360389116729240");
    private BigInteger gy = new BigInteger("32670510020758816978083085130507043184471273380659243275938904335757337482424");

    private Point doublePoint(Point p){
        // slope = (3x₁² + a) / 2y₁
        BigInteger slope = ((p.x.pow(2).multiply(new BigInteger("3")).add(this.a)).multiply(p.y.multiply(new BigInteger("2")).modInverse(this.prime))).mod(this.prime);

        // slope² - 2x₁
        BigInteger x = (slope.pow(2).subtract(p.x.multiply(new BigInteger("2")))).mod(this.prime);

        // slope * (x₁ - x) - y₁
        BigInteger y = (slope.multiply(p.x.subtract(x)).subtract(p.y)).mod(this.prime);

        return new Point(x, y);
    }

    public Point addPoints(Point p1, Point p2){
        if(p1.equals(p2)){
            return doublePoint(p1);
        }

        // slope = (y₁ - y₂) / (x₁ - x₂)
        BigInteger slope = ((p1.y.subtract(p2.y)).divide(p1.x.subtract(p2.y))).mod(this.prime);

        // x = slope ^ 2 - x1 - x2
        BigInteger x = (slope.pow(2).subtract(p1.x).subtract(p2.x)).mod(this.prime);

        // y = slope * (x1-x) - y1
        BigInteger y = (slope.multiply(p1.x.subtract(x)).subtract(p1.y)).mod(this.prime);

        return new Point(x,y);
    }

    public Point multiply(BigInteger k, Point p){
        Point current = new Point(p);

        String binary = k.toString(2);

        // skip first digit
        for(int i = 1; i < binary.length(); i++){
            char c = binary.charAt(i);

            // 0/1 then double
            current = doublePoint(current);

            // 1 then add
            if(c == '1'){
                current = addPoints(current, p);
            }
        }

        return current;
    }
}
