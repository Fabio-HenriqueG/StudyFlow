package com.example.studyflow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Anotacao;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executors;

public class ConfiguracoesFragment extends Fragment {

    private EditText editNome;
    private ImageView imgPerfil;
    private MaterialButtonToggleGroup toggleTema, toggleInsistencia;
    private Spinner spinnerEstilo;
    private SeekBar seekBarFonte, seekBarFreqMetas, seekBarFreqChecklist;
    private TextView txtFonteValor, txtFreqMetasValor, txtFreqChecklistValor;
    private MaterialSwitch switchNotificacoes, switchSom, switchVibracao;
    private Button btnSilencioInicio, btnSilencioFim, btnResetMetas;
    private SharedPreferences prefs;
    private String currentProfilePicUri = "";

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    currentProfilePicUri = uri.toString();
                    imgPerfil.setImageURI(uri);
                    // Persiste permissão persistente se necessário, mas para Uri de mídia costuma ser temporária
                    // Idealmente salvaríamos o arquivo internamente, mas seguindo o requisito de salvar o path:
                    getContext().getContentResolver().takePersistableUriPermission(uri, 
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    private final ActivityResultLauncher<String> createDoc =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
                if (uri != null) exportarParaUri(uri);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracoes, container, false);

        // Inicializa componentes
        editNome = view.findViewById(R.id.editNomeUsuario);
        imgPerfil = view.findViewById(R.id.imgPerfilConfig);
        toggleTema = view.findViewById(R.id.toggleGroupTema);
        toggleInsistencia = view.findViewById(R.id.toggleGroupInsistencia);
        spinnerEstilo = view.findViewById(R.id.spinnerEstiloCaderno);
        seekBarFonte = view.findViewById(R.id.seekBarFontSize);
        txtFonteValor = view.findViewById(R.id.txtFontSizeValue);
        seekBarFreqMetas = view.findViewById(R.id.seekBarFreqMetas);
        txtFreqMetasValor = view.findViewById(R.id.txtFreqMetasValor);
        seekBarFreqChecklist = view.findViewById(R.id.seekBarFreqChecklist);
        txtFreqChecklistValor = view.findViewById(R.id.txtFreqChecklistValor);
        switchNotificacoes = view.findViewById(R.id.switchNotificacoes);
        switchSom = view.findViewById(R.id.switchSom);
        switchVibracao = view.findViewById(R.id.switchVibracao);
        btnSilencioInicio = view.findViewById(R.id.btnSilencioInicio);
        btnSilencioFim = view.findViewById(R.id.btnSilencioFim);
        btnResetMetas = view.findViewById(R.id.btnResetMetas);
        
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarConfig);
        Button btnSalvar = view.findViewById(R.id.btnSalvarConfig);
        Button btnAlterarFoto = view.findViewById(R.id.btnAlterarFoto);
        Button btnExportar = view.findViewById(R.id.btnExportarDados);
        Button btnLimpar = view.findViewById(R.id.btnLimparTudo);

        prefs = requireContext().getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);

        // Configura Spinner de Estilo
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, new String[]{"BLANK", "GRID", "LINES", "DOTTED"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstilo.setAdapter(adapter);

        // Configura SeekBar de Fonte
        seekBarFonte.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtFonteValor.setText(progress + "sp");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Configura SeekBar de Frequência de Metas (4 a 20)
        seekBarFreqMetas.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtFreqMetasValor.setText((progress + 4) + " vezes");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Configura SeekBar de Frequência de Checklists (4 a 20)
        seekBarFreqChecklist.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtFreqChecklistValor.setText((progress + 4) + " vezes");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Listeners de Seleção de Horário
        btnSilencioInicio.setOnClickListener(v -> mostrarTimePicker("silence_start", btnSilencioInicio));
        btnSilencioFim.setOnClickListener(v -> mostrarTimePicker("silence_end", btnSilencioFim));
        btnResetMetas.setOnClickListener(v -> mostrarTimePicker("goal_reset_time", btnResetMetas));

        // Outros Listeners
        btnAlterarFoto.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        
        btnExportar.setOnClickListener(v -> createDoc.launch("studyflow_export.json"));
        btnLimpar.setOnClickListener(v -> confirmarLimpeza());
        
        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSalvar.setOnClickListener(v -> salvarConfiguracoes());

        carregarConfiguracoes();

        return view;
    }

    private void mostrarTimePicker(String key, Button btn) {
        String current = prefs.getString(key, "00:00");
        String[] parts = current.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(h)
                .setMinute(m)
                .setTitleText("Selecione o Horário")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format("%02d:%02d", picker.getHour(), picker.getMinute());
            btn.setText(btn.getText().toString().split(":")[0] + ": " + time);
            prefs.edit().putString(key, time).apply();
        });

        picker.show(getParentFragmentManager(), "TIME_PICKER");
    }

    private void carregarConfiguracoes() {
        editNome.setText(prefs.getString("user_name", ""));
        
        currentProfilePicUri = prefs.getString("user_profile_pic", "");
        if (!currentProfilePicUri.isEmpty()) {
            imgPerfil.setImageURI(Uri.parse(currentProfilePicUri));
        }

        int tema = prefs.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (tema == AppCompatDelegate.MODE_NIGHT_NO) toggleTema.check(R.id.btnTemaClaro);
        else if (tema == AppCompatDelegate.MODE_NIGHT_YES) toggleTema.check(R.id.btnTemaEscuro);
        else toggleTema.check(R.id.btnTemaSistema);

        int perfil = prefs.getInt("notification_insistence_perfil", 1); // 0=Discreto, 1=Equilibrado, 2=Chato
        if (perfil == 0) toggleInsistencia.check(R.id.btnPerfilDiscreto);
        else if (perfil == 2) toggleInsistencia.check(R.id.btnPerfilChato);
        else toggleInsistencia.check(R.id.btnPerfilEquilibrado);

        String estilo = prefs.getString("notebook_style", "GRID");
        spinnerEstilo.setSelection(((ArrayAdapter)spinnerEstilo.getAdapter()).getPosition(estilo));

        int fontSize = prefs.getInt("default_font_size", 24);
        seekBarFonte.setProgress(fontSize);
        txtFonteValor.setText(fontSize + "sp");

        int freqMetas = prefs.getInt("goal_notification_frequency", 4);
        seekBarFreqMetas.setProgress(freqMetas - 4);
        txtFreqMetasValor.setText(freqMetas + " vezes");

        int freqChecklist = prefs.getInt("checklist_notification_frequency", 4);
        seekBarFreqChecklist.setProgress(freqChecklist - 4);
        txtFreqChecklistValor.setText(freqChecklist + " vezes");

        switchNotificacoes.setChecked(prefs.getBoolean("notifications_enabled", true));
        switchSom.setChecked(prefs.getBoolean("notification_sound", true));
        switchVibracao.setChecked(prefs.getBoolean("notification_vibration", true));

        btnSilencioInicio.setText("Início: " + prefs.getString("silence_start", "22:00"));
        btnSilencioFim.setText("Fim: " + prefs.getString("silence_end", "07:00"));
        btnResetMetas.setText("Horário de Reset: " + prefs.getString("goal_reset_time", "00:00"));
    }

    private void salvarConfiguracoes() {
        int tema = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        int checkedId = toggleTema.getCheckedButtonId();
        if (checkedId == R.id.btnTemaClaro) tema = AppCompatDelegate.MODE_NIGHT_NO;
        else if (checkedId == R.id.btnTemaEscuro) tema = AppCompatDelegate.MODE_NIGHT_YES;

        int perfil = 1;
        int checkedPerfilId = toggleInsistencia.getCheckedButtonId();
        if (checkedPerfilId == R.id.btnPerfilDiscreto) perfil = 0;
        else if (checkedPerfilId == R.id.btnPerfilChato) perfil = 2;

        prefs.edit()
                .putString("user_name", editNome.getText().toString().trim())
                .putString("user_profile_pic", currentProfilePicUri)
                .putInt("app_theme", tema)
                .putInt("notification_insistence_perfil", perfil)
                .putString("notebook_style", spinnerEstilo.getSelectedItem().toString())
                .putInt("default_font_size", seekBarFonte.getProgress())
                .putInt("goal_notification_frequency", seekBarFreqMetas.getProgress() + 4)
                .putInt("checklist_notification_frequency", seekBarFreqChecklist.getProgress() + 4)
                .putBoolean("notifications_enabled", switchNotificacoes.isChecked())
                .putBoolean("notification_sound", switchSom.isChecked())
                .putBoolean("notification_vibration", switchVibracao.isChecked())
                .apply();

        AppCompatDelegate.setDefaultNightMode(tema);
        Toast.makeText(getContext(), "Configurações salvas!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    private void confirmarLimpeza() {
        new AlertDialog.Builder(getContext())
                .setTitle("Limpar Tudo")
                .setMessage("Isso apagará permanentemente todos os seus dados. Continuar?")
                .setPositiveButton("Sim", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(getContext()).clearAllTables();
                        prefs.edit().clear().apply();
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Dados limpos!", Toast.LENGTH_SHORT).show();
                            getActivity().recreate();
                        });
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void exportarParaUri(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Anotacao> anotacoes = AppDatabase.getInstance(getContext()).anotacaoDao().buscarTodas();
                JSONArray array = new JSONArray();
                for (Anotacao a : anotacoes) {
                    JSONObject obj = new JSONObject();
                    obj.put("titulo", a.titulo);
                    obj.put("conteudo", a.conteudoHtml);
                    obj.put("data", a.dataUltimaEdicao);
                    array.put(obj);
                }
                
                OutputStream os = getContext().getContentResolver().openOutputStream(uri);
                os.write(array.toString(4).getBytes());
                os.close();
                
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Dados exportados!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro ao exportar", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
