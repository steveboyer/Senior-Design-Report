#!/usr/bin/env python
import webiopi
import time
import vw
import pigpio

GPIO = webiopi.GPIO

pi = pigpio.pi()
BPS = 1000
RX = 11
TX = 25
rx = vw.tx(pi, RX, BPS)
tx = vw.tx(pi, TX, BPS)
STATUS = 4
NUM_SENDS = 1

webiopi.setDebug()


def setup():
    webiopi.debug("Setup...")
    GPIO.setFunction(STATUS, GPIO.OUT)
    GPIO.digitalWrite(STATUS, GPIO.HIGH)


def loop():
    webiopi.sleep(5)


def destroy():
    GPIO.digitalWrite(STATUS, GPIO.LOW)


def blink(numTiimes):
    for x in range(0, numTiimes):
        GPIO.digitalWrite(STATUS, GPIO.LOW)
        webiopi.sleep(0.2)
        GPIO.digitalWrite(STATUS, GPIO.HIGH)
        webiopi.sleep(0.2)


@webiopi.macro
def sendButton(button):
    blink(1)
    while not tx.ready():
        time.sleep(0.1)
    time.sleep(0.2)
    webiopi.debug("Sending " + str(0) + button)
    tx.put(str(0) + button)


@webiopi.macro
def sendRecord(button):
    blink(2)
    while not tx.ready():
        time.sleep(0.1)
    time.sleep(0.2)
    webiopi.debug("Sending " + str(1) + button)
    tx.put(str(1) + button)


@webiopi.macro
def sendReset():
    blink(3)
    while not tx.ready():
        time.sleep(0.1)
    blink(2)
    time.sleep(0.2)
    webiopi.debug("Sending " + str(2))
    tx.put(str(2))
    time.sleep(1)