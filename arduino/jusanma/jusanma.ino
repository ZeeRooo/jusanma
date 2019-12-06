#define ENA 6 // PWM
#define IN1 2
#define IN2 3

#define ENB 5 // PWM
#define IN3 7
#define IN4 4

//#define luzTrasera 10
//#define luzDelantera 9

#define A 13
#define B 12
#define C 11
#define D 10

#define trigger 8
#define echo 9 // PWM

void setup() {
  Serial.begin(9600);
  pinMode(ENA, OUTPUT);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);

  pinMode(ENB, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);

/*  pinMode(luzTrasera, OUTPUT);
  pinMode(luzDelantera, OUTPUT);*/

  pinMode(A, OUTPUT);
  pinMode(B, OUTPUT);
  pinMode(C, OUTPUT);
  pinMode(D, OUTPUT);

  pinMode(trigger, OUTPUT);
  pinMode(echo, INPUT);
}

byte velocidad, pinA, pinB, pinC, pinD, resto, inicio = 0;//, intensidadLed = 0;
bool movimiento = 0;
int lectura, distancia;

void loop() {
  switch (Serial.read()) {
    case 'C':
      medirDist();
      break;
    case 'F':
      velocidad = Serial.parseInt();
      motorA(1, 0, velocidad);
      motorB(1, 0, velocidad);
      movimiento = 1;
      break;
    case 'B':
      velocidad = Serial.parseInt();
      motorA(0, 1, velocidad);
      motorB(0, 1, velocidad);
      movimiento = 1;
      break;
    case 'R':
      velocidad = Serial.parseInt();
      motorA(0, 1, velocidad);
      motorB(1, 0, velocidad);
      movimiento = 1;
      break;
    case 'L':
      velocidad = Serial.parseInt();
      motorA(1, 0, velocidad);
      motorB(0, 1, velocidad);
      movimiento = 1;
      break;
   /* case 'Q':
      intensidadLed = Serial.parseInt();
      analogWrite(luzTrasera, intensidadLed);
      analogWrite(luzDelantera, intensidadLed);
      break;*/
    case 'S':
      if (movimiento) {
        motorA(0, 0, 0);
        motorB(0, 0, 0);

        movimiento = 0;

       /* if (intensidadLed > 0)
          digitalWrite(luzTrasera, HIGH);*/
      }
      break;
    case 'T':
      lectura = (Serial.parseInt() * 2048) / 360;

  /*
     <signo><pasos>
     + -> horario
     - -> antihorario

     Ejemplo de funcionamiento:
     -2038 -> giro completo antihorario.
     +2038 o 2038 -> giro completo horario.
  */
  if (lectura > 0) { // La cantidad de pasos es positiva, se trata de un giro horario
    inicio = 0;
    Serial.println("horario");
  } else if (lectura < 0) { // La cantidad de pasos es negativa, se trata de un giro antihorario
    inicio = 5;
    Serial.println("antihorario");
    lectura = lectura * -1; // Se convierte en positivo
  }

  if (lectura != 0) { // Serial.parseInt() siempre devuelve cero por defecto si no se ha ingresado ningun valor. Necesitamos ejecutarlo el codigo si 'lectura' posee algun valor, es decir, es diferente de cero.
    Serial.println(lectura);
    resto = lectura % 4; // Devuelve el resto entre la cantidad de pasos ingresado / 4

    /*
       Habran tantos ciclos como cantidad de pasos / 4.
       Por ejemplo:
       pasos = 21
       pasos / 4 = 5.25 , es decir, 5 ciclos completos y 1 extra por el resto (.25 x 4 = 1)
    */

    for (int ciclos = lectura / 4; ciclos >= 0; ciclos--) {
      paso();
    }

    if (resto != 0) {
      for (int ciclos = resto; ciclos >= 0; ciclos--) {
        paso(); // Siempre y cuanto la cuenta no de resto cero
      }
    }
  }
      break;
  }
        Serial.println(distancia);

 /* if (intensidadLed > 0 && movimiento)
    analogWrite(luzTrasera, intensidadLed);*/
}

/*
   - PWM para regular la velocidad (0 a 255).
   - accion para controlar el sentido:

   1 -> adelante
   0 -> atras
*/
void motorA(byte accion1, byte accion2, byte pwm) {
  analogWrite(ENA, pwm);
  digitalWrite(IN1, accion1);
  digitalWrite(IN2, accion2);
}

void motorB(byte accion1, byte accion2, byte pwm) {
  analogWrite(ENB, pwm);
  digitalWrite(IN3, accion1);
  digitalWrite(IN4, accion2);
}

void paso() {
  /*
     Metodo que hará rotar el motor. El codigo se divide en cuatro pasos, por lo que ejecutarlos 4 a 1 determinará un sentido antihorario, mientras que 1 a 4 será horario:
     - Horario: (a = 0; a < 5; a++), es decir, mientras 'a' sea menor que cinco, el loop se ejecuta. Crece con cada ejecución.
     - Antihorario: (a = 5; a > 0; a--), es decir, mientras 'a' sea mayor que cero, el loop se ejecuta. Decrece con cada ejecución.

     La variable 'inicio' varia segun el sentido de giro, siendo 0 para el caso horario y 5 para el antihorario.
  */
  byte a = inicio;

  do {
    switch (a) {
      case 1:
        pasoWriter(0, 0, 1, 1);
        break;
      case 2:
        pasoWriter(1, 0, 0, 1);
        break;
      case 3:
        pasoWriter(1, 1, 0, 0);
        break;
      case 4:
        pasoWriter(0, 1, 1, 0);
        break;
    }

    if (inicio == 0) // Si inicio es igual a cero, se trata de giro horario. Se procede a incrementar 'a' en una unidad.
      a++;
    else
      a--;

    delay(2);
  } while ((inicio == 0 && a < 5) || (inicio == 5 && a > 0));

  pasoWriter(0, 0, 0, 0);
}

void pasoWriter(byte a, byte b, byte c, byte d) {
  /*
     Metodo que cambia los estados digitales de los cuatro pines utilizados para el funcionamiento del driver.
  */
  digitalWrite(A, a);
  digitalWrite(B, b);
  digitalWrite(C, c);
  digitalWrite(D, d);
}

void medirDist() {
  digitalWrite(trigger, LOW);
  delayMicroseconds(2);

  digitalWrite(trigger, HIGH);
  delayMicroseconds(20);
  digitalWrite(trigger, LOW);

  distancia= (pulseIn(echo, HIGH) / 58);
}
