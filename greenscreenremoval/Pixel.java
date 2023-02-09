package greenscreenremoval;

/**
 * Class to hold all values related to pixel
 */
public class Pixel {
    int r;
    int g;
    int b;
    int typeIntRGB;

    float h;
    float s;
    float v;
    boolean isGreen;
    boolean changed;

    public Pixel() {
    }

    /**
     * Constructor to assign the values to class variables
     * @param r red
     * @param g green
     * @param b blue
     * @param typeIntRGB Integer value to represent rgb values
//     * @param h hue
//     * @param s saturation
//     * @param v value
     */
    public Pixel(int r, int g, int b, int typeIntRGB ,float h, float s, float v) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.h = h;
        this.s = s;
        this.v = v;
        this.typeIntRGB = typeIntRGB;
    }
}
