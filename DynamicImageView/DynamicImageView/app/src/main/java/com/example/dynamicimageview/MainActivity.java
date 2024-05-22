package com.example.dynamicimageview;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    int numRows = 8;
    int numColumns = 8;
    HashMap<String, ImageView> images;
    SharedPreferences sharedPreferences;
    int currentFoxRow = 8;
    int currentFoxCol;
    boolean isFoxTurn = true;
    int selectedRow = -1;
    int selectedCol = -1;
    String selectedType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        images = new HashMap<>();
        LinearLayout llmain = findViewById(R.id.lvmain);
        sharedPreferences = getSharedPreferences("FoxPositionPrefs", MODE_PRIVATE);

        int lastFoxPosition = sharedPreferences.getInt("lastFoxPosition", 0);

        int[] foxPositions = {1, 3, 5, 7};
        currentFoxCol = foxPositions[lastFoxPosition % foxPositions.length];

        boolean lastGameWasFoxTurn = sharedPreferences.getBoolean("lastGameWasFoxTurn", true);
        isFoxTurn = !lastGameWasFoxTurn;

        String firstPlayer = isFoxTurn ? "Fox" : "Geese";
        Toast.makeText(this, "Prvi igra: " + firstPlayer, Toast.LENGTH_SHORT).show();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("lastGameWasFoxTurn", isFoxTurn);
        editor.putInt("lastFoxPosition", lastFoxPosition + 1);
        editor.apply();

        for (int row = 1; row <= numRows; row++) {
            LinearLayout llrow = new LinearLayout(this);
            llrow.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 1; col <= numColumns; col++) {
                ImageView iv = new ImageView(this);
                iv.setTag(row + "," + col);
                images.put(row + "," + col, iv);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 130);
                layoutParams.weight = 1;
                iv.setLayoutParams(layoutParams);

                // Postavljanje osnovnih boja polja
                if ((row + col) % 2 == 0) {
                    iv.setImageResource(R.drawable.bela);
                } else {
                    iv.setImageResource(R.drawable.plava);
                }

                // Postavljanje figura igrača A (lisica) na dnu table
                if (row == currentFoxRow && col == currentFoxCol) {
                    iv.setImageResource(R.drawable.plava_fox);
                }

                // Postavljanje figura igrača B (guske) na vrhu table
                if (row == 1 && (row + col) % 2 != 0) {
                    iv.setImageResource(R.drawable.plava_geese);
                }

                final int finalRow = row;
                final int finalCol = col;
                iv.setOnClickListener((v) -> {
                    int clickedRow = Integer.parseInt(v.getTag().toString().split(",")[0]);
                    int clickedCol = Integer.parseInt(v.getTag().toString().split(",")[1]);

                    if (selectedRow == -1 && selectedCol == -1) {
                        // Selektovanje figura
                        if (isFoxTurn && finalRow == currentFoxRow && finalCol == currentFoxCol) {
                            selectedRow = clickedRow;
                            selectedCol = clickedCol;
                            selectedType = "fox";
                        } else if (!isFoxTurn && images.get(finalRow + "," + finalCol).getDrawable() != null &&
                                images.get(finalRow + "," + finalCol).getDrawable().getConstantState() != null &&
                                images.get(finalRow + "," + finalCol).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.plava_geese).getConstantState())) {
                            selectedRow = clickedRow;
                            selectedCol = clickedCol;
                            selectedType = "goose";
                        }
                    } else {
                        // Pomeranje selektovane figure
                        if (selectedType.equals("fox")) {
                            // Dijagonalno kretanje za lisicu
                            if ((clickedRow + clickedCol) % 2 != 0 &&
                                    Math.abs(clickedRow - selectedRow) == 1 && Math.abs(clickedCol - selectedCol) == 1) {

                                // Da li na ciljanoj poziciji već postoji figura
                                if (images.get(clickedRow + "," + clickedCol).getDrawable() != null &&
                                        (images.get(clickedRow + "," + clickedCol).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.plava_fox).getConstantState()) ||
                                                images.get(clickedRow + "," + clickedCol).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.plava_geese).getConstantState()))) {
                                    Toast.makeText(MainActivity.this, "Na tom mestu već postoji figura. Izaberite drugo mesto.", Toast.LENGTH_SHORT).show();
                                } else {
                                    images.get(selectedRow + "," + selectedCol).setImageResource((selectedRow + selectedCol) % 2 == 0 ? R.drawable.bela : R.drawable.plava);
                                    currentFoxRow = clickedRow;
                                    currentFoxCol = clickedCol;
                                    iv.setImageResource(R.drawable.plava_fox);
                                    isFoxTurn = false;
                                    resetSelection();
                                    checkWinCondition();
                                }
                            }
                        } else if (selectedType.equals("goose")) {
                            // Dijagonalno kretanje unapred za guske
                            if ((clickedRow + clickedCol) % 2 != 0 &&
                                    clickedRow == selectedRow + 1 &&
                                    Math.abs(clickedCol - selectedCol) == 1) {

                                //Da li na ciljanoj poziciji već postoji figura
                                if (images.get(clickedRow + "," + clickedCol).getDrawable() != null &&
                                        (images.get(clickedRow + "," + clickedCol).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.plava_fox).getConstantState()) ||
                                                images.get(clickedRow + "," + clickedCol).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.plava_geese).getConstantState()))) {
                                    Toast.makeText(MainActivity.this, "Na tom mestu već postoji figura. Izaberite drugo mesto.", Toast.LENGTH_SHORT).show();
                                } else {
                                    images.get(selectedRow + "," + selectedCol).setImageResource((selectedRow + selectedCol) % 2 == 0 ? R.drawable.bela : R.drawable.plava);
                                    images.get(clickedRow + "," + clickedCol).setImageResource(R.drawable.plava_geese);
                                    isFoxTurn = true;
                                    resetSelection();
                                    checkWinCondition();
                                }
                            }
                        }
                    }
                   // Toast.makeText(MainActivity.this, "Kliknuo si na sliku " + v.getTag().toString(), Toast.LENGTH_SHORT).show();
                });
                llrow.addView(iv);
            }
            llmain.addView(llrow);
        }
    }

    private void resetSelection() {
        selectedRow = -1;
        selectedCol = -1;
        selectedType = "";
    }

    private void checkWinCondition() {
        if (currentFoxRow == 1) {
            Toast.makeText(this, "Fox je pobedio!", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(this::resetGame, 2000);
            return;
        }

        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        boolean isTrapped = true;
        for (int[] dir : directions) {
            int newRow = currentFoxRow + dir[0];
            int newCol = currentFoxCol + dir[1];
            if (newRow > 0 && newRow <= numRows && newCol > 0 && newCol <= numColumns) {
                if (images.get(newRow + "," + newCol).getDrawable() == null ||
                        (!images.get(newRow + "," + newCol).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.plava_fox).getConstantState()) &&
                                !images.get(newRow + "," + newCol).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.plava_geese).getConstantState()))) {
                    isTrapped = false;
                    break;
                }
            }
        }
        if (isTrapped) {
            Toast.makeText(this, "Geese su pobedile!", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(this::resetGame, 2000);
        }
    }

    private void resetGame() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
