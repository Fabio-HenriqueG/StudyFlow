package com.example.studyflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MenuMaisBottomSheet extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout da listinha que criamos
        return inflater.inflate(R.layout.layout_menu_mais, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vincular os botões
        Button btnTarefa = view.findViewById(R.id.btn_menu_tarefa);
        Button btnAnotacao = view.findViewById(R.id.btn_menu_anotacao);
        Button btnBlocoNotas = view.findViewById(R.id.btn_menu_bloco_notas);
        Button btnMeta = view.findViewById(R.id.btn_menu_meta);
        Button btnChecklist = view.findViewById(R.id.btn_menu_checklist);
        Button btnFlashcard = view.findViewById(R.id.btn_menu_flashcard);


        // Configurar a ação de clique para cada um
        btnTarefa.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abrindo criação de Tarefa...", Toast.LENGTH_SHORT).show();
            CriaTarefaFragment criaTarefaFrag = new CriaTarefaFragment();
            //Usa o FragmentManager da MainActivity para fazer a troca de tela
            getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, criaTarefaFrag) //Troca pelo fragmento de criação
                            .addToBackStack(null) //Permite voltar/cancelar pela seta do celular
                            .commit();
            dismiss(); // Fecha a listinha
        });

        btnAnotacao.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abrindo Caderno Livre...", Toast.LENGTH_SHORT).show();
            EditorAnotacaoFragment fragment = new EditorAnotacaoFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            dismiss();
        });

        btnBlocoNotas.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abrindo Bloco de Notas...", Toast.LENGTH_SHORT).show();
            EditorTextoFragment fragment = new EditorTextoFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            dismiss();
        });


        btnMeta.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abrindo criação de Meta...", Toast.LENGTH_SHORT).show();
            CriaMetaFragment criaMetaFrag = new CriaMetaFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, criaMetaFrag)
                    .addToBackStack(null)
                    .commit();
            dismiss();
        });

        btnChecklist.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abrindo criação de Checklist...", Toast.LENGTH_SHORT).show();
            CriaChecklistFragment criaChecklistFrag = new CriaChecklistFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, criaChecklistFrag)
                    .addToBackStack(null)
                    .commit();
            dismiss();
        });

        btnFlashcard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abrindo criação de Flashcard...", Toast.LENGTH_SHORT).show();
            CriaFlashcardFragment fragment = new CriaFlashcardFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            dismiss();
        });
    }
}
