package com.fsck.k9.crypto;


import java.math.BigInteger;


public class EllipticCurveDigitalSignature {
    BigInteger r;
    BigInteger s;

    static EllipticCurve ec = new EllipticCurve();

    public EllipticCurveDigitalSignature(BigInteger r, BigInteger s){
        this.r = r;
        this.s = s;
    }

    public static EllipticCurveDigitalSignature sign(BigInteger privateKey, BigInteger hash, BigInteger nonce){
        // r = the x value of a random point on the curve
        BigInteger r = ec.multiply(nonce).x;
        r = r.mod(ec.n);

        // s = nonce⁻¹ * (hash + private_key * r) mod n
        BigInteger s = (nonce.modInverse(ec.n).multiply(hash.add(privateKey.multiply(r))));
        s = s.mod(ec.n);

        return new EllipticCurveDigitalSignature(r, s);
    }

    public static boolean verify(Point publicKey, EllipticCurveDigitalSignature signature, BigInteger hash){
        Point p1 = ec.multiply(signature.s.modInverse(ec.n).multiply(hash));

        Point p2 = ec.multiply((signature.s.modInverse(ec.n).multiply(signature.r)), publicKey);

        Point p3 = ec.addPoints(p1, p2);

        return p3.x == signature.r;
    }

    public static Point getPublicKey(BigInteger privateKey){
        // public key is the generator function multiplied by the private key
        return ec.multiply(privateKey);
    }

    public static String getPublicKeyString(Point publicKey){
        // Convert x and y values of the public key to hexadecimal of length 64
        String x = publicKey.x.toString(16);
        x = String.format("%64s", x).replace(' ', '0');

        String y = publicKey.y.toString(16);
        y = String.format("%64s", y).replace(' ', '0');

        return "04" + x + y;
    }
}
