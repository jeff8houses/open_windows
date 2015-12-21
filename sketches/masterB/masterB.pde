import java.util.Map;
import java.util.Iterator;
import SimpleOpenNI.*;
import java.util.Random;

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

void setup()
{
  context = new SimpleOpenNI(1, this);
  context.enableDepth();

  blank = new PVector();
  blank.x = 0;
  blank.y = 0;

  //Use height for width and height to draw square window
  size(240, 240);

  // Connect to the local instance of fcserver
  opc = new OPC(this, "127.0.0.1", 7891);

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

float noiseScale=0.02;

float fractalNoise(float x, float y, float z) {
  float r = 0;
  float amp = 1.0;
  for (int octave = 0; octave < 4; octave++) {
    r += noise(x, y, z) * amp;
    amp /= 2;
    x *= 2;
    y *= 2;
    z *= 2;
  }
  return r;
}

void draw()
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

          fadeDot[i].x = (int)(fadeCircle[i].x - radius * cos(a) + 0.5); 
          fadeDot[i].y = (int)(fadeCircle[i].y - radius * sin(a) + 0.5);

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
      float speed = 0.002;
      float angle = sin(now * 0.001);
      float z = now * 0.00008;
      float hue = now * 0.01;
      float scale = 0.005;

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
            speed = 0.005;

          int depthmin2=2000;
          int depthmax2=3000;
          if (rawDepth < depthmax2 && rawDepth > depthmin2)
            speed = 0.004;

          int depthmin3 = 3000;
          int depthmax3 = 3500;
          if (rawDepth < depthmax3 && rawDepth > depthmin3)
            speed = 0.003;
        }
      }

      dx += cos(angle) * speed;
      dy += sin(angle) * speed;

      loadPixels();

      for (int x=0; x < width; x++) {
        for (int y=0; y < height; y++) {

          float n = fractalNoise(dx + x*scale, dy + y*scale, z) - 0.75;
          float m = fractalNoise(dx + x*scale, dy + y*scale, z + 10.0) - 0.75;

          color c = color(
          (hue + 80.0 * m) % 100.0, 
          100 - 100 * constrain(pow(3.0 * n, 3.5), 0, 0.9), 
          brightnessScale * constrain(pow(3.0 * n, 1.5), 0, 0.9)
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

      image(flipped, 0, 0, width*1.333333333, height);

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
  color colour; 
  PVector pos; 

  shapeClass(color colour, PVector pos) 
  { 
    this.colour = colour; 
    this.pos = pos;
  } 

  void display() 
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

