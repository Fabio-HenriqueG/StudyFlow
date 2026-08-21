package com.example.studyflow;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;


import com.example.studyflow.data.Anotacao;
import com.example.studyflow.data.AppDatabase;

import java.util.concurrent.Executors;

public class EditorTextoFragment extends Fragment {

    private EditText editTitulo, editConteudo;
    private Anotacao anotacaoExistente = null;
    private int corSelecionada = Color.BLACK;
    
    private final int[] tamanhosFixos = {12, 14, 16, 18, 20, 24, 28, 32, 40};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_editor_texto, container, false);

        editTitulo = view.findViewById(R.id.editTituloTexto);
        editConteudo = view.findViewById(R.id.editConteudoTexto);
        ImageButton btnSalvar = view.findViewById(R.id.btnSalvarTexto);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarTexto);

        ImageButton btnNegrito = view.findViewById(R.id.btnNegrito);
        ImageButton btnItalico = view.findViewById(R.id.btnItalico);
        ImageButton btnSublinhado = view.findViewById(R.id.btnSublinhado);
        ImageButton btnCor = view.findViewById(R.id.btnCorTexto);
        SeekBar seekBarTamanho = view.findViewById(R.id.seekBarTamanhoFonte);

        if (getArguments() != null) {
            anotacaoExistente = (Anotacao) getArguments().getSerializable("anotacao");
            if (anotacaoExistente != null) {
                editTitulo.setText(anotacaoExistente.titulo);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    editConteudo.setText(Html.fromHtml(anotacaoExistente.conteudoHtml, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    editConteudo.setText(Html.fromHtml(anotacaoExistente.conteudoHtml));
                }
            }
        }

        btnVoltar.setOnClickListener(v -> voltar());
        btnSalvar.setOnClickListener(v -> salvarNota());

        btnNegrito.setOnClickListener(v -> aplicarSpan(new StyleSpan(Typeface.BOLD)));
        btnItalico.setOnClickListener(v -> aplicarSpan(new StyleSpan(Typeface.ITALIC)));
        btnSublinhado.setOnClickListener(v -> aplicarSpan(new UnderlineSpan()));
        
        btnCor.setOnClickListener(this::mostrarDialogoCor);

        seekBarTamanho.setMax(tamanhosFixos.length - 1);
        seekBarTamanho.setProgress(3); // Começa no 18 (índice 3)

        seekBarTamanho.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int sizeSp = tamanhosFixos[progress];
                    int sizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sizeSp, getResources().getDisplayMetrics());
                    aplicarSpan(new AbsoluteSizeSpan(sizePx));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(View.GONE);
        }

        // Listener para fazer a barra de ferramentas subir com o teclado
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // O padding inferior deve ser o maior entre o teclado (IME) e a barra de navegação
            int bottomPadding = Math.max(imeInsets.bottom, systemBars.bottom);
            
            // Aplicamos também os insets laterais e superior para evitar sobreposição com status bar
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            
            return WindowInsetsCompat.CONSUMED; // Indica que já tratamos os insets nesta tela
        });


        return view;
    }


    private void aplicarSpan(Object span) {
        int start = editConteudo.getSelectionStart();
        int end = editConteudo.getSelectionEnd();
        if (start != end) {
            Spannable str = editConteudo.getText();
            str.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void mostrarDialogoCor(View v) {
        // Podemos reaproveitar o diálogo de cores do EditorAnotacaoFragment ou criar um simplificado
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Escolha a Cor");
        
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_shape, null);
        SeekBar rainbowBar = dialogView.findViewById(R.id.seekBarCorArcoIrisShape);
        
        final int[] tempCor = {corSelecionada};
        
        // Como o método configurarSeekBarArcoIris está no outro fragmento, vamos replicar aqui ou mover para um helper
        configurarSeekBarArcoIris(rainbowBar, cor -> tempCor[0] = cor);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            corSelecionada = tempCor[0];
            aplicarSpan(new ForegroundColorSpan(corSelecionada));
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void configurarSeekBarArcoIris(SeekBar seekBar, EditorAnotacaoFragment.ColorSelectionListener listener) {
        int[] rainbowColors = {
            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
        };
        android.graphics.drawable.GradientDrawable gradient = new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT, rainbowColors
        );
        seekBar.setBackground(gradient);
        seekBar.setProgressDrawable(null);
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float[] hsv = {progress, 1f, 1f};
                listener.onColorSelected(Color.HSVToColor(hsv));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void salvarNota() {
        String titulo = editTitulo.getText().toString().trim();
        if (titulo.isEmpty()) titulo = "Nota sem título";
        
        String html;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            html = Html.toHtml(editConteudo.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        } else {
            html = Html.toHtml(editConteudo.getText());
        }

        long data = System.currentTimeMillis();
        if (anotacaoExistente != null) {
            anotacaoExistente.titulo = titulo;
            anotacaoExistente.conteudoHtml = html;
            anotacaoExistente.dataUltimaEdicao = data;
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).anotacaoDao().atualizar(anotacaoExistente);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Nota salva!", Toast.LENGTH_SHORT).show();
                    voltar();
                });
            });
        } else {
            Anotacao nova = new Anotacao(titulo, html, data);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).anotacaoDao().inserir(nova);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Nota criada!", Toast.LENGTH_SHORT).show();
                    voltar();
                });
            });
        }
    }

    private void voltar() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(View.VISIBLE);
        }
    }
}
