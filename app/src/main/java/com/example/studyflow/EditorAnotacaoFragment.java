package com.example.studyflow;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.studyflow.data.Anotacao;
import com.example.studyflow.data.AppDatabase;
import java.util.concurrent.Executors;

/**
 * Tela de edição da Anotação (Caderno).
 * Nesta fase 1, implementamos apenas o salvamento básico.
 */
public class EditorAnotacaoFragment extends Fragment {

    private EditText editTitulo, editConteudo;
    private Anotacao anotacaoExistente = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editor_anotacao, container, false);

        editTitulo = view.findViewById(R.id.editTituloNota);
        editConteudo = view.findViewById(R.id.editConteudoNota);
        ImageButton btnSalvar = view.findViewById(R.id.btnSalvarNota);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarEditor);
        Button btnNegrito = view.findViewById(R.id.btnNegrito);
        Button btnItalico = view.findViewById(R.id.btnItalico);

        // Carrega dados se for uma edição
        if (getArguments() != null) {
            anotacaoExistente = (Anotacao) getArguments().getSerializable("anotacao");
            if (anotacaoExistente != null) {
                editTitulo.setText(anotacaoExistente.titulo);
                // Converte HTML salvo no banco para texto formatado no EditText
                editConteudo.setText(Html.fromHtml(anotacaoExistente.conteudoHtml, Html.FROM_HTML_MODE_COMPACT));
            }
        }

        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSalvar.setOnClickListener(v -> salvarNota());

        // Ações de Formatação (Fase 2)
        btnNegrito.setOnClickListener(v -> aplicarEstilo(Typeface.BOLD));
        btnItalico.setOnClickListener(v -> aplicarEstilo(Typeface.ITALIC));

        return view;
    }

    /**
     * Aplica um estilo (Negrito ou Itálico) ao texto que o usuário selecionou.
     */
    private void aplicarEstilo(int estilo) {
        int start = editConteudo.getSelectionStart();
        int end = editConteudo.getSelectionEnd();

        // Se não houver nada selecionado, não faz nada
        if (start == end) return;

        SpannableStringBuilder spannable = new SpannableStringBuilder(editConteudo.getText());
        
        // Verifica se a seleção já tem esse estilo para decidir se coloca ou retira
        StyleSpan[] spans = spannable.getSpans(start, end, StyleSpan.class);
        boolean jaTemEstilo = false;

        for (StyleSpan span : spans) {
            if (span.getStyle() == estilo) {
                spannable.removeSpan(span);
                jaTemEstilo = true;
            }
        }

        // Se não tinha o estilo, aplica agora
        if (!jaTemEstilo) {
            spannable.setSpan(new StyleSpan(estilo), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        editConteudo.setText(spannable);
        // Mantém a seleção no mesmo lugar após a mudança
        editConteudo.setSelection(start, end);
    }

    private void salvarNota() {
        String titulo = editTitulo.getText().toString().trim();
        // Converte o conteúdo formatado do EditText para HTML para salvar
        String conteudoHtml = Html.toHtml(editConteudo.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);

        if (titulo.isEmpty()) {
            titulo = "Sem título";
        }

        long agora = System.currentTimeMillis();
        
        if (anotacaoExistente != null) {
            anotacaoExistente.titulo = titulo;
            anotacaoExistente.conteudoHtml = conteudoHtml;
            anotacaoExistente.dataUltimaEdicao = agora;
            
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).anotacaoDao().atualizar(anotacaoExistente);
                voltarComFeedback("Nota atualizada!");
            });
        } else {
            Anotacao nova = new Anotacao(titulo, conteudoHtml, agora);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).anotacaoDao().inserir(nova);
                voltarComFeedback("Nota salva!");
            });
        }
    }

    private void voltarComFeedback(String msg) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }
    }
}
