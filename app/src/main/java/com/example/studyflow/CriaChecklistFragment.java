package com.example.studyflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Checklist;
import java.util.concurrent.Executors;

public class CriaChecklistFragment extends Fragment {

    private EditText editTitulo;
    private Checklist checklistEmEdicao = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cria_checklist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTitulo = view.findViewById(R.id.editTituloChecklist);
        Button btnSalvar = view.findViewById(R.id.btnSalvarChecklist);

        if (getArguments() != null && getArguments().containsKey("checklist_editar")) {
            checklistEmEdicao = (Checklist) getArguments().getSerializable("checklist_editar");
            if (checklistEmEdicao != null) {
                editTitulo.setText(checklistEmEdicao.titulo);
            }
        }

        btnSalvar.setOnClickListener(v -> salvarChecklist());
    }

    private void salvarChecklist() {
        String titulo = editTitulo.getText().toString().trim();
        if (titulo.isEmpty()) {
            editTitulo.setError("Digite um título");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            if (checklistEmEdicao != null) {
                checklistEmEdicao.titulo = titulo;
                AppDatabase.getInstance(getContext()).checklistDao().atualizar(checklistEmEdicao);
            } else {
                Checklist novo = new Checklist(titulo);
                AppDatabase.getInstance(getContext()).checklistDao().inserir(novo);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Checklist salvo!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }
}
