package blacksmith.sullivanway.data;

import blacksmith.sullivanway.R;

// 호선정보 추상 클래스
public abstract class LineInfo {
    public static String[] lineNm = {
            "1호선", "2호선", "3호선", "4호선",
            "5호선", "6호선", "7호선", "8호선",
            "9호선", "경강선", "경의중앙선", "경춘선",
            "분당선", "수인선", "신분당선", "에버라인",
            "의정부경전철", "인천1호선", "인천2호선", "인천국제공항철도",
            "자기부상철도", "우이신설선" };
    private static String[] lineSymbol = {
            "1", "2", "3", "4",
            "5", "6", "7", "8",
            "9", "경강", "경의\n중앙", "경춘",
            "분당", "수인", "신분당", "에버",
            "의정부\n경전철", "인천1", "인천2", "공항\n철도",
            "자기\n부상", "우이\n신설"};
    private static String[] lineColor = {
            "#00498B", "#009246", "#F36630", "#00A2D1",
            "#5940FF", "#CC660D", "#4D8000", "#FF33A6",
            "#8E764B", "#0054A6", "#72C7A6", "#2ABFD0",
            "#E0A134", "#E0A134", "#BB1833", "#509F22",
            "#fda600", "#6E98BB", "#ED8B00", "#006D9D",
            "#D79C49", "#B0CE18" };
    private static int[] lineResId = {
            R.drawable.line_first, R.drawable.line_second, R.drawable.line_third, R.drawable.line_fourth,
            R.drawable.line_fifth, R.drawable.line_sixth, R.drawable.line_seventh, R.drawable.line_eighth,
            R.drawable.line_nineth, R.drawable.line_kk, R.drawable.line_ku, R.drawable.line_kc,
            R.drawable.line_b, R.drawable.line_su, R.drawable.line_sb, R.drawable.line_e,
            R.drawable.line_uj, R.drawable.line_i1, R.drawable.line_i2, R.drawable.line_a,
            R.drawable.line_m, R.drawable.line_ui };
    private static int[] lineBgResId = {
            R.drawable.bg_line_first, R.drawable.bg_line_second, R.drawable.bg_line_third, R.drawable.bg_line_fourth,
            R.drawable.bg_line_fifth, R.drawable.bg_line_sixth, R.drawable.bg_line_seventh, R.drawable.bg_line_eighth,
            R.drawable.bg_line_nineth, R.drawable.bg_line_kk, R.drawable.bg_line_ku, R.drawable.bg_line_kc,
            R.drawable.bg_line_b, R.drawable.bg_line_su, R.drawable.bg_line_sb, R.drawable.bg_line_e,
            R.drawable.bg_line_uj, R.drawable.bg_line_i1, R.drawable.bg_line_i2, R.drawable.bg_line_a,
            R.drawable.bg_line_m, R.drawable.bg_line_ui };

    public static String getLineSymbol(String mLineNm) {
        for (int i = 0; i < lineNm.length; i++)
            if (mLineNm.equals(lineNm[i]))
                return lineSymbol[i];
        return null;
    }

    public static int getResId(String mLineNm) {
        for (int i = 0; i < lineNm.length; i++)
            if (mLineNm.equals(lineNm[i]))
                return lineResId[i];
        return -1;
    }

    public static String getLineColor(String mLineSymbol) {
        for (int i = 0; i < lineSymbol.length; i++) {
            if (mLineSymbol.equals(lineSymbol[i]))
                return lineColor[i];
        }
        return null;
    }

    public static int getBgResId(String mLineNm) {
        for (int i = 0; i < lineNm.length; i++)
            if (mLineNm.equals(lineNm[i]))
                return lineBgResId[i];
        return -1;
    }

    public static String getLineNm(String mLineSymbol) {
        for (int i = 0; i < lineNm.length; i++)
            if (mLineSymbol.equals(lineSymbol[i]))
                return lineNm[i];
        return null;
    }

}

/*
 * 1      #00498B
 * 2      #009246
 * 3      #F36630
 * 4      #00A2D1
 * 5      #5940FF
 * 6      #CC660D
 * 7      #4D8000
 * 8      #FF33A6
 * 9      #8E764B
 * 10     A (인천국제공항철도) #006D9D
 * 11     M (자기부상철도)     #D79C49 (공식적으로 정해지지 않음. 임의의 주황색)
 * 12     B (분당선)           #E0A134
 * 13     E (에버라인)         #509F22
 * 14     G (경춘선)           #2ABFD0
 * 15     I (인천 1호선)       #6E98BB
 * 16     I2 (인천 2호선)      #ED8B00
 * 17     K (경의중앙선)       #72C7A6
 * 18     KK (경강선)          #0054A6
 * 19     S (신분당선)         #BB1833
 * 20     SU (수인선)          #E0A134
 * 21     U (의정부경전철)     #fda600
 * 22     UI (우이신설)        #B0CE18
 * 출처: https://ko.wikipedia.org/wiki/틀:한국_철도_노선색
 */
