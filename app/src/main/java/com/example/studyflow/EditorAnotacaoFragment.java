package com.example.studyflow;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.io.OutputStream;
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
    private View layoutOpcoesFerramenta, btnConcluirDesenho;
    private View btnFerramentas;
    private boolean modoDesenhoAtivo = false;
    private boolean modoBorrachaAtivo = false;

    private final ActivityResultLauncher<String> pdfExportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
                if (uri != null) gravarPDF(uri);
            });

    private final ActivityResultLauncher<String> pngExportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("image/png"), uri -> {
                if (uri != null) gravarPNG(uri);
            });

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
        
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("StudyFlowPrefs", android.content.Context.MODE_PRIVATE);
        String estiloStr = prefs.getString("notebook_style", "GRID");
        GridDrawable.Style estilo = GridDrawable.Style.valueOf(estiloStr);
        canvasNotas.setBackground(new GridDrawable(estilo));
        
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

        btnVoltar.setOnClickListener(v -> voltarOuHome());
        btnSalvar.setOnClickListener(v -> salvarNota());
        view.findViewById(R.id.btnExportarNota).setOnClickListener(v -> iniciarExportacao());

        desenhoView = view.findViewById(R.id.desenhoViewSobreposto);
        layoutOpcoesFerramenta = view.findViewById(R.id.layoutOpcoesFerramenta);
        btnConcluirDesenho = view.findViewById(R.id.btnConcluirDesenho);
        
        ImageButton btnPincel = view.findViewById(R.id.btnPincel);
        ImageButton btnBorracha = view.findViewById(R.id.btnBorracha);
        ImageButton btnDesfazer = view.findViewById(R.id.btnDesfazer);
        ImageButton btnRefazer = view.findViewById(R.id.btnRefazer);

        btnPincel.setOnClickListener(v -> {
            if (modoDesenhoAtivo && !modoBorrachaAtivo) desativarModoDesenho();
            else ativarModoDesenho(false);
        });

        btnBorracha.setOnClickListener(v -> {
            if (modoDesenhoAtivo && modoBorrachaAtivo) desativarModoDesenho();
            else ativarModoDesenho(true);
        });

        btnDesfazer.setOnClickListener(v -> desenhoView.desfazer());
        btnRefazer.setOnClickListener(v -> desenhoView.refazer());

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

        desativarModoDesenho();

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

    private void iniciarExportacao() {
        String[] opcoes = {"Exportar como PDF", "Exportar como PNG"};
        new AlertDialog.Builder(getContext())
                .setTitle("Escolha o formato")
                .setItems(opcoes, (dialog, which) -> {
                    String nomeSugerido = "Anotacao_" + System.currentTimeMillis();
                    if (which == 0) {
                        pdfExportLauncher.launch(nomeSugerido + ".pdf");
                    } else {
                        pngExportLauncher.launch(nomeSugerido + ".png");
                    }
                })
                .show();
    }

    private void gravarPDF(Uri uri) {
        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            if (os == null) return;
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(canvasNotas.getWidth(), canvasNotas.getHeight(), 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            canvasNotas.draw(page.getCanvas());
            document.finishPage(page);
            document.writeTo(os);
            document.close();
            Toast.makeText(getContext(), "PDF Exportado!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erro ao exportar PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void gravarPNG(Uri uri) {
        if (canvasNotas.getWidth() <= 0 || canvasNotas.getHeight() <= 0) {
            Toast.makeText(getContext(), "Área de desenho vazia", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = Bitmap.createBitmap(canvasNotas.getWidth(), canvasNotas.getHeight(), Bitmap.Config.ARGB_8888);
        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            if (os == null) return;
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE); // Fundo branco
            canvasNotas.draw(canvas);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            Toast.makeText(getContext(), "Imagem Exportada!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erro ao exportar PNG", Toast.LENGTH_SHORT).show();
        } finally {
            bitmap.recycle();
        }
    }

    private void mostrarMenuFerramentas(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add("Adicionar Texto");
        popup.getMenu().add("Inserir Imagem");
        popup.getMenu().add("Inserir Forma");
        popup.getMenu().add("Biblioteca de Stickers");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "Adicionar Texto": mostrarDialogoNovoTexto(); break;
                case "Inserir Imagem": 
                    pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
                    break;
                case "Inserir Forma": mostrarMenuFormas(v); break;
                case "Biblioteca de Stickers": mostrarDialogoStickers(); break;
            }
            return true;
        });
        popup.show();
    }

    private void mostrarMenuFormas(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add("Retângulo");
        popup.getMenu().add("Círculo");
        popup.getMenu().add("Quadrado");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            ShapeDrawableHelper.ShapeType type = ShapeDrawableHelper.ShapeType.RECTANGLE;
            if (title.equals("Círculo")) type = ShapeDrawableHelper.ShapeType.CIRCLE;
            else if (title.equals("Quadrado")) type = ShapeDrawableHelper.ShapeType.SQUARE;
            adicionarFormaAoCanvas(type);
            return true;
        });
        popup.show();
    }

    private void adicionarFormaAoCanvas(ShapeDrawableHelper.ShapeType type) {
        View shapeView = new View(getContext());
        shapeView.setBackground(ShapeDrawableHelper.createShape(type, Color.BLACK));
        shapeView.setTag(type.name());
        int w = 200, h = 200;
        if (type == ShapeDrawableHelper.ShapeType.RECTANGLE) w = 400;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        shapeView.setLayoutParams(params);
        shapeView.setOnTouchListener(new MultiTouchListener(getContext()));
        shapeView.setOnLongClickListener(v -> { mostrarMenuOpcoesObjeto(shapeView); return true; });
        canvasNotas.addView(shapeView);
    }

    private void mostrarDialogoStickers() {
        String[] emojis = {"📌", "❓", "✅", "💡", "⭐", "🔥", "📚", "🎯"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Escolha um Sticker");
        builder.setItems(emojis, (dialog, which) -> adicionarStickerAoCanvas(emojis[which]));
        builder.show();
    }

    private void adicionarStickerAoCanvas(String emoji) {
        TextView textView = new TextView(getContext());
        textView.setText(emoji);
        textView.setTextSize(50);
        textView.setTag("sticker:" + emoji);
        textView.setOnTouchListener(new MultiTouchListener(getContext()));
        textView.setOnLongClickListener(v -> { mostrarMenuOpcoesObjeto(textView); return true; });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        canvasNotas.addView(textView, params);
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
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

    private void ativarModoDesenho(boolean isBorracha) {
        modoDesenhoAtivo = true;
        modoBorrachaAtivo = isBorracha;
        desenhoView.setDrawingEnabled(true);
        desenhoView.setBorracha(isBorracha);
        layoutOpcoesFerramenta.setVisibility(View.VISIBLE);
        btnConcluirDesenho.setVisibility(View.VISIBLE);
        atualizarHighlightBotoes();
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void desativarModoDesenho() {
        modoDesenhoAtivo = false;
        desenhoView.setDrawingEnabled(false);
        layoutOpcoesFerramenta.setVisibility(View.GONE);
        btnConcluirDesenho.setVisibility(View.GONE);
        atualizarHighlightBotoes();
    }

    private void atualizarHighlightBotoes() {
        View root = getView();
        if (root == null) return;
        ImageButton btnPincel = root.findViewById(R.id.btnPincel);
        ImageButton btnBorracha = root.findViewById(R.id.btnBorracha);
        if (btnPincel == null || btnBorracha == null) return;

        int corPadraoSetas = Color.GRAY;
        android.util.TypedValue typedValue = new android.util.TypedValue();
        if (requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)) {
            corPadraoSetas = typedValue.data;
        }

        if (modoDesenhoAtivo) {
            if (modoBorrachaAtivo) {
                btnBorracha.setImageTintList(android.content.res.ColorStateList.valueOf(Color.RED));
                btnPincel.setImageTintList(android.content.res.ColorStateList.valueOf(corPadraoSetas));
            } else {
                btnPincel.setImageTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary)));
                btnBorracha.setImageTintList(android.content.res.ColorStateList.valueOf(corPadraoSetas));
            }
        } else {
            btnPincel.setImageTintList(android.content.res.ColorStateList.valueOf(corPadraoSetas));
            btnBorracha.setImageTintList(android.content.res.ColorStateList.valueOf(corPadraoSetas));
        }
        btnPincel.setBackgroundTintList(null);
        btnBorracha.setBackgroundTintList(null);
    }

    private void concluirDesenho() {
        Bitmap drawingBitmap = desenhoView.getBitmap();
        Bitmap bitmapCortado = cortarEspacosVazios(drawingBitmap);
        Bitmap finalBitmap = Bitmap.createBitmap(bitmapCortado.getWidth(), bitmapCortado.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(finalBitmap).drawBitmap(bitmapCortado, 0, 0, null);
        Uri uri = salvarFinalBitmap(finalBitmap);
        if (uri != null) exibirImagemNoCanvas(uri, finalBitmap.getWidth(), finalBitmap.getHeight());
        desenhoView.limpar();
        desativarModoDesenho();
    }

    private Bitmap cortarEspacosVazios(Bitmap source) {
        int width = source.getWidth(), height = source.getHeight();
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
        if (v instanceof TextView && !(v.getTag() != null && v.getTag().toString().startsWith("sticker:"))) popup.getMenu().add("Editar Texto");
        popup.getMenu().add("Trazer para Frente");
        popup.getMenu().add("Enviar para Trás");
        popup.getMenu().add("Excluir");
        popup.setOnMenuItemClickListener(item -> {
            String t = item.getTitle().toString();
            if (t.equals("Editar Texto")) mostrarDialogoEditarTexto((TextView) v);
            else if (t.equals("Excluir")) canvasNotas.removeView(v);
            else if (t.equals("Trazer para Frente")) v.bringToFront();
            else if (t.equals("Enviar para Trás")) {
                canvasNotas.removeView(v);
                canvasNotas.addView(v, 0);
            }
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
            v.setBackgroundColor(cor); 
            v.setOnClickListener(view -> {
                desenhoView.setCor(cor);
                if (!modoDesenhoAtivo || modoBorrachaAtivo) ativarModoDesenho(false);
            });
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
                String tag = child.getTag() != null ? child.getTag().toString() : "";
                if (child instanceof TextView) {
                    if (tag.startsWith("sticker:")) {
                        obj.put("tipo", "sticker");
                        obj.put("conteudo", tag.substring(8));
                    } else {
                        obj.put("tipo", "texto"); 
                        obj.put("conteudo", ((TextView) child).getText().toString());
                    }
                } else if (child instanceof ImageView) {
                    obj.put("tipo", "imagem"); 
                    obj.put("conteudo", tag);
                } else {
                    obj.put("tipo", "forma");
                    obj.put("conteudo", tag.isEmpty() ? "RECTANGLE" : tag);
                }
                obj.put("x", child.getTranslationX()); obj.put("y", child.getTranslationY());
                obj.put("scale", child.getScaleX()); obj.put("rotation", child.getRotation());
                obj.put("w", child.getWidth()); obj.put("h", child.getHeight());
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
                int w = obj.getInt("w"), h = obj.getInt("h");
                View v;
                if (tipo.equals("texto")) {
                    TextView tv = new TextView(getContext());
                    tv.setText(conteudo); tv.setTextSize(24); tv.setTextColor(Color.BLACK);
                    tv.setPadding(20, 20, 20, 20);
                    v = tv;
                } else if (tipo.equals("sticker")) {
                    TextView tv = new TextView(getContext());
                    tv.setText(conteudo); tv.setTextSize(50); tv.setTag("sticker:" + conteudo);
                    v = tv;
                } else if (tipo.equals("forma")) {
                    v = new View(getContext());
                    v.setBackground(ShapeDrawableHelper.createShape(ShapeDrawableHelper.ShapeType.valueOf(conteudo), Color.BLACK));
                    v.setTag(conteudo);
                } else {
                    ImageView iv = new ImageView(getContext());
                    Bitmap b = carregarBitmapOtimizado(Uri.fromFile(new File(conteudo)), 1000, 1000);
                    if (b != null) iv.setImageBitmap(b);
                    iv.setAdjustViewBounds(true); iv.setTag(conteudo);
                    v = iv;
                }
                v.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
                v.setOnTouchListener(new MultiTouchListener(getContext()));
                v.setOnLongClickListener(view -> { mostrarMenuOpcoesObjeto(v); return true; });
                v.setTranslationX((float) obj.getDouble("x")); v.setTranslationY((float) obj.getDouble("y"));
                v.setScaleX((float) obj.getDouble("scale")); v.setScaleY((float) obj.getDouble("scale"));
                v.setRotation((float) obj.getDouble("rotation"));
                canvasNotas.addView(v);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void voltarOuHome() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) getParentFragmentManager().popBackStack();
        else getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    private void voltarComFeedback(String msg) {
        if (getActivity() != null) getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            voltarOuHome();
        });
    }
}
