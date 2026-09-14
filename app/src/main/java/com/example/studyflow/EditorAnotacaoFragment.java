package com.example.studyflow;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.studyflow.data.Anotacao;
import com.example.studyflow.data.AppDatabase;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Tela de edição da Anotação (Caderno Livre).
 * Otimizada para persistência fiel de objetos móveis.
 */
public class EditorAnotacaoFragment extends Fragment {

    private EditText editTitulo;
    private RelativeLayout canvasNotas;
    private View containerEditor;
    private Anotacao anotacaoExistente = null;
    private DesenhoView desenhoView;
    private View layoutOpcoesFerramenta, btnConcluirDesenho, layoutMenusSuperiores;
    private MaterialButton btnRecolherMenus, btnExpandirMenus;
    private View btnFerramentas;
    private boolean modoDesenhoAtivo = false;
    private boolean modoBorrachaAtivo = false;
    private boolean isModoNavegacao = false;
    private boolean isMenusOcultos = false;
    private float lastX, lastY;

    private final ActivityResultLauncher<String> pdfExportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
                if (uri != null) gravarPDF(uri);
            });

    private final ActivityResultLauncher<String> pngExportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("image/png"), uri -> {
                if (uri != null) gravarPNG(uri);
            });

    private final ActivityResultLauncher<String[]> pickFile =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        String type = requireContext().getContentResolver().getType(uri);
                        if (type != null && type.contains("pdf")) {
                            processarPdf(uri);
                        } else {
                            processarImagemGaleria(uri);
                        }
                    } catch (Exception e) {
                        processarImagemGaleria(uri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editor_anotacao, container, false);

        editTitulo = view.findViewById(R.id.editTituloNota);
        canvasNotas = view.findViewById(R.id.canvasNotas);
        canvasNotas.setPivotX(0);
        canvasNotas.setPivotY(0);
        
        SharedPreferences prefs = requireContext().getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        String estiloStr = prefs.getString("notebook_style", "GRID");
        GridDrawable.Style estilo = GridDrawable.Style.valueOf(estiloStr);
        canvasNotas.setBackground(new GridDrawable(estilo));
        
        canvasNotas.setOnTouchListener(null);

        SeekBar zoomBar = view.findViewById(R.id.seekBarZoomCanvas);
        View btnModoNavegacao = view.findViewById(R.id.btnModoNavegacao);

        if (zoomBar != null) {
            zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float oldScale = canvasNotas.getScaleX();
                    float newScale = 0.5f + (progress / 100.0f);
                    
                    View parent = (View) canvasNotas.getParent();
                    if (parent != null && oldScale != newScale) {
                        // Centro da tela (onde o usuário está olhando)
                        float cx = parent.getWidth() / 2f;
                        float cy = parent.getHeight() / 2f;
                        
                        // Ponto exato no "papel" que está no centro da tela antes do zoom
                        float px = (cx - canvasNotas.getTranslationX()) / oldScale;
                        float py = (cy - canvasNotas.getTranslationY()) / oldScale;
                        
                        // Aplica o novo zoom
                        canvasNotas.setScaleX(newScale);
                        canvasNotas.setScaleY(newScale);
                        
                        // Calcula a nova translação para manter o mesmo ponto (px, py) centralizado
                        float newTx = cx - (px * newScale);
                        float newTy = cy - (py * newScale);
                        
                        canvasNotas.setTranslationX(newTx);
                        canvasNotas.setTranslationY(newTy);
                    } else {
                        canvasNotas.setScaleX(newScale);
                        canvasNotas.setScaleY(newScale);
                    }
                    
                    reajustarPosicaoSeNecessario();
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (btnModoNavegacao != null) {
            btnModoNavegacao.setOnClickListener(v -> toggleModoNavegacao());
        }

        containerEditor = view.findViewById(R.id.containerEditor);
        if (containerEditor != null) {
            containerEditor.setOnTouchListener((v, event) -> {
                if (!isModoNavegacao) return false;
                
                float x = event.getRawX();
                float y = event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = x - lastX;
                        float dy = y - lastY;
                        
                        float novoX = canvasNotas.getTranslationX() + dx;
                        float novoY = canvasNotas.getTranslationY() + dy;

                        View parentView = (View) canvasNotas.getParent();
                        if (parentView != null) {
                            float scale = canvasNotas.getScaleX();
                            float larguraReal = canvasNotas.getWidth() * scale;
                            float alturaReal = canvasNotas.getHeight() * scale;

                            // Se a largura real for menor que a tela, centraliza horizontalmente
                            if (larguraReal <= parentView.getWidth()) {
                                novoX = (parentView.getWidth() - larguraReal) / 2f;
                            } else {
                                float minX = parentView.getWidth() - larguraReal;
                                if (novoX > 0) novoX = 0;
                                if (novoX < minX) novoX = minX;
                            }

                            // Se a altura real for menor que a tela, centraliza verticalmente
                            if (alturaReal <= parentView.getHeight()) {
                                novoY = (parentView.getHeight() - alturaReal) / 2f;
                            } else {
                                float minY = parentView.getHeight() - alturaReal;
                                if (novoY > 0) novoY = 0;
                                if (novoY < minY) novoY = minY;
                            }
                        }

                        canvasNotas.setTranslationX(novoX);
                        canvasNotas.setTranslationY(novoY);
                        lastX = x;
                        lastY = y;
                        break;
                }
                return true;
            });
        }

        canvasNotas.post(() -> {
            if (zoomBar != null) zoomBar.setProgress(50);
            
            // Centraliza o canvas 3000x3000 dentro do container
            View parent = (View) canvasNotas.getParent();
            if (parent != null) {
                float centerX = (parent.getWidth() - canvasNotas.getWidth()) / 2f;
                float centerY = (parent.getHeight() - canvasNotas.getHeight()) / 2f;
                canvasNotas.setTranslationX(centerX);
                canvasNotas.setTranslationY(centerY);
            }
        });


        ImageButton btnSalvar = view.findViewById(R.id.btnSalvarNota);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarEditor);
        btnFerramentas = view.findViewById(R.id.btnAbrirFerramentas);
        layoutMenusSuperiores = view.findViewById(R.id.layoutMenusSuperiores);
        btnRecolherMenus = view.findViewById(R.id.btnRecolherMenus);
        btnExpandirMenus = view.findViewById(R.id.btnExpandirMenus);

        if (btnRecolherMenus != null) {
            btnRecolherMenus.setOnClickListener(v -> toggleMenusSuperiores());
        }
        if (btnExpandirMenus != null) {
            btnExpandirMenus.setOnClickListener(v -> toggleMenusSuperiores());
        }

        if (getArguments() != null) {
            anotacaoExistente = (Anotacao) getArguments().getSerializable("anotacao");
            if (anotacaoExistente != null) {
                editTitulo.setText(anotacaoExistente.titulo);
                carregarObjetosDoJson(anotacaoExistente.conteudoHtml);
            }
        }

        btnVoltar.setOnClickListener(v -> voltarOuHome());
        btnSalvar.setOnClickListener(v -> salvarNota());
        
        View btnExportar = view.findViewById(R.id.btnExportarNota);
        if (btnExportar != null) {
            btnExportar.setOnClickListener(v -> iniciarExportacao());
        }

        desenhoView = view.findViewById(R.id.desenhoViewSobreposto);
        layoutOpcoesFerramenta = view.findViewById(R.id.layoutOpcoesFerramenta);
        btnConcluirDesenho = view.findViewById(R.id.btnConcluirDesenho);
        
        ImageButton btnPincel = view.findViewById(R.id.btnPincel);
        ImageButton btnBorracha = view.findViewById(R.id.btnBorracha);
        ImageButton btnDesfazer = view.findViewById(R.id.btnDesfazer);
        ImageButton btnRefazer = view.findViewById(R.id.btnRefazer);

        if (btnPincel != null) {
            btnPincel.setOnClickListener(v -> {
                if (modoDesenhoAtivo && !modoBorrachaAtivo) desativarModoDesenho();
                else ativarModoDesenho(false);
            });
        }

        if (btnBorracha != null) {
            btnBorracha.setOnClickListener(v -> {
                if (modoDesenhoAtivo && modoBorrachaAtivo) desativarModoDesenho();
                else ativarModoDesenho(true);
            });
        }

        if (btnDesfazer != null) btnDesfazer.setOnClickListener(v -> desenhoView.desfazer());
        if (btnRefazer != null) btnRefazer.setOnClickListener(v -> desenhoView.refazer());

        View btnLimpar = view.findViewById(R.id.btnLimparCanvas);
        if (btnLimpar != null) btnLimpar.setOnClickListener(v -> desenhoView.limpar());
        if (btnConcluirDesenho != null) btnConcluirDesenho.setOnClickListener(v -> concluirDesenho());

        SeekBar seekBar = view.findViewById(R.id.seekBarEspessura);
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    desenhoView.setTamanhoPincel(progress);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        View layoutCores = view.findViewById(R.id.layoutCoresDesenho);
        if (layoutCores instanceof LinearLayout) {
            adicionarBotoesDeCores((LinearLayout) layoutCores);
        }
        
        SeekBar rainbowBarDesenho = view.findViewById(R.id.seekBarCorArcoIrisDesenho);
        if (rainbowBarDesenho != null) {
            configurarSeekBarArcoIris(rainbowBarDesenho, cor -> {
                desenhoView.setCor(cor);
                if (!modoDesenhoAtivo || modoBorrachaAtivo) ativarModoDesenho(false);
            });
        }

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
        new MaterialAlertDialogBuilder(requireContext())
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
        if (containerEditor == null || containerEditor.getWidth() <= 0) return;
        
        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            if (os == null) return;
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(containerEditor.getWidth(), containerEditor.getHeight(), 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            containerEditor.draw(page.getCanvas());
            document.finishPage(page);
            document.writeTo(os);
            document.close();
            Toast.makeText(getContext(), "PDF Exportado (Visão Atual)!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erro ao exportar PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void processarPdf(Uri uri) {
        Context context = getContext();
        if (context == null) return;

        final ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setPadding(60, 40, 60, 40);
        
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Importando PDF");
        builder.setMessage("Preparando páginas...");
        builder.setView(progressBar);
        builder.setCancelable(false);
        final AlertDialog progressDialog = builder.create();
        progressDialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (fd == null) {
                    safeDismiss(progressDialog, "Erro ao abrir arquivo.");
                    return;
                }
                
                PdfRenderer renderer = new PdfRenderer(fd);
                int pageCount = renderer.getPageCount();
                
                if (pageCount > 50) {
                    renderer.close(); fd.close();
                    safeDismiss(progressDialog, "O PDF é muito grande (limite de 50 páginas).");
                    return;
                }

                if (getActivity() != null) getActivity().runOnUiThread(() -> progressBar.setMax(pageCount));

                float totalHeight = 0;
                int screenW = context.getResources().getDisplayMetrics().widthPixels;
                int targetWidth = Math.min(screenW, 1000); 

                List<Uri> pageUris = new ArrayList<>();
                List<Integer> pageHeights = new ArrayList<>();

                for (int i = 0; i < pageCount; i++) {
                    final int currentIdx = i;
                    PdfRenderer.Page page = renderer.openPage(i);
                    
                    float scale = (float) targetWidth / page.getWidth();
                    int renderW = targetWidth;
                    int renderH = (int) (page.getHeight() * scale);

                    if (renderH > 3500) renderH = 3500;
                    if (renderH <= 0) renderH = 100;

                    Bitmap bitmap = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    
                    File file = new File(context.getCacheDir(), "p_" + System.currentTimeMillis() + "_" + i + ".jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos); 
                    fos.close();
                    
                    pageUris.add(Uri.fromFile(file));
                    pageHeights.add(renderH);
                    totalHeight += renderH + 30;
                    
                    bitmap.recycle();
                    page.close();
                    
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        progressBar.setProgress(currentIdx + 1);
                        progressDialog.setMessage("Lendo página " + (currentIdx + 1) + "/" + pageCount);
                    });
                }
                renderer.close();
                fd.close();

                final float finalTotalHeight = Math.max(3000f, totalHeight + 1000f);
                final int finalTargetWidth = targetWidth;

                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    if (!isAdded() || canvasNotas == null) {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        return;
                    }

                    canvasNotas.setBackgroundColor(Color.parseColor("#2C2C2C"));

                    ViewGroup.LayoutParams lp = canvasNotas.getLayoutParams();
                    lp.height = (int) finalTotalHeight;
                    lp.width = containerEditor != null ? containerEditor.getWidth() : finalTargetWidth;
                    if (lp.width <= 0) lp.width = finalTargetWidth;
                    canvasNotas.setLayoutParams(lp);

                    float currentY = 50;
                    int canvasW = lp.width;

                    for (int i = 0; i < pageUris.size(); i++) {
                        Bitmap bmp = carregarBitmapOtimizado(pageUris.get(i), finalTargetWidth, pageHeights.get(i));
                        if (bmp != null) {
                            ImageView iv = new ImageView(getContext());
                            iv.setImageBitmap(bmp);
                            iv.setAdjustViewBounds(true);
                            iv.setTag(pageUris.get(i).getPath());
                            
                            int w = bmp.getWidth();
                            int h = bmp.getHeight();
                            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
                            iv.setTranslationX((canvasW - w) / 2f);
                            iv.setTranslationY(currentY);
                            canvasNotas.addView(iv, i, p);
                            currentY += h + 30;
                        }
                    }
                    
                    if (desenhoView != null) desenhoView.bringToFront();
                    
                    canvasNotas.setScaleX(1f);
                    canvasNotas.setScaleY(1f);
                    canvasNotas.setTranslationX(0);
                    canvasNotas.setTranslationY(0);

                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(getContext(), "PDF Pronto!", Toast.LENGTH_SHORT).show();
                    
                    canvasNotas.requestLayout();
                    canvasNotas.invalidate();
                });

            } catch (Exception e) {
                e.printStackTrace();
                safeDismiss(progressDialog, "Erro: Memória cheia ou arquivo inválido.");
            }
        });
    }

    private void safeDismiss(AlertDialog dialog, String msg) {
        if (getActivity() != null) getActivity().runOnUiThread(() -> {
            if (dialog.isShowing()) dialog.dismiss();
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    private void gravarPNG(Uri uri) {
        if (containerEditor == null || containerEditor.getWidth() <= 0 || containerEditor.getHeight() <= 0) {
            Toast.makeText(getContext(), "Área de visão inválida", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Bitmap bitmap = Bitmap.createBitmap(containerEditor.getWidth(), containerEditor.getHeight(), Bitmap.Config.ARGB_8888);
        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            if (os == null) return;
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE); // Fundo branco
            containerEditor.draw(canvas);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            Toast.makeText(getContext(), "Imagem Exportada (Visão Atual)!", Toast.LENGTH_SHORT).show();
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
        popup.getMenu().add("Importar Arquivo (PDF/Imagem)");
        popup.getMenu().add("Inserir Forma");
        popup.getMenu().add("Biblioteca de Stickers");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "Adicionar Texto": mostrarDialogoNovoTexto(); break;
                case "Importar Arquivo (PDF/Imagem)":
                    pickFile.launch(new String[]{"application/pdf", "image/*"});
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
        popup.getMenu().add("Retângulo (Vazado)");
        popup.getMenu().add("Retângulo (Preenchido)");
        popup.getMenu().add("Círculo (Vazado)");
        popup.getMenu().add("Círculo (Preenchido)");
        popup.getMenu().add("Seta");
        
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            ShapeDrawableHelper.ShapeType type = ShapeDrawableHelper.ShapeType.RECTANGLE;
            boolean filled = title.contains("Preenchido");
            
            if (title.contains("Círculo")) type = ShapeDrawableHelper.ShapeType.CIRCLE;
            else if (title.contains("Seta")) {
                type = ShapeDrawableHelper.ShapeType.ARROW;
                filled = true;
            }
            
            final ShapeDrawableHelper.ShapeType finalType = type;
            final boolean finalFilled = filled;
            
            // Abrir diálogo de cor antes de adicionar
            mostrarDialogoCorForma(finalType, finalFilled);
            return true;
        });
        popup.show();
    }

    private void mostrarDialogoCorForma(ShapeDrawableHelper.ShapeType type, boolean filled) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Cor da Forma");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_shape, null);
        SeekBar rainbowBar = dialogView.findViewById(R.id.seekBarCorArcoIrisShape);
        LinearLayout layoutCores = dialogView.findViewById(R.id.layoutCoresShape);
        
        final int[] corSelecionada = {Color.BLACK};
        configurarSeekBarArcoIris(rainbowBar, cor -> corSelecionada[0] = cor);
        adicionarSelecaoDeCores(layoutCores, cor -> corSelecionada[0] = cor);

        builder.setView(dialogView);
        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            adicionarFormaAoCanvas(type, corSelecionada[0], filled);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }


    private void adicionarFormaAoCanvas(ShapeDrawableHelper.ShapeType type, int color, boolean filled) {
        View shapeView = new View(getContext());
        shapeView.setBackground(ShapeDrawableHelper.createShape(type, color, filled));
        
        JSONObject meta = new JSONObject();
        try {
            meta.put("type", type.name());
            meta.put("color", color);
            meta.put("filled", filled);
        } catch (Exception ignored) {}
        shapeView.setTag(meta.toString());
        
        int w = 200, h = 200;
        if (type == ShapeDrawableHelper.ShapeType.RECTANGLE) w = 400;
        else if (type == ShapeDrawableHelper.ShapeType.ARROW) { w = 300; h = 150; }
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        shapeView.setLayoutParams(params);
        shapeView.setOnTouchListener(new MultiTouchListener(getContext()));
        shapeView.setOnLongClickListener(v -> { mostrarMenuOpcoesObjeto(shapeView); return true; });
        
        posicionarNoCentroDaVisao(shapeView);
        canvasNotas.addView(shapeView);
    }

    private void posicionarNoCentroDaVisao(View v) {
        View parent = (View) canvasNotas.getParent();
        if (parent == null) return;
        
        float scale = canvasNotas.getScaleX();
        // Calcula o centro da tela em relação às coordenadas do canvas
        float centroX = (parent.getWidth() / 2f - canvasNotas.getTranslationX()) / scale;
        float centroY = (parent.getHeight() / 2f - canvasNotas.getTranslationY()) / scale;
        
        // Ajusta para o item não ficar com o canto no centro, mas sim o seu próprio meio
        v.post(() -> {
            v.setTranslationX(centroX - v.getWidth() / 2f);
            v.setTranslationY(centroY - v.getHeight() / 2f);
        });
    }


    private void mostrarDialogoStickers() {
        String[] emojis = {"📌", "❓", "✅", "💡", "⭐", "🔥", "📚", "🎯"};
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
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
        
        posicionarNoCentroDaVisao(textView);
        canvasNotas.addView(textView, params);
    }

    private void mostrarDialogoNovoTexto() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Novo Texto");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_text_custom, null);
        EditText input = dialogView.findViewById(R.id.editTextoDialog);
        SeekBar barSize = dialogView.findViewById(R.id.seekBarTamanhoTexto);
        EditText editSize = dialogView.findViewById(R.id.editTamanhoTexto);
        SeekBar rainbowBar = dialogView.findViewById(R.id.seekBarCorArcoIris);
        LinearLayout layoutCores = dialogView.findViewById(R.id.layoutCoresTexto);
        
        final int[] corSelecionada = {Color.BLACK};
        configurarSeekBarArcoIris(rainbowBar, cor -> corSelecionada[0] = cor);
        adicionarSelecaoDeCores(layoutCores, cor -> corSelecionada[0] = cor);

        // Sync SeekBar and EditText for size
        barSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) editSize.setText(String.valueOf(progress + 12));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        editSize.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val >= 12 && val <= 112) barSize.setProgress(val - 12);
                } catch (Exception ignored) {}
            }
        });

        editSize.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val >= 12 && val <= 112) barSize.setProgress(val - 12);
                } catch (Exception ignored) {}
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            String texto = input.getText().toString();
            int tamanho = 24;
            try { tamanho = Integer.parseInt(editSize.getText().toString()); } catch (Exception ignored) {}
            
            if (!texto.isEmpty()) {
                adicionarTextoAoCanvas(texto, corSelecionada[0], tamanho);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void adicionarTextoAoCanvas(String texto, int cor, int tamanho) {
        TextView textView = new TextView(getContext());
        textView.setText(texto);
        textView.setTextSize(tamanho);
        textView.setTextColor(cor);
        textView.setPadding(24, 24, 24, 24);
        
        // Aplica a fonte Quicksand Bold para ficar mais moderno
        try {
            textView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.quicksand_bold));
        } catch (Exception ignored) {}

        textView.setOnTouchListener(new MultiTouchListener(getContext()));
        textView.setOnLongClickListener(v -> { mostrarMenuOpcoesObjeto(textView); return true; });
        
        JSONObject meta = new JSONObject();
        try {
            meta.put("type", "text");
            meta.put("color", cor);
            meta.put("size", tamanho);
        } catch (Exception ignored) {}
        textView.setTag(meta.toString());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        posicionarNoCentroDaVisao(textView);
        canvasNotas.addView(textView, params);
    }

    private void mostrarDialogoEditarTexto(TextView textView) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Editar Texto");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_text_custom, null);
        EditText input = dialogView.findViewById(R.id.editTextoDialog);
        SeekBar barSize = dialogView.findViewById(R.id.seekBarTamanhoTexto);
        EditText editSize = dialogView.findViewById(R.id.editTamanhoTexto);
        SeekBar rainbowBar = dialogView.findViewById(R.id.seekBarCorArcoIris);
        LinearLayout layoutCores = dialogView.findViewById(R.id.layoutCoresTexto);

        input.setText(textView.getText().toString());
        
        // Recupera o tamanho em SP do metadado JSON salvo na Tag
        int currentSizeSp = 24;
        String tag = textView.getTag() != null ? textView.getTag().toString() : "";
        if (tag.startsWith("{")) {
            try {
                JSONObject meta = new JSONObject(tag);
                currentSizeSp = meta.optInt("size", 24);
            } catch (Exception ignored) {}
        }
        
        barSize.setProgress(Math.max(0, currentSizeSp - 12));
        editSize.setText(String.valueOf(currentSizeSp));
        
        final int[] corSelecionada = {textView.getCurrentTextColor()};
        configurarSeekBarArcoIris(rainbowBar, cor -> corSelecionada[0] = cor);
        adicionarSelecaoDeCores(layoutCores, cor -> corSelecionada[0] = cor);

        barSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) editSize.setText(String.valueOf(progress + 12));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        editSize.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val >= 12 && val <= 112) barSize.setProgress(val - 12);
                } catch (Exception ignored) {}
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            int novoTamanho = 24;
            try { novoTamanho = Integer.parseInt(editSize.getText().toString()); } catch (Exception ignored) {}

            textView.setText(input.getText().toString());
            textView.setTextColor(corSelecionada[0]);
            textView.setTextSize(novoTamanho);
            
            JSONObject meta = new JSONObject();
            try {
                meta.put("type", "text");
                meta.put("color", corSelecionada[0]);
                meta.put("size", novoTamanho);
            } catch (Exception ignored) {}
            textView.setTag(meta.toString());
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void adicionarSelecaoDeCores(LinearLayout layout, ColorSelectionListener listener) {
        int[] cores = {
            Color.BLACK, 
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#00BCD4")  // Cyan
        };
        for (int cor : cores) {
            MaterialCardView card = new MaterialCardView(getContext());
            int size = (int) (40 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(size, size);
            p.setMargins(12, 12, 12, 12); 
            card.setLayoutParams(p);
            card.setCardBackgroundColor(cor);
            card.setRadius(size / 2f);
            card.setStrokeWidth(0);
            card.setClickable(true);
            card.setFocusable(true);
            card.setOnClickListener(view -> listener.onColorSelected(cor));
            layout.addView(card);
        }
    }

    private void configurarSeekBarArcoIris(SeekBar seekBar, ColorSelectionListener listener) {
        if (seekBar == null) return;
        int[] rainbowColors = {
            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
        };
        GradientDrawable gradient = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, rainbowColors
        );
        seekBar.setBackground(gradient);
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float[] hsv = {progress, 1f, 1f};
                int color = Color.HSVToColor(hsv);
                listener.onColorSelected(color);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    interface ColorSelectionListener {
        void onColorSelected(int color);
    }


    private void toggleMenusSuperiores() {
        isMenusOcultos = !isMenusOcultos;
        if (layoutMenusSuperiores != null) {
            layoutMenusSuperiores.setVisibility(isMenusOcultos ? View.GONE : View.VISIBLE);
        }
        
        if (btnRecolherMenus != null) {
            btnRecolherMenus.setVisibility(isMenusOcultos ? View.GONE : View.VISIBLE);
        }
        if (btnExpandirMenus != null) {
            btnExpandirMenus.setVisibility(isMenusOcultos ? View.VISIBLE : View.GONE);
        }
    }

    private void toggleModoNavegacao() {
        isModoNavegacao = !isModoNavegacao;
        
        View view = getView();
        if (view == null) return;
        
        MaterialButton btn = view.findViewById(R.id.btnModoNavegacao);
        if (btn == null) return;

        if (isModoNavegacao) {
            try {
                btn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary)));
            } catch (Exception ignored) {}
            btn.setBackgroundColor(Color.LTGRAY);
            Snackbar.make(getView(), "Modo Navegação Ativo (Arraste a tela)", Snackbar.LENGTH_SHORT).show();
            desativarModoDesenho();
        } else {
            btn.setIconTint(ColorStateList.valueOf(Color.GRAY));
            btn.setBackgroundColor(Color.TRANSPARENT);
        }
        
        // Bloqueia/Desbloqueia objetos
        for (int i = 0; i < canvasNotas.getChildCount(); i++) {
            View child = canvasNotas.getChildAt(i);
            child.setEnabled(!isModoNavegacao);
        }
        
        // Só mostra o DesenhoView se NÃO estiver navegando E se o modo desenho estiver ativo
        if (desenhoView != null) {
            desenhoView.setVisibility((!isModoNavegacao && modoDesenhoAtivo) ? View.VISIBLE : View.GONE);
        }
    }

    private void ativarModoDesenho(boolean isBorracha) {
        modoDesenhoAtivo = true;
        modoBorrachaAtivo = isBorracha;
        
        if (desenhoView != null) {
            desenhoView.setVisibility(View.VISIBLE);
            desenhoView.setDrawingEnabled(true);
            desenhoView.setBorracha(isBorracha);
            desenhoView.bringToFront(); // Garante que fique acima do PDF
        }
        
        if (layoutMenusSuperiores != null) layoutMenusSuperiores.setVisibility(View.GONE);
        if (btnRecolherMenus != null) btnRecolherMenus.setVisibility(View.GONE);
        if (btnExpandirMenus != null) btnExpandirMenus.setVisibility(View.GONE);

        if (layoutOpcoesFerramenta != null) layoutOpcoesFerramenta.setVisibility(View.VISIBLE);
        if (btnConcluirDesenho != null) btnConcluirDesenho.setVisibility(View.VISIBLE);
        atualizarHighlightBotoes();
        esconderTeclado();
    }

    private void desativarModoDesenho() {
        modoDesenhoAtivo = false;
        if (desenhoView != null) {
            desenhoView.setDrawingEnabled(false);
            desenhoView.setVisibility(View.GONE);
        }
        
        // Restaura barras superiores conforme o estado do toggle
        if (layoutMenusSuperiores != null) {
            layoutMenusSuperiores.setVisibility(isMenusOcultos ? View.GONE : View.VISIBLE);
        }
        if (btnRecolherMenus != null) {
            btnRecolherMenus.setVisibility(isMenusOcultos ? View.GONE : View.VISIBLE);
        }
        if (btnExpandirMenus != null) {
            btnExpandirMenus.setVisibility(isMenusOcultos ? View.VISIBLE : View.GONE);
        }

        if (layoutOpcoesFerramenta != null) layoutOpcoesFerramenta.setVisibility(View.GONE);
        if (btnConcluirDesenho != null) btnConcluirDesenho.setVisibility(View.GONE);
        atualizarHighlightBotoes();
    }

    private void reajustarPosicaoSeNecessario() {
        float x = canvasNotas.getTranslationX();
        float y = canvasNotas.getTranslationY();
        float scale = canvasNotas.getScaleX();
        
        View parent = (View) canvasNotas.getParent();
        if (parent == null) return;

        float larguraReal = canvasNotas.getWidth() * scale;
        float alturaReal = canvasNotas.getHeight() * scale;

        // Centraliza se for menor que a tela, senão trava nas bordas
        if (larguraReal <= parent.getWidth()) {
            x = (parent.getWidth() - larguraReal) / 2f;
        } else {
            float minX = parent.getWidth() - larguraReal;
            if (x > 0) x = 0;
            if (x < minX) x = minX;
        }

        if (alturaReal <= parent.getHeight()) {
            y = (parent.getHeight() - alturaReal) / 2f;
        } else {
            float minY = parent.getHeight() - alturaReal;
            if (y > 0) y = 0;
            if (y < minY) y = minY;
        }

        canvasNotas.setTranslationX(x);
        canvasNotas.setTranslationY(y);
    }

    private void atualizarHighlightBotoes() {
        View root = getView();
        if (root == null) return;
        ImageButton btnPincel = root.findViewById(R.id.btnPincel);
        ImageButton btnBorracha = root.findViewById(R.id.btnBorracha);
        if (btnPincel == null || btnBorracha == null) return;

        int corPadraoSetas = Color.GRAY;
        TypedValue typedValue = new TypedValue();
        if (requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)) {
            corPadraoSetas = typedValue.data;
        }

        if (modoDesenhoAtivo) {
            if (modoBorrachaAtivo) {
                btnBorracha.setImageTintList(ColorStateList.valueOf(Color.RED));
                btnPincel.setImageTintList(ColorStateList.valueOf(corPadraoSetas));
            } else {
                btnPincel.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary)));
                btnBorracha.setImageTintList(ColorStateList.valueOf(corPadraoSetas));
            }
        } else {
            btnPincel.setImageTintList(ColorStateList.valueOf(corPadraoSetas));
            btnBorracha.setImageTintList(ColorStateList.valueOf(corPadraoSetas));
        }
        btnPincel.setBackgroundTintList(null);
        btnBorracha.setBackgroundTintList(null);
    }

    private static class CropResult {
        Bitmap bitmap;
        int x, y;
        CropResult(Bitmap b, int x, int y) { this.bitmap = b; this.x = x; this.y = y; }
    }

    private CropResult cortarEspacosVaziosComCoordenadas(Bitmap source) {
        int width = source.getWidth(), height = source.getHeight();
        int minX = width, minY = height, maxX = -1, maxY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = source.getPixel(x, y);
                if (pixel != Color.TRANSPARENT && pixel != 0) {
                    if (x < minX) minX = x; if (x > maxX) maxX = x;
                    if (y < minY) minY = y; if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < minX || maxY < minY) return new CropResult(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888), 0, 0);
        
        int p = 10;
        int sX = Math.max(0, minX - p), sY = Math.max(0, minY - p);
        int fW = Math.min(width - sX, (maxX - minX) + (p * 2)), fH = Math.min(height - sY, (maxY - minY) + (p * 2));
        
        return new CropResult(Bitmap.createBitmap(source, sX, sY, fW, fH), sX, sY);
    }

    private void concluirDesenho() {
        if (desenhoView == null) return;
        Bitmap drawingBitmap = desenhoView.getBitmap();
        if (drawingBitmap == null) return;

        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Finalizando rabisco...");
        pd.setCancelable(false);
        pd.show();

        // Salva estados de visão ANTES de processar (para precisão)
        final float currentScale = canvasNotas.getScaleX();
        final float transX = canvasNotas.getTranslationX();
        final float transY = canvasNotas.getTranslationY();

        Executors.newSingleThreadExecutor().execute(() -> {
            CropResult result = cortarEspacosVaziosComCoordenadas(drawingBitmap);
            
            requireActivity().runOnUiThread(() -> {
                Uri uri = salvarFinalBitmap(result.bitmap);
                if (uri != null) {
                    // CONVERSÃO DE COORDENADAS: Mapeia o desenho da tela para o local correto no papel com zoom
                    float papelX = (result.x - transX) / currentScale;
                    float papelY = (result.y - transY) / currentScale;
                    float papelW = result.bitmap.getWidth() / currentScale;
                    float papelH = result.bitmap.getHeight() / currentScale;

                    exibirDesenhoNoCanvas(uri, (int) papelW, (int) papelH, papelX, papelY);
                }
                desenhoView.limpar();
                desativarModoDesenho();
                pd.dismiss();
            });
        });
    }

    private void exibirDesenhoNoCanvas(Uri uri, int w, int h, float x, float y) {
        ImageView iv = new ImageView(getContext());
        iv.setImageBitmap(carregarBitmapOtimizado(uri, w, h));
        iv.setAdjustViewBounds(true);
        iv.setOnTouchListener(new MultiTouchListener(getContext()));
        iv.setOnLongClickListener(v -> { mostrarMenuOpcoesObjeto(iv); return true; });
        iv.setTag(uri.getPath());
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        iv.setTranslationX(x);
        iv.setTranslationY(y);
        
        canvasNotas.addView(iv, params);
        iv.bringToFront();
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
        exibirImagemNoCanvas(fileUri, w, h, -1, -1, -1);
    }

    private void exibirImagemNoCanvas(Uri fileUri, int w, int h, float startX, float startY, int index) {
        Bitmap bitmap = carregarBitmapOtimizado(fileUri, 1000, 1000);
        if (bitmap == null) return;
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setOnTouchListener(new MultiTouchListener(getContext()));
        imageView.setOnLongClickListener(v -> { mostrarMenuOpcoesObjeto(imageView); return true; });
        imageView.setTag(fileUri.getPath());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);

        if (startX >= 0 && startY >= 0) {
            imageView.setTranslationX(startX);
            imageView.setTranslationY(startY);
        } else {
            posicionarNoCentroDaVisao(imageView);
        }
        
        if (index >= 0) {
            canvasNotas.addView(imageView, index, params);
        } else {
            canvasNotas.addView(imageView, params);
        }
    }

    private Bitmap carregarBitmapOtimizado(Uri uri, int reqW, int reqH) {
        try {
            if ("file".equals(uri.getScheme())) {
                String path = uri.getPath();
                if (path == null) return null;
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, opt);
                opt.inSampleSize = calcularSampleSize(opt, reqW, reqH);
                opt.inJustDecodeBounds = false;
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                return BitmapFactory.decodeFile(path, opt);
            }

            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            if (is != null) is.close();
            options.inSampleSize = calcularSampleSize(options, reqW, reqH);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
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
        if (v instanceof TextView && !(v.getTag() != null && v.getTag().toString().startsWith("sticker:"))) {
            popup.getMenu().add("Editar Texto");
        } else if (!(v instanceof ImageView) && !(v instanceof TextView)) {
            popup.getMenu().add("Trocar Cor");
        }
        popup.getMenu().add("Trazer para Frente");

        popup.getMenu().add("Enviar para Trás");
        popup.getMenu().add("Excluir");
        popup.setOnMenuItemClickListener(item -> {
            String t = item.getTitle().toString();
            if (t.equals("Editar Texto")) mostrarDialogoEditarTexto((TextView) v);
            else if (t.equals("Trocar Cor")) mostrarDialogoTrocarCorForma(v);
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

    private void mostrarDialogoTrocarCorForma(View shapeView) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Trocar Cor da Forma");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_shape, null);
        SeekBar rainbowBar = dialogView.findViewById(R.id.seekBarCorArcoIrisShape);
        LinearLayout layoutCores = dialogView.findViewById(R.id.layoutCoresShape);
        
        final int[] corSelecionada = {Color.BLACK};
        configurarSeekBarArcoIris(rainbowBar, cor -> corSelecionada[0] = cor);
        adicionarSelecaoDeCores(layoutCores, cor -> corSelecionada[0] = cor);

        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String tag = shapeView.getTag() != null ? shapeView.getTag().toString() : "";
            if (tag.startsWith("{")) {
                try {
                    JSONObject meta = new JSONObject(tag);
                    ShapeDrawableHelper.ShapeType type = ShapeDrawableHelper.ShapeType.valueOf(meta.getString("type"));
                    boolean filled = meta.getBoolean("filled");
                    
                    shapeView.setBackground(ShapeDrawableHelper.createShape(type, corSelecionada[0], filled));
                    
                    meta.put("color", corSelecionada[0]);
                    shapeView.setTag(meta.toString());
                } catch (Exception ignored) {}
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
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
        esconderTeclado();
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
            JSONObject root = new JSONObject();
            root.put("canvasW", canvasNotas.getWidth());
            root.put("canvasH", canvasNotas.getHeight());
            
            JSONArray array = new JSONArray();
            for (int i = 0; i < canvasNotas.getChildCount(); i++) {
                View child = canvasNotas.getChildAt(i);
                if (child == desenhoView) continue;
                
                JSONObject obj = new JSONObject();
                String tag = child.getTag() != null ? child.getTag().toString() : "";
                
                if (child instanceof TextView) {
                    if (tag.startsWith("sticker:")) {
                        obj.put("tipo", "sticker");
                        obj.put("conteudo", tag.substring(8));
                    } else {
                        obj.put("tipo", "texto"); 
                        obj.put("conteudo", ((TextView) child).getText().toString());
                        if (tag.startsWith("{")) {
                            JSONObject meta = new JSONObject(tag);
                            obj.put("color", meta.optInt("color", Color.BLACK));
                            obj.put("size", meta.optInt("size", 24));
                        }
                    }
                } else if (child instanceof ImageView) {
                    obj.put("tipo", "imagem"); 
                    obj.put("conteudo", tag);
                } else {
                    obj.put("tipo", "forma");
                    if (tag.startsWith("{")) {
                        JSONObject meta = new JSONObject(tag);
                        obj.put("conteudo", meta.optString("type", "RECTANGLE"));
                        obj.put("color", meta.optInt("color", Color.BLACK));
                        obj.put("filled", meta.optBoolean("filled", false));
                    } else {
                        obj.put("conteudo", tag.isEmpty() ? "RECTANGLE" : tag);
                    }
                }
                obj.put("x", child.getTranslationX()); obj.put("y", child.getTranslationY());
                obj.put("scale", child.getScaleX()); obj.put("rotation", child.getRotation());
                obj.put("w", child.getWidth()); obj.put("h", child.getHeight());
                array.put(obj);
            }
            root.put("objetos", array);
            return root.toString();
        } catch (Exception e) { return "[]"; }
    }

    private void carregarObjetosDoJson(String json) {
        if (json == null || json.isEmpty() || json.startsWith("<")) return;
        try {
            JSONArray array;
            if (json.startsWith("{")) {
                JSONObject root = new JSONObject(json);
                int savedW = root.optInt("canvasW", 3000);
                int savedH = root.optInt("canvasH", 3000);
                
                ViewGroup.LayoutParams lp = canvasNotas.getLayoutParams();
                lp.width = savedW;
                lp.height = savedH;
                canvasNotas.setLayoutParams(lp);
                
                array = root.getJSONArray("objetos");
            } else {
                array = new JSONArray(json);
            }

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String tipo = obj.getString("tipo"), conteudo = obj.getString("conteudo");
                int w = obj.getInt("w"), h = obj.getInt("h");
                View v;
                if (tipo.equals("texto")) {
                    TextView tv = new TextView(getContext());
                    tv.setText(conteudo); 
                    int size = obj.optInt("size", 24);
                    int color = obj.optInt("color", Color.BLACK);
                    tv.setTextSize(size); 
                    tv.setTextColor(color);
                    tv.setPadding(20, 20, 20, 20);
                    
                    JSONObject meta = new JSONObject();
                    meta.put("type", "text"); meta.put("color", color); meta.put("size", size);
                    tv.setTag(meta.toString());
                    v = tv;
                } else if (tipo.equals("sticker")) {
                    TextView tv = new TextView(getContext());
                    tv.setText(conteudo); tv.setTextSize(50); tv.setTag("sticker:" + conteudo);
                    v = tv;
                } else if (tipo.equals("forma")) {
                    v = new View(getContext());
                    int color = obj.optInt("color", Color.BLACK);
                    boolean filled = obj.optBoolean("filled", false);
                    try {
                        v.setBackground(ShapeDrawableHelper.createShape(ShapeDrawableHelper.ShapeType.valueOf(conteudo), color, filled));
                    } catch (Exception e) {
                        v.setBackground(ShapeDrawableHelper.createShape(ShapeDrawableHelper.ShapeType.RECTANGLE, color, filled));
                    }
                    
                    JSONObject meta = new JSONObject();
                    meta.put("type", conteudo); meta.put("color", color); meta.put("filled", filled);
                    v.setTag(meta.toString());
                } else {
                    ImageView iv = new ImageView(getContext());
                    Bitmap b = carregarBitmapOtimizado(Uri.fromFile(new File(conteudo)), w, h);
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
            
            if (desenhoView != null) desenhoView.bringToFront();
            
        } catch (Exception e) { e.printStackTrace(); }
    }


    private void voltarOuHome() {
        esconderTeclado();
        if (getParentFragmentManager().getBackStackEntryCount() > 0) getParentFragmentManager().popBackStack();
        else getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    private void esconderTeclado() {
        View view = getActivity() != null ? getActivity().getCurrentFocus() : null;
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void voltarComFeedback(String msg) {
        if (getActivity() != null) getActivity().runOnUiThread(() -> {
            Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                msg,
                Snackbar.LENGTH_SHORT
            ).show();
            voltarOuHome();
        });
    }
}
