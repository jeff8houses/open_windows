//clears the board
OPC opc;
int numPixels = 60; //per ring
float angleOfFirst = PI;

void setup()
{
  size(240, 240);

  // Connect to the local instance of fcserver
  opc = new OPC(this, "127.0.0.1", 7890);
  
  // Map an 8x8 grid of rings of LEDs to the center of the window
  int numPixelsPerRing = numPixels;
  opc.ledRingGrid(width, height, numPixelsPerRing);
  background(0, 0, 0);
}

void draw()
{
  background(0);

}

