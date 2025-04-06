package com.example.calculadora
import android.animation.ValueAnimator
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private lateinit var numResultado: EditText
    private var operacionPendiente: String = ""
    private var numero1: Double = 0.0
    private var hayPunto: Boolean = false
    private var mostrandoResultado: Boolean = false
    private lateinit var parpadeoAnimation: ValueAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        numResultado = findViewById(R.id.tvResultado)

        // Configurar listeners para los botones numéricos
        setNumberButtonListeners()

        // Configurar listeners para los botones de operación
        setOperationButtonListeners()

        // Configura la animación de parpadeo
        setupBlinkAnimation()

        // Botón de limpiar (C)
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            numResultado.setText("")
            operacionPendiente = ""
            hayPunto = false
            mostrandoResultado = false  
            setupBlinkAnimation()
        }

        // Botón de igual (=)
        findViewById<Button>(R.id.btnIgual).setOnClickListener {
            if (operacionPendiente.isNotEmpty()) {
                calcularResultado()
            }
        }
    }

    private fun setupBlinkAnimation() {
        parpadeoAnimation = ValueAnimator.ofInt(0, 1).apply {
            duration = 650
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            addUpdateListener { animator ->
                val value = animator.animatedValue as Int
                numResultado.hint = if (value == 1) "|" else " "  // Alterna entre visible e invisible
            }
        }
        parpadeoAnimation.start()
    }

    private fun stopBlinkAnimation() {
        if (::parpadeoAnimation.isInitialized && parpadeoAnimation.isRunning) {
            parpadeoAnimation.cancel()
            numResultado.hint = ""  // Elimina el cursor al empezar a escribir
        }
    }

    private fun setNumberButtonListeners() {
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnPunto
        )

        numberButtons.forEach { buttonId ->
            findViewById<Button>(buttonId).setOnClickListener {
                stopBlinkAnimation()  // Detiene la animación al primer input
                // Si acabamos de mostrar un resultado, limpiamos para nueva operación
                if (mostrandoResultado) {  // <- Cambio nuevo
                    numResultado.setText("0")
                    mostrandoResultado = false
                }

                val buttonText = (it as Button).text.toString()
                val currentText = numResultado.text.toString()

                if (currentText == "0" && buttonText != ".") {
                    numResultado.setText(buttonText)
                } else {
                    if (buttonText == ".") {
                        if (!hayPunto) {
                            numResultado.append(buttonText)
                            hayPunto = true
                        }
                    } else {
                        numResultado.append(buttonText)
                    }
                }
            }
        }
    }

    private fun setOperationButtonListeners() {
        val operationButtons = listOf(
            R.id.btnSumar, R.id.btnRestar, R.id.btnMultiplicar, R.id.btnDividir,
            R.id.btnParentesisIzq, R.id.btnParentesisDer
        )

        operationButtons.forEach { buttonId ->
            findViewById<Button>(buttonId).setOnClickListener {
                val buttonText = (it as Button).text.toString()
                val currentText = numResultado.text.toString()

                if (operacionPendiente.isEmpty()) {
                    numero1 = currentText.toDouble()
                    operacionPendiente = buttonText
                    // Ahora añadimos el operador al EditText en lugar de resetear
                    numResultado.append(" $buttonText ")  // <- Cambio importante
                } else {
                    // Si ya hay operación pendiente, calculamos primero
                    calcularResultado()
                    numero1 = numResultado.text.toString().toDouble()
                    operacionPendiente = buttonText
                    numResultado.append(" $buttonText ")
                }

                hayPunto = false
                mostrandoResultado = false
            }
        }
    }

    // metodo para formatear resultados
    private fun formatResult(value: Double): String {
        return when {
            value.isNaN() -> "Error"
            value % 1 == 0.0 -> value.toInt().toString() // Enteros sin decimales
            else -> {
                val decimalFormat = DecimalFormat("#.########") // Máximo 8 decimales
                decimalFormat.roundingMode = RoundingMode.HALF_UP
                decimalFormat.format(value)
                    .replace(",", ".") // Asegurar punto decimal
                    .trimEnd('0')     // Eliminar ceros innecesarios
                    .trimEnd('.')      // Eliminar punto si no hay decimales
            }
        }
    }

    private fun calcularResultado() {
        val operacionCompleta = numResultado.text.toString()  // <- Obtenemos todo el texto
        val partes = operacionCompleta.split(" ")  // <- Dividimos por espacios

        if (partes.size >= 3) {  // <- Verificamos formato "num op num"
            numero1 = partes[0].toDouble()
            operacionPendiente = partes[1]
            val numero2 = partes[2].toDouble()

            val resultado = when(operacionPendiente) {  // <- Cálculo normal
                "+" -> numero1 + numero2
                "-" -> numero1 - numero2
                "×" -> numero1 * numero2
                "/" -> if(numero2 != 0.0) numero1 / numero2 else Double.NaN
                else -> numero2
            }

            // Mostramos resultado (limpia el EditText)
            /*
            tvResultado.setText(  // <- Cambio importante
                when {
                    resultado.isNaN() -> "Error"
                    resultado % 1 == 0.0 -> resultado.toInt().toString()
                    else -> resultado.toString()
                }
            )*/
            // Formateo mejorado del resultado
            numResultado.setText(formatResult(resultado))
            mostrandoResultado = true  // <- Marcamos que estamos mostrando resultado
            operacionPendiente = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::parpadeoAnimation.isInitialized) {
            parpadeoAnimation.cancel()
        }
    }
}