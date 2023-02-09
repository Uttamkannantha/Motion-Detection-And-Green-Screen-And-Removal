package greenscreenremoval;


import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Arrays;
import java.util.TimerTask;
import java.util.Timer;


public class GreenScreenDetection {

    int height = 480;
    int width = 640;
    int frameCount = 480;

    public CustomImage readImageRGB(String imgPath){
        CustomImage image = new CustomImage(height,width); //Initialize the image with given height and width

        int frameLength = width*height*3;
        File file = new File(imgPath);

        try(RandomAccessFile raf = new RandomAccessFile(file, "r")){//Try with resources to catch exception
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);
            int ind = 0;

            //Read the image from height to width
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    //integer value to be used in bufferedImage
                    int rgbPix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);

                    float[] hsv = rgbToHsv(r & 0xff, g & 0xff, b & 0xff);

                    //Creating the new pixel object for every pixel
                    Pixel pixel = new Pixel(r & 0xff, g & 0xff, b & 0xff, rgbPix, hsv[0], hsv[1], hsv[2]);

                    image.pixels[y][x] = pixel;
                    ind++;
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return image;
    }

    float[] rgbToHsv(int r, int g, int b){
        //Convert rgb to hsv
        float h, s, v;

        float min, max, delta;

        min = Math.min(Math.min(r, g), b);
        max = Math.max(Math.max(r, g), b);

        // V
        v = max;

        delta = max - min;

        // S
        if( max != 0 )
            s = delta / max;
        else {
            s = 0;
            h = 0; // -1 to 0
            return new float[]{h,s,v};
        }

        // H
        if( r == max )
            h = ( g - b ) / delta; // between yellow & magenta
        else if( g == max )
            h = 2 + ( b - r ) / delta; // between cyan & yellow
        else
            h = 4 + ( r - g ) / delta; // between magenta & cyan

        h *= 60;    // degrees

        if( h < 0 )
            h += 360;

        return new float[]{h,s,v};

    }

    BufferedImage[] readAndProcessImages(File[] foreground, File[] background){

        BufferedImage[] images = new BufferedImage[frameCount];

        for(int i = 0; i < frameCount; i++){
            CustomImage foregroundImage = readImageRGB(foreground[i].getPath());
            CustomImage backgroundImage = readImageRGB(background[i].getPath());
            images[i] = processImage(foregroundImage, readImageRGB(foreground[i].getPath()), backgroundImage);
        }

        return images;
    }

    BufferedImage processImage(CustomImage foreground, CustomImage temp, CustomImage background){

            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    Pixel pixel = foreground.pixels[y][x];

//                  Detect all variants on green in hsv //.5 60
                    if(pixel.h >= 80 && pixel.h <= 170 && pixel.s >= 0.5  && pixel.v >= 60){
                        foreground.pixels[y][x] = background.pixels[y][x];
                    }

                }
            }

            foreground = processEdgeOfImage(foreground, background);

            //foreground = alphaProcessing(foreground, temp, background);

        return foreground.getBufferedImage();
    }

    CustomImage processEdgeOfImage(CustomImage image, CustomImage background){
        //Clean the green edges of the image
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                Pixel pixel = image.pixels[y][x];

                //Check if the pixel is green
//                if(pixel.h >= 80 && pixel.h <= 170 && pixel.s >= 0.2  && pixel.v >= 30) {
//
//                    //Calculate the average of the surrounding 16 pixels
//                    int sumR = 0;
//                    int sumG = 0;
//                    int sumB = 0;
//                    int count = 0;
//                    for (int i = -3; i < 3; i++) {
//                        for (int j = -3; j < 3; j++) {
//                            if (y + i >= 0 && y + i < height && x + j >= 0 && x + j < width) {
//                                sumR += image.pixels[y + i][x + j].r;
//                                sumG += image.pixels[y + i][x + j].g;
//                                sumB += image.pixels[y + i][x + j].b;
//                                count++;
//                            }
//                        }
//                    }
//                    if(count != 0) {
//                        sumR /= count;
//                        sumG /= count;
//                        sumB /= count;
//
//                        float[] hsv = rgbToHsv(sumR, sumG, sumB);
//                        int rgbPix = 0xff000000 | ((sumR & 0xff) << 16) | ((sumG & 0xff) << 8) | (sumB & 0xff);
//
//
//                        if (!(hsv[0] >= 80 && hsv[0] <= 130 && hsv[1] >= 0.3 && hsv[2] >= 30)) {
//                            image.pixels[y][x] = image.pixels[y][x];
//                        }
//                        else{
//                            image.pixels[y][x] = new Pixel(sumR, sumG, sumB, rgbPix, hsv[0], hsv[1], hsv[2]);
//                        }
//                    }
//                }


//
//
//
//                    float[] hsv = rgbToHsv(sumR/count, sumG/count, sumB/count);
                if(pixel.h >= 80 && pixel.h <= 170 && pixel.s >= 0.2  && pixel.v >= 30) {

                int r = image.pixels[y][x].r;
                int g = image.pixels[y][x].g;
                int b = image.pixels[y][x].b;

                    if((r*b) !=0 && (g*g) / (r*b) > 1.5){
                        r = (int) (r*1.3);
                        b = (int) (b*1.3);
                        g = (int) (g*.8);
                    } else{
                       r = (int) (r*1.3);
                          b = (int) (b*1.3);
                        //g = (int) (g*.8);
                    }
                    int rgbPix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    float[] hsv = rgbToHsv(r & 0xff, g & 0xff, b & 0xff);


                        image.pixels[y][x] =  new Pixel(r & 0xff, g & 0xff, b & 0xff, rgbPix, hsv[0], hsv[1], hsv[2]);
                    }

                }
            }
        return image;
    }




    CustomImage alphaProcessing(CustomImage currentImage, CustomImage foreground, CustomImage background){
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++) {
                //If the pixel is green, calculate the alpha value

                if (currentImage.pixels[y][x].h >= 80 && currentImage.pixels[y][x].h <=170 && currentImage.pixels[y][x].s >= 0.2 && currentImage.pixels[y][x].v >= 30) {

                    //Calculate the average of the non-green surrounding 16 pixels of the foreground
                    int sumR = 0;
                    int sumG = 0;
                    int sumB = 0;
                    int count = 0;
                    for (int i = -3; i < 3; i++) {
                        for (int j = -3; j < 3; j++) {
                            if (y + i >= 0 && y + i < height && x + j >= 0 && x + j < width) {
                                if (!(foreground.pixels[y + i][x + j].h >= 80 && foreground.pixels[y + i][x + j].h <= 170 && foreground.pixels[y + i][x + j].s >= 0.2 && foreground.pixels[y + i][x + j].v >= 30)) {
                                    sumR += foreground.pixels[y + i][x + j].r;
                                    sumG += foreground.pixels[y + i][x + j].g;
                                    sumB += foreground.pixels[y + i][x + j].b;
                                    count++;
                                }
                            }
                        }
                    }
                    if (count == 0 ) {
//                        currentImage.pixels[y][x].typeIntRGB = foreground.pixels[y][x].typeIntRGB;
                      sumR = foreground.pixels[y][x].r;
                        sumG = foreground.pixels[y][x].g;
                        sumB = foreground.pixels[y][x].b;

                    } else {
                        sumR /= count;
                        sumG /= count;
                        sumB /= count;
                    }
                        //rgb value for sumR sumG sumB
                        int foregroundRgb = 0xff000000 | ((sumR & 0xff) << 16) | ((sumG & 0xff) << 8) | (sumB & 0xff);
                        int backgroundRgb = background.pixels[y][x].typeIntRGB;
                        int currentRgb = currentImage.pixels[y][x].typeIntRGB;

                        int alpha = (int) (currentRgb - backgroundRgb) / (foregroundRgb - backgroundRgb);

                        int newRgb = alpha * foregroundRgb + (1 - alpha) * backgroundRgb;

                        currentImage.pixels[y][x].typeIntRGB = newRgb;

                }
            }
        }

        return currentImage;
    }


    BufferedImage[] detectAndChangeBackground(File[] foreground, File[] background){

        BufferedImage[] images = new BufferedImage[frameCount];
        int[][][] hueCount = new int[height][width][360];

        for(int i = 0; i < frameCount; i++){
            CustomImage foregroundImage = readImageRGB(foreground[i].getPath());
            for(int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    Pixel pixel = foregroundImage.pixels[y][x];

                    hueCount[y][x][(int)pixel.h]++;
                }
            }
        }

        int[][] maxHue = new int[height][width];
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int max = 0;
                for(int i = 0; i < 360; i++){
                    if(hueCount[y][x][i] > max){
                        max = hueCount[y][x][i];
                        maxHue[y][x] = i;
                    }
                }
            }
        }

        for(int i = 0; i < frameCount; i++){

            CustomImage foregroundImage = foreGroundDetection(foreground, i, maxHue);

            //CustomImage foregroundImage = foreGroundDetectionWithFrameComparison(foreground, i);

            CustomImage backgroundImage = readImageRGB(background[i].getPath());

            images[i] = changeBackGround(foregroundImage, backgroundImage).getBufferedImage();
        }

        return images;
    }

    public CustomImage changeBackGround(CustomImage foreground, CustomImage background){
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                Pixel pixel = foreground.pixels[y][x];

                if(pixel.changed){
                    foreground.pixels[y][x] = background.pixels[y][x];
                }

            }
        }

        return foreground;
    }

    public CustomImage foreGroundDetectionWithFrameComparison(File[] foregroundList, int index){

        int previousIndex = index - 1;
        if(previousIndex < 0){
            previousIndex = index + 1;
        }
        int nextIndex = index + 2;
        if(nextIndex >= frameCount){
            nextIndex = index - 2;
        }

        CustomImage currentImage = readImageRGB(foregroundList[index].getPath());
        CustomImage previousImage = readImageRGB(foregroundList[previousIndex].getPath());
        CustomImage nextImage = readImageRGB(foregroundList[nextIndex].getPath());

       //Comapre the current image with the previous and next image see if the pixels are different

        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                Pixel currentPixel = currentImage.pixels[y][x];
                Pixel previousPixel = previousImage.pixels[y][x];
                Pixel nextPixel = nextImage.pixels[y][x];

                if(currentPixel.h == previousPixel.h && currentPixel.h == nextPixel.h){
                    currentImage.pixels[y][x].changed = true;
                }
            }
        }


        return currentImage;


    }

    public CustomImage foreGroundDetection(File[] foregroundList, int index, int[][] maxHue){

        CustomImage currentImage = readImageRGB(foregroundList[index].getPath());

        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                Pixel pixel = currentImage.pixels[y][x];

                boolean previousToCurrent = false;

                if(Math.abs(pixel.h - maxHue[y][x]) < 20){
                    previousToCurrent = true;
                }

                if(previousToCurrent){
                    currentImage.pixels[y][x].changed = true;
                }
                else{
                    currentImage.pixels[y][x].changed = false;
                }

            }
        }
        return currentImage;
    }

    void playVideo(BufferedImage[] images){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setVisible(true);

        JLabel label = new JLabel();
        frame.add(label);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int i = 0;
            @Override
            public void run() {
                label.setIcon(new ImageIcon(images[i]));
                i++;
                if(i == frameCount){
                    timer.cancel();
                }
            }
        }, 0, 1000/24);

    }

    public static void main(String[] args){
        GreenScreenDetection greenScreenDetection = new GreenScreenDetection();

        String foregroundPath = args[0];

        String backgroundPath = args[1];

        String mode = args[2];

        File foregroundImageFolderInput = new File(foregroundPath);
        File backgroundImageFolderInput = new File(backgroundPath);
        File[] fimageList = foregroundImageFolderInput.listFiles();
        File[] bimageList = backgroundImageFolderInput.listFiles();

        assert fimageList != null;
        Arrays.sort(fimageList);

        assert bimageList != null;
        Arrays.sort(bimageList);

        if(mode.equals("1")){
            BufferedImage[] result = greenScreenDetection.readAndProcessImages(fimageList, bimageList);
            greenScreenDetection.playVideo(result);
        }
        else{
            BufferedImage[] result = greenScreenDetection.detectAndChangeBackground(fimageList, bimageList);
        greenScreenDetection.playVideo(result);
        }

    }
}
