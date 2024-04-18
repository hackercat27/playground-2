package ca.hackercat.playground.math;

import java.util.Random;

public class PGMath {
    private PGMath() {}

    public static final double PI = 3.141592653589793d;
    public static final double TAU = 6.283185307179586;
    public static final float PIf = 3.1415927f;
    public static final float TAUf = 6.2831855f;
    private static final double DG_RDd = 0.017453292519943295;
    private static final double RD_DGd = 57.29577951308232;
    private static final float DG_RDf = 0.017453292f;
    private static final float RD_DGf = 57.29578f;

    private static final Random random = new Random();

    public static double randomd() {
        return random.nextDouble();
    }
    public static float randomf() {
        return random.nextFloat();
    }

    public static float sin(float a) {
        return (float) Math.sin(a);
    }
    public static float cos(float a) {
        return (float) Math.cos(a);
    }
    public static float tan(float a) {
        return (float) Math.tan(a);
    }

    public static float asin(float a) {
        return (float) Math.asin(a);
    }
    public static float acos(float a) {
        return (float) Math.acos(a);
    }
    public static float atan(float a) {
        return (float) Math.atan(a);
    }
    public static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    public static double sin(double a) {
        return Math.sin(a);
    }
    public static double cos(double a) {
        return Math.cos(a);
    }
    public static double tan(double a) {
        return Math.tan(a);
    }

    public static double asin(double a) {
        return Math.asin(a);
    }
    public static double acos(double a) {
        return Math.acos(a);
    }
    public static double atan(double a) {
        return Math.atan(a);
    }
    public static double atan2(double y, double x) {
        return Math.atan2(y, x);
    }

    public static int max(int... nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        int max = Integer.MIN_VALUE;
        for (int num : nums) {
            max = Math.max(num, max);
        }
        return max;
    }
    public static int min(int... nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        int min = Integer.MAX_VALUE;
        for (int num : nums) {
            min = Math.min(num, min);
        }
        return min;
    }
    public static int clamp(int num, int lower, int upper) {
        return Math.min(Math.max(num, lower), upper);
    }

    public static long max(long... nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        long max = Long.MIN_VALUE;
        for (long num : nums) {
            max = Math.max(num, max);
        }
        return max;
    }
    public static long min(long... nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        long min = Long.MAX_VALUE;
        for (long num : nums) {
            min = Math.min(num, min);
        }
        return min;
    }
    public static long clamp(long num, long lower, long upper) {
        return Math.min(Math.max(num, lower), upper);
    }

    public static float max(float... nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        float max = Float.NEGATIVE_INFINITY;
        for (float num : nums) {
            max = Math.max(num, max);
        }
        return max;
    }
    public static float min(float... nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        float min = Float.POSITIVE_INFINITY;
        for (float num : nums) {
            min = Math.min(num, min);
        }
        return min;
    }
    public static float clamp(float num, float lower, float upper) {
        return Math.min(Math.max(num, lower), upper);
    }

    public static double max(double... nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        double max = Double.NEGATIVE_INFINITY;
        for (double num : nums) {
            max = Math.max(num, max);
        }
        return max;
    }
    public static double min(double... nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        double min = Double.POSITIVE_INFINITY;
        for (double num : nums) {
            min = Math.min(num, min);
        }
        return min;
    }
    public static double clamp(double num, double lower, double upper) {
        return Math.min(Math.max(num, lower), upper);
    }

    public static int abs(int a) {
        return Math.abs(a);
    }
    public static long abs(long a) {
        return Math.abs(a);
    }
    public static float abs(float a) {
        return Math.abs(a);
    }
    public static double abs(double a) {
        return Math.abs(a);
    }

    public static float copySign(float magnitude, float sign) {
        return Float.intBitsToFloat(
                (Float.floatToIntBits(magnitude) & 0x7fffffff) | (Float.floatToIntBits(sign) & 0x80000000)
        );
    }
    public static double copySign(double magnitude, double sign) {
        return Double.longBitsToDouble(
                (Double.doubleToLongBits(magnitude) & 0x7fffffffffffffffL) | (Double.doubleToLongBits(sign) & 0x8000000000000000L)
        );
    }

    public static int round(float a) {
        float remainder = abs(a % 1);
        if (remainder < 0.5f) {
            return (int) a;
        }
        return (int) copySign(abs(a) + 1f, a);
    }
    public static int floor(float a) {
        return (int) a;
    }
    public static int ceiling(float a) {
        return (int) copySign(abs(a) + 1f, a);
    }
    public static int floorStrict(float a) {
        if (a < 0) {
            return ceiling(a);
        }
        return floor(a);
    }
    public static int ceilingStrict(float a) {
        if (a < 0) {
            return floor(a);
        }
        return ceiling(a);
    }

    public static long round(double a) {
        double remainder = abs(a % 1);
        if (remainder < 0.5) {
            return (long) a;
        }
        return (long) copySign(abs(a) + 1d, a);
    }
    public static long floor(double a) {
        return (long) a;
    }
    public static long ceiling(double a) {
        return (long) copySign(abs(a) + 1f, a);
    }
    public static long floorStrict(double a) {
        if (a < 0) {
            return ceiling(a);
        }
        return floor(a);
    }
    public static long ceilingStrict(double a) {
        if (a < 0) {
            return floor(a);
        }
        return ceiling(a);
    }

    public static double random() {
        return Math.random();
    }

    public static double lerp(double a, double b, double t) {
        return a + ((b - a) * t);
    }
    public static float lerp(float a, float b, float t) {
        return a + ((b - a) * t);
    }
    public static double cyclicalLerp(double a1, double a2, double t, double lowerEnd, double upperEnd) {
        double range = Math.abs(lowerEnd - upperEnd);
        double a3;
        if (a2 < a1) {
            a3 = a2 + range;
        }
        else {
            a3 = a2 - range;
        }
        double dist12 = Math.abs(a2 - a1);
        double dist13 = Math.abs(a3 - a1);
        if (dist12 < dist13) {
            return lerp(a1, a2, t);
        }
        return lerp(a1, a3, t);
    }
    public static float cyclicalLerp(float a1, float a2, float t, float lowerEnd, float upperEnd) {
        float range = Math.abs(lowerEnd - upperEnd);
        float a3;
        if (a2 < a1) {
            a3 = a2 + range;
        }
        else {
            a3 = a2 - range;
        }
        double dist12 = Math.abs(a2 - a1);
        double dist13 = Math.abs(a3 - a1);
        if (dist12 < dist13) {
            return lerp(a1, a2, t);
        }
        return lerp(a1, a3, t);
    }

    public static int mod(int a, int b) {
        int c = a;
        int div = abs(b);
        while (c <= b) {
            c += div;
        }
        while (c >= b) {
            c -= div;
        }
        return c;
    }
    public static long mod(long a, long b) {
        long c = a;
        long div = abs(b);
        while (c <= b) {
            c += div;
        }
        while (c >= b) {
            c -= div;
        }
        return c;
    }
    public static float mod(float a, float b) {
        float c = a;
        float div = abs(b);
        while (c <= b) {
            c += div;
        }
        while (c >= b) {
            c -= div;
        }
        return c;
    }
    public static double mod(double a, double b) {
        double c = a;
        double div = abs(b);
        while (c <= b) {
            c += div;
        }
        while (c >= b) {
            c -= div;
        }
        return c;
    }

    public static double sqrt(double a) {
        return Math.sqrt(a);
    }

    public static float toRadians(float a) {
        return a * DG_RDf;
    }
    public static float toDegrees(float a) {
        return a * RD_DGf;
    }
    public static double toRadians(double a) {
        return a * DG_RDd;
    }
    public static double toDegrees(double a) {
        return a * RD_DGd;
    }

    public static double hypot(double x, double y) {
        return sqrt(pow(x, 2) + pow(y, 2));
    }

    public static double pow(double base, double exp) {
        return Math.pow(base, exp);
    }

    public static float pow(float base, float exp) {
        return (float) Math.pow(base, exp);
    }

    public static boolean equals(double a, double b, double epsilon) {
        return abs(a - b) < epsilon;
    }

    public static boolean equals(float a, float b, float epsilon) {
        return abs(a - b) < epsilon;
    }

    public static double log10(double a) {
        return Math.log10(a);
    }

    public static float log10(float a) {
        return (float) Math.log10(a);
    }

    public static double ln(double a) {
        return Math.log(a);
    }

    public static float ln(float a) {
        return (float) Math.log(a);
    }
}
