package com.example.studyflow;

import android.os.Bundle;
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
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Checklist;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CriaChecklistFragment extends Fragment {

    private EditText editTitulo;
    private MaterialSwitch switchFixar;
    private Button btnData;
    private Checklist checklistEmEdicao = null;
    private long dataSelecionada = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cria_checklist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTitulo = view.findViewById(R.id.editTituloChecklist);
        switchFixar = view.findViewById(R.id.switchFixar);
        btnData = view.findViewById(R.id.btnDataValidade);
        Button btnSalvar = view.findViewById(R.id.btnSalvarChecklist);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> voltarOuHome());

        if (getArguments() != null && getArguments().containsKey("checklist_editar")) {
            checklistEmEdicao = (Checklist) getArguments().getSerializable("checklist_editar");
            if (checklistEmEdicao != null) {
                editTitulo.setText(checklistEmEdicao.titulo);
                switchFixar.setChecked(checklistEmEdicao.isPinned);
                dataSelecionada = checklistEmEdicao.dataValidade;
                if (dataSelecionada > 0) atualizarBotaoData();
            }
        }

        btnData.setOnClickListener(v -> abrirDatePicker());
        btnSalvar.setOnClickListener(v -> salvarChecklist());
    }

    private void voltarOuHome() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void abrirDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Data de Validade")
                .setSelection(dataSelecionada > 0 ? dataSelecionada : MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            dataSelecionada = selection;
            atualizarBotaoData();
        });
        picker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void atualizarBotaoData() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnData.setText("Validade: " + sdf.format(new Date(dataSelecionada)));
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
                checklistEmEdicao.isPinned = switchFixar.isChecked();
                checklistEmEdicao.dataValidade = dataSelecionada;
                AppDatabase.getInstance(getContext()).checklistDao().atualizar(checklistEmEdicao);
            } else {
                Checklist novo = new Checklist(titulo);
                novo.isPinned = switchFixar.isChecked();
                novo.dataValidade = dataSelecionada;
                AppDatabase.getInstance(getContext()).checklistDao().inserir(novo);
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Checklist salvo!", Toast.LENGTH_SHORT).show();
                    voltarOuHome();
                });
            }
        });
    }
}
