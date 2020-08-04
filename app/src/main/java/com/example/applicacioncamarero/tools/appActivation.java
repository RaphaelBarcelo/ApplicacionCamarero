package com.example.applicacioncamarero.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.applicacioncamarero.R;
import com.example.applicacioncamarero.activity.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public abstract class appValidation {
    private Activity m_activity; // Handler to the Activity
    private String  m_keyFile ;  // File where the key is to be stored;
    private String m_uniqueID;

    private static encryptTool crypt = new encryptTool();

    public abstract void setValidationResul(void);

    appValidation(Activity activity, String keyFile)
    {
        m_activity = activity;
        m_keyFile = keyFile;
        // On récupère le uniqueID de l'appareil
        m_uniqueID = Settings.Secure.getString(m_activity.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase(); // UUID.randomUUID().toString();
    }

    public void askForValidation( int validationResult)
    {



            // Ici il faut qu'on demande la clé d'activation

            LayoutInflater inflater = (LayoutInflater) m_activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.popup_activate, null, false);

            CoordinatorLayout coordinatorLayout = m_activity.findViewById(R.id.coordinatorLayoutMain);
            int widthConstraintLayout = coordinatorLayout.getWidth();
            int heightConstraintLayout = coordinatorLayout.getHeight();

            final PopupWindow pw = new PopupWindow(linearLayout, (int) (widthConstraintLayout * 0.70), (int) (heightConstraintLayout * 0.30), true);

            pw.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pw.setElevation(10);
            }

            pw.setAnimationStyle(R.style.Animation);
            pw.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
            pw.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            pw.setOutsideTouchable(true);
            pw.setFocusable(true);


            TextView tv = linearLayout.findViewById(R.id.textViewUniqueId);

            if (tv != null) {
                String code = m_uniqueID.substring(0,4) + " " + m_uniqueID.substring(4,8) + " " + m_uniqueID.substring(8,12)+ " " + m_uniqueID.substring(12,16);
                tv.setText(code);
            }

            final EditText editTextKey1 = (EditText) linearLayout.findViewById(R.id.edtKey1);
            final EditText editTextKey2 = (EditText) linearLayout.findViewById(R.id.edtKey2);
            final EditText editTextKey3 = (EditText) linearLayout.findViewById(R.id.edtKey3);
            //final EditText editTextKey4 = (EditText) linearLayout.findViewById(R.id.edtKey4);

               /* cryptedUniqueID = encryptIt(uniqueID);

                if(editTextKey1 != null)
                    editTextKey1.setText(cryptedUniqueID.substring(0,4));
                if(editTextKey2 != null)
                    editTextKey2.setText(cryptedUniqueID.substring(4,9));
                if(editTextKey3 != null)
                    editTextKey3.setText(cryptedUniqueID.substring(9,14));
                //if(editTextKey4 != null)
                   // editTextKey4.setText(cryptedUniqueID.substring(24,32));*/

            Button buttonCancel = linearLayout.findViewById(R.id.btnCancel);
            Button buttonAccept = linearLayout.findViewById(R.id.btnAccept);

            if (buttonAccept != null)
                buttonAccept.setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View view) {



                                // Enregistrer la cle dans le fichier config
                                if(editTextKey1 != null && editTextKey2 != null && editTextKey3 != null )
                                {
                                    String cryptedUniqueID = editTextKey1.getText() + editTextKey2.getText().toString() + editTextKey3.getText() ; //+ editTextKey4.getText();

                                    Log.d("Key",cryptedUniqueID);

                                    // On vérifie que la clé est valable;
                                    if(crypt.decryptIt(cryptedUniqueID).equalsIgnoreCase(m_uniqueID)) {
                                        // Enregistrer la clé dans un fichier externe
                                        File keyPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                        if (!keyPath.exists()) {
                                            boolean res = keyPath.mkdirs();
                                            Log.d("Mkdir", res ? "Ok" : "Error");
                                        }
                                        final File file = new File(keyPath + java.io.File.separator, "config.txt");


                                        try {
                                            boolean newFile = file.createNewFile();
                                            FileOutputStream fOut = new FileOutputStream(file);
                                            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                            myOutWriter.append(cryptedUniqueID);

                                            myOutWriter.close();

                                            fOut.flush();
                                            fOut.close();
                                            MainActivity.appActivated = 1;
                                        } catch (IOException e) {
                                            Log.e("Exception", "File write failed: " + e.toString());
                                        }
                                        pw.dismiss();
                                    }
                                    else
                                    {
                                        // On affiche le message à l'utilisateur
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(m_activity);
                                        alertDialogBuilder.setMessage(R.string.invalid_key);
                                        alertDialogBuilder.setTitle(R.string.activate);
                                        alertDialogBuilder.setIcon(R.drawable.alert_512);

                                        alertDialogBuilder.setCancelable(false); // Not to be removed by the back button

                                        // Buttun Accept
                                        alertDialogBuilder.setPositiveButton(R.string.aceptar,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface arg0, int arg1) {

                                                    }
                                                });
                                        // Only one button ... Accept
                                        alertDialogBuilder.setNegativeButton(R.string.cancelar,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface arg0, int arg1) {
                                                        m_activity.finish();
                                                        m_activity.moveTaskToBack(true);
                                                        System.exit(0);
                                                    }
                                                });

                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                    }
                                }


                            }
                        });




            if (buttonCancel != null)
                buttonCancel.setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View view) {
                                //pw.dismiss();
                                m_activity.finishAffinity();
                                System.exit(0);
                                int pid = android.os.Process.myPid();
                                android.os.Process.killProcess(pid);
                                android.os.Process.killProcess(android.os.Process.myUid());
                            }
                        });



            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pw.showAtLocation(m_activity.findViewById(R.id.coordinatorLayoutMain), Gravity.CENTER, 0, 0);
                }
            }, 250L);








    }

    // On vérifie l'activation de l'aap selon différenyes méthodes
    public int chechActivation( Context context)
    {
        int result = 0;





        // Verification de l'activation sur le stockage du terminal
        result = checkLocalActivation( context , uniqueID);

        // //////////////////////////////////////////////////
        // Si on n'a rien trouvé en local on cherche sur la base de donnée Empresas
        //
        //   A IMPLEMENTER
        // //////////////////////////////






        return result;
    }

    // Verification de l'activation en local

    private int checkLocalActivation(Context context, String uniqueID)
    {
        int result = 0;
        String cryptedUniqueID=null;
        String localUniqueID=null;

        // Lets check if we have permissions to acces external stoqage
        if (ContextCompat.checkSelfPermission(m_activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                // Lecture écriture de fichiers
                == PackageManager.PERMISSION_GRANTED) {

            // Lire la clé dans un fichier externe
            File keyPath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if(keyPath.exists()) {

                final File file = new File(keyPath + java.io.File.separator, m_keyFile);

                try {
                    //boolean newFile = file.createNewFile();
                    char[] keyBuf = new char[40];
                    FileInputStream fIn = new FileInputStream(file);
                    InputStreamReader myInReader = new InputStreamReader(fIn);
                    int bytesRead = myInReader.read(keyBuf,0,34);

                    myInReader.close();
                    fIn.close();

                    cryptedUniqueID = String.valueOf(keyBuf).substring(0,bytesRead);
                } catch (IOException e) {
                    Log.e("Exception", "File read failed: " + e.toString());
                }
            }

            // Si il y a une clé on vérifie qu'elle est valable
            if(cryptedUniqueID != null)
            {
                // On decrypte la clée stockeée crypté
                localUniqueID = crypt.decryptIt(cryptedUniqueID);

                // Et on compare les deux identifiants
                if(!uniqueID.equals(localUniqueID))
                {
                    result = 0; // Les clés s0nt différentes donc pas d'activation
                }
                else
                    result = 1; // Le logiciel est activé
            }
            else result  = 0;

        }
        return result;
    }
    // Check tha the app is activated



    //  public synchronized String getUniqueId(Context context) {
    public String getUniqueId(Context context) {
        String localUniqueID;
        String cryptedUniqueID = null;
        String uniqueID;


        if (uniqueID == null) {


            // Lire la clé dans un fichier externe
            File keyPath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if(keyPath.exists()) {

                final File file = new File(keyPath + java.io.File.separator, "config.txt");


                try {
                    //boolean newFile = file.createNewFile();
                    char[] keyBuf = new char[40];
                    FileInputStream fIn = new FileInputStream(file);
                    InputStreamReader myInReader = new InputStreamReader(fIn);
                    int bytesRead = myInReader.read(keyBuf,0,34);

                    myInReader.close();
                    fIn.close();

                    cryptedUniqueID = String.valueOf(keyBuf).substring(0,bytesRead);
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            }


            // Si il y a une clé on vérifie qu'elle est valable
            if(cryptedUniqueID != null)
            {
                // On récupère le uniqueID de l'appareil
                localUniqueID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase(); // UUID.randomUUID().toString();

                // On decrypte la clée stockeée crypté
                uniqueID = decryptIt(cryptedUniqueID);

                // Et on compare les deux identifiants
                if(!uniqueID.equals(localUniqueID))
                {
                    uniqueID = null;
                }
                else
                    appActivated = 1; // Le logiciel est activé
            }
            else uniqueID = null;


            if (uniqueID == null) { // Si il n'y

                uniqueID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase(); // UUID.randomUUID().toString();

                // Ici il faut qu'on demande la clé d'activation

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.popup_activate, null, false);

                CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayoutMain);
                int widthConstraintLayout = coordinatorLayout.getWidth();
                int heightConstraintLayout = coordinatorLayout.getHeight();

                final PopupWindow pw = new PopupWindow(linearLayout, (int) (widthConstraintLayout * 0.70), (int) (heightConstraintLayout * 0.30), true);

                pw.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pw.setElevation(10);
                }

                pw.setAnimationStyle(R.style.Animation);
                pw.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                pw.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                pw.setOutsideTouchable(true);
                pw.setFocusable(true);


                TextView tv = linearLayout.findViewById(R.id.textViewUniqueId);

                if (tv != null) {
                    String code = uniqueID.substring(0,4) + " " +uniqueID.substring(4,8) + " " +uniqueID.substring(8,12)+ " " +uniqueID.substring(12,16);
                    tv.setText(code);
                }

                final EditText editTextKey1 = (EditText) linearLayout.findViewById(R.id.edtKey1);
                final EditText editTextKey2 = (EditText) linearLayout.findViewById(R.id.edtKey2);
                final EditText editTextKey3 = (EditText) linearLayout.findViewById(R.id.edtKey3);
                //final EditText editTextKey4 = (EditText) linearLayout.findViewById(R.id.edtKey4);

               /* cryptedUniqueID = encryptIt(uniqueID);

                if(editTextKey1 != null)
                    editTextKey1.setText(cryptedUniqueID.substring(0,4));
                if(editTextKey2 != null)
                    editTextKey2.setText(cryptedUniqueID.substring(4,9));
                if(editTextKey3 != null)
                    editTextKey3.setText(cryptedUniqueID.substring(9,14));
                //if(editTextKey4 != null)
                   // editTextKey4.setText(cryptedUniqueID.substring(24,32));*/

                Button buttonCancel = linearLayout.findViewById(R.id.btnCancel);
                Button buttonAccept = linearLayout.findViewById(R.id.btnAccept);

                if (buttonAccept != null)
                    buttonAccept.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View view) {



                                    // Enregistrer la cle dans le fichier config
                                    if(editTextKey1 != null && editTextKey2 != null && editTextKey3 != null )
                                    {
                                        String cryptedUniqueID = editTextKey1.getText() + editTextKey2.getText().toString() + editTextKey3.getText() ; //+ editTextKey4.getText();

                                        Log.d("Key",cryptedUniqueID);

                                        // On vérifie que la clé est valable;
                                        if(decryptIt(cryptedUniqueID).equalsIgnoreCase(uniqueID)) {
                                            // Enregistrer la clé dans un fichier externe
                                            File keyPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                            if (!keyPath.exists()) {
                                                boolean res = keyPath.mkdirs();
                                                Log.d("Mkdir", res ? "Ok" : "Error");
                                            }
                                            final File file = new File(keyPath + java.io.File.separator, "config.txt");


                                            try {
                                                boolean newFile = file.createNewFile();
                                                FileOutputStream fOut = new FileOutputStream(file);
                                                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                                myOutWriter.append(cryptedUniqueID);

                                                myOutWriter.close();

                                                fOut.flush();
                                                fOut.close();
                                                appActivated = 1;
                                            } catch (IOException e) {
                                                Log.e("Exception", "File write failed: " + e.toString());
                                            }
                                            pw.dismiss();
                                        }
                                        else
                                        {
                                            // On affiche le message à l'utilisateur
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(m_activity.this);
                                            alertDialogBuilder.setMessage(R.string.invalid_key);
                                            alertDialogBuilder.setTitle(R.string.activate);
                                            alertDialogBuilder.setIcon(R.drawable.alert_512);

                                            alertDialogBuilder.setCancelable(false); // Not to be removed by the back button

                                            // Buttun Accept
                                            alertDialogBuilder.setPositiveButton(R.string.aceptar,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface arg0, int arg1) {

                                                        }
                                                    });
                                            // Only one button ... Accept
                                            alertDialogBuilder.setNegativeButton(R.string.cancelar,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                            m_activity.finish();
                                                            m_activity.moveTaskToBack(true);
                                                            System.exit(0);
                                                        }
                                                    });

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.show();
                                        }
                                    }


                                }
                            });




                if (buttonCancel != null)
                    buttonCancel.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View view) {
                                    //pw.dismiss();
                                    m_activity.finishAffinity();
                                    System.exit(0);
                                    int pid = android.os.Process.myPid();
                                    android.os.Process.killProcess(pid);
                                    android.os.Process.killProcess(android.os.Process.myUid());
                                }
                            });



                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pw.showAtLocation(m_activity.findViewById(R.id.coordinatorLayoutMain), Gravity.CENTER, 0, 0);
                    }
                }, 250L);






            }

        }

        return uniqueID;
    }

}
