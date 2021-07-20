package com.example.tirarfotoevideo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    ImageView image;
    Button btnTakePhoto, btnSavePhoto, btnVideo;
    VideoView videoView;

    static final int COD_IMAGE_PHOTO = 1;
    static final int COD_PERMISSION = 1;
    static final int COD_VIDEO = 2;

    int cameraPermission;
    int writePermission;

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //permissions
        cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        writePermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(cameraPermission == PackageManager.PERMISSION_DENIED ||
                writePermission == PackageManager.PERMISSION_DENIED)
            permission();

        //region bind
        btnSavePhoto = findViewById(R.id.btnSavePhoto);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnVideo = findViewById(R.id.btnVideo);
        image = findViewById(R.id.ivImage);
        videoView = findViewById(R.id.vvVideo);

        //endregion e visibility
        videoView.setVisibility(View.INVISIBLE);
        image.setVisibility(View.INVISIBLE);
        btnSavePhoto.setVisibility(View.INVISIBLE);

        //adiciona um media de controle ao vídeo
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        //region eventos clique
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePhoto, COD_IMAGE_PHOTO);
            }
        });

        btnSavePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap == null){
                    Toast.makeText(MainActivity.this, "Tire uma foto primeiro",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                final AlertDialog.Builder builder = new
                        AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Salvar");
                final EditText edName = new EditText(MainActivity.this);
                builder.setView(edName);
                builder.setMessage("De um nome para a foto");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sName = edName.getText().toString();
                        save(sName);
                    }
                });
                builder.setNegativeButton("Cancel", new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();
            }
        });
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, COD_VIDEO);
            }
        });
        //endregion
    }
    //metodo para salvar a imagem no celular
    public void save(String name){ String root =
            Environment.getExternalStorageDirectory().getAbsolutePath(); //pega o caminho interno
        File myDir = new File(root + "/images"); //cria um caminho para a pasta
        myDir.mkdirs(); // cria a pasta
        String fname = name +".jpg"; // da um nome para um arquivo imagem
        File file = new File (myDir, fname); //cria o arquivo na pasta
        if (file.exists ()) file.delete (); //verifica se já existe um arquivo com o nome se sim ele deleta
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Mensagem");
            b.setMessage("Arquivo salvo no celular no caminho " +
                    file.getAbsolutePath());
            b.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            b.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent
            data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COD_IMAGE_PHOTO && resultCode == RESULT_OK) {
            if (image.getVisibility() == View.INVISIBLE) {
                image.setVisibility(View.VISIBLE);
                btnSavePhoto.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.INVISIBLE);
            }
            //pega imagem
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            image.setImageBitmap(bitmap);
        }
        if (requestCode == COD_VIDEO && resultCode == RESULT_OK) {
            if (videoView.getVisibility() == View.INVISIBLE) {
                videoView.setVisibility(View.VISIBLE);
                image.setVisibility(View.INVISIBLE);
                btnSavePhoto.setVisibility(View.INVISIBLE); }
            Uri videoUri = data.getData(); //pega o caminho do video que foi salvo
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Mensagem");
            alert.setMessage("Video salvo em: " + videoUri.toString());
            videoView.setVideoURI(videoUri); //inicia no player e video
            videoView.start();
        }
    }
    //metodo que pede permissoes
    public void permission(){
        ActivityCompat.requestPermissions(this, new
                String[]{Manifest.permission.CAMERA
                ,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE},COD_PERMISSION);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == COD_PERMISSION){
            if (cameraPermission == PackageManager.PERMISSION_DENIED ||
                    writePermission == PackageManager.PERMISSION_DENIED)
                permission();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        //pausa o video se entrar em segundo plano
        if (videoView.getVisibility() == View.VISIBLE){
            videoView.pause();
        }
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        //reinicia o video ao voltar para o primeiro plano
        if (videoView.getVisibility() == View.VISIBLE){
            videoView.start();
        }
    }
}