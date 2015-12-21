import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Map; 
import java.util.Iterator; 
import SimpleOpenNI.*; 
import java.util.Random; 
import java.net.*; 
import java.util.Arrays; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class masterA extends PApplet {






SimpleOpenNI context;
OPC opc;
float dx, dy;

int test = 0;

int numSketches = 4;
int numPixels = 60; //per ring
float angleOfFirst = PI;

//For Spinning Pixels
int[] spinHue;
int backgroundBright = 0;
int backgroundSat = 0;
int backgroundSpinHue = 0;
int numSpinners = 30;
int spinCounter = 0;
int spinSat = 100;
int shapeCounter[];
int randomPixel[];
int speed = 1;
int numInThreshold1 = 0;
int numInThreshold2 = 0;
int numInThreshold3 = 0;
PVector[] spinCircle = new PVector[numSpinners];
shapeClass[][] spinningDots;
PVector[] spinDot = new PVector[numSpinners];
//

//For fading circles
boolean personOnLeft = false;
boolean personOnRight = false;
boolean leftChanged = false;
boolean rightChanged = false;
int numFaders = 30;
shapeClass[][] fadingDots;
PVector[] fadeCircle = new PVector[numFaders];
PVector[] fadeDot = new PVector[numFaders];
int[] fadeHue = new int[numFaders];
int[] fadeSat = new int[numFaders];
int[] faderBrightnesses = new int[numFaders];
boolean[] increaseBrightnesses = new boolean[numFaders];
int initIncBright = 0;
int[] oldHue = new int[numFaders];
int faderBrightness = 1;
boolean increaseBrightness = true;
//

//For depthThreshold
PImage display;
PImage flipped;
int depthHue = 0;
int depthSat = 100;
int depthBri = 100;
int backgroundHue = 50;//

int hue = 0;
int brightness = 0;
boolean backwards = false;
int numShapes = 1;
int counter = 0;
boolean firstPass = true;
Random randomGenerator = new Random();

char direction;
PVector circle = new PVector();
PVector circle2 = new PVector();
shapeClass[] dots;
shapeClass[] dots2;
shapeClass[][][] dotsArray = new shapeClass[8][8][numPixels];
int pixelCounter = 0;
PVector dot = new PVector();
PVector dot2 = new PVector();
float radius;

PVector blank;

public void setup()
{
  context = new SimpleOpenNI(0, this);
  context.enableDepth();

  blank = new PVector();
  blank.x = 0;
  blank.y = 0;

  //Use height for width and height to draw square window
  size(240, 240);

  // Connect to the local instance of fcserver
  opc = new OPC(this, "127.0.0.1", 7890);

  // Map an 8x8 grid of rings of LEDs to the center of the window
  int numPixelsPerRing = numPixels;
  opc.ledRingGrid(width, height, numPixelsPerRing);

  colorMode(HSB, 100);

  background(0, 0, 0);

  ///Fading initialization
  fadingDots = new shapeClass[numFaders][numPixels];

  for (int i=0; i<numFaders; i++)
  {
    fadeCircle[i] = new PVector();
    fadeCircle[i].x = width/16 + (width/8 * (int)random(8));
    fadeCircle[i].y = height/16 + (height/8 * (int)random(8));
    fadeDot[i] = new PVector();   
    fadeHue[i] = (int)random(100);
    oldHue[i] = fadeHue[i];
    fadeSat[i] = 100;
    faderBrightnesses[i] = (int)random(100);
    initIncBright = (int)random(1);
    if (initIncBright == 1)
      increaseBrightnesses[i] = true;
    else
      increaseBrightnesses[i] = false;

    for (int j=0; j<numPixels; j++)
      fadingDots[i][j] = new shapeClass(color(0, 0, 0), blank);
  }
  ///

  ////Spinner initialization
  spinHue = new int[numSpinners];
  shapeCounter = new int[numSpinners];
  randomPixel = new int[numSpinners];
  spinningDots = new shapeClass[numSpinners][numPixels];

  for (int i=0; i<numSpinners; i++)
  {
    spinCircle[i] = new PVector();
    spinCircle[i].x = width/16 + (width/8 * (int)random(8));
    spinCircle[i].y = height/16 + (height/8 * (int)random(8));
    spinDot[i] = new PVector();
    randomPixel[i] = (int)random(numPixels);    
    spinHue[i] = (int)random(100);

    for (int j=0; j<numPixels; j++)
    {
      spinningDots[i][j] = new shapeClass(color(0, 0, 0), blank);
    }
  }
  ////

  opc.setStatusLed(false);

  radius = ((width + height) / 2) / 20;
}

float noiseScale=0.02f;

public float fractalNoise(float x, float y, float z) {
  float r = 0;
  float amp = 1.0f;
  for (int octave = 0; octave < 4; octave++) {
    r += noise(x, y, z) * amp;
    amp /= 2;
    x *= 2;
    y *= 2;
    z *= 2;
  }
  return r;
}

public void draw()
{

  if (hour() >= 5 && hour() < 23)  
  {  
    //Fading Circles
    if (minute() % 15 >= 10)
//    if(test == 1)
    { 
      background(0, 0, 0); 

      context.update();

      for (int x = 0; x < context.depthWidth (); x++) {      
        for (int y = 0; y < context.depthHeight (); y++) {       

          // mirroring image
          int offset = x + y * context.depthWidth();

          int[] depthValues = context.depthMap();
          int rawDepth = depthValues[offset];

          //only get the pixel corresponding to a certain depth
          int depthmin=0;
          int depthmax=3000;
          if (rawDepth < depthmax && rawDepth > depthmin) {       
            if ((x % 640) < 320)
              personOnLeft = true;
            if ((x % 640) > 320)
              personOnRight = true;
          }
        }
      }

      for (int i=0; i<numFaders; i++)
      {
        if (faderBrightnesses[i] >= 100)
          increaseBrightnesses[i] = false;
        else if (faderBrightnesses[i] <= 0)
          increaseBrightnesses[i] = true;

        if (increaseBrightnesses[i] == true)
          faderBrightnesses[i]+=5;
        else if (increaseBrightnesses[i] == false)
          faderBrightnesses[i]-=5;
      }

      for (int i=0; i<numFaders; i++) 
      {
        for (int j=0; j<numPixels; j++)
        {
          float a = angleOfFirst + j * 2 * PI / numPixels; 

          fadeDot[i].x = (int)(fadeCircle[i].x - radius * cos(a) + 0.5f); 
          fadeDot[i].y = (int)(fadeCircle[i].y - radius * sin(a) + 0.5f);

          if (personOnLeft == true)
          {
            if (fadeCircle[i].x < width/2)
            {
              fadeHue[i] = (int)random(100);
              leftChanged = true;
            }
          }
          if (personOnLeft == false && leftChanged == true)
          {
            fadeHue[i] = oldHue[i];

            if (i == numFaders)
              leftChanged = false;
          }

          if (personOnRight == true)
          {
            if (fadeCircle[i].x > width/2)
            {
              fadeHue[i] = (int)random(100);
              rightChanged = true;
            }
          }
          if (personOnRight == false && rightChanged == true && leftChanged == false)
          {
            fadeHue[i] = oldHue[i];

            if (i == numFaders)
              rightChanged = false;
          }

          fadingDots[i][j] = new shapeClass(color(fadeHue[i], fadeSat[i], faderBrightnesses[i]), fadeDot[i]);
          fadingDots[i][j].display();
        }
      }

      for (int i=0; i<numFaders; i++)
      {
        if (faderBrightnesses[i] == 0)
        {
          fadeCircle[i].x = width/16 + (width/8 * (int)random(8));
          fadeCircle[i].y = height/16 + (height/8 * (int)random(8));
          fadeHue[i] = (int)random(100);
          oldHue[i] = fadeHue[i];
          fadeSat[i] = 100;

          for (int k=0; k<numFaders; k++)
          {
            if (k != i)
            {
              while ( (fadeCircle[i].x == fadeCircle[k].x) && (fadeCircle[i].y == fadeCircle[k].y))
              {
                fadeCircle[i].x = width/16 + (width/8 * (int)random(8));
                fadeCircle[i].y = height/16 + (height/8 * (int)random(8));
              }
            }
          }
        }
      }

      personOnLeft = false;
      personOnRight = false;
    }

    //Clouds
    if (minute() % 15 <= 5) {
//      if(test == 1) {
      long now = millis();
      float speed = 0.002f;
      float angle = sin(now * 0.001f);
      float z = now * 0.00008f;
      float hue = now * 0.01f;
      float scale = 0.005f;

      int brightnessScale = 100;

      context.update();

      for (int x = 0; x < context.depthHeight (); x++) {      
        for (int y = 0; y < context.depthHeight (); y++) {       

          // mirroring image
          int offset = context.depthWidth()-x-1+y*context.depthWidth();

          int[] depthValues = context.depthMap();
          int rawDepth = depthValues[offset];

          int pix = x + y * context.depthWidth();
          //only get the pixel corresponding to a certain depth

          int depthmin1 = 0;
          int depthmax1 = 2000; 
          if (rawDepth < depthmax1 && rawDepth > depthmin1)
            speed = 0.005f;

          int depthmin2=2000;
          int depthmax2=3000;
          if (rawDepth < depthmax2 && rawDepth > depthmin2)
            speed = 0.004f;

          int depthmin3 = 3000;
          int depthmax3 = 3500;
          if (rawDepth < depthmax3 && rawDepth > depthmin3)
            speed = 0.003f;
        }
      }

      dx += cos(angle) * speed;
      dy += sin(angle) * speed;

      loadPixels();

      for (int x=0; x < width; x++) {
        for (int y=0; y < height; y++) {

          float n = fractalNoise(dx + x*scale, dy + y*scale, z) - 0.75f;
          float m = fractalNoise(dx + x*scale, dy + y*scale, z + 10.0f) - 0.75f;

          int c = color(
          (hue + 80.0f * m) % 100.0f, 
          100 - 100 * constrain(pow(3.0f * n, 3.5f), 0, 0.9f), 
          brightnessScale * constrain(pow(3.0f * n, 1.5f), 0, 0.9f)
            );

          pixels[x + width*y] = c;
        }
      }
      updatePixels();
    }

    //Xtion Depth Threshold
    if ((minute() % 15) >= 5 && (minute() % 15) < 10)
//    if(test == 0)
    {
      context.update();

      display = context.depthImage();
      display.loadPixels();
      
      flipped = createImage(display.width, display.height, HSB);
      flipped.loadPixels();

      //loop to select a depthzone  
      for (int x = 0; x < context.depthWidth (); x++) {      
        for (int y = 0; y < context.depthHeight (); y++) {       

          // mirroring image
          int offset = context.depthWidth()-x-1+y*context.depthWidth();

          int[] depthValues = context.depthMap();
          int rawDepth = depthValues[offset];

          int pix = x + y * context.depthWidth();
          //only get the pixel corresponding to a certain depth
          int depthmin=0;
          int depthmax=3530;
          if (rawDepth < depthmax && rawDepth > depthmin) {       
            display.pixels[pix] = color(depthHue, depthSat, depthBri); 
            /*          hue+=10;
             bri+=1;           
             if(hue>=100) {
             hue=0;
             }
             if(bri>=100) 
             bri=50;  
             */
          } else
            display.pixels[pix] = color(backgroundHue, 50, 50);
        }
      }
      
      for(int x = 0; x < context.depthWidth(); x++)
      {
        for(int y = 0; y < context.depthHeight(); y++)
        {
          int pix = x + y * context.depthWidth();
          int pixInv = (context.depthWidth() - x - 1) + (context.depthHeight() - y - 1) * context.depthWidth();
          
          flipped.pixels[pixInv] = display.pixels[pix];

        } 
      }

      image(flipped, 0, 0, width*1.333333333f, height);

      depthHue++;
      if (depthHue==100)
        depthHue=0;
        
      backgroundHue++;
      if (backgroundHue==100)
        backgroundHue=0;
    }


/*
    //Spinning Dots
    if ((minute() % 20 >= 15) && (minute() % 20 < 20))
    if(test == 0)
    { 
      background(backgroundSpinHue, backgroundSat, backgroundBright); 

      context.update();

      display = context.depthImage();
      display.loadPixels();

      for (int x = 0; x < context.depthHeight (); x++) {      
        for (int y = 0; y < context.depthHeight (); y++) {       

          // mirroring image
          int offset = context.depthWidth()-x-1+y*context.depthWidth();

          int[] depthValues = context.depthMap();
          int rawDepth = depthValues[offset];

          int pix = x + y * context.depthWidth();
          //only get the pixel corresponding to a certain depth
          int depthmin2=2000;
          int depthmax2=3000;
          if (rawDepth < depthmax2 && rawDepth > depthmin2) {       
            //        display.pixels[pix] = color(depthHue%100, depthSat, depthBri);
            numInThreshold2++;
          } else
            display.pixels[pix] = color(0);

          int depthmin1 = 0;
          int depthmax1 = 2000; 
          if (rawDepth < depthmax1 && rawDepth > depthmin1)
            numInThreshold1++;

          int depthmin3 = 3000;
          int depthmax3 = 3500;
          if (rawDepth < depthmax3 && rawDepth > depthmin3)
            numInThreshold3++;
        }
      }
      //  display.updatePixels();

      //    image(display, 0, 0, width*1.333333, height);

      depthHue++;

      if (numInThreshold1 < 10000 && numInThreshold2 < 10000 && numInThreshold3 < 35000)
        speed=1;
      if (numInThreshold3 > 35000 && numInThreshold3 > numInThreshold2 && numInThreshold3 > numInThreshold1)
        speed=2;
      if (numInThreshold2 > 10000 && numInThreshold2 > numInThreshold1 && numInThreshold2 > numInThreshold3)
        speed=3;
      if (numInThreshold1 > 10000 && numInThreshold1 > numInThreshold2 && numInThreshold1 > numInThreshold2)
        speed=4;

      //assign idividual dot positions from circle center point position
      for (int i=0; i<numSpinners; i++)
      {
        shapeCounter[i]++;
        float a = angleOfFirst + ((spinCounter + randomPixel[i]) % numPixels) * 2 * PI / numPixels * speed; 
        spinDot[i].x = (int)(spinCircle[i].x - radius * cos(a) + 0.5); 
        spinDot[i].y = (int)(spinCircle[i].y - radius * sin(a) + 0.5);
      }

      //create the dots from positions
      for (int i=0; i<numSpinners; i++)
        spinningDots[i][spinCounter%numPixels] = new shapeClass(color(spinHue[i], spinSat, 100), spinDot[i]); 

      //display all dots
      for (int i=0; i<numSpinners; i++)
      {
        for (int j=0; j<numPixels; j++)
          spinningDots[i][j].display();
      }

      if (spinCounter%5==0) {
        for (int i=0; i<numSpinners; i++) {
          spinHue[i]++;
          if (spinHue[i] == 100)
            spinHue[i] = 0;
        }
      }

      spinCounter++;
      if (spinCounter == numPixels*4)
        spinCounter = 0;

      for (int i=0; i<numSpinners; i++)
      {
        if (shapeCounter[i]%5 == (int)random(5) && spinCounter%numPixels == 0) 
        {  
          spinCircle[i].x = width/16 + (width/8 * (int)random(8));
          spinCircle[i].y = height/16 + (height/8 * (int)random(8));

          randomPixel[i] = (int)random(numPixels);

          //check to make sure dots aren't on top of each other
          for (int k=0; k<numSpinners; k++)
          {
            if (k != i)
            {
              while ( (spinCircle[i].x == spinCircle[k].x) && (spinCircle[i].y == spinCircle[k].y))
              {
                spinCircle[i].x = width/16 + (width/8 * (int)random(8));
                spinCircle[i].y = height/16 + (height/8 * (int)random(8));
              }
            }
          }

          for (int j=0; j<numPixels; j++)
          {
            float a = angleOfFirst + j * 2 * PI / numPixels; 
            spinDot[i].x = (int)(spinCircle[i].x - radius * cos(a) + 0.5); 
            spinDot[i].y = (int)(spinCircle[i].y - radius * sin(a) + 0.5);
          }
        }
      }

      numInThreshold1 = 0;
      numInThreshold2 = 0;
      numInThreshold3 = 0;
    }
*/
    
  } else
    background(0, 0, 0);
}

class shapeClass 
{ 
  int colour; 
  PVector pos; 

  shapeClass(int colour, PVector pos) 
  { 
    this.colour = colour; 
    this.pos = pos;
  } 

  public void display() 
  { 
    smooth(); 
    fill(colour); 
    strokeWeight(0);
    ellipseMode(CENTER); 
    //    ellipse(pos.x - 40, pos.y - 40, 50, 50); 

    //    println("shape.x: " + pos.x);
    //    println("shape.y: " + pos.y);

    ellipse(pos.x, pos.y, 3, 3);
  }
} 

/*
 * Simple Open Pixel Control client for Processing,
 * designed to sample each LED's color from some point on the canvas.
 *
 * Micah Elizabeth Scott, 2013
 * This file is released into the public domain.
 */




public class OPC
{
  Socket socket;
  OutputStream output;
  String host;
  int port;

  int[] pixelLocations;
  byte[] packetData;
  byte firmwareConfig;
  String colorCorrection;
  boolean enableShowLocations;

  OPC(PApplet parent, String host, int port)
  {
    this.host = host;
    this.port = port;
    this.enableShowLocations = true;
    parent.registerDraw(this);
  }

  // Set the location of a single LED
  public void led(int index, int x, int y)  
  {
    // For convenience, automatically grow the pixelLocations array. We do want this to be an array,
    // instead of a HashMap, to keep draw() as fast as it can be.
    if (pixelLocations == null) {
      pixelLocations = new int[index + 1];
    } else if (index >= pixelLocations.length) {
      pixelLocations = Arrays.copyOf(pixelLocations, index + 1);
    }

    pixelLocations[index] = x + width * y;
  }
  

  // Set the locations of a ring of LEDs. The center of the ring is at (x, y),
  // with "radius" pixels between the center and each LED. The first LED is at
  // the indicated angle, in radians, measured clockwise from +X.
  public void ledRing(int index, int count, float x, float y, float radius, float angle, boolean inverted)
  {
    float pi = PI;
    if(inverted == true)
      pi *= -1;
       
    for (int i = 0; i < count; i++) {
      float a = angle + i * 2 * pi / count;
      led(index + i, (int)(x - radius * cos(a) + 0.5f),
        (int)(y - radius * sin(a) + 0.5f));
    }
  }
  
  
  //Draws an 8x8 grid of pixel rings.
  //Grid is drawn column by column, starting at the bottom left, 
  //and is relative to screen size.
  public void ledRingGrid(int screenWidth, int screenHeight, int numPixels)
  {    
      int xPosition = screenWidth / 16;   
      int index = 0;
      float radius = ((screenWidth + screenHeight) / 2) / 20;
      float angleOfFirstPixel = PI;
      boolean inverted = false;
      
      if(numPixels == 16)
        inverted = true;
      
      for(int x = 0; x < 8; x++)
      {
        int yPosition = screenHeight - (screenHeight / 16);
         for(int y = 0; y < 8; y++)
         { 
            ledRing(index, numPixels, xPosition, yPosition, radius, angleOfFirstPixel, inverted);
            yPosition -= screenHeight / 8;  
            index += numPixels;
         }
         
         xPosition += screenHeight / 8;
      }
  }  

  // Should the pixel sampling locations be visible? This helps with debugging.
  // Showing locations is enabled by default. You might need to disable it if our drawing
  // is interfering with your processing sketch, or if you'd simply like the screen to be
  // less cluttered.
  public void showLocations(boolean enabled)
  {
    enableShowLocations = enabled;
  }
  
  // Enable or disable dithering. Dithering avoids the "stair-stepping" artifact and increases color
  // resolution by quickly jittering between adjacent 8-bit brightness levels about 400 times a second.
  // Dithering is on by default.
  public void setDithering(boolean enabled)
  {
    if (enabled)
      firmwareConfig &= ~0x01;
    else
      firmwareConfig |= 0x01;
    sendFirmwareConfigPacket();
  }

  // Enable or disable frame interpolation. Interpolation automatically blends between consecutive frames
  // in hardware, and it does so with 16-bit per channel resolution. Combined with dithering, this helps make
  // fades very smooth. Interpolation is on by default.
  public void setInterpolation(boolean enabled)
  {
    if (enabled)
      firmwareConfig &= ~0x02;
    else
      firmwareConfig |= 0x02;
    sendFirmwareConfigPacket();
  }

  // Put the Fadecandy onboard LED under automatic control. It blinks any time the firmware processes a packet.
  // This is the default configuration for the LED.
  public void statusLedAuto()
  {
    firmwareConfig &= 0x0C;
    sendFirmwareConfigPacket();
  }    

  // Manually turn the Fadecandy onboard LED on or off. This disables automatic LED control.
  public void setStatusLed(boolean on)
  {
    firmwareConfig |= 0x04;   // Manual LED control
    if (on)
      firmwareConfig |= 0x08;
    else
      firmwareConfig &= ~0x08;
    sendFirmwareConfigPacket();
  } 

  // Set the color correction parameters
  public void setColorCorrection(float gamma, float red, float green, float blue)
  {
    colorCorrection = "{ \"gamma\": " + gamma + ", \"whitepoint\": [" + red + "," + green + "," + blue + "]}";
    sendColorCorrectionPacket();
  }
  
  // Set custom color correction parameters from a string
  public void setColorCorrection(String s)
  {
    colorCorrection = s;
    sendColorCorrectionPacket();
  }

  // Send a packet with the current firmware configuration settings
  public void sendFirmwareConfigPacket()
  {
    if (output == null) {
      // We'll do this when we reconnect
      return;
    }
 
    byte[] packet = new byte[9];
    packet[0] = 0;          // Channel (reserved)
    packet[1] = (byte)0xFF; // Command (System Exclusive)
    packet[2] = 0;          // Length high byte
    packet[3] = 5;          // Length low byte
    packet[4] = 0x00;       // System ID high byte
    packet[5] = 0x01;       // System ID low byte
    packet[6] = 0x00;       // Command ID high byte
    packet[7] = 0x02;       // Command ID low byte
    packet[8] = firmwareConfig;

    try {
      output.write(packet);
    } catch (Exception e) {
      dispose();
    }
  }

  // Send a packet with the current color correction settings
  public void sendColorCorrectionPacket()
  {
    if (colorCorrection == null) {
      // No color correction defined
      return;
    }
    if (output == null) {
      // We'll do this when we reconnect
      return;
    }

    byte[] content = colorCorrection.getBytes();
    int packetLen = content.length + 4;
    byte[] header = new byte[8];
    header[0] = 0;          // Channel (reserved)
    header[1] = (byte)0xFF; // Command (System Exclusive)
    header[2] = (byte)(packetLen >> 8);
    header[3] = (byte)(packetLen & 0xFF);
    header[4] = 0x00;       // System ID high byte
    header[5] = 0x01;       // System ID low byte
    header[6] = 0x00;       // Command ID high byte
    header[7] = 0x01;       // Command ID low byte

    try {
      output.write(header);
      output.write(content);
    } catch (Exception e) {
      dispose();
    }
  }

  // Automatically called at the end of each draw().
  // This handles the automatic Pixel to LED mapping.
  // If you aren't using that mapping, this function has no effect.
  // In that case, you can call setPixelCount(), setPixel(), and writePixels()
  // separately.
  public void draw()
  {
    if (pixelLocations == null) {
      // No pixels defined yet
      return;
    }
 
    if (output == null) {
      // Try to (re)connect
      connect();
    }
    if (output == null) {
      return;
    }

    int numPixels = pixelLocations.length;
    int ledAddress = 4;

    setPixelCount(numPixels);
    loadPixels();

    for (int i = 0; i < numPixels; i++) {
      int pixelLocation = pixelLocations[i];
      int pixel = pixels[pixelLocation];

      packetData[ledAddress] = (byte)(pixel >> 16);
      packetData[ledAddress + 1] = (byte)(pixel >> 8);
      packetData[ledAddress + 2] = (byte)pixel;
      ledAddress += 3;

      if (enableShowLocations) {
        pixels[pixelLocation] = 0xFFFFFF ^ pixel;
      }
    }

    writePixels();

    if (enableShowLocations) {
      updatePixels();
    }
  }
  
  // Change the number of pixels in our output packet.
  // This is normally not needed; the output packet is automatically sized
  // by draw() and by setPixel().
  public void setPixelCount(int numPixels)
  {
    int numBytes = 3 * numPixels;
    int packetLen = 4 + numBytes;
    if (packetData == null || packetData.length != packetLen) {
      // Set up our packet buffer
      packetData = new byte[packetLen];
      packetData[0] = 0;  // Channel
      packetData[1] = 0;  // Command (Set pixel colors)
      packetData[2] = (byte)(numBytes >> 8);
      packetData[3] = (byte)(numBytes & 0xFF);
    }
  }
  
  // Directly manipulate a pixel in the output buffer. This isn't needed
  // for pixels that are mapped to the screen.
  public void setPixel(int number, int c)
  {
    int offset = 4 + number * 3;
    if (packetData == null || packetData.length < offset + 3) {
      setPixelCount(number + 1);
    }

    packetData[offset] = (byte) (c >> 16);
    packetData[offset + 1] = (byte) (c >> 8);
    packetData[offset + 2] = (byte) c;
  }
  
  // Read a pixel from the output buffer. If the pixel was mapped to the display,
  // this returns the value we captured on the previous frame.
  public int getPixel(int number)
  {
    int offset = 4 + number * 3;
    if (packetData == null || packetData.length < offset + 3) {
      return 0;
    }
    return (packetData[offset] << 16) | (packetData[offset + 1] << 8) | packetData[offset + 2];
  }

  // Transmit our current buffer of pixel values to the OPC server. This is handled
  // automatically in draw() if any pixels are mapped to the screen, but if you haven't
  // mapped any pixels to the screen you'll want to call this directly.
  public void writePixels()
  {
    if (packetData == null || packetData.length == 0) {
      // No pixel buffer
      return;
    }
    if (output == null) {
      // Try to (re)connect
      connect();
    }
    if (output == null) {
      return;
    }

    try {
      output.write(packetData);
    } catch (Exception e) {
      dispose();
    }
  }

  public void dispose()
  {
    // Destroy the socket. Called internally when we've disconnected.
    if (output != null) {
      println("Disconnected from OPC server");
    }
    socket = null;
    output = null;
  }

  public void connect()
  {
    // Try to connect to the OPC server. This normally happens automatically in draw()
    try {
      socket = new Socket(host, port);
      socket.setTcpNoDelay(true);
      output = socket.getOutputStream();
      println("Connected to OPC server");
    } catch (ConnectException e) {
      dispose();
    } catch (IOException e) {
      dispose();
    }
    
    sendColorCorrectionPacket();
    sendFirmwareConfigPacket();
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "masterA" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
