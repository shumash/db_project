package db_proj;

import java.awt.Color;

// u* and v* range ±100. By definition, 0 ≤ L* ≤ 100.
public class LuvColor {
	// Reference: http://www.easyrgb.com/index.php?X=MATH&H=02#text2

	private double L = 0;
	private double U = 0;
	private double V = 0;

	public LuvColor(Color color) {
		double xyz[] = toXYZ(color.getRed(), color.getGreen(), color.getBlue());
		//System.out.println("xyz = "+ xyz[0] + "," + xyz[1] + "," + xyz[2]);
		double luv[] = toLuv(xyz[0], xyz[1], xyz[2]);
		L = luv[0];
		U = luv[1];
		V = luv[2];
	}

	public LuvColor(double inL, double inU, double inV) {
		L = inL;
		U = inU;
		V = inV;
	}

	public int qunatize() {
        int[] quantized = {
            (int) // L: 0-100
            Math.floor(L / 15.0),  // 0-6: 3 bits
            (int) // u: -120 - 120
            Math.floor(Math.min(Math.max(0, U + 120), 240) / 24.0), //0-10: 4 bits
            (int) // v: -120 - 120
            Math.floor(Math.min(Math.max(0, V + 120), 240) / 24.0)}; //0-10: 4 bits

        int result = 0;
        result += quantized[0];
        result <<= 4;
        result += quantized[1];
        result <<= 4;
        result += quantized[2];

        SimpleTimer.timedLog("\n Qunatized (" + L + ", " + U + ", " + V + ") as " + result + "\n");
        return -result;  // to distinguish from other hashes
    }

	public double getL() {
		return L;
	}

	public double getU() {
		return U;
	}

	public double getV() {
		return V;
	}

	public String toString() {
		return "L: " + L + ", U: " + U + ", V: " + V;
	}

	static public double[] toXYZ(double R, double G, double B) {
		double var_R = ( R / 255.0 );        //R from 0 to 255
		double var_G = ( G / 255.0 );        //G from 0 to 255
		double var_B = ( B / 255.0 );        //B from 0 to 255

		if ( var_R > 0.04045 ) var_R = Math.pow( ( var_R + 0.055 ) / 1.055, 2.4);
		else                   var_R = var_R / 12.92;
		if ( var_G > 0.04045 ) var_G = Math.pow( ( var_G + 0.055 ) / 1.055, 2.4);
		else                   var_G = var_G / 12.92;
		if ( var_B > 0.04045 ) var_B = Math.pow( ( var_B + 0.055 ) / 1.055, 2.4);
		else                   var_B = var_B / 12.92;

		var_R = var_R * 100;
		var_G = var_G * 100;
		var_B = var_B * 100;

		//Observer. = 2°, Illuminant = D65
		double X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
		double Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
		double Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;
		double res[] = new double[3];
		res[0] = X;
		res[1] = Y;
		res[2] = Z;
		return res;
	}

	static public double[] toLuv(double [] xyz) {
		return toLuv(xyz[0], xyz[1], xyz[2]);
	}

	static public double[] toLuv(double X, double Y, double Z) {
		double var_U = ( 4 * X ) / ( X + ( 15 * Y ) + ( 3 * Z ) );
		double var_V = ( 9 * Y ) / ( X + ( 15 * Y ) + ( 3 * Z ) );

		if (Double.isNaN(var_U)) {
			var_U = 0;
		}
		if (Double.isNaN(var_V)) {
			var_V = 0;
		}

		double var_Y = Y / 100;
		if ( var_Y > 0.008856 ) var_Y = Math.pow(var_Y, 1/3.0 );
		else                    var_Y = ( 7.787 * var_Y ) + ( 16 / 116 );

		double ref_X =  95.047;        //Observer= 2°, Illuminant= D65
		double ref_Y = 100.000;
		double ref_Z = 108.883;

		double ref_U = ( 4 * ref_X ) / ( ref_X + ( 15 * ref_Y ) + ( 3 * ref_Z ) );
		double ref_V = ( 9 * ref_Y ) / ( ref_X + ( 15 * ref_Y ) + ( 3 * ref_Z ) );

		double L = ( 116 * var_Y ) - 16;
		double u = 13 * L * ( var_U - ref_U );
		double v = 13 * L * ( var_V - ref_V );

		double res[] = new double[3];
		res[0] = L;
		res[1] = u;
		res[2] = v;
		return res;
	}

}
