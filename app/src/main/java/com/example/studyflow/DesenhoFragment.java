package com.example.studyflow;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.File;
import java.io.FileOutputStream;

public class DesenhoFragment extends Fragment {

    public interface OnDesenhoFinalizadoListener {
        void onDesenhoPronto(Uri uri);
    }

    private OnDesenhoFinalizadoListener listener;

    public void setOnDesenhoFinalizadoListener(OnDesenhoFinalizadoListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_desenho, container, false);

        DesenhoView desenhoView = view.findViewById(R.id.desenhoView);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbarDesenho);
        
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnLimparDesenho).setOnClickListener(v -> desenhoView.limpar());

        view.findViewById(R.id.btnConfirmarDesenho).setOnClickListener(v -> {
            Bitmap bitmap = desenhoView.getBitmap();
            Uri uri = salvarBitmapComoArquivo(bitmap);
            if (listener != null) {
                listener.onDesenhoPronto(uri);
            }
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private Uri salvarBitmapComoArquivo(Bitmap bitmap) {
        try {
            String nome = "desenho_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), nome);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
