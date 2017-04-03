#include <VirtualWire.h>
#include <IRremote.h>
#include <EEPROMvar.h>
#include <EEPROMex.h>

#define CHILD_ID 0x30;
#define SBUTTON 0x30
#define SRECORD 0x31
#define SRESET 0x32
#define NULL 0x21

const int maxAllowedWrites = 20;
const int memBase = 120;

const int IR_IN = 10;
const int STATUS_PIN = 11;
const int VW_TRANSMIT_PIN = 12;
const int VW_RECEIVE_PIN = 13;

const int VW_TRANSMIT_EN_PIN = 4; // No wire here

const int WAIT_MAX = 5; // Max time to wait for IR input

IRrecv irrecv(IR_IN);
IRsend irsend;

decode_results results;

int codeType = -1;
unsigned long codeValue;
unsigned int rawCodes[RAWBUF];
int codeLen;
int toggle = 0;
const int NUM_IR_CODES = 20;
unsigned long IR_codes[NUM_IR_CODES];
int address = 0;
int lastButtonState;
const int drSize = 20;
const int numCodes = 20;
int addresses[numCodes];
decode_results zeroedDR;

void writeVar(EEPROMVar<decode_results> &eepromvar) {
  eepromvar.restore();
}

void generateAddresses(){  
  for(int i = 0; i < numCodes; i++){
     addresses[i] = drSize*i; 
  }
}

void setup()
{
  // Delay
  delay(1000);
  
  // Init status
  pinMode(STATUS_PIN, OUTPUT);
  
  // Init serial
  Serial.begin(9600);	
  Serial.println("setup");
  
  // Init VirtualWire
  vw_set_tx_pin(VW_TRANSMIT_PIN);
  vw_set_rx_pin(VW_RECEIVE_PIN);
  vw_set_ptt_pin(VW_TRANSMIT_EN_PIN);
  //vw_set_ptt_inverted(true); // Required for DR3100
  vw_setup(1000);	 // Bits per sec
  vw_rx_start();       // Start the receiver PLL running
  
  // Init IR in
  irrecv.enableIRIn();
  
  EEPROM.setMemPool(memBase, EEPROMSizeUno);
  
  EEPROM.setMaxAllowedWrites(maxAllowedWrites);
  digitalWrite(STATUS_PIN, HIGH);
  generateAddresses();
}

void blink(int ntimes){
  for(int i = 0; i < ntimes; i++){
    digitalWrite(STATUS_PIN, LOW);
    delay(100);
    digitalWrite(STATUS_PIN, HIGH);
    delay(100);
  }
}

void loop()
{
  uint8_t buf[VW_MAX_MESSAGE_LEN];
  uint8_t buflen = VW_MAX_MESSAGE_LEN;
  if (vw_get_message(buf, &buflen)) // Non-blocking
  {
      int i;
      digitalWrite(STATUS_PIN, HIGH); // Flash a light to show received good message
      // Message with a good checksum received, dump it.
      Serial.print("Got: ");
      for (i = 0; i < buflen; i++) {
        Serial.print(buf[i], HEX);
        Serial.print(' ');
      }
      Serial.println();
      digitalWrite(STATUS_PIN, LOW);
      if (buf[0] == SRECORD) {
          blink(1);
          Serial.println("Record mode");
          int button = 0;
          int b1 = buf[1];
          if(buf[2] != NULL){
            int b2 = buf[2];
            button = b1 * 10 + b2;  
          } else {
            button = b1;  
          }
          int currentAddress = addresses[button];
          irrecv.enableIRIn();
          boolean wait = false;
          int time = 0;
          digitalWrite(STATUS_PIN, HIGH);
          while(!irrecv.decode(&results)){ 
            if(!wait){
              wait = true;
              Serial.println("Waiting...");
            }
          }
          digitalWrite(STATUS_PIN, LOW);
          storeCode(&results);
          EEPROMVar<decode_results> eepResults = results;
          writeVar(eepResults);
          Serial.print("size: ");
          Serial.println(sizeof(eepResults));
          irrecv.resume();
      } else if (buf[0] == SBUTTON) {
          blink(2);
          int button = 0;
          Serial.println("Button mode");
          for(int i = 0; i<3; i++){
            int b1 = buf[1];
            if(buf[2] != NULL){
              int b2 = buf[2];
              button = b1 * 10 + b2;  
            } else {
              button = b1;  
            }
            int currentAddress = addresses[button];
            EEPROM.readBlock<decode_results>(address + currentAddress,results);
            //Serial.println(sizeof(results));
            sendCode(false);
            delay(100); 
          }    
      } else if(buf[0] == SRESET) {
          blink(3);
          for(int i = 0; i < numCodes; i++){
             
          } 
      } else {
          Serial.println("Bad receive: " + buf[0]);
          blink(4);
      }
  } 
}

// Stores the code for later playback
// Most of this code is just logging
void storeCode(void* v){
	decode_results *results = (decode_results *)v;
//void storeCode(decode_results *results) {

  //codeType = results->decode_type;
  int count = results->rawlen;
  if (codeType == UNKNOWN) {
    Serial.println("Received unknown code, saving as raw");
    codeLen = results->rawlen - 1;
    // To store raw codes:
    // Drop first value (gap)
    // Convert from ticks to microseconds
    // Tweak marks shorter, and spaces longer to cancel out IR receiver distortion
    for (int i = 1; i <= codeLen; i++) {
      if (i % 2) {
        // Mark
        rawCodes[i - 1] = results->rawbuf[i]*USECPERTICK - MARK_EXCESS;
        Serial.print(" m");
      } 
      else {
        // Space
        rawCodes[i - 1] = results->rawbuf[i]*USECPERTICK + MARK_EXCESS;
        Serial.print(" s");
      }
      Serial.print(rawCodes[i - 1], DEC);
    }
    Serial.println("");
  }
  else {
    if (codeType == NEC) {
      Serial.print("Received NEC: ");
      if (results->value == REPEAT) {
        // Don't record a NEC repeat value as that's useless.
        Serial.println("repeat; ignoring.");
        return;
      }
    } 
    else if (codeType == SONY) {
      Serial.print("Received SONY: ");
    } 
    else if (codeType == RC5) {
      Serial.print("Received RC5: ");
    } 
    else if (codeType == RC6) {
      Serial.print("Received RC6: ");
    } 
    else {
      Serial.print("Unexpected codeType ");
      Serial.print(codeType, DEC);
      Serial.println("");
    }
    Serial.println(results->value, HEX);
    codeValue = results->value;
    codeLen = results->bits;
  }
}

void sendCode(int repeat) {
  if (codeType == NEC) {
    if (repeat) {
      irsend.sendNEC(REPEAT, codeLen);
      Serial.println("Sent NEC repeat");
    } 
    else {
      irsend.sendNEC(codeValue, codeLen);
      Serial.print("Sent NEC ");
      Serial.println(codeValue, HEX);
    }
  } 
  else if (codeType == SONY) {
    irsend.sendSony(codeValue, codeLen);
    Serial.print("Sent Sony ");
    Serial.println(codeValue, HEX);
  } 
  else if (codeType == RC5 || codeType == RC6) {
    if (!repeat) {
      // Flip the toggle bit for a new button press
      toggle = 1 - toggle;
    }
    // Put the toggle bit into the code to send
    codeValue = codeValue & ~(1 << (codeLen - 1));
    codeValue = codeValue | (toggle << (codeLen - 1));
    if (codeType == RC5) {
      Serial.print("Sent RC5 ");
      Serial.println(codeValue, HEX);
      irsend.sendRC5(codeValue, codeLen);
    } 
    else {
      irsend.sendRC6(codeValue, codeLen);
      Serial.print("Sent RC6 ");
      Serial.println(codeValue, HEX);
    }
  } 
  else if (codeType == UNKNOWN /* i.e. raw */) {
    // Assume 38 KHz
    irsend.sendRaw(rawCodes, codeLen, 38);
    Serial.println("Sent raw");
  }
}



