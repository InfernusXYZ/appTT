package com.example.apptt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EncyclopediaActivity extends AppCompatActivity {

    private boolean[] isExpanded = new boolean[9]; // Controla el estado de expansión de cada cuadro
    private TextView tvDescripcion, tvtitulo;
    private Handler handler;
    private Runnable runnable;
    private int contador = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encyclopedia);
        Toast.makeText(EncyclopediaActivity.this, "Se encuentra en el módulo de Enciclopedia", Toast.LENGTH_SHORT).show();

        //Colores en titulo
        TextView textViewTitle = findViewById(R.id.textViewTitle2);
        String text = "Polliwallet";
        // Crea un SpannableString
        SpannableString spannableString = new SpannableString(text);
        // Aplica el color "secondary" para "Poli"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary)), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Aplica el color "grey" para "wallet"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey)), 5, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Establece el SpannableString en el TextView
        textViewTitle.setText(spannableString);

        // Inicializar y configurar todos los cuadros (9 en total)
        configurarCuadro(R.id.image_concept1, R.id.tv_concept_description1, 0);
        configurarCuadro(R.id.image_concept2, R.id.tv_concept_description2, 1);
        configurarCuadro(R.id.image_concept3, R.id.tv_concept_description3, 2);
        configurarCuadro(R.id.image_concept4, R.id.tv_concept_description4, 3);
        configurarCuadro(R.id.image_concept5, R.id.tv_concept_description5, 4);
        configurarCuadro(R.id.image_concept6, R.id.tv_concept_description6, 5);
        configurarCuadro(R.id.image_concept7, R.id.tv_concept_description7, 6);
        configurarCuadro(R.id.image_concept8, R.id.tv_concept_description8, 7);
        configurarCuadro(R.id.image_concept9, R.id.tv_concept_description9, 8);

        // Inicializar el TextView para la descripción de la introducción
        tvtitulo = findViewById(R.id.tvtitulo);
        tvDescripcion = findViewById(R.id.tv_descripcion);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                actualizarContenido();
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);


        // Inicializar los botones
        Button btnReload = findViewById(R.id.btn_enciclopedia);
        Button btnOption2 = findViewById(R.id.btn_balance);
        Button btnOption3 = findViewById(R.id.btn_ahorros);
        Button btnOption4 = findViewById(R.id.btn_endeudamiento);

        // Manejar el clic en el botón Recargar
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate(); // Recargar la actividad
            }
        });

        // Manejar el clic en el botón "Balance" (navegar a BalanceActivity)
        btnOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EncyclopediaActivity.this, BalanceActivity.class);
                startActivity(intent);
            }
        });

        // Manejar el clic en el botón "Ahorros" (navegar a AhorroActivity)
        btnOption3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EncyclopediaActivity.this, AhorroActivity.class);
                startActivity(intent);
            }
        });

        // Manejar el clic en el botón "Endeudamiento" (navegar a EndeudamientoActivity)
        btnOption4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EncyclopediaActivity.this, EndeudamientoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void actualizarContenido(){
        contador++;
        String[] titulos= {"¿Que es un ingreso?",
                "¿Que es un gasto?",
                "¿Que es una deuda?"};
        String[] descripciones={"Un ingreso es toda aquella ganancia que se recibe por la venta de un bien o servicio, que generalmente se hace efectiva mediante un cobro en forma de dinero.",
                "El gasto es el uso de un cierto dinero presupuestado, ya sea por parte de un individuo, una empresa, una organización o un Estado, para obtener a cambio un bien o un servicio determinado." ,
                "La deuda es una obligación que tiene una persona física o jurídica para cumplir sus compromisos de pago, fruto del ejercicio de su actividad económica."};
        //String nuevoContenido="Descripcion #"+contador;
        tvDescripcion.setText(descripciones[contador]);
        tvtitulo.setText(titulos[contador]);
        if (contador==2){
            contador = -1;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }


    // Método para configurar cada cuadro con la funcionalidad de expansión
    private void configurarCuadro(int imageId, final int descriptionId, final int index) {
        ImageView imageConcept = findViewById(imageId);
        final TextView tvConceptDescription = findViewById(descriptionId);

        // Manejar el clic en la imagen para expandir o colapsar la descripción
        imageConcept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded[index]) {
                    // Si está expandido, colapsar la descripción a 2 líneas
                    tvConceptDescription.setMaxLines(2);
                    tvConceptDescription.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    isExpanded[index] = false;
                } else {
                    // Si está colapsado, expandir para mostrar toda la descripción
                    tvConceptDescription.setMaxLines(Integer.MAX_VALUE);
                    tvConceptDescription.setEllipsize(null);
                    isExpanded[index] = true;
                }
            }
        });
    }
}
