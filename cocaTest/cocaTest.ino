#include "FastLED.h"
#define LED_PIN     7
#define NUM_LEDS    20
CRGB leds[NUM_LEDS];
void setup() {
  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUM_LEDS);
  
}
void loop() {
  /*make a gui that is 16x16. gui will let user click on a square, and that square's color
   * can be changed using an rgb picker. Then, the gui will make a file that contains
   * all of the squares that 
   * 
   */
  
  leds[0] = CRGB(100, 0, 0);
  FastLED.show();
  delay(500);  
  leds[1] = CRGB(170, 70, 0);
  FastLED.show();
  delay(500);
  leds[2] = CRGB(200, 150, 0);
  FastLED.show();
  delay(500);
  leds[3] = CRGB(0, 150, 3);
  FastLED.show();
  delay(500);
  leds[4] = CRGB(0, 30, 190);
  FastLED.show();
  delay(500);
  leds[5] = CRGB(85, 60, 180);
  FastLED.show();
  delay(500);
  leds[6] = CRGB(150, 0, 255);
  FastLED.show();
  delay(500);
}
