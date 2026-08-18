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
import com.example.studyflow.data.Meta;
import java.util.concurrent.Executors;

public class CriaMetaFragment extends Fragment {

    private EditText txtTituloMeta;
    private Button btnSalvarMeta;
    private Meta metaEmEdicao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cria_meta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTituloMeta = view.findViewById(R.id.txtTituloMeta);
        btnSalvarMeta = view.findViewById(R.id.btnSalvarMeta);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> voltarOuHome());

        if (getArguments() != null && getArguments().containsKey("meta_editar")) {
            metaEmEdicao = (Meta) getArguments().getSerializable("meta_editar");
            if (metaEmEdicao != null) {
                txtTituloMeta.setText(metaEmEdicao.titulo);
                btnSalvarMeta.setText("Atualizar Meta");
            }
        }

        btnSalvarMeta.setOnClickListener(v -> salvarMeta());
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

    private void salvarMeta() {
        String titulo = txtTituloMeta.getText().toString().trim();
        if (titulo.isEmpty()) {
            txtTituloMeta.setError("Digite o título da meta");
            return;
        }

        if (metaEmEdicao != null) {
            metaEmEdicao.titulo = titulo;
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).metaDao().atualizar(metaEmEdicao);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Meta atualizada!", Toast.LENGTH_SHORT).show();
                        voltarOuHome();
                    });
                }
            });
        } else {
            Meta novaMeta = new Meta(titulo, System.currentTimeMillis());
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).metaDao().inserir(novaMeta);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Meta iniciada!", Toast.LENGTH_SHORT).show();
                        voltarOuHome();
                    });
                }
            });
        }
    }
}
