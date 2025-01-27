package dinidiniz.eggsearcher.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dinidiniz.eggsearcher.R;

/**
 * Created by leon on 26/08/15.
 */
public class TelaFilePicker extends ListActivity {

    public final static String EXTRA_FILE_PATH = "file_path";
    public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";
    public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "accepted_file_extensions";
    String DEFAULT_INITIAL_DIRECTORY;
    String filepath;
    private Intent intent;
    private int processSpinnerSelected;

    protected File Directory;
    protected ArrayList<File> Files;
    protected FilePickerListAdapter Adapter;
    protected boolean ShowHiddenFiles = false;
    protected String[] acceptedFileExtensions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load no ultimo diretorio
        loadScreen();

        LayoutInflater inflator = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View emptyView = inflator.inflate(R.layout.empty_view, null);
        ((ViewGroup) getListView().getParent()).addView(emptyView);
        getListView().setEmptyView(emptyView);

        // Set initial directory
        Directory = new File(DEFAULT_INITIAL_DIRECTORY);

        // Initialize the ArrayList
        Files = new ArrayList<File>();

        // Set the ListAdapter
        Adapter = new FilePickerListAdapter(this, Files);
        setListAdapter(Adapter);

        // Initialize the extensions array to allow any file extensions
        acceptedFileExtensions = new String[] {};

        // Get intent extras
        if(getIntent().hasExtra(EXTRA_FILE_PATH))
            Directory = new File(getIntent().getStringExtra(EXTRA_FILE_PATH));

        if(getIntent().hasExtra(EXTRA_SHOW_HIDDEN_FILES))
            ShowHiddenFiles = getIntent().getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false);

        if(getIntent().hasExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS)) {

            ArrayList<String> collection =
                    getIntent().getStringArrayListExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS);

            acceptedFileExtensions = (String[])
                    collection.toArray(new String[collection.size()]);
        }
    }

    @Override
    protected void onResume() {
        refreshFilesList();
        super.onResume();
    }


    protected void refreshFilesList() {

        Files.clear();
        ExtensionFilenameFilter filter =
                new ExtensionFilenameFilter(acceptedFileExtensions);

        File[] files = Directory.listFiles(filter);

        if(files != null && files.length > 0) {

            for(File f : files) {

                if(f.isHidden() && !ShowHiddenFiles) {

                    continue;
                }

                Files.add(f);
            }

            Collections.sort(Files, new FileComparator());
        }

        Adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {

        if(Directory.getParentFile() != null) {

            Directory = Directory.getParentFile();
            refreshFilesList();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File newFile = (File)l.getItemAtPosition(position);

        if(newFile.isFile()) {

            try {
                filepath = newFile.getAbsolutePath();
                String extension = filepath.substring(filepath.length() - 4);
                if (extension.equals(".jpg") || extension.equals(".png")) {
                    Toast.makeText(this, "3...2...1...!", Toast.LENGTH_SHORT).show();

                    if (processSpinnerSelected == 0){
                        intent = new Intent(this, TelaContagem.class);
                    } else {
                        intent = new Intent(this, TelaFullAutomatic.class);
                    }

                    try {
                        DEFAULT_INITIAL_DIRECTORY = newFile.getParentFile().getAbsolutePath();
                        ;
                        if (DEFAULT_INITIAL_DIRECTORY == null) {
                            DEFAULT_INITIAL_DIRECTORY = "/";
                        }
                    } catch (Exception e) {
                        Log.i("TelaFilePicker", e.toString());
                    }
                    saveScreen();
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Extension must be jpg or png", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) { Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();}

        }
        else {

            Directory = newFile;
            refreshFilesList();
        }

        super.onListItemClick(l, v, position, id);
    }

    private class FilePickerListAdapter extends ArrayAdapter<File> {

        private List<File> mObjects;

        public FilePickerListAdapter(Context context, List<File> objects) {

            super(context, R.layout.tela_file_picker, android.R.id.text1, objects);
            mObjects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = null;

            if(convertView == null) {

                LayoutInflater inflater = (LayoutInflater)
                        getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                row = inflater.inflate(R.layout.tela_file_picker, parent, false);
            }
            else
                row = convertView;

            File object = mObjects.get(position);

            ImageView imageView = (ImageView)row.findViewById(R.id.file_picker_image);
            TextView textView = (TextView)row.findViewById(R.id.file_picker_text);
            textView.setSingleLine(true);
            textView.setText(object.getName());

            if(object.isFile())
                imageView.setImageResource(R.drawable.file);

            else
                imageView.setImageResource(R.drawable.folder);

            return row;
        }
    }

    private class FileComparator implements Comparator<File> {

        public int compare(File f1, File f2) {

            if(f1 == f2)
                return 0;

            if(f1.isDirectory() && f2.isFile())
                // Show directories above files
                return -1;

            if(f1.isFile() && f2.isDirectory())
                // Show files below directories
                return 1;

            // Sort the directories alphabetically
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }

    private class ExtensionFilenameFilter implements FilenameFilter {

        private String[] Extensions;

        public ExtensionFilenameFilter(String[] extensions) {

            super();
            Extensions = extensions;
        }

        public boolean accept(File dir, String filename) {

            if(new File(dir, filename).isDirectory()) {

                // Accept all directory names
                return true;
            }

            if(Extensions != null && Extensions.length > 0) {

                for(int i = 0; i < Extensions.length; i++) {

                    if(filename.endsWith(Extensions[i])) {

                        // The filename ends with the extension
                        return true;
                    }
                }
                // The filename did not match any of the extensions
                return false;
            }
            // No extensions has been set. Accept all file extensions.
            return true;
        }
    }

    public void saveScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("directory", DEFAULT_INITIAL_DIRECTORY);
        editor.putString("imagepath", filepath);
        editor.commit();
    }

    public void loadScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        processSpinnerSelected = sharedPref.getInt("processSpinnerSelected", 0);
        DEFAULT_INITIAL_DIRECTORY = sharedPref.getString("directory", "/");
    }
}
