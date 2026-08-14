package com.example.studyflow;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studyflow.data.Anotacao;
import com.example.studyflow.data.AppDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;

/**
 * Tela de edição da Anotação (Caderno Livre).
 * Otimizada para persistência fiel de objetos móveis.
 */
public class EditorAnotacaoFragment extends Fragment {

    private EditText editTitulo;
    private RelativeLayout canvasNotas;
    private Anotacao anotacaoExistente = null;
    private DesenhoView desenhoView;
    private View layoutControlesDesenho;
    private View btnFerramentas;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) processarImagemGaleria(uri);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editor_anotacao, container, false);

        editTitulo = view.findViewById(R.id.editTituloNota);
        canvasNotas = view.findViewById(R.id.canvasNotas);
        canvasNotas.setPivotX(0);
        canvasNotas.setPivotY(0);
        canvasNotas.setBackground(new GridDrawable());
        canvasNotas.setOnTouchListener(null);

        SeekBar zoomBar = view.findViewById(R.id.seekBarZoomCanvas);
        SeekBar navXBar = view.findViewById(R.id.seekBarNavX);
        SeekBar navYBar = view.findViewById(R.id.seekBarNavY);

        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale = 0.5f + (progress / 100.0f);
                canvasNotas.setScaleX(scale);
                canvasNotas.setScaleY(scale);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        navXBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                canvasNotas.setTranslationX(-progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        navYBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                canvasNotas.setTranslationY(-progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        canvasNotas.post(() -> {
            zoomBar.setProgress(50);
            navXBar.setProgress(0);
            navYBar.setProgress(0);
        });

        ImageButton btnSalvar = view.findViewById(R.id.btnSalvarNota);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarEditor);
        btnFerramentas = view.findViewById(R.id.btnAbrirFerramentas);

        if (getArguments() != null) {
            anotacaoExistente = (Anotacao) getArguments().getSerializable("anotacao");
            if (anotacaoExistente != null) {
                editTitulo.setText(anotacaoExistente.titulo);
                carregarObjetosDoJson(anotacaoExistente.conteudoHtml);
            }
        }

        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSalvar.setOnClickListener(v -> salvarNota());

        desenhoView = view.findViewById(R.id.desenhoViewSobreposto);
        layoutControlesDesenho = view.findViewById(R.id.layoutControlesDesenho);
        view.findViewById(R.id.btnLimparCanvas).setOnClickListener(v -> desenhoView.limpar());
        view.findViewById(R.id.btnConcluirDesenho).setOnClickListener(v -> concluirDesenho());

        SeekBar seekBar = view.findViewById(R.id.seekBarEspessura);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                desenhoView.setTamanhoPincel(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        adicionarBotoesDeCores(view.findViewById(R.id.layoutCoresDesenho));
        btnFerramentas.setOnClickListener(this::mostrarMenuFerramentas);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(View.VISIBLE);
        }
    }

    private void mostrarMenuFerramentas(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add("Adicionar Texto");
        popup.getMenu().add("Inserir Imagem");
        popup.getMenu().add("Desenhar Livre");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "Adicionar Texto": mostrarDialogoNovoTexto(); break;
                case "Inserir Imagem": 
                    pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
                    break;
                case "Desenhar Livre": ativarModoDesenho(); break;
            }
            return true;
        });
        popup.show();
    }

    private void mostrarDialogoNovoTexto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Novo Texto");
        final EditText input = new EditText(getContext());
        input.setHint("Escreva algo...");
        builder.setView(input);
        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            String texto = input.getText().toString();
            if (!texto.isEmpty()) adicionarTextoAoCanvas(texto);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void adicionarTextoAoCanvas(String texto) {
        TextView textView = new TextView(getContext());
        textView.setText(texto);
        textView.setTextSize(24);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(20, 20, 20, 20);
        textView.setOnTouchListener(new MultiTouchListener(getContext()));
        textView.setOnLongClickListener(v -> { mostrarMenuOpcoesObjeto(textView); return true; });

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        canvasNotas.addView(textView, params);
    }

    private void mostrarDialogoEditarTexto(TextView textView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Editar Texto");
        final EditText input = new EditText(getContext());
        input.setText(textView.getText().toString());
        builder.setView(input);
        builder.setPositiveButton("Salvar", (dialog, which) -> textView.setText(input.getText().toString()));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void ativarModoDesenho() {
        desenhoView.setVisibility(View.VISIBLE);
        layoutControlesDesenho.setVisibility(View.VISIBLE);
        btnFerramentas.setVisibility(View.GONE);
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void concluirDesenho() {
        Bitmap drawingBitmap = desenhoView.getBitmap();
        Bitmap bitmapCortado = cortarEspacosVazios(drawingBitmap);
        Bitmap finalBitmap = Bitmap.createBitmap(bitmapCortado.getWidth(), bitmapCortado.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalBitmap);
        canvas.drawBitmap(bitmapCortado, 0, 0, null);

        Uri uri = salvarFinalBitmap(finalBitmap);
        if (uri != null) exibirImagemNoCanvas(uri, finalBitmap.getWidth(), finalBitmap.getHeight());

        desenhoView.limpar();
        desenhoView.setVisibility(View.GONE);
        layoutControlesDesenho.setVisibility(View.GONE);
        btnFerramentas.setVisibility(View.VISIBLE);
    }

    private Bitmap cortarEspacosVazios(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int minX = width, minY = height, maxX = -1, maxY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = source.getPixel(x, y);
                if (pixel != Color.TRANSPARENT && pixel != Color.WHITE) {
                    if (x < minX) minX = x; if (x > maxX) maxX = x;
                    if (y < minY) minY = y; if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < minX || maxY < minY) return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        int p = 20;
        int sX = Math.max(0, minX - p), sY = Math.max(0, minY - p);
        int fW = Math.min(width - sX, (maxX - minX) + (p * 2)), fH = Math.min(height - sY, (maxY - minY) + (p * 2));
        return Bitmap.createBitmap(source, sX, sY, fW, fH);
    }

    private Uri salvarFinalBitmap(Bitmap bitmap) {
        try {
            File file = new File(requireContext().getFilesDir(), "desenho_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return Uri.fromFile(file);
        } catch (Exception e) { return null; }
    }

    private void processarImagemGaleria(Uri uri) {
        try {
            File file = new File(requireContext().getFilesDir(), "img_" + System.currentTimeMillis() + ".png");
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is != null) {
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) fos.write(buffer, 0, read);
                fos.close(); is.close();
                if (isAdded()) {
                    Bitmap b = carregarBitmapOtimizado(Uri.fromFile(file), 1000, 1000);
                    if (b != null) exibirImagemNoCanvas(Uri.fromFile(file), b.getWidth(), b.getHeight());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void exibirImagemNoCanvas(Uri fileUri, int w, int h) {
        Bitmap bitmap = carregarBitmapOtimizado(fileUri, 1000, 1000);
        if (bitmap == null) return;

        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setOnTouchListener(new MultiTouchListener(getContext()));
        imageView.setOnLongClickListener(v -> { mostrarMenuOpcoesObjeto(imageView); return true; });
        imageView.setTag(fileUri.getPath());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        canvasNotas.addView(imageView, params);
    }

    private Bitmap carregarBitmapOtimizado(Uri uri, int reqW, int reqH) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            if (is != null) is.close();
            options.inSampleSize = calcularSampleSize(options, reqW, reqH);
            options.inJustDecodeBounds = false;
            is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap res = BitmapFactory.decodeStream(is, null, options);
            if (is != null) is.close();
            return res;
        } catch (Exception e) { return null; }
    }

    private int calcularSampleSize(BitmapFactory.Options options, int reqW, int reqH) {
        int h = options.outHeight, w = options.outWidth, s = 1;
        if (h > reqH || w > reqW) {
            int hH = h / 2, hW = w / 2;
            while ((hH / s) >= reqH && (hW / s) >= reqW) s *= 2;
        }
        return s;
    }

    private void mostrarMenuOpcoesObjeto(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        if (v instanceof TextView) popup.getMenu().add("Editar Texto");
        popup.getMenu().add("Excluir");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Editar Texto")) mostrarDialogoEditarTexto((TextView) v);
            else if (item.getTitle().equals("Excluir")) canvasNotas.removeView(v);
            return true;
        });
        popup.show();
    }

    private void adicionarBotoesDeCores(LinearLayout layout) {
        int[] cores = {Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN};
        for (int cor : cores) {
            View v = new View(getContext());
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(80, 80);
            p.setMargins(8, 0, 8, 0); v.setLayoutParams(p);
            v.setBackgroundColor(cor); v.setOnClickListener(view -> desenhoView.setCor(cor));
            layout.addView(v);
        }
    }

    private void salvarNota() {
        String t = editTitulo.getText().toString().trim();
        if (t.isEmpty()) t = "Sem título";
        String j = serializarCanvas();
        long a = System.currentTimeMillis();
        if (anotacaoExistente != null) {
            anotacaoExistente.titulo = t; anotacaoExistente.conteudoHtml = j; anotacaoExistente.dataUltimaEdicao = a;
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).anotacaoDao().atualizar(anotacaoExistente);
                voltarComFeedback("Caderno atualizado!");
            });
        } else {
            Anotacao n = new Anotacao(t, j, a);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).anotacaoDao().inserir(n);
                voltarComFeedback("Caderno salvo!");
            });
        }
    }

    private String serializarCanvas() {
        try {
            JSONArray array = new JSONArray();
            for (int i = 0; i < canvasNotas.getChildCount(); i++) {
                View child = canvasNotas.getChildAt(i);
                JSONObject obj = new JSONObject();
                if (child instanceof TextView) {
                    obj.put("tipo", "texto"); obj.put("conteudo", ((TextView) child).getText().toString());
                } else if (child instanceof ImageView) {
                    obj.put("tipo", "imagem"); obj.put("conteudo", child.getTag() != null ? child.getTag().toString() : "");
                }
                obj.put("x", child.getTranslationX()); obj.put("y", child.getTranslationY());
                obj.put("scale", child.getScaleX()); obj.put("rotation", child.getRotation());
                obj.put("w", child.getWidth()); obj.put("h", child.getHeight()); // Salva largura e altura reais
                array.put(obj);
            }
            return array.toString();
        } catch (Exception e) { return "[]"; }
    }

    private void carregarObjetosDoJson(String json) {
        if (json == null || json.isEmpty() || json.startsWith("<")) return;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String tipo = obj.getString("tipo"), conteudo = obj.getString("conteudo");
                int w = obj.getInt("w"), h = obj.getInt("h"); // Recupera largura e altura
                View v;
                if (tipo.equals("texto")) {
                    TextView tv = new TextView(getContext());
                    tv.setText(conteudo); tv.setTextSize(24); tv.setTextColor(Color.BLACK);
                    tv.setPadding(20, 20, 20, 20);
                    tv.setOnLongClickListener(view -> { mostrarMenuOpcoesObjeto(tv); return true; });
                    v = tv;
                } else {
                    ImageView iv = new ImageView(getContext());
                    Bitmap b = carregarBitmapOtimizado(Uri.fromFile(new File(conteudo)), 1000, 1000);
                    if (b != null) iv.setImageBitmap(b);
                    iv.setAdjustViewBounds(true); iv.setTag(conteudo);
                    iv.setOnLongClickListener(view -> { mostrarMenuOpcoesObjeto(iv); return true; });
                    v = iv;
                }
                v.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
                v.setOnTouchListener(new MultiTouchListener(getContext()));
                v.setTranslationX((float) obj.getDouble("x")); v.setTranslationY((float) obj.getDouble("y"));
                v.setScaleX((float) obj.getDouble("scale")); v.setScaleY((float) obj.getDouble("scale"));
                v.setRotation((float) obj.getDouble("rotation"));
                canvasNotas.addView(v);
            }
        } catch (Exception e) { e.printStackTrace(); }
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
