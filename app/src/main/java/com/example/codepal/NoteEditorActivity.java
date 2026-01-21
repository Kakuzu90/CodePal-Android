package com.example.codepal;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.codepal.Services.Database;

import org.eclipse.tm4e.core.registry.IThemeSource;

import java.util.Map;

import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.widget.CodeEditor;

public class NoteEditorActivity extends AppCompatActivity {
    ImageView back, run, save, ai;
    EditText title;
    CodeEditor editor;
    Database database;
    private int USERID;
    private int NOTED_ID = -1;
    private boolean EDIT_MODE = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_editor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        database = new Database(this);
        SharedPreferences shared = getSharedPreferences("AuthSession", MODE_PRIVATE);
        USERID = shared.getInt("userId", -1);

        back = findViewById(R.id.back);
        run = findViewById(R.id.run);
        save = findViewById(R.id.save);
        title = findViewById(R.id.title);
        editor = findViewById(R.id.codeEditor);
        ai = findViewById(R.id.assistant);

        try {
            setupEditor();
            editor.setColorScheme(TextMateColorScheme.create(ThemeRegistry.getInstance()));
            var languageScope = "source.python";
            var language = TextMateLanguage.create(
                    languageScope, true
            );
            editor.setEditorLanguage(language);

            // Check if editing existing note
            if (getIntent().hasExtra("title")) {
                EDIT_MODE = true;
                NOTED_ID = getIntent().getIntExtra("id", -1);
                String getTitle = getIntent().getStringExtra("title");
                String getContent = getIntent().getStringExtra("content");

                title.setText(getTitle);
                editor.setText(getContent);
            }

            run.setOnClickListener(v -> {
                //validatePythonCode();
                checkSyntax();
            });

            save.setOnClickListener(v -> {
                saveNote();
            });

            ai.setOnClickListener(v -> {
                String getTitle = title.getText().toString().trim();
                String getContent = editor.getText().toString().trim();

                Intent intent = new Intent(NoteEditorActivity.this, ChatHistoryActivity.class);

                if (NOTED_ID != -1) {
                    intent.putExtra("id", NOTED_ID);
                    intent.putExtra("title", getTitle);
                    intent.putExtra("content", getContent);
                }

                startActivity(intent);
                finish();
            });

            back.setOnClickListener(v -> {
                String getTitle = title.getText().toString().trim();
                String getContent = editor.getText().toString().trim();

                if (!getTitle.isEmpty() || !getContent.isEmpty()) {
                    // Show confirmation dialog
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Unsaved Changes")
                            .setMessage("Do you want to save your changes?")
                            .setPositiveButton("Save", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialog, int which) {
                                    saveNote();
                                }
                            })
                            .setNegativeButton("Discard", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setNeutralButton("Cancel", null)
                            .show();
                } else {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void saveNote() {
        String getTitle = title.getText().toString().trim();
        String getContent = editor.getText().toString().trim();

        // Validation
        if (getTitle.isEmpty()) {
            title.setError("Title is required");
            title.requestFocus();
            return;
        }

        if (getContent.isEmpty()) {
            editor.requestFocus();
            return;
        }

        boolean success;
        if (EDIT_MODE) {
            // Update existing note
            success = database.updateNote(NOTED_ID, getTitle, getContent);
            if (success) {
                Toast.makeText(this, "Note updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Create new note
            success = database.storeNote(USERID, getTitle, getContent);
            if (success) {
                Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
            }
        }

        if (success) {
            finish(); // Return to notes list
        }
    }
    private void setupEditor() throws Exception {
        FileProviderRegistry.getInstance().addFileProvider(
                new AssetsFileResolver(
                        getApplicationContext().getAssets()
                )
        );

        var themeRegistry = ThemeRegistry.getInstance();
        var name = "quietlight";
        var themeAssetsPath = "textmate/" + name + ".json";
        var model = new ThemeModel(
                IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(themeAssetsPath), themeAssetsPath, null
                ),
                name
        );
        themeRegistry.loadTheme(model);

        ThemeRegistry.getInstance().setTheme(name);
        GrammarRegistry.getInstance().loadGrammars("textmate/languages.json");
    }
    private void checkSyntax() {
        try {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(this));
            }

            Python py = Python.getInstance();
            PyObject module = py.getModule("syntax_checker");

            String code = editor.getText().toString().trim();
            PyObject response = module.callAttr("check", code);
            Map<PyObject, PyObject> result = response.asMap();

            PyObject statusObj = result.get("status");
            if (statusObj == null) {
                return;
            }

            String status = statusObj.toString();
            if (status.equals("ok")) {
                Toast.makeText(this, "Python code is valid", Toast.LENGTH_LONG).show();
            } else {
                int line = result.get("line").toInt();
                String msg = result.get("msg").toString();
                String errorMessage = "SyntaxError at line " + line + ": " + msg;

                if (line > 0) {
                    editor.setSelection((line - 1), 0);
                    editor.post(() -> {
                        editor.ensureSelectionVisible();
                    });
                }

                new AlertDialog.Builder(this)
                        .setTitle("Syntax Error")
                        .setMessage(errorMessage)
                        .setNeutralButton("Close", null)
                        .show();
            }

        } catch (Exception e) {
            Log.e("Exception", Log.getStackTraceString(e));
        }
    }

    @Override
    public void onBackPressed() {
        // Check if there are unsaved changes
        String getTitle = title.getText().toString().trim();
        String getContent = editor.getText().toString().trim();

        if (!getTitle.isEmpty() || !getContent.isEmpty()) {
            // Show confirmation dialog
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("Do you want to save your changes?")
                    .setPositiveButton("Save", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            saveNote();
                        }
                    })
                    .setNegativeButton("Discard", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (editor != null) {
            editor.release();
        }
    }
}