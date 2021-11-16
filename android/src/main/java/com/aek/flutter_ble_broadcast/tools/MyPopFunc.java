package com.aek.flutter_ble_broadcast.tools;

import android.content.res.Resources;

import androidx.core.view.GravityCompat;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.ViewCompat;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MyPopFunc {
    private static final String TAG = "MyPopF";

    public static String getTimeStampStringLong() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
    }

    public static String getTimeStampString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }

    public static String getDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    public static String getDateStringForFile() {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss_SSS", Locale.US).format(new Date());
    }

    public static String getTimeStampStringLong(long dTic) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Long.valueOf(dTic));
    }

    public static String getTimeStampString(long dTic) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Long.valueOf(dTic));
    }

    public static String getDateString(long dTic) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Long.valueOf(dTic));
    }

    public static String byteToBinaryString(int bd) {
        String bin_str = "";
        int mask_bit = 128;
        for (int i = 0; i < 8; i++) {
            if ((bd & mask_bit) > 0) {
                bin_str = String.valueOf(bin_str) + " 1";
            } else {
                bin_str = String.valueOf(bin_str) + " 0";
            }
            mask_bit >>>= 1;
        }
        return String.valueOf(bin_str) + " b";
    }

    public static String byteToHexString(byte bd) {
        byte nib;
        byte[] hex_char = new byte[2];
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                nib = (byte) ((bd & 240) >>> 4);
            } else {
                nib = (byte) (bd & 15);
            }
            if (nib < 10) {
                hex_char[i] = (byte) (nib | 48);
            } else {
                hex_char[i] = (byte) (((byte) (nib - 9)) | 64);
            }
        }
        return new String(hex_char);
    }

    public static String byteArrayToHexString(byte[] ba) {
        if (ba == null) {
            return null;
        }
        String hex_bytes = "";
        for (int i = 0; i < ba.length; i++) {
            hex_bytes = String.valueOf(hex_bytes) + byteToHexString(ba[i]) + " ";
        }
        return hex_bytes;
    }

    public static String byteArrayToHexValueString(byte[] ba, int offset, int size_b) {
        if (ba == null || offset < 0 || offset >= ba.length) {
            return "";
        }
        int max_i = offset + size_b;
        if (size_b == 0) {
            max_i = ba.length;
        }
        if (max_i > ba.length) {
            max_i = ba.length;
        }
        String hex_str = "0x";
        for (int i = offset; i < max_i; i++) {
            hex_str = String.valueOf(hex_str) + byteToHexString(ba[i]);
        }
        return hex_str;
    }

    public static byte[] getByteArrayFromHexString(String hStr, boolean strict) {
        int j;
        if (hStr == null) {
            return null;
        }
        int hStr_size = hStr.length();
        int b_size = hStr_size;
        byte[] r_bytes = new byte[b_size];
        int i = 0;
        int j2 = 0;
        while (true) {
            if (i >= hStr_size) {
                j = j2;
                break;
            } else if (hStr.charAt(i) == ' ') {
                i++;
            } else {
                int f = Character.digit(hStr.charAt(i), 16);
                if (f != -1 || !strict) {
                    int f2 = f & 15;
                    i++;
                    if (i < hStr_size) {
                        if (hStr.charAt(i) != ' ') {
                            int fl = Character.digit(hStr.charAt(i), 16);
                            if (fl == -1 && strict) {
                                return null;
                            }
                            f2 = (f2 << 4) | (fl & 15);
                        }
                        i++;
                    }
                    j = j2 + 1;
                    r_bytes[j2] = (byte) (f2 & 255);
                    if (j >= b_size) {
                        break;
                    }
                    j2 = j;
                } else {
                    return null;
                }
            }
        }
        return Arrays.copyOf(r_bytes, j);
    }

    public static byte[] reverseByteArray(byte[] org_bd, int d_offset, int d_size) {
        int from_idx;
        int from_idx2 = d_offset;
        int max_idx = org_bd.length - 1;
        int a_size = d_size;
        if (a_size == 0) {
            a_size = org_bd.length;
        }
        byte[] r_bytes = new byte[a_size];
        if (from_idx2 >= 0) {
            int i = a_size - 1;
            int from_idx3 = from_idx2;
            while (i >= 0) {
                if (from_idx3 > max_idx) {
                    r_bytes[i] = 0;
                    from_idx = from_idx3;
                } else {
                    from_idx = from_idx3 + 1;
                    r_bytes[i] = org_bd[from_idx3];
                }
                i--;
                from_idx3 = from_idx;
            }
            int i2 = from_idx3;
        }
        return r_bytes;
    }

    public static String getHexStringFromInt(int d_in, int num_char) {
        StringBuilder s_int = new StringBuilder(num_char);
        for (int i = num_char - 1; i >= 0; i--) {
            int tmp_int = ((d_in >> (i * 4)) & 15) + 48;
            if (tmp_int > 57) {
                tmp_int += 7;
            }
            s_int.append((char) tmp_int);
        }
        return s_int.toString();
    }

    public static long[] stringToLongArray(String s_in) {
        String[] s_array = s_in.split(" ");
        long[] r_long = new long[s_array.length];
        for (int i = 0; i < s_array.length; i++) {
            try {
                r_long[i] = Long.parseLong(s_array[i]);
            } catch (Exception e) {
                r_long[i] = 0;
            }
        }
        return r_long;
    }

    public static double[] stringToDoubleArray(String s_in) {
        String[] s_array = s_in.split(" ");
        double[] r_double = new double[s_array.length];
        for (int i = 0; i < s_array.length; i++) {
            try {
                r_double[i] = Double.parseDouble(s_array[i]);
            } catch (Exception e) {
                r_double[i] = 0.0d;
            }
        }
        return r_double;
    }

    public static void getMantiExpntFromDouble16(double dbl_in) {
        double pow = dbl_in / Math.pow(10.0d, Math.floor(Math.log10(Math.abs(dbl_in))));
    }

    public static class IntMantiExpnt10 {
        private double d_expnt;
        private double d_manti;
        public double d_val;
        public int exp_man;
        public int expnt;
        public int manti;
        private final int nE_Min16 = -8;
        private final int nE_Min32 = -128;
        private final int nM_INF16 = -2046;
        private final int nM_INF32 = -8388606;
        private final int nM_Min16 = -2048;
        private final int nM_Min32 = -8388608;
        private final int pE_Max16 = 7;
        private final int pM_INF16 = 2046;
        private final int pM_INF32 = 8388606;
        private final int pM_Max16 = 2047;
        private final int pM_Max32 = 8388607;
        public int size;

        public IntMantiExpnt10(int mnt, int epnt, int b_size) {
            this.size = b_size;
            this.manti = mnt;
            this.expnt = epnt;
            this.d_val = ((double) this.manti) * Math.pow(10.0d, (double) this.expnt);
            if (b_size == 16) {
                this.exp_man = this.expnt;
                this.exp_man &= 15;
                this.exp_man <<= 12;
                this.exp_man |= this.manti & 4095;
            } else if (b_size == 32) {
                this.exp_man = this.expnt;
                this.exp_man &= 255;
                this.exp_man <<= 24;
                this.exp_man |= this.manti & ViewCompat.MEASURED_SIZE_MASK;
            } else {
                setAll0();
            }
        }

        public IntMantiExpnt10(int e_m, int b_size) {
            this.size = b_size;
            this.exp_man = e_m;
            if (b_size == 16) {
                this.manti = e_m & 4095;
                if ((this.manti & 2048) > 0) {
                    this.manti |= -4096;
                }
                this.expnt = (e_m >> 12) & 15;
                if ((this.expnt & 8) > 0) {
                    this.expnt |= -16;
                }
                this.d_val = ((double) this.manti) * Math.pow(10.0d, (double) this.expnt);
            } else if (b_size == 32) {
                this.manti = 16777215 & e_m;
                if ((this.manti & GravityCompat.RELATIVE_LAYOUT_DIRECTION) > 0) {
                    this.manti |= -16777216;
                }
                this.expnt = (e_m >> 24) & 255;
                if ((this.expnt & 128) > 0) {
                    this.expnt |= InputDeviceCompat.SOURCE_ANY;
                }
                this.d_val = ((double) this.manti) * Math.pow(10.0d, (double) this.expnt);
            } else {
                setAll0();
            }
        }

        public IntMantiExpnt10(double dbl_in, int b_size) {
            this.d_val = dbl_in;
            this.size = b_size;
            if (dbl_in == 0.0d) {
                this.manti = 0;
                this.expnt = 0;
                this.exp_man = 0;
                return;
            }
            this.d_expnt = Math.floor(Math.log10(Math.abs(dbl_in)));
            this.d_manti = dbl_in / Math.pow(10.0d, this.d_expnt);
            if (b_size == 16) {
                make16();
            } else if (b_size == 32) {
                make32();
            } else {
                setAll0();
            }
        }

        private void make16() {
            if (this.d_manti * 1000.0d > 2047.0d || this.d_manti * 1000.0d < -2048.0d) {
                this.d_manti *= 100.0d;
                this.d_expnt -= 2.0d;
            } else {
                this.d_manti *= 1000.0d;
                this.d_expnt -= 3.0d;
            }
            if (this.d_expnt > 7.0d) {
                this.manti = this.d_manti > 0.0d ? 2046 : -2046;
                this.expnt = 0;
            } else if (this.d_expnt < -8.0d) {
                while (true) {
                    this.d_manti /= 10.0d;
                    this.d_expnt += 1.0d;
                    if (((int) this.d_manti) != 0) {
                        if (this.d_expnt >= -8.0d) {
                            this.manti = (int) Math.round(this.d_manti);
                            this.expnt = (int) Math.round(this.d_expnt);
                            break;
                        }
                    } else {
                        this.manti = 0;
                        this.expnt = 0;
                        break;
                    }
                }
            } else {
                this.manti = (int) Math.round(this.d_manti);
                this.expnt = (int) Math.round(this.d_expnt);
                while (this.manti % 10 == 0 && this.expnt < 7) {
                    this.manti /= 10;
                    this.expnt++;
                }
            }
            this.exp_man = this.expnt;
            this.exp_man &= 15;
            this.exp_man <<= 12;
            this.exp_man |= this.manti & 4095;
        }

        private void make32() {
            if (this.d_manti * 1000000.0d > 8388607.0d || this.d_manti * 1000000.0d < -8388608.0d) {
                this.d_manti *= 100000.0d;
                this.d_expnt -= 5.0d;
            } else {
                this.d_manti *= 1000000.0d;
                this.d_expnt -= 6.0d;
            }
            if (this.d_expnt > 127.0d) {
                this.manti = this.d_manti > 0.0d ? 8388606 : -8388606;
                this.expnt = 0;
            } else if (this.d_expnt < -128.0d) {
                while (true) {
                    this.d_manti /= 10.0d;
                    this.d_expnt += 1.0d;
                    if (((int) this.d_manti) != 0) {
                        if (this.d_expnt >= -128.0d) {
                            this.manti = (int) Math.round(this.d_manti);
                            this.expnt = (int) Math.round(this.d_expnt);
                            break;
                        }
                    } else {
                        this.manti = 0;
                        this.expnt = 0;
                        break;
                    }
                }
            } else {
                this.manti = (int) Math.round(this.d_manti);
                this.expnt = (int) Math.round(this.d_expnt);
                while (this.manti % 10 == 0 && this.expnt < 127) {
                    this.manti /= 10;
                    this.expnt++;
                }
            }
            this.exp_man = this.expnt;
            this.exp_man &= 255;
            this.exp_man <<= 24;
            this.exp_man |= this.manti & ViewCompat.MEASURED_SIZE_MASK;
        }

        private void setAll0() {
            this.manti = 0;
            this.expnt = 0;
            this.d_val = 0.0d;
            this.size = 0;
            this.exp_man = 0;
        }
    }

    public static long getNumFromLEBytes(byte[] a_data, int b_pos, int by_cnt, int sh_cnt, long r_val, boolean s_int) {
        if (b_pos >= a_data.length || by_cnt <= 0) {
            return r_val;
        }
        long r_val2 = getNumFromLEBytes(a_data, b_pos + 1, by_cnt - 1, sh_cnt + 1, r_val, s_int);
        if (by_cnt != 1 || !s_int) {
            return r_val2 | (((long) (a_data[b_pos] & 255)) << (sh_cnt * 8));
        }
        return r_val2 | (((long) a_data[b_pos]) << (sh_cnt * 8));
    }

    public static byte[] getLEByteArrayFromLongArray(long[] l_arr, int bytes, boolean b_unsigned) {
        byte[] le_bytes = new byte[(l_arr.length * bytes)];
        for (int ai = 0; ai < l_arr.length; ai++) {
            if (b_unsigned && l_arr[ai] < 0) {
                l_arr[ai] = 0;
            }
            for (int bi = 0; bi < bytes; bi++) {
                le_bytes[(ai * bytes) + bi] = (byte) ((int) ((l_arr[ai] >> (bi * 8)) & 255));
            }
        }
        return le_bytes;
    }

    public static String makeRandomString(int s_size) {
        int str_size = s_size;
        Random ran_gen = new Random();
        if (s_size == 0) {
            str_size = ran_gen.nextInt(32);
        }
        if (str_size < 10) {
            str_size = 10;
        }
        StringBuilder ranStrBuilder = new StringBuilder();
        for (int i = 0; i < str_size; i++) {
            ranStrBuilder.append((char) (ran_gen.nextInt(96) + 32));
        }
        return ranStrBuilder.toString();
    }

    public static String getRandomString(int sizeOfRandomString) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; i++) {
            sb.append("OPQRST4567defghi89abcjkVWXYZlmnxyzFGHIopq0123rstuvwABCDEJKLMNU".charAt(random.nextInt("OPQRST4567defghi89abcjkVWXYZlmnxyzFGHIopq0123rstuvwABCDEJKLMNU".length())));
        }
        return sb.toString();
    }

    public static int convertDpToPixel(float dp) {
        return (int) (dp * (((float) Resources.getSystem().getDisplayMetrics().densityDpi) / 160.0f));
    }

    public static int convertPixelsToDp(float px) {
        return (int) (px / (((float) Resources.getSystem().getDisplayMetrics().densityDpi) / 160.0f));
    }

    public static int getIndexFromArray(int[] ar_i, int key_i) {
        int a_size = ar_i.length;
        if (a_size == 0) {
            return -1;
        }
        for (int i = 0; i < a_size; i++) {
            if (ar_i[i] == key_i) {
                return i;
            }
        }
        return -1;
    }

    public static void testFunc(int t_int) {
    }
}
