package com.example.studyflow;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottom_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottom_menu = findViewById(R.id.botto_menu);
        if (savedInstanceState == null) {

            // 1. Instancia o fragment que você quer que seja a tela inicial (ex: HomeFragment)
            Fragment fragmentHome = new HomeFragment();

            // 2. Inicia o processo de substituição no container
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // 3. Substitui o espaço do container pelo seu fragment padrão
            // (Troque 'R.id.fragment_container' pelo ID real do seu container no XML)
            fragmentTransaction.replace(R.id.fragment_container, fragmentHome);

            // 4. Aplica as mudanças
            fragmentTransaction.commit();
        }
        /*bottom_menu.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_tarefas) {
                selectedFragment = new TarefasFragment(); // fragment de tarefas
            }
            else if (itemId == R.id.nav_metas) {
                selectedFragment = new AnotacoesFragment();   // fragment de metas
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false; */
    }
}

