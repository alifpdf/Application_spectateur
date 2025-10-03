#include <Arduino.h>
#define BT Serial3  // Module BLE branché sur RX3/TX3 de la MegaPi

unsigned long lastBLETime = 0;  // Dernier moment où une commande BLE valide a été reçue
const unsigned long debounceDelay = 200;  // Délai anti-rebond en millisecondes

void setup() {
  Serial.begin(115200);
  while (!Serial);
  BT.begin(115200);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
  Serial.println("=== MegaPi prêt ✅ ===");
  BT.println("MegaPi prêt ✅");
}

void loop() {
  // 📩 Réception BLE → USB
  if (BT.available()) {
    char c = BT.read();
    // Ignorer les retours à la ligne et les caractères non valides
    if (c == '1' || c == '0') {
      unsigned long now = millis();
      // Vérifier qu'on ne traite pas la même commande trop rapidement
      if (now - lastBLETime > debounceDelay) {
        lastBLETime = now;
        if (c == '1') {
          digitalWrite(LED_BUILTIN, HIGH);
          Serial.println("LED ON ✅");
          BT.println("LED ON ✅");
        }
        else if (c == '0') {
          digitalWrite(LED_BUILTIN, LOW);
          Serial.println("LED OFF ❌");
          BT.println("LED OFF ❌");
        }
      }
    }
  }

  // 📤 Envoi USB → BLE
  if (Serial.available()) {
    char c = Serial.read();
    BT.write(c);
    Serial.print("Envoyé au BLE : ");
    Serial.println(c);
  }
}
