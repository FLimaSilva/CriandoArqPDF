package com.codecorp.felipelima.criandoarqpdf;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.List;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText mContentEditText;
    private Button mCreateButton;
    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContentEditText = findViewById(R.id.edit_text_content);
        mCreateButton = findViewById(R.id.button_create);

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContentEditText.getText().toString().isEmpty()){
                    mContentEditText.setError("Body is empty");
                    mContentEditText.requestFocus();
                    return;
                }

                try{
                    createPdfWrapper();
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                } catch (DocumentException e){
                    e.printStackTrace();
                }

            }
        });
    }

    private void createPdfWrapper() throws FileNotFoundException,DocumentException {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)){
                    showMessageOKCancel("You need to allow acess to Storage", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        }
                    });
                    return;
                }
                
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        } else {
            createPdf();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permissão aceita
                    try{
                        createPdfWrapper();
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    } catch (DocumentException e){
                        e.printStackTrace();
                    }
                } else {
                    //Permissão negada
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied",Toast.LENGTH_SHORT).show();
                }
                break;
             default:
                 super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel (String message, DialogInterface.OnClickListener okListener){
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Ok",okListener)
                .setNegativeButton("Cancel",null)
                .create()
                .show();
    }

    private void createPdf() throws FileNotFoundException,DocumentException{
        
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!docsFolder.exists()){
            docsFolder.mkdir();
            Log.i(TAG,"Create a new directory for PDF");
        }
        
        pdfFile = new File(docsFolder.getAbsolutePath(),"HelloWorld.pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document();
        PdfWriter.getInstance(document,output);
        document.open();

        //FontSelector selector = new FontSelector();
        //Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN,12);
        //f1.setColor(BaseColor.RED);

        //Font f2 = FontFactory.getFont("MSung-Light","UniCND-UCS-H", BaseFont.NOT_EMBEDDED);
        //f2.setColor(BaseColor.BLUE);

        //Font font3 = FontFactory.getFont(FontFactory.TIMES_BOLD,22,BaseColor.GREEN);

        //selector.addFont(font3);

        //Phrase ph = selector.process(mContentEditText.getText().toString());

        Font red = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.RED);
        Chunk redText = new Chunk("This text is red. ", red);

        Font blue = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLUE);
        Chunk blueText = new Chunk("This text is blue and bold. ", blue);

        Font green = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.GREEN);
        Chunk greenText = new Chunk("This text is green and italic. ", green);

        Paragraph p1 = new Paragraph(redText);
        document.add(p1);
        Paragraph p2 = new Paragraph();
        p2.add(blueText);
        p2.add(greenText);
        document.add(p2);

        /*String[] test = mContentEditText.getText().toString().split(" ");

        FontSelector selector1 = new FontSelector();
        Font f1 = FontFactory.getFont(FontFactory.TIMES_BOLD,15);
        f1.setColor(BaseColor.RED);
        selector1.addFont(f1);
        Phrase ph = selector1.process(test[0]);

        FontSelector selector2 = new FontSelector();
        Font f2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD,19);
        f2.setColor(BaseColor.BLACK);
        selector2.addFont(f2);
        Phrase ph2 = selector2.process(test[1]);

        document.add(new Paragraph(ph));
        document.add(ph2);*/

        document.close();
        previewPdf();
    }

    private void previewPdf() {
        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        java.util.List list = packageManager.queryIntentActivities(testIntent,PackageManager.MATCH_DEFAULT_ONLY);

        if (list.size() > 0){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//sem essa porra não abre o arquivo de fato
            //I just tested this with Android 5.0, 6.0, 7.1 and 8.1, it works in all cases.
            //So the (Build.VERSION.SDK_INT > M) condition is useless.
            Uri uri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID+".provider",pdfFile);
            intent.setDataAndType(uri,"application/pdf");

            startActivity(intent);
        } else {
            Toast.makeText(this, "Download a PDF Viewer to see the generated PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
